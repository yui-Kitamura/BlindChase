package pro.eng.yui.mcpl.blindChase.lib.field;

import org.bukkit.Material;

public class WoodSet {

    private final Material planks;
    public Material getPlanks(){ return planks; }
    private final Material log;
    public Material getLog(){ return log; }
    private final Material fence;
    public Material getFence(){ return fence; }
    private final Material glass;
    public Material getGlass(){ return glass; }

    public WoodSet(Material planks, Material log, Material fence, Material glass) {
        this.planks = planks;
        this.log = log;
        this.fence = fence;
        this.glass = glass;
    }

    public static WoodSet of(int pattern) {
        int idx = Math.floorMod(pattern, 9);
        return switch (idx) {
            case 0 -> new WoodSet(Material.OAK_PLANKS, Material.OAK_LOG, Material.OAK_FENCE, Material.YELLOW_STAINED_GLASS);
            case 1 -> new WoodSet(Material.SPRUCE_PLANKS, Material.SPRUCE_LOG, Material.SPRUCE_FENCE, Material.BROWN_STAINED_GLASS);
            case 2 -> new WoodSet(Material.BIRCH_PLANKS, Material.BIRCH_LOG, Material.BIRCH_FENCE, Material.WHITE_STAINED_GLASS);
            case 3 -> new WoodSet(Material.JUNGLE_PLANKS, Material.JUNGLE_LOG, Material.JUNGLE_FENCE, Material.ORANGE_STAINED_GLASS);
            case 4 -> new WoodSet(Material.ACACIA_PLANKS, Material.ACACIA_LOG, Material.ACACIA_FENCE, Material.ORANGE_STAINED_GLASS);
            case 5 -> new WoodSet(Material.DARK_OAK_PLANKS, Material.DARK_OAK_LOG, Material.DARK_OAK_FENCE, Material.BROWN_STAINED_GLASS);
            case 6 -> new WoodSet(Material.MANGROVE_PLANKS, Material.MANGROVE_LOG, Material.MANGROVE_FENCE, Material.RED_STAINED_GLASS);
            case 7 -> new WoodSet(Material.CHERRY_PLANKS, Material.CHERRY_LOG, Material.CHERRY_FENCE, Material.PINK_STAINED_GLASS);
            default -> new WoodSet(Material.BAMBOO_PLANKS, Material.BAMBOO_BLOCK, Material.BAMBOO_FENCE, Material.LIME_STAINED_GLASS);
        };
    }
    
}
