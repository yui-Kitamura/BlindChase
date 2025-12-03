package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.command.CommandSender;

public final class HelpCommandHandler {

    public static final String SUBCOMMAND = "help";

    private HelpCommandHandler(){
        // ignore create instance
    }

    public static boolean execute(CommandSender sender, String[] args){
        sender.sendMessage("/blindchase " + RegenerateCommandHandler.SUBCOMMAND);
        sender.sendMessage("/blindchase " + JoinCommandHandler.SUBCOMMAND);
        sender.sendMessage("/blindchase " + SUBCOMMAND);
        return true;
    }
}
