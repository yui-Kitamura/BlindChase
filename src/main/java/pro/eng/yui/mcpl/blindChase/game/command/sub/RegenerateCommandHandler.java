package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pro.eng.yui.mcpl.blindChase.game.field.FieldGenerator;
import pro.eng.yui.mcpl.blindChase.game.field.FieldImpl;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;

import java.util.Collections;

public final class RegenerateCommandHandler {

    public static final String SUBCOMMAND = "regenerate";

    private RegenerateCommandHandler(){
        // ignore create instance
    }

    public static boolean execute(CommandSender sender, String[] args){
        int pattern = 0; // default pattern
        World baseWorld = null;
        if (sender instanceof Player p){
            baseWorld = p.getWorld();
        }

        // Generate field without auto-moving players
        FieldImpl field = FieldGenerator.generate(FieldType.FLAT, baseWorld, pattern, Collections.emptyList());

        // Notify players in that world and the executor
        String msg = "新しいフィールドができました";
        World w = field.world();
        for (Player pl : w.getPlayers()){
            pl.sendMessage(msg);
        }
        sender.sendMessage(msg);
        return true;
    }
}
