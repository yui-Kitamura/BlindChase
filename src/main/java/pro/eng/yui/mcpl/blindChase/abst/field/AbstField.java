package pro.eng.yui.mcpl.blindChase.abst.field;

import org.bukkit.World;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;

public abstract class AbstField implements Field {

    protected final World world;

    protected AbstField(World world) {
        this.world = world;
    }

    @Override
    public World world() {
        return world;
    }
}
