package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.configuration.file.YamlConfiguration;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;

import java.io.File;
import java.util.UUID;

public class LeaveCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "leave";
    public static final Permission PERMISSION = new Permission(Permissions.LEAVE.get());

    public LeaveCommandHandler(){ super(SUBCOMMAND, PERMISSION); }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args){
        if (!(sender instanceof Player player)){
            sender.sendMessage("このコマンドはプレイヤーのみが実行できます");
            return true;
        }

        UUID uuid = player.getUniqueId();
        File playersDir = new File(BlindChase.plugin().getDataFolder(), "players");
        File saveFile = new File(playersDir, uuid.toString() + ".yml");
        if (!saveFile.exists()){
            // Fallback: unknown original position -> teleport to world spawn
            teleportToWorldSpawn(player, "元の位置の保存データが見つからないため、ワールドスポーンへ戻しました");
            return true;
        }

        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(saveFile);
        String worldName = yaml.getString("world");
        double x = yaml.getDouble("x");
        double y = yaml.getDouble("y");
        double z = yaml.getDouble("z");
        float yaw = (float) yaml.getDouble("yaw");
        float pitch = (float) yaml.getDouble("pitch");

        if (worldName == null){
            // Fallback: invalid data -> teleport to world spawn
            teleportToWorldSpawn(player, "保存データが壊れているため、ワールドスポーンへ戻しました");
            return true;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null){
            // Fallback: missing original world -> teleport to world spawn
            teleportToWorldSpawn(player, "元のワールドが見つからないため、ワールドスポーンへ戻しました");
            return true;
        }

        Location dest = new Location(world, x, y, z, yaw, pitch);
        boolean ok = player.teleport(dest);
        if (ok){
            player.sendMessage("元の位置に戻しました");
            // 後片付け: 成功したら保存を削除
            //noinspection ResultOfMethodCallIgnored
            saveFile.delete();
        } else {
            // Fallback: teleport failed -> try world spawn
            teleportToWorldSpawn(player, "テレポートに失敗したため、ワールドスポーンへ戻しました");
        }
        return true;
    }

    /**
     * Fallback teleport: send player to the spawn location of their current world.
     */
    private void teleportToWorldSpawn(final Player player, final String message){
        Location spawn = null;
        World any = Bukkit.getWorlds().isEmpty() ? null : Bukkit.getWorlds().get(0);
        if (any != null) {
            spawn = any.getSpawnLocation();
        }
        if (spawn != null) {
            player.teleport(spawn);
            if (message != null && !message.isEmpty()) {
                player.sendMessage(message);
            }
        } else {
            player.sendMessage("ワールドスポーンへのテレポートに失敗しました");
        }
    }
}
