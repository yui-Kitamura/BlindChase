package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;
import pro.eng.yui.mcpl.blindChase.lib.resourcepack.ResourcePackService;

public class ResourceCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "resource";
    public static final Permission PERMISSION = new Permission(Permissions.ADMIN.get());

    public ResourceCommandHandler(){ super(SUBCOMMAND, PERMISSION); }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /blindchase resource <reload|info>");
            return true;
        }
        String sub = args[1].toLowerCase();
        switch (sub) {
            case "reload":
                try {
                    ResourcePackService.get().resolveNow();
                    sender.sendMessage("Resource pack info reloaded.");
                } catch (Exception e) {
                    sender.sendMessage("Reload failed: " + e.getMessage());
                    e.printStackTrace();
                }
                return true;
            case "info":
                ResourcePackService.Info info = ResourcePackService.get() == null ? null : ResourcePackService.get().getInfo();
                String pluginVer = BlindChase.plugin().getDescription().getVersion();
                if (info == null) {
                    sender.sendMessage("plugin version: " + pluginVer);
                    sender.sendMessage("resolved: (not resolved)");
                } else {
                    sender.sendMessage("plugin version: " + info.pluginVersion);
                    sender.sendMessage("resolved tag: " + info.resolvedTag);
                    sender.sendMessage("asset: " + info.assetName);
                    sender.sendMessage("url: " + info.url);
                }
                return true;
            default:
                sender.sendMessage("Unknown: " + sub);
                return true;
        }
    }
}
