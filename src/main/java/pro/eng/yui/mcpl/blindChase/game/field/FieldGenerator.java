package pro.eng.yui.mcpl.blindChase.game.field;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;
import pro.eng.yui.mcpl.blindChase.lib.field.WoodSet;

import java.util.Collection;
import java.util.Random;

public class FieldGenerator {

    private FieldGenerator() {
        /* ignore create instance */
    }

    public static FieldImpl generate(FieldType type) {
        return generate(type, null, 0, Bukkit.getOnlinePlayers());
    }

    public static FieldImpl generate(FieldType type, int pattern) {
        return generate(type, null, pattern, Bukkit.getOnlinePlayers());
    }

    public static FieldImpl generate(FieldType type, World world) {
        return generate(type, world, 0, Bukkit.getOnlinePlayers());
    }

    public static FieldImpl generate(FieldType type, World world, int pattern) {
        return generate(type, world, pattern, Bukkit.getOnlinePlayers());
    }

    public static FieldImpl generate(FieldType type, int pattern, Collection<? extends Player> playersToMove) {
        return generate(type, null, pattern, playersToMove);
    }

    public static FieldImpl generate(FieldType type, World world, int pattern, Collection<? extends Player> playersToMove) {
        World targetWorld = (world != null) ? world : getOrCreateVoidWorld(Field.FIELD_WORLD_NAME);
        FieldImpl field = new FieldImpl(type, targetWorld);
        movePlayersToWaiting(targetWorld, playersToMove);
        generate(targetWorld, pattern);
        return field;
    }

    private static void movePlayersToWaiting(World world, Collection<? extends Player> players) {
        if (world == null || players == null) { return; }
        Location wait = waitingLocation(world);
        for (Player p : players) {
            if (p == null) { continue; }
            p.teleport(wait);
        }
    }

    private static Location waitingLocation(World world) {
        // Center of the glass platform
        Location loc = new Location(world, 0.5, 22.0, 0.5);
        loc.setYaw(0.0f);
        loc.setPitch(0.0f);
        return loc;
    }

    private static World getOrCreateVoidWorld(String name) {
        World w = Bukkit.getWorld(name);
        if (w != null){ return w; }
        WorldCreator creator = new WorldCreator(name);
        creator.generator(new EmptyChunkGenerator());
        creator.generateStructures(false);
        return Bukkit.createWorld(creator);
    }

    /**
     * Simple empty chunk generator for a void world.
     */
    @SuppressWarnings("deprecation")
    private static class EmptyChunkGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world); // empty chunk
        }
    }

    private static void generate(World world, int pattern) {
        WoodSet wood = WoodSet.of(pattern);

        // y=0: 100x100 planks (centered around origin: -50..49)
        fillSquare(world, -50, 50, -50, 50, 0, wood.getPlanks());

        // y=20: 20x20 glass with fence on perimeter
        fillSquare(world, -10, 10, -10, 10, 20, wood.getGlass());
        // replace fence with two-high stained glass perimeter similar to wood color
        placeTwoHighGlassPerimeter(world, -10, 10, -10, 10, 21, wood.getGlass());

        // y=40: 20x20 award floor of logs (same wood type)
        fillSquare(world, -10, 10, -10, 10, 40, wood.getLog());

        // refresh lighting for modified chunks
        refreshAreaLighting(world, -50, 50, -50, 50);
    }

    private static void fillSquare(World world, int xMin, int xMax, int zMin, int zMax, int y, Material material) {
        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                setBlock(world, x, y, z, material);
            }
        }
    }

    private static void placeTwoHighGlassPerimeter(World world, int xMin, int xMax, int zMin, int zMax, int baseY, Material glass) {
        for (int x = xMin; x <= xMax; x++) {
            setBlock(world, x, baseY, zMin, glass);
            setBlock(world, x, baseY + 1, zMin, glass);
            setBlock(world, x, baseY, zMax, glass);
            setBlock(world, x, baseY + 1, zMax, glass);
        }
        for (int z = zMin; z <= zMax; z++) {
            setBlock(world, xMin, baseY, z, glass);
            setBlock(world, xMin, baseY + 1, z, glass);
            setBlock(world, xMax, baseY, z, glass);
            setBlock(world, xMax, baseY + 1, z, glass);
        }
    }

    private static void setBlock(World world, int x, int y, int z, Material material) {
        Block b = world.getBlockAt(x, y, z);
        if (b.getType() != material) {
            // apply physics to trigger lighting updates
            b.setType(material, true);
        }
    }

    private static void refreshAreaLighting(World world, int xMin, int xMax, int zMin, int zMax) {
        final int blocksParChunk = 16;
        int chunkXMin = Math.floorDiv(xMin, blocksParChunk);
        int chunkXMax = Math.floorDiv(xMax, blocksParChunk);
        int chunkZMin = Math.floorDiv(zMin, blocksParChunk);
        int chunkZMax = Math.floorDiv(zMax, blocksParChunk);
        for (int cx = chunkXMin; cx <= chunkXMax; cx++) {
            for (int cz = chunkZMin; cz <= chunkZMax; cz++) {
                world.refreshChunk(cx, cz);
            }
        }
    }
}
