package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;

public class HelpCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "help";
    public static final Permission PERMISSION = new Permission(Permissions.ALL.get());

    public HelpCommandHandler(){ super(SUBCOMMAND, PERMISSION); }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args){
        sender.sendMessage("/blindchase " + RegenerateCommandHandler.SUBCOMMAND);
        sender.sendMessage("/blindchase " + JoinCommandHandler.SUBCOMMAND);
        sender.sendMessage("/blindchase " + SUBCOMMAND);
        return true;
    }
}
