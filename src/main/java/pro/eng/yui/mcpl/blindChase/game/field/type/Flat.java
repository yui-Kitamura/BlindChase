package pro.eng.yui.mcpl.blindChase.game.field.type;

import org.bukkit.World;
import pro.eng.yui.mcpl.blindChase.abst.field.type.AbstTypeGenerator;
import pro.eng.yui.mcpl.blindChase.lib.field.WoodSet;

public class Flat extends AbstTypeGenerator {

    @Override
    protected void generateBody(final World world, final int pattern) {
        final WoodSet wood = WoodSet.of(pattern);

        // y=0: 100x100 planks (centered around origin: -50..49)
        fillSquare(world, -50, 50, -50, 50, 0, wood.getPlanks());

        // y=1: yellow carpet tactile paving in a 20-block grid across the floor
        generateYellowCarpetGrid(world, -50, 50, -50, 50, 1, 20);

        // y=20: 20x20 glass with two-high glass perimeter
        fillSquare(world, -10, 10, -10, 10, 20, wood.getGlass());
        placeTwoHighPerimeter(world, -10, 10, -10, 10, 21, wood.getGlass());

        // y=40: 20x20 award floor of logs (same wood type)
        fillSquare(world, -10, 10, -10, 10, 40, wood.getLog());

        // refresh lighting for modified chunks
        refreshAreaLighting(world, -50, 50, -50, 50);
    }

    /** 碁盤目状に配置する */
    private void generateYellowCarpetGrid(final World world,
                                          final int xMin, final int xMax,
                                          final int zMin, final int zMax,
                                          final int y, final int spacing) {
        if (spacing <= 0) { return; }
        for (int x = xMin; x <= xMax; x++) {
            final boolean onX = Math.floorMod(x, spacing) == 0;
            for (int z = zMin; z <= zMax; z++) {
                final boolean onZ = Math.floorMod(z, spacing) == 0;
                if (onX || onZ) {
                    placeYellowCarpet(world, x, y, z);
                }
            }
        }
    }
}
