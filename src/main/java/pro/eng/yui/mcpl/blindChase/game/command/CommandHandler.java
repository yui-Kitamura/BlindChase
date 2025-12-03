package pro.eng.yui.mcpl.blindChase.game.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import pro.eng.yui.mcpl.blindChase.game.command.sub.HelpCommandHandler;
import pro.eng.yui.mcpl.blindChase.game.command.sub.JoinCommandHandler;
import pro.eng.yui.mcpl.blindChase.game.command.sub.RegenerateCommandHandler;
import pro.eng.yui.mcpl.blindChase.lib.command.ISubCommandRunner;

import java.util.*;

public class CommandHandler implements CommandExecutor, TabCompleter {
    
    public static final String COMMAND = "blindchase";
    private final Map<String, ISubCommandRunner> runners;
    private ISubCommandRunner findRunner(Map<String, ISubCommandRunner> runners, String name){
        if (name == null){ return null; }
        String key = name.toLowerCase();
        return runners.get(key);
    }
    private List<String> findRunnerWithNameHead(final CommandSender sender, final String input){
        List<String> result = new ArrayList<>();
        final String inputLower = input.toLowerCase();
        final boolean returnAll = inputLower.isEmpty();
        Set<String> keys = runners.keySet();
        for(String k : keys) {
            if (returnAll || k.startsWith(inputLower)) {
                if(sender.hasPermission(runners.get(k).getPermission())) {
                    result.add(k);
                }
            }
        }
        return result;
    }
    
    /** コンストラクタ */
    public CommandHandler() {
        this.runners = new LinkedHashMap<>();
        register(new RegenerateCommandHandler());
        register(new JoinCommandHandler());
        register(new HelpCommandHandler());
    }
    private void register(final ISubCommandRunner runner){
        if (runner == null){ return; }
        this.runners.put(runner.getName().toLowerCase(), runner);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        if(!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        if (args.length == 0){
            return findRunner(runners, HelpCommandHandler.SUBCOMMAND).execute(sender, args);
        }

        final String sub = args[0].toLowerCase();
        ISubCommandRunner runner = findRunner(runners, sub);
        if (runner != null){
            return runner.execute(sender, args);
        }
        sender.sendMessage("Unknown subcommand: " + sub);
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase(COMMAND)) {
            throw new IllegalStateException("wrong command body");
        }
        List<String> completions = new ArrayList<>();
        if (args.length == 0){
            completions = findRunnerWithNameHead(sender, "");
            return completions;
        }
        if (args.length == 1){
            completions = findRunnerWithNameHead(sender, args[0]);
            return completions;
        }
        return completions;
    }
}
