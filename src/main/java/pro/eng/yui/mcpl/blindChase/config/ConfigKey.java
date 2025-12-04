package pro.eng.yui.mcpl.blindChase.config;

/**
 * Configuration keys defined as an enum for type-safe access.
 */
public enum ConfigKey {
    // Resource pack related
    RESOURCEPACK_GITHUB_USER("resourcePack.github.user"),
    RESOURCEPACK_GITHUB_REPO("resourcePack.github.repo"),
    RESOURCEPACK_TIMEOUT_SEC("resourcePack.timeoutSec"),
    RESOURCEPACK_RETRIES("resourcePack.retries");

    private final String path;

    ConfigKey(String path) {
        this.path = path;
    }

    /**
     * Returns the YAML path string for this key.
     */
    public String path() {
        return path;
    }
}
