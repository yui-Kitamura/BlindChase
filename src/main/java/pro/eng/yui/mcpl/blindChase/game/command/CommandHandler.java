package pro.eng.yui.mcpl.blindChase.game.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    public static final String COMMAND = "blindchase";
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        List<String> completions = new ArrayList<>();
        
        return completions;
    }
}
