package pro.eng.yui.mcpl.blindChase.lib.resourcepack;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import pro.eng.yui.mcpl.blindChase.BlindChase;
import pro.eng.yui.mcpl.blindChase.config.BlindChaseConfig;
import pro.eng.yui.mcpl.blindChase.config.ConfigKey;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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
    private volatile byte[] lastSha1; // 20 bytes

    private ResourcePackService(Plugin plugin) {
        this.plugin = Objects.requireNonNull(plugin);
        this.ghUser = BlindChaseConfig.getString(ConfigKey.RESOURCEPACK_GITHUB_USER, "");
        this.ghRepo = BlindChaseConfig.getString(ConfigKey.RESOURCEPACK_GITHUB_REPO, "");
        this.assetNamePattern = Pattern.compile("^BlindChase-(?<tag>.+)-resourcepack\\.zip$");
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
        this.lastInfo = new Info(ver, ra.tag, ra.assetName, ra.downloadUrl);
        // obtain sha1
        byte[] sha = null;
        if (ra.sha1Url != null) {
            try {
                String hex = new String(fetch(ra.sha1Url), java.nio.charset.StandardCharsets.UTF_8).trim();
                sha = hexToBytes(hex);
            } catch (Exception ignored) {}
        }
        if (sha == null) {
            // compute by downloading file into memory once
            try {
                byte[] data = fetch(ra.downloadUrl);
                sha = sha1(data);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to compute SHA-1: " + e.getMessage());
            }
        }
        this.lastSha1 = sha; // may be null
        plugin.getLogger().info("Resolved resource pack: tag=" + ra.tag + " asset=" + ra.assetName);
    }

    public void applyToPlayer(Player player) {
        Info info = this.lastInfo;
        if (info == null || info.url == null) {
            player.sendMessage("リソースパックの準備に失敗しました（後で /blindchase resource reload を試してください）");
            return;
        }
        try {
            if (this.lastSha1 != null && this.lastSha1.length == 20) {
                player.setResourcePack(info.url, this.lastSha1, this.prompt);
            } else {
                player.setResourcePack(info.url);
            }
        } catch (Throwable t) {
            // for older API fallback
            try {
                player.setResourcePack(info.url);
            } catch (Throwable ignored) {}
        }
    }

    public Info getInfo() { return lastInfo; }

    // utils
    private byte[] fetch(String urlStr) throws IOException {
        IOException last = null;
        for (int i = 0; i <= retries; i++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(timeoutMs);
                conn.setReadTimeout(timeoutMs);
                int code = conn.getResponseCode();
                InputStream in = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                byte[] body = readAllBytes(in);
                if (code >= 200 && code < 300) {
                    return body;
                }
                last = new IOException("HTTP " + code + ": " + new String(body));
            } catch (IOException e) {
                last = e;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        throw last != null ? last : new IOException("fetch failed");
    }

    private static byte[] readAllBytes(InputStream in) throws IOException {
        if (in == null) {
            return new byte[0];
        }
        try (InputStream is = in; ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = is.read(buf)) > 0) {
                baos.write(buf, 0, n);
            }
            return baos.toByteArray();
        }
    }

    private static byte[] sha1(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return md.digest(data);
    }

    private static byte[] hexToBytes(String hex) {
        hex = hex.replaceAll("[^0-9A-Fa-f]", "");
        if (hex.length() < 40) {
            return null;
        }
        int len = 20;
        byte[] out = new byte[len];
        for (int i = 0; i < len; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi < 0 || lo < 0) {
                return null;
            }
            out[i] = (byte) ((hi << 4) + lo);
        }
        return out;
    }
}
