package pro.eng.yui.mcpl.blindChase.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import pro.eng.yui.mcpl.blindChase.BlindChase;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class BlindChaseConfig {

    private BlindChaseConfig() {
        // ignore instance creation
    }

    private static FileConfiguration config;

    /**
     * Load configuration and merge defaults, saving any missing keys back to the file.
     */
    public static synchronized void load() {
        var plugin = BlindChase.plugin();
        // Ensure base file exists
        plugin.saveDefaultConfig();

        // Reload current config
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        // Load defaults from the jar's config.yml and merge into the current config
        try (InputStream is = plugin.getResource("config.yml")) {
            if (is != null) {
                YamlConfiguration def = YamlConfiguration.loadConfiguration(new InputStreamReader(is, StandardCharsets.UTF_8));
                cfg.setDefaults(def);
                cfg.options().copyDefaults(true); // copy any missing keys
                plugin.saveConfig(); // persist back to disk so file gets new fields
            }
        } catch (Exception ignored) {
            // If default cannot be read, proceed with whatever is loaded
        }

        config = cfg;
    }

    /**
     * Get the loaded configuration. Ensure load() was called first in plugin lifecycle.
     */
    public static FileConfiguration config() {
        return config != null ? config : BlindChase.plugin().getConfig();
    }

    /**
     * Shortcut to get a String value by a typed config key.
     * Returns null if the key is not present or if key is null.
     */
    public static String getString(ConfigKey key) {
        if (key == null){ return null; }
        FileConfiguration cfg = config();
        if (cfg == null){ return null; }
        return cfg.getString(key.path());
    }

    /**
     * Get a String with default fallback if missing or null.
     */
    public static String getString(ConfigKey key, String def) {
        String v = getString(key);
        return v != null ? v : def;
    }

    /**
     * Get an int with default fallback.
     */
    public static int getInt(ConfigKey key, int def) {
        if (key == null) { return def; }
        FileConfiguration cfg = config();
        if (cfg == null) { return def; }
        return cfg.getInt(key.path(), def);
    }
}
