package pro.eng.yui.mcpl.blindChase;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import pro.eng.yui.mcpl.blindChase.config.BlindChaseConfig;
import pro.eng.yui.mcpl.blindChase.impl.command.CommandHandler;

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
        this.getLogger().info("=== BlindChase copyright ===");
        this.getLogger().info("= all right reserved by Yui-KITAMURA =");
        this.getLogger().info("=== (C)Yui-KITAMURA 2025- ===");
    }
    
    @Override
    public void onEnable() {
        super.onEnable();
        addCommandHandler();
        plugin().getLogger().info("BlindChase is enabled!");    
    }
    private void addCommandHandler(){
        PluginCommand cmd = this.getCommand(CommandHandler.COMMAND);
        if (cmd == null) {
            this.getLogger().warning("Command '" + CommandHandler.COMMAND + "' not found in plugin.yml");
            return;
        }
        CommandHandler handler = new CommandHandler();
        cmd.setExecutor(handler);
        cmd.setTabCompleter(handler);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        plugin().getLogger().info("BlindChase is disabled.");
    }
}
