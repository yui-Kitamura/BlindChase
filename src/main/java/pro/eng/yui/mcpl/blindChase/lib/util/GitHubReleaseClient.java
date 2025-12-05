package pro.eng.yui.mcpl.blindChase.lib.resourcepack;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Minimal GitHub Releases client using public API with no auth.
 */
public final class GitHubReleaseClient {

    static final class ResolvedAsset {
        final String tag;
        final String assetName;
        final String downloadUrl;
        final String sha1Url; // may be null

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
            String[] tags = new String[]{pluginVersion, "v" + pluginVersion};
            for (String t : tags) {
                try {
                    return resolveFromReleaseJson(fetch("https://api.github.com/repos/" + user + "/" + repo + "/releases/tags/" + t));
                } catch (IOException e) {
                    last = e;
                }
            }
        }
        // fallback latest
        try {
            return resolveFromReleaseJson(fetch("https://api.github.com/repos/" + user + "/" + repo + "/releases/latest"));
        } catch (IOException e) {
            last = e;
        }
        throw last != null ? last : new IOException("Failed to resolve release");
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
        // extract tag_name
        String tag = matchFirst(json, "\\\"tag_name\\\"\\s*:\\s*\\\"(.*?)\\\"");
        if (tag == null){ tag = ""; }
        // iterate assets by simplistic regex for name and browser_download_url
        Pattern assetBlock = Pattern.compile("\\{[^{}]*\\\"name\\\"\\s*:\\s*\\\"(.*?)\\\"[^{}]*\\\"browser_download_url\\\"\\s*:\\s*\\\"(.*?)\\\"[^{}]*\\}", Pattern.DOTALL);
        Matcher m = assetBlock.matcher(json);
        String foundName = null;
        String foundUrl = null;
        String sha1Url = null;
        while (m.find()) {
            String name = m.group(1);
            String url = m.group(2);
            if (name == null || url == null) {
                continue;
            }
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

    private static String matchFirst(String src, String pattern) {
        Matcher m = Pattern.compile(pattern, Pattern.DOTALL).matcher(src);
        if (m.find()){ return m.group(1); }
        return null;
    }
}
