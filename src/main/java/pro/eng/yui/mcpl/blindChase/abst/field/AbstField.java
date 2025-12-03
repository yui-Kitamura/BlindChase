package pro.eng.yui.mcpl.blindChase.abst.field;

import org.bukkit.World;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;

public abstract class AbstField implements Field {

    protected final FieldType fieldType;
    protected final World world;

    protected AbstField(FieldType fieldType, World world) {
        this.fieldType = fieldType;
        this.world = world;
    }

    @Override
    public FieldType fieldType(){ return fieldType; }
    @Override
    public World world(){ return world; }
}
