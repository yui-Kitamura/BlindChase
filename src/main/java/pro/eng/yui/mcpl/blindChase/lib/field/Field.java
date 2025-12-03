package pro.eng.yui.mcpl.blindChase.lib.field;

import org.bukkit.World;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;

public interface Field {
    FieldType fieldType();
    World world();
}
