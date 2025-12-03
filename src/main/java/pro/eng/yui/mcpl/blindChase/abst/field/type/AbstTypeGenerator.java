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
        if (world == null) { throw new IllegalArgumentException(new NullPointerException("world")); }
        generateBody(world, pattern);
        // Ensure yellow carpet paths form connected lines on the same Y level
        ensureYellowCarpetAdjacency(world);
        // Ensure yellow carpets have a solid 3x3 support right beneath them
        ensureYellowCarpetSupport(world);
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

    // ===== Yellow carpet (tactile paving) helpers =====

    protected void placeYellowCarpet(final World world, final int x, final int y, final int z) {
        setBlock(world, x, y, z, Material.YELLOW_CARPET);
    }


    /**
     * For every yellow carpet in loaded chunks, ensure at least one of the cross-adjacent
     * (N,E,S,W) blocks at the same Y is also a yellow carpet. If none exists, place one
     * in the first available direction in the order +X, -X, +Z, -Z.
     */
    protected void ensureYellowCarpetAdjacency(final World world) {
        if (world == null) { return; }

        final int blocksPerChunk = 16;
        final int minY = world.getMinHeight();
        final int maxY = world.getMaxHeight();

        for (final Chunk chunk : world.getLoadedChunks()) {
            final int baseX = chunk.getX() * blocksPerChunk;
            final int baseZ = chunk.getZ() * blocksPerChunk;

            for (int lx = 0; lx < blocksPerChunk; lx++) {
                for (int lz = 0; lz < blocksPerChunk; lz++) {
                    final int x = baseX + lx;
                    final int z = baseZ + lz;

                    for (int y = minY; y < maxY; y++) {
                        final Block b = world.getBlockAt(x, y, z);
                        if (b.getType() != Material.YELLOW_CARPET) { continue; }

                        // Check four neighbors on the same Y
                        boolean hasNeighbor =
                            world.getBlockAt(x + 1, y, z).getType() == Material.YELLOW_CARPET ||
                            world.getBlockAt(x - 1, y, z).getType() == Material.YELLOW_CARPET ||
                            world.getBlockAt(x, y, z + 1).getType() == Material.YELLOW_CARPET ||
                            world.getBlockAt(x, y, z - 1).getType() == Material.YELLOW_CARPET;

                        if (hasNeighbor) { continue; }

                        // Place a neighbor in the first available direction
                        if (world.getBlockAt(x + 1, y, z).getType() == Material.AIR) {
                            placeYellowCarpet(world, x + 1, y, z);
                        } else if (world.getBlockAt(x - 1, y, z).getType() == Material.AIR) {
                            placeYellowCarpet(world, x - 1, y, z);
                        } else if (world.getBlockAt(x, y, z + 1).getType() == Material.AIR) {
                            placeYellowCarpet(world, x, y, z + 1);
                        } else if (world.getBlockAt(x, y, z - 1).getType() == Material.AIR) {
                            placeYellowCarpet(world, x, y, z - 1);
                        }
                    }
                }
            }
        }
    }

    /**
     * Helper: For every yellow carpet in loaded chunks, ensure the 3x3 area directly beneath it (y-1)
     * is filled with blocks. Missing blocks (AIR) are filled using the same material as the block
     * directly beneath the carpet's center.
     */
    protected void ensureYellowCarpetSupport(final World world) {
        if (world == null) { return; }

        final int blocksPerChunk = 16;
        final int minY = world.getMinHeight();
        final int maxY = world.getMaxHeight();

        for (final Chunk chunk : world.getLoadedChunks()) {
            final int baseX = chunk.getX() * blocksPerChunk;
            final int baseZ = chunk.getZ() * blocksPerChunk;

            for (int lx = 0; lx < blocksPerChunk; lx++) {
                for (int lz = 0; lz < blocksPerChunk; lz++) {
                    final int x = baseX + lx;
                    final int z = baseZ + lz;

                    for (int y = minY + 1; y < maxY; y++) { // start at minY+1 to safely access y-1
                        final Block b = world.getBlockAt(x, y, z);
                        if (b.getType() != Material.YELLOW_CARPET) { continue; }

                        final int supportY = y - 1;
                        if (supportY < minY) { continue; }

                        final Material centerBelowMat = world.getBlockAt(x, supportY, z).getType();
                        if (centerBelowMat == Material.AIR) { continue; }

                        for (int dx = -1; dx <= 1; dx++) {
                            for (int dz = -1; dz <= 1; dz++) {
                                final int tx = x + dx;
                                final int tz = z + dz;
                                final Block target = world.getBlockAt(tx, supportY, tz);
                                if (target.getType() == Material.AIR) {
                                    target.setType(centerBelowMat, true);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
