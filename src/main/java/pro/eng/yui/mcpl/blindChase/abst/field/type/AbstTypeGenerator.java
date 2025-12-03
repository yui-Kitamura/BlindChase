package pro.eng.yui.mcpl.blindChase.abst.field.type;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import pro.eng.yui.mcpl.blindChase.lib.field.type.TypeGenerator;

/**
 * Base class providing utilities for field type generators.
 * Implements template method that calls concrete body generation.
 */
public abstract class AbstTypeGenerator implements TypeGenerator {

    @Override
    public final void generate(final World world, final int pattern) {
        if (world == null) { return; }
        generateBody(world, pattern);
    }

    protected abstract void generateBody(World world, int pattern);

    // ===== Utilities for block placement and lighting =====
    protected void fillSquare(final World world, final int xMin, final int xMax,
                              final int zMin, final int zMax, final int y, final Material material) {
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                setBlock(world, x, y, z, material);
            }
        }
    }

    protected void placeTwoHighPerimeter(final World world, final int xMin, final int xMax,
                                         final int zMin, final int zMax, final int baseY, final Material material) {
        for (int x = xMin; x <= xMax; x++) {
            setBlock(world, x, baseY, zMin, material);
            setBlock(world, x, baseY + 1, zMin, material);
            setBlock(world, x, baseY, zMax, material);
            setBlock(world, x, baseY + 1, zMax, material);
        }
        for (int z = zMin; z <= zMax; z++) {
            setBlock(world, xMin, baseY, z, material);
            setBlock(world, xMin, baseY + 1, z, material);
            setBlock(world, xMax, baseY, z, material);
            setBlock(world, xMax, baseY + 1, z, material);
        }
    }

    protected void setBlock(final World world, final int x, final int y, final int z, final Material material) {
        final Block b = world.getBlockAt(x, y, z);
        if (b.getType() != material) {
            b.setType(material, true);
        }
    }

    protected void refreshAreaLighting(final World world, final int xMin, final int xMax,
                                       final int zMin, final int zMax) {
        final int blocksPerChunk = 16;
        final int chunkXMin = Math.floorDiv(xMin, blocksPerChunk);
        final int chunkXMax = Math.floorDiv(xMax, blocksPerChunk);
        final int chunkZMin = Math.floorDiv(zMin, blocksPerChunk);
        final int chunkZMax = Math.floorDiv(zMax, blocksPerChunk);
        for (int cx = chunkXMin; cx <= chunkXMax; cx++) {
            for (int cz = chunkZMin; cz <= chunkZMax; cz++) {
                world.refreshChunk(cx, cz);
            }
        }
    }
}
