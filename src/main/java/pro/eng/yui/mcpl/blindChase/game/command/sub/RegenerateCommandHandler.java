package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;
import pro.eng.yui.mcpl.blindChase.game.field.FieldGenerator;
import pro.eng.yui.mcpl.blindChase.game.field.FieldImpl;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;

import java.util.Collections;

public class RegenerateCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "regenerate";
    public static final Permission PERMISSION = new Permission(Permissions.REGENERATE.get());

    public RegenerateCommandHandler(){ super(SUBCOMMAND, PERMISSION); }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args){
        int pattern = 0; // default pattern
        World baseWorld = null;
        if (sender instanceof Player p){
            baseWorld = p.getWorld();
        }

        // Generate field without auto-moving players
        FieldImpl field = FieldGenerator.generate(FieldType.FLAT, baseWorld, pattern, Collections.emptyList());

        // Notify players in that world and the executor
        final String msg = "新しいフィールドができました";
        World w = field.world();
        for (Player pl : w.getPlayers()){
            pl.sendMessage(msg);
        }
        sender.sendMessage(msg);
        return true;
    }
}
