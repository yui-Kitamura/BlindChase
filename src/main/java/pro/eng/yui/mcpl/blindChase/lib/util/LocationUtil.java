package pro.eng.yui.mcpl.blindChase.lib.util;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import pro.eng.yui.mcpl.blindChase.BlindChase;

import java.util.ArrayList;
import java.util.List;

public class LocationUtil {
    private LocationUtil(){
        //ignore create instance
    }
    
    public double distance(Location a, Location b){
        if (a == null || b == null){ return Double.NaN; }
        if (a.getWorld() == null || b.getWorld() == null){ return Double.NaN; }
        if (!a.getWorld().equals(b.getWorld())){ return Double.NaN; }
        return a.distance(b);
    }
    
    public boolean isInRange(Location base, Location target, double radius){
        if (base == null || target == null){ return false; }
        if (base.getWorld() == null || target.getWorld() == null) { return false; }
        if (!base.getWorld().equals(target.getWorld())) { return false; }
        if (radius < 0) { return false; }
        double r2 = radius * radius;
        return base.distanceSquared(target) <= r2;
    }
    
    public Vector direction(Location from, Location to){
        if (from == null || to == null) { return null; }
        if (from.getWorld() == null || to.getWorld() == null) { return null; }
        if (!from.getWorld().equals(to.getWorld())) { return null; }
        Vector dir = to.toVector().subtract(from.toVector());
        if (dir.lengthSquared() == 0) {
            return new Vector(0, 0, 0);
        }
        return dir.normalize();
    }
    
    public Block getBlockAt(Location loc){
        if (loc == null) { return null; }
        return loc.getBlock();
    }
    
    public Block lookingAt(Location from, float pitch, float yaw){
        int visible = BlindChase.plugin().getServer().getViewDistance();
        return getFirstBlockInDistance(from, pitch, yaw, visible);
    }
    
    public boolean isInSameBlock(Location a, Location b){
        if (a == null || b == null) { return false; }
        if (a.getWorld() == null || b.getWorld() == null) { return false; }
        if (!a.getWorld().equals(b.getWorld())) { return false; }
        return a.getBlockX() == b.getBlockX() && a.getBlockY() == b.getBlockY() && a.getBlockZ() == b.getBlockZ();
    }
    
    public Block getFirstBlockInDistance(Location from, float pitch, float yaw, int maxDistance){
        if (from == null || from.getWorld() == null) { return null;}
        if (maxDistance <= 0) { return null; }

        final double yawRad = Math.toRadians(yaw);
        final double pitchRad = Math.toRadians(pitch);
        final double xz = Math.cos(pitchRad);
        Vector dir = new Vector(-Math.sin(yawRad) * xz, -Math.sin(pitchRad), Math.cos(yawRad) * xz).normalize();

        World world = from.getWorld();
        // 隙間対策 1/5 blockずつ確認する
        final double step = 0.2;
        Vector stepVec = dir.clone().multiply(step);
        Location cursor = from.clone();

        final int maxSteps = (int) Math.ceil(maxDistance / step);
        for (int i = 0; i < maxSteps; i++) {
            cursor.add(stepVec);
            Block block = world.getBlockAt(cursor);
            if (block.getType().isAir() == false) {
                return block;
            }
        }
        return null;
    }
    
    public Location getEyeLocation(Entity entity){
        if (entity == null) { return null; }
        if (entity instanceof LivingEntity living) {
            return living.getEyeLocation();
        }
        return entity.getLocation();
    }
    
    public List<Player> getPlayersInRadius(Location from, double radius){
        List<Player> result = new ArrayList<>();
        if (from == null || from.getWorld() == null) {
            return result;
        }
        double r2 = radius * radius;
        for (Player p : from.getWorld().getPlayers()) {
            Location pl = p.getLocation();
            if (pl.getWorld() != null && pl.getWorld().equals(from.getWorld())) {
                if (from.distanceSquared(pl) <= r2) {
                    result.add(p);
                }
            }
        }
        return result;
    }
    
}
