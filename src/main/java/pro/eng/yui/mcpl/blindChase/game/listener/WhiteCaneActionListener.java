package pro.eng.yui.mcpl.blindChase.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import pro.eng.yui.mcpl.blindChase.lib.item.WhiteCaneUtil;
import pro.eng.yui.mcpl.blindChase.BlindChase;

import java.util.HashMap;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * Handles right-click actions with the White Cane applying a 0.6s per-player cooldown.
 */
public class WhiteCaneActionListener implements Listener {

    /**
     * Stores last use timestamp per player for right-click cooldown.
     * Static so it can be reset from game control flows (e.g., start command).
     */
    private static final Map<UUID, Long> lastUse = new HashMap<>();
    /** Tracks players currently playing the sway animation to avoid overlap. */
    private static final Set<UUID> animating = Collections.newSetFromMap(new ConcurrentHashMap<>());
    /** Per-player scheduled animation task holders. */
    private static final Map<UUID, TaskHolder> tasks = new ConcurrentHashMap<>();
    private static class TaskHolder {
        int id;
        int step = 0;
        TaskHolder(int id) { this.id = id; }
    }

    /** Clear the stored last-use time for given player. */
    public static void clearLastUse(UUID playerId) {
        if (playerId == null) { return; }
        lastUse.remove(playerId);
    }

    /** Clear the stored last-use times for all players. */
    public static void clearAllLastUse() {
        lastUse.clear();
    }

    private boolean isRightClick(Action action){
        return action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK;
    }
    private boolean isLeftClick(Action action){
        return action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK;
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        // Only process when holding the cane in main hand
        if (!WhiteCaneUtil.isHoldingCaneInMainHand(player)) { return; }

        // Disable left click while holding the cane
        if (isLeftClick(event.getAction())) {
            event.setCancelled(true);
            return;
        }

        if (!isRightClick(event.getAction())) { return; }

        long now = System.currentTimeMillis();
        UUID id = player.getUniqueId();
        Long last = lastUse.get(id);
        long cooldownMs = Math.max(0L, WhiteCaneCooldowns.getProvider().getCooldownMillis(player));
        if (last != null && (now - last) < cooldownMs) {
            // Still cooling down: prevent spamming action
            event.setCancelled(true);
            return;
        }

        // Accept interaction and start cooldown visuals client-side too
        lastUse.put(id, now);
        int ticks = WhiteCaneCooldowns.getProvider().getClientCooldownTicks(player);
        if (ticks > 0) {
            player.setCooldown(Material.STICK, ticks);
        }

        // Start sway animation on right-click
        startSwayAnimation(player);
    }

    /**
     * Prevent damaging entities with left-click while holding the white cane in main hand.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player damager)) { return; }
        if (!WhiteCaneUtil.isHoldingCaneInMainHand(damager)) { return; }
        // Cancel the damage to effectively disable attack with the cane
        event.setCancelled(true);
    }

    /**
     * Cancel swapping the White Cane into the offhand via the F-key (swap hands action).
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        ItemStack main = event.getMainHandItem();
        // If the item moving to offhand is the White Cane, cancel the swap
        if (WhiteCaneUtil.isWhiteCane(main)) {
            event.setCancelled(true);
            // ensure client/server stay in sync
            event.getPlayer().updateInventory();
        }
    }

    /**
     * Cancel placing the White Cane into the offhand through inventory clicks.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) { return; }

        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            ItemStack clickedItem = event.getCurrentItem();
            if (WhiteCaneUtil.isWhiteCane(clickedItem)) {
                event.setCancelled(true);
                safeUpdateInventory((Player) event.getWhoClicked());
                return;
            }
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked instanceof PlayerInventory) {
            // In Bukkit, offhand slot index in PlayerInventory is 40
            if (event.getSlot() == 40) {
                ItemStack cursor = event.getCursor();
                if (WhiteCaneUtil.isWhiteCane(cursor)) {
                    ItemStack keep = cursor.clone();
                    event.setCancelled(true);
                    event.getWhoClicked().setItemOnCursor(keep);
                    safeUpdateInventory((Player) event.getWhoClicked());
                }
            }
        }
    }

    /**
     * Stop the swing animation while holding the White Cane in the main hand.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAnimation(PlayerAnimationEvent event) {
        if (event.getAnimationType() != PlayerAnimationType.ARM_SWING) { return; }
        if (WhiteCaneUtil.isHoldingCaneInMainHand(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    // --- Animation helpers ---
    private void startSwayAnimation(Player player) {
        UUID id = player.getUniqueId();
        if (!animating.add(id)) { return; } // already animating

        final int base = WhiteCaneUtil.getBaseCustomModelData();
        final int left = WhiteCaneUtil.CMD_SWAY_LEFT;
        final int right = WhiteCaneUtil.CMD_SWAY_RIGHT;

        final int[] frames = new int[] { left, right, left, base };
        final int periodTicks = 2; // every 2 ticks (~0.1s)

        final TaskHolder holder = new TaskHolder(-1);
        tasks.put(id, holder);

        final int taskId = Bukkit.getScheduler().runTaskTimer(BlindChase.plugin(), () -> {
            try {
                if (player.isOnline() == false || WhiteCaneUtil.isHoldingCaneInMainHand(player) == false) {
                    // Player no longer valid or not holding cane: stop and try to restore base
                    setCaneCMD(player, base);
                    Bukkit.getScheduler().cancelTask(holder.id);
                    animating.remove(id);
                    tasks.remove(id);
                    return;
                }
                int step = holder.step;
                if (step >= frames.length) {
                    setCaneCMD(player, base);
                    Bukkit.getScheduler().cancelTask(holder.id);
                    animating.remove(id);
                    tasks.remove(id);
                    return;
                }
                setCaneCMD(player, frames[step]);
                holder.step++;
            } catch (Exception ex) {
                // Ensure cleanup on any unexpected error
                try { setCaneCMD(player, base); } catch (Exception ignore) {}
                Bukkit.getScheduler().cancelTask(holder.id);
                animating.remove(id);
                tasks.remove(id);
            }
        }, 0L, periodTicks).getTaskId();

        holder.id = taskId;
    }

    private void setCaneCMD(Player player, int cmd) {
        ItemStack hand = player.getInventory().getItemInMainHand();
        if (!WhiteCaneUtil.isWhiteCane(hand)) { return; }
        ItemMeta meta = hand.getItemMeta();
        if (meta == null) { return; }
        meta.setCustomModelData(cmd);
        hand.setItemMeta(meta);
        // Force client update by re-setting the stack (optional but helps in some clients)
        player.getInventory().setItemInMainHand(hand);
        player.updateInventory();
    }

    private void safeUpdateInventory(Player player) {
        // Schedule update next tick to ensure server state is authoritative
        Bukkit.getScheduler().runTask(BlindChase.plugin(), ()->{player.updateInventory();});
    }
}
