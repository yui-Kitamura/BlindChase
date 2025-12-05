package pro.eng.yui.mcpl.blindChase.game.listener;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;
import pro.eng.yui.mcpl.blindChase.lib.resourcepack.ResourcePackService;

/**
 * Ensures resource pack is applied when players join or are teleported into the game world
 * without using the join command.
 */
public final class ResourcePackAutoApplyListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        ResourcePackService svc = ResourcePackService.get();
        if (svc == null) { return; }
        if(Field.FIELD_WORLD_NAME.equalsIgnoreCase(e.getPlayer().getWorld().getName())) {
            Bukkit.getScheduler().runTask(BlindChase.plugin(), () -> svc.applyToPlayer(e.getPlayer()));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent e) {
        if (e.getTo() == null || e.getTo().getWorld() == null) { return; }
        final String toWorldName = e.getTo().getWorld().getName();
        final String fromWorldName = (e.getFrom().getWorld() != null) ? e.getFrom().getWorld().getName() : null;
        if (Field.FIELD_WORLD_NAME.equalsIgnoreCase(toWorldName) && (toWorldName.equals(fromWorldName) == false)) {
            // ワールド内tpは除外
            ResourcePackService svc = ResourcePackService.get();
            if (svc == null) { return; }
            Bukkit.getScheduler().runTask(BlindChase.plugin(), () -> svc.applyToPlayer(e.getPlayer()));
        }
    }
}
