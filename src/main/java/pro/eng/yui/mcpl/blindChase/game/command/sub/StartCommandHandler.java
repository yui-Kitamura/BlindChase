package pro.eng.yui.mcpl.blindChase.game.command.sub;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import pro.eng.yui.mcpl.blindChase.abst.command.AbstSubCommandRunner;
import pro.eng.yui.mcpl.blindChase.abst.command.Permissions;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;
import pro.eng.yui.mcpl.blindChase.lib.item.WhiteCaneUtil;
import pro.eng.yui.mcpl.blindChase.game.listener.WhiteCaneRightClickListener;

import java.util.List;
import java.util.Map;

/**
 * /blindchase start
 * 専用ワールド(BlindChase フィールドワールド)内の全プレイヤーのインベントリをクリアし、
 * 白杖(1本)をメインハンドに配布します。
 * 実行は専用ワールド内でのみ可能（コンソールは専用ワールドがロード済みのときのみ実行可）。
 */
public class StartCommandHandler extends AbstSubCommandRunner {

    public static final String SUBCOMMAND = "start";
    public static final Permission PERMISSION = new Permission(Permissions.START.get());

    public StartCommandHandler(){
        super(SUBCOMMAND, PERMISSION);
    }

    @Override
    protected boolean executeBody(CommandSender sender, String[] args) {
        // 決め打ちの専用ワールドのみ対象
        World fieldWorld = Bukkit.getWorld(Field.FIELD_WORLD_NAME);

        if (sender instanceof Player player) {
            if (player.getWorld() == null || !player.getWorld().getName().equals(Field.FIELD_WORLD_NAME)) {
                player.sendMessage("このコマンドは専用ワールド内でのみ実行できます: " + Field.FIELD_WORLD_NAME);
                return true;
            }
            fieldWorld = player.getWorld();
        } else {
            // Console or other sender: 専用ワールドがロードされていなければ実行不可
            if (fieldWorld == null) {
                sender.sendMessage("専用ワールド(" + Field.FIELD_WORLD_NAME + ")がロードされていないため実行できません");
                return true;
            }
        }

        if (fieldWorld == null) {
            sender.sendMessage("対象ワールドが見つかりませんでした");
            return true;
        }

        int affected = 0;
        for (Player p : fieldWorld.getPlayers()) {
            // インベントリをクリア（メイン・ホットバー・アーマー・オフハンド）
            var inv = p.getInventory();
            inv.clear();
            inv.setHelmet(null);
            inv.setChestplate(null);
            inv.setLeggings(null);
            inv.setBoots(null);
            inv.setItemInOffHand(null);

            // 白杖をメインハンドに付与
            ItemStack cane = WhiteCaneUtil.createWhiteCane();
            inv.setItemInMainHand(cane);
            p.updateInventory();
            // クールダウン(lastUse)とクライアント側のクールダウン表示をリセット
            WhiteCaneRightClickListener.clearLastUse(p.getUniqueId());
            p.setCooldown(Material.STICK, 0);
            affected++;
        }

        sender.sendMessage("専用ワールド(" + fieldWorld.getName() + ")のプレイヤー " + affected + " 人のインベントリをクリアし、白杖を配布しました");
        return true;
    }
}
