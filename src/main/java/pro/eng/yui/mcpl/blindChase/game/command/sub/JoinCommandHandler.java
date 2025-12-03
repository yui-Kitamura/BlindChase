package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.eng.yui.mcpl.blindChase.game.field.FieldGenerator;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;

import java.util.Collections;

public final class JoinCommandHandler {

    public static final String SUBCOMMAND = "join";
    private static final String FIELD_WORLD_NAME = "blindchase_world";

    private JoinCommandHandler(){
        // ignore create instance
    }

    public static boolean execute(CommandSender sender, String[] args){
        if (!(sender instanceof Player player)){
            sender.sendMessage("このコマンドはプレイヤーのみが実行できます");
            return true;
        }

        World world = Bukkit.getWorld(FIELD_WORLD_NAME);
        if (world == null){
            // Create field world and move only this player
            FieldGenerator.generate(FieldType.FLAT, null, 0, Collections.singleton(player));
            return true;
        }

        // Teleport to waiting center
        Location wait = new Location(world, 0.5, 22.0, 0.5, 0.0f, 0.0f);
        player.teleport(wait);
        return true;
    }
}
