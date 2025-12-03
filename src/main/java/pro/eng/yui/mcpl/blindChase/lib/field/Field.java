package pro.eng.yui.mcpl.blindChase.lib.field;

import org.bukkit.World;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;

public interface Field {
    // constants
    String FIELD_WORLD_NAME = "blindchase_world";

    // basic info
    FieldType fieldType();
    World world();
}
