package pro.eng.yui.mcpl.blindChase.lib.resourcepack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.config.BlindChaseConfig;
import pro.eng.yui.mcpl.blindChase.config.ConfigKey;
import pro.eng.yui.mcpl.blindChase.lib.util.GitHubReleaseClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

/**
 * Resolves GitHub Release asset and applies ResourcePack to players.
 */
public final class ResourcePackService {

    public static final class Info {
        public final String pluginVersion;
        public final String resolvedTag;
        public final String assetName;
        public final String url;

        Info(String pluginVersion, String resolvedTag, String assetName, String url) {
            this.pluginVersion = pluginVersion;
            this.resolvedTag = resolvedTag;
            this.assetName = assetName;
            this.url = url;
        }
    }

    private static final AtomicReference<ResourcePackService> INSTANCE = new AtomicReference<>();

    public static void init(Plugin plugin) {
        ResourcePackService svc = new ResourcePackService(plugin);
        INSTANCE.set(svc);
        svc.resolveAsync();
    }

    public static ResourcePackService get() {
        return INSTANCE.get();
    }

    private final Plugin plugin;
    private final String ghUser;
    private final String ghRepo;
    private final Pattern assetNamePattern;
    private final String prompt;
    private final int timeoutMs;
    private final int retries;

    private volatile Info lastInfo;
    public Info getInfo() { return lastInfo; }
    
    private ResourcePackService(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.ghUser = BlindChaseConfig.getString(ConfigKey.RESOURCEPACK_GITHUB_USER, "");
        this.ghRepo = BlindChaseConfig.getString(ConfigKey.RESOURCEPACK_GITHUB_REPO, "");
        this.assetNamePattern = Pattern.compile("^BlindChase-(?<tag>\\d+\\.\\d+\\.\\d+)-resourcepack\\.zip$");
        this.prompt = "BlindChase Resource Pack を適用します";
        int timeoutSec = BlindChaseConfig.getInt(ConfigKey.RESOURCEPACK_TIMEOUT_SEC, 10);
        this.timeoutMs = Math.max(1000, timeoutSec * 1000);
        this.retries = Math.max(0, BlindChaseConfig.getInt(ConfigKey.RESOURCEPACK_RETRIES, 2));
    }

    public void resolveAsync() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                resolveNow();
            } catch (Exception e) {
                plugin.getLogger().warning("ResourcePack resolve failed: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    public synchronized void resolveNow() throws IOException {
        if (ghUser == null || ghUser.isBlank() || ghRepo == null || ghRepo.isBlank()) {
            throw new IOException("resourcePack.github.user/repo not configured");
        }
        String ver = BlindChase.plugin().getDescription().getVersion();
        GitHubReleaseClient client = new GitHubReleaseClient(ghUser, ghRepo, assetNamePattern, timeoutMs, retries);
        GitHubReleaseClient.ResolvedAsset ra = client.resolveByMatchVersionThenLatest(ver);
        this.lastInfo = new Info(ver, ra.getTag(), ra.getAssetName(), ra.getDownloadUrl());
        plugin.getLogger().info("Resolved resource pack: tag=" + ra.getTag() + " asset=" + ra.getAssetName());
    }

    public void applyToPlayer(Player player) {
        Info info = this.lastInfo;
        if (info == null || info.url == null) {
            player.sendMessage("リソースパックの準備に失敗しました（後で /blindchase resource reload を試してください）");
            return;
        }
        try {
            player.setResourcePack(info.url);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
