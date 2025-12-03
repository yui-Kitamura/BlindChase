package pro.eng.yui.mcpl.blindChase.game.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import pro.eng.yui.mcpl.blindChase.game.command.sub.RegenerateCommandHandler;
import pro.eng.yui.mcpl.blindChase.game.command.sub.JoinCommandHandler;
import pro.eng.yui.mcpl.blindChase.game.command.sub.HelpCommandHandler;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    public static final String COMMAND = "blindchase";
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        if (args.length == 0){
            return HelpCommandHandler.execute(sender, args);
        }

        String sub = args[0].toLowerCase();
        switch (sub){
            case RegenerateCommandHandler.SUBCOMMAND:
                return RegenerateCommandHandler.execute(sender, args);
            case JoinCommandHandler.SUBCOMMAND:
                return JoinCommandHandler.execute(sender, args);
            case HelpCommandHandler.SUBCOMMAND:
                return HelpCommandHandler.execute(sender, args);
            default:
                sender.sendMessage("Unknown subcommand: " + sub);
                return true;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 0){
            completions.add(RegenerateCommandHandler.SUBCOMMAND);
            completions.add(JoinCommandHandler.SUBCOMMAND);
            completions.add(HelpCommandHandler.SUBCOMMAND);
        }else if (args.length == 1){
            String prefix = args[0].toLowerCase();
            if (RegenerateCommandHandler.SUBCOMMAND.startsWith(prefix)){ completions.add(RegenerateCommandHandler.SUBCOMMAND); }
            if (JoinCommandHandler.SUBCOMMAND.startsWith(prefix)){ completions.add(JoinCommandHandler.SUBCOMMAND); }
            if (HelpCommandHandler.SUBCOMMAND.startsWith(prefix)){ completions.add(HelpCommandHandler.SUBCOMMAND); }
        }
        return completions;
    }
}
