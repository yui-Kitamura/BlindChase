package pro.eng.yui.mcpl.blindChase.lib.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Minimal GitHub Releases client using public API with no auth.
 */
public final class GitHubReleaseClient {

    public static final class ResolvedAsset {
        private final String tag;
        public String getTag() {
            return tag;
        }
        private final String assetName;
        public String getAssetName() {
            return assetName;
        }
        private final String downloadUrl;
        public String getDownloadUrl() {
            return downloadUrl;
        }
        private final String sha1Url; // may be null
        public String getSha1Url() {
            return sha1Url;
        }

        ResolvedAsset(String tag, String assetName, String downloadUrl, String sha1Url) {
            this.tag = tag;
            this.assetName = assetName;
            this.downloadUrl = downloadUrl;
            this.sha1Url = sha1Url;
        }
    }

    private final String user;
    private final String repo;
    private final Pattern assetNamePattern;
    private final int timeoutMs;
    private final int retries;

    public GitHubReleaseClient(String user, String repo, Pattern assetNamePattern, int timeoutMs, int retries) {
        this.user = Objects.requireNonNull(user);
        this.repo = Objects.requireNonNull(repo);
        this.assetNamePattern = Objects.requireNonNull(assetNamePattern);
        this.timeoutMs = Math.max(1000, timeoutMs);
        this.retries = Math.max(0, retries);
    }

    public ResolvedAsset resolveByMatchVersionThenLatest(String pluginVersion) throws IOException {
        IOException last = null;
        // try exact tag
        if (pluginVersion != null && !pluginVersion.isBlank()) {
            try {
                // tag は v1.2.3 に対応して 1.2.3 を決め打ち
                return resolveFromReleaseJson(fetch("https://api.github.com/repos/" + user + "/" + repo + "/releases/tags/" + pluginVersion));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // fallback latest
        return resolveFromReleaseJson(fetch("https://api.github.com/repos/" + user + "/" + repo + "/releases/latest"));
    }

    private String fetch(String urlStr) throws IOException {
        IOException last = null;
        for (int i = 0; i <= retries; i++) {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(urlStr);
                conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(true);
                conn.setConnectTimeout(timeoutMs);
                conn.setReadTimeout(timeoutMs);
                conn.setRequestProperty("Accept", "application/vnd.github+json");
                int code = conn.getResponseCode();
                InputStream in = (200 <= code && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String body = readAll(in);
                if (200 <= code && code < 300) {
                    return body;
                }
                last = new IOException("GitHub API HTTP " + code + ": " + body);
            } catch (IOException e) {
                last = e;
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
        throw last != null ? last : new IOException("GitHub fetch failed");
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null){ return ""; }
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }

    private ResolvedAsset resolveFromReleaseJson(String json) throws IOException {
        JsonElement rootEl = JsonParser.parseString(json);
        if (!rootEl.isJsonObject()) {
            throw new IOException("Invalid GitHub release JSON: not an object");
        }
        JsonObject obj = rootEl.getAsJsonObject();
        String tag = obj.has("tag_name") && !obj.get("tag_name").isJsonNull()
                ? obj.get("tag_name").getAsString()
                : "";

        if (!obj.has("assets") || !obj.get("assets").isJsonArray()) {
            throw new IOException("GitHub release JSON does not contain assets array");
        }
        JsonArray assets = obj.getAsJsonArray("assets");

        String foundName = null;
        String foundUrl = null;
        String sha1Url = null;
        for (JsonElement asset : assets) {
            if (!asset.isJsonObject()) { continue; }
            JsonObject assetObj = asset.getAsJsonObject();
            final String name = (assetObj.has("name") && assetObj.get("name").isJsonNull() == false) ? assetObj.get("name").getAsString() : null;
            final String url = (assetObj.has("browser_download_url") && assetObj.get("browser_download_url").isJsonNull() == false) ? assetObj.get("browser_download_url").getAsString() : null;
            if (name == null || url == null) { continue; }
            if (assetNamePattern.matcher(name).matches()) {
                foundName = name;
                foundUrl = url;
            }
            if (name.endsWith(".sha1")) {
                sha1Url = url;
            }
        }
        if (foundName == null || foundUrl == null) {
            throw new IOException("No asset matched pattern in release: " + assetNamePattern);
        }
        return new ResolvedAsset(tag, foundName, foundUrl, sha1Url);
    }
}
