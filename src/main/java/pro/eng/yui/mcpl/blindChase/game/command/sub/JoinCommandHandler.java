package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.bukkit.configuration.file.YamlConfiguration;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;
import pro.eng.yui.mcpl.blindChase.game.field.FieldGenerator;
import pro.eng.yui.mcpl.blindChase.game.field.FieldType;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;
import pro.eng.yui.mcpl.blindChase.lib.item.WhiteCaneUtil;
import pro.eng.yui.mcpl.blindChase.lib.resourcepack.ResourcePackService;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class JoinCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "join";
    public static final Permission PERMISSION = new Permission(Permissions.JOIN.get());

    public JoinCommandHandler(){ super(SUBCOMMAND, PERMISSION); }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args){
        if (!(sender instanceof Player player)){
            sender.sendMessage("このコマンドはプレイヤーのみが実行できます");
            return true;
        }

        // Save player's original location, inventory and XP before teleporting
        try {
            File playersDir = new File(BlindChase.plugin().getDataFolder(), "players");
            if (!playersDir.exists() && !playersDir.mkdirs()) {
                player.sendMessage("元の位置の保存に失敗しました（フォルダ作成失敗）");
            } else {
                File saveFile = new File(playersDir, player.getUniqueId().toString() + ".yml");
                Location cur = player.getLocation();
                YamlConfiguration yaml = new YamlConfiguration();
                yaml.set("world", cur.getWorld() == null ? null : cur.getWorld().getName());
                yaml.set("x", cur.getX());
                yaml.set("y", cur.getY());
                yaml.set("z", cur.getZ());
                yaml.set("yaw", cur.getYaw());
                yaml.set("pitch", cur.getPitch());
                // inventory
                ItemStack[] contents = player.getInventory().getContents();
                ItemStack[] armor = player.getInventory().getArmorContents();
                ItemStack offhand = player.getInventory().getItemInOffHand();
                yaml.set("inv.contents", Arrays.asList(contents));
                yaml.set("inv.armor", Arrays.asList(armor));
                yaml.set("inv.offhand", offhand);
                // experience
                yaml.set("xp.level", player.getLevel());
                yaml.set("xp.exp", (double) player.getExp()); // store as double for YAML
                yaml.set("xp.total", player.getTotalExperience());
                yaml.save(saveFile);
            }
        } catch (IOException e) {
            player.sendMessage("元の位置の保存に失敗しました");
            BlindChase.plugin().getLogger().warning("Failed to save original location: " + e.getMessage());
        }

        World world = Bukkit.getWorld(Field.FIELD_WORLD_NAME);
        if (world == null){
            // Create field world
            player.sendMessage("転送先を生成しています…");
            Field newField = FieldGenerator.generate(FieldType.FLAT, null, 0, Collections.singleton(player));
            world = newField.world();
        }

        // Teleport to waiting location
        Location wait = new Location(world, 0.5, 22.0, 0.5, 0.0f, 0.0f);
        // Fix respawn
        world.setSpawnLocation(wait);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        player.setBedSpawnLocation(wait, true);
        player.sendMessage("転送します…");
        player.teleport(wait);
        // Clear inventory for the game and give one White Cane
        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
        player.getInventory().setItemInOffHand(null);
        player.getInventory().addItem(WhiteCaneUtil.createWhiteCane());
        // Set experience to 0.8Lv as requested
        player.setTotalExperience(0);
        player.setLevel(0);
        player.setExp(0.8f);
        // Apply resource pack from GitHub releases (if resolved)
        ResourcePackService svc = ResourcePackService.get();
        if (svc != null) {
            svc.applyToPlayer(player);
        }
        player.sendMessage("転送しました");
        return true;
    }
}
