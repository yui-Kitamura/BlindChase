package pro.eng.yui.mcpl.blindChase.lib.command;

import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;

/**
 * Common interface for sub-command handlers.
 */
public interface ISubCommandRunner {
    /**
     * Subcommand name (first argument), e.g. "help".
     * @return subcommand name in lower-case
     */
    String getName();

    /**
     * @return permission requirement (never null)
     */
    Permission getPermission();

    /**
     * Execute this subcommand.
     * @param sender command sender
     * @param args raw args including subcommand at index 0
     * @return true if handled
     */
    boolean execute(CommandSender sender, String[] args);
}
