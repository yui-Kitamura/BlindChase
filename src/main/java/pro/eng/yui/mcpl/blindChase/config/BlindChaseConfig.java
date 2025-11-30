package pro.eng.yui.mcpl.blindChase.config;

import org.bukkit.configuration.Configuration;
import pro.eng.yui.mcpl.blindChase.BlindChase;

public class BlindChaseConfig {
    
    private BlindChaseConfig(){
        //ignore create instance
    }
    
    private static Configuration config;
    
    public static void load(){
        BlindChase.plugin().reloadConfig();
        config = BlindChase.plugin().getConfig();
    }
    
    
}
