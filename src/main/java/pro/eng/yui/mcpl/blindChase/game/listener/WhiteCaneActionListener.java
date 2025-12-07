package pro.eng.yui.mcpl.blindChase.game.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import pro.eng.yui.mcpl.blindChase.lib.item.WhiteCaneUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles right-click actions with the White Cane applying a 0.6s per-player cooldown.
 */
public class WhiteCaneActionListener implements Listener {

    /**
     * Stores last use timestamp per player for right-click cooldown.
     * Static so it can be reset from game control flows (e.g., start command).
     */
    private static final Map<UUID, Long> lastUse = new HashMap<>();

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
    }

    /**
     * Prevent damaging entities with left-click while holding the white cane in main hand.
     */
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) { return; }
        Player damager = (Player) event.getDamager();
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
                return;
            }
        }
        Inventory clicked = event.getClickedInventory();
        if (clicked instanceof PlayerInventory) {
            // In Bukkit, offhand slot index in PlayerInventory is 40
            if (event.getSlot() == 40) {
                ItemStack cursor = event.getCursor();
                if (WhiteCaneUtil.isWhiteCane(cursor)) {
                    event.setCancelled(true);
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
        Player player = event.getPlayer();
        if (!WhiteCaneUtil.isHoldingCaneInMainHand(player)) { return; }
        event.setCancelled(true);
    }
}
