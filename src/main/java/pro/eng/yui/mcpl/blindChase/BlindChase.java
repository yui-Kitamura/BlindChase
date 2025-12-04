package pro.eng.yui.mcpl.blindChase;

import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pro.eng.yui.mcpl.blindChase.config.BlindChaseConfig;
import pro.eng.yui.mcpl.blindChase.game.command.CommandHandler;
import pro.eng.yui.mcpl.blindChase.game.field.FieldGenerator;
import pro.eng.yui.mcpl.blindChase.lib.field.Field;

public final class BlindChase extends JavaPlugin {

    // fields
    private static BlindChase plugin;
    public static BlindChase plugin(){
        return plugin;
    }
    
    // constructor
    public BlindChase(){
        super();
    }
    
    //methods
    @Override
    public void onLoad(){
        super.onLoad();
        plugin = this;
        createDataFolder();
        generateDefaultConfig();
        loadConfig();
        outputCopyright();
    }
    private void createDataFolder(){
        //noinspection PointlessBooleanExpression
        if(getDataFolder().exists() == false){
            if(getDataFolder().mkdir()) {
                plugin.getLogger().info("created data folder");
            }else{
                plugin.getLogger().warning("failed to create data folder");
            }
        }
    }
    private void generateDefaultConfig(){
        this.saveDefaultConfig();
    }
    private void loadConfig(){
        BlindChaseConfig.load();
    }
    private void outputCopyright() {
        plugin().getLogger().info("=== BlindChase copyright ===");
        plugin().getLogger().info("= all right reserved by Yui-KITAMURA =");
        plugin().getLogger().info("=== (C)Yui-KITAMURA 2025- ===");
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        plugin = this;
        addCommandHandler();
        loadWorld();
        plugin().getLogger().info("BlindChase is enabled!");
    }
    private void addCommandHandler(){
        PluginCommand cmd = plugin().getCommand(CommandHandler.COMMAND);
        if (cmd == null) {
            plugin().getLogger().warning("Command '" + CommandHandler.COMMAND + "' not found in plugin.yml");
            return;
        }
        CommandHandler handler = new CommandHandler();
        cmd.setExecutor(handler);
        cmd.setTabCompleter(handler);
    }
    private void loadWorld() {
        FieldGenerator.getOrCreateVoidWorld(Field.FIELD_WORLD_NAME);
        plugin().getLogger().info("BlindChase world is loaded!");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        plugin().getLogger().info("BlindChase is disabled.");
    }
}
