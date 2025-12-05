package pro.eng.yui.mcpl.blindChase.lib.resourcepack;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerResourcePackStatusEvent;

public class ResourcePackStatusListener implements Listener {

    @EventHandler
    public void onPackStatus(PlayerResourcePackStatusEvent e) {
        switch (e.getStatus()) {
            case DECLINED:
                e.getPlayer().sendMessage(ChatColor.RED + "（警告）リソースパックが拒否されました" + ChatColor.RESET);
                break;
            case FAILED_DOWNLOAD:
            case FAILED_RELOAD:
                e.getPlayer().sendMessage(ChatColor.RED + "（警告）リソースパックのダウンロード/適用に失敗しました" + ChatColor.RESET);
                break;
            case ACCEPTED:
            case SUCCESSFULLY_LOADED:
            default:
                break;
        }
    }
}
