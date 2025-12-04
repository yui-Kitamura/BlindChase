package pro.eng.yui.mcpl.blindChase.game.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import pro.eng.yui.mcpl.blindChase.lib.item.WhiteCaneUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles right-click actions with the White Cane applying a 0.6s per-player cooldown.
 */
public class WhiteCaneRightClickListener implements Listener {

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

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event){
        if (!isRightClick(event.getAction())) { return; }

        Player player = event.getPlayer();
        // Only when holding the cane in dominant (main) hand
        if (!WhiteCaneUtil.isHoldingCaneInDominantHand(player)) { return; }

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
}
