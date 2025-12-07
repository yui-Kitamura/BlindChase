package pro.eng.yui.mcpl.blindChase.lib.item;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import pro.eng.yui.mcpl.blindChase.BlindChase;

import java.util.List;

/**
 * Utility for White Cane item (based on stick with texture override).
 */
public final class WhiteCaneUtil {

    private static final NamespacedKey KEY = new NamespacedKey(BlindChase.plugin(), "white_cane");
    // Adjust to match your resource pack's CustomModelData id
    private static final int CUSTOM_MODEL_DATA = 10_001;
    // Optional animation frames for right-click sway
    public static final int CMD_SWAY_LEFT = 10_011;
    public static final int CMD_SWAY_RIGHT = 10_013;

    private WhiteCaneUtil() {
    }

    /**
     * Create a White Cane ItemStack (stick-based with custom model data and tag).
     */
    public static ItemStack createWhiteCane() {
        ItemStack item = new ItemStack(Material.STICK, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.WHITE + "White Cane");
            meta.setLore(List.of(ChatColor.GRAY + "A helpful white cane."));
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            meta.setCustomModelData(CUSTOM_MODEL_DATA);
            PersistentDataContainer pdc = meta.getPersistentDataContainer();
            pdc.set(KEY, PersistentDataType.BYTE, (byte) 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    /**
     * Determine whether an ItemStack is the White Cane managed by this plugin.
     */
    public static boolean isWhiteCane(ItemStack item) {
        if (item == null || item.getType() != Material.STICK) { return false; }
        ItemMeta meta = item.getItemMeta();
        if (meta == null) { return false; }
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        Byte flag = pdc.get(KEY, PersistentDataType.BYTE);
        return flag != null && flag == (byte) 1;
    }

    /**
     * Checks if the player holds the White Cane in their main hand.
     */
    public static boolean isHoldingCaneInMainHand(Player player) {
        if (player == null) { return false; }
        ItemStack inMain = player.getInventory().getItemInMainHand();
        return isWhiteCane(inMain);
    }

    /** Base CustomModelData value for the cane. */
    public static int getBaseCustomModelData() {
        return CUSTOM_MODEL_DATA;
    }
}
