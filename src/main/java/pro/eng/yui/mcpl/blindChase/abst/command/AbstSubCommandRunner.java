package pro.eng.yui.mcpl.blindChase.abst.command;

import org.bukkit.Color;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import pro.eng.yui.mcpl.blindChase.lib.command.ISubCommandRunner;

public abstract class AbstSubCommandRunner implements ISubCommandRunner {

    private final String name;
    private final Permission permission;

    protected AbstSubCommandRunner(final String name, final Permission permission) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("subcommand name is required");
        }
        this.name = name.toLowerCase();
        this.permission = permission;
    }

    @Override
    public final String getName() {
        return name;
    }

    @Override
    public final Permission getPermission() {
        return permission;
    }

    @Override
    public final boolean execute(final CommandSender sender, final String[] args) {
        if (!sender.hasPermission(permission)) {
            sender.sendMessage(Color.RED + "権限がありません");
            return true;
        }
        return executeBody(sender, args);
    }

    /**
     * Actual command body executed after permission check passes.
     */
    protected abstract boolean executeBody(CommandSender sender, String[] args);
}
