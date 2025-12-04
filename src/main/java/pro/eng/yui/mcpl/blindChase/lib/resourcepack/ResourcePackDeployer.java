package pro.eng.yui.mcpl.blindChase.lib.resourcepack;

import org.bukkit.plugin.java.JavaPlugin;
import pro.eng.yui.mcpl.blindChase.BlindChase;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ResourcePackDeployer {

    private static final String SOURCE_DIR = "resourcepack/";
    private static final String DEFAULT_ZIP_NAME = "BlindChase_ResourcePack.zip";

    private ResourcePackDeployer(){
        // no instance
    }

    public static void deploy() {
        JavaPlugin plugin = BlindChase.plugin();
        
        File outDir = resolveResourcePacksDir(plugin);
        if (!outDir.exists() && !outDir.mkdirs()) {
            plugin.getLogger().warning("Failed to create resourcepacks directory: " + outDir.getAbsolutePath());
            return;
        }

        File outZip = new File(outDir, DEFAULT_ZIP_NAME);
        try {
            writeZipFromOwnJar(plugin, outZip);
            plugin.getLogger().info("Deployed resource pack: " + outZip.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to deploy resource pack: " + e.getMessage());
        }
    }

    private static File resolveResourcePacksDir(JavaPlugin plugin) {
        // Try to place next to server root where resource packs folder usually exists
        File serverRoot = plugin.getServer().getWorldContainer();
        return new File(serverRoot, "resourcepacks");
    }

    private static void writeZipFromOwnJar(JavaPlugin plugin, File outZip) throws IOException, URISyntaxException {
        CodeSource cs = plugin.getClass().getProtectionDomain().getCodeSource();
        if (cs == null) {
            throw new IOException("No CodeSource available for plugin class");
        }
        URL url = cs.getLocation();
        File jarFile = new File(url.toURI());

        if (!jarFile.isFile()) {
            throw new IOException("Plugin location is not a file: " + jarFile.getAbsolutePath());
        }

        try (JarFile jar = new JarFile(jarFile);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(outZip)))
        ) {
            Enumeration<JarEntry> entries = jar.entries();
            int count = 0;
            while (entries.hasMoreElements()) {
                JarEntry je = entries.nextElement();
                String name = je.getName();
                if (!name.startsWith(SOURCE_DIR)) {
                    continue;
                }
                String rel = name.substring(SOURCE_DIR.length());
                if (rel.isEmpty()) { // the folder itself
                    continue;
                }
                // Normalize directory entries
                if (je.isDirectory()) {
                    ZipEntry dirEntry = new ZipEntry(rel.endsWith("/") ? rel : rel + "/");
                    zos.putNextEntry(dirEntry);
                    zos.closeEntry();
                    continue;
                }

                ZipEntry ze = new ZipEntry(rel);
                zos.putNextEntry(ze);
                try (InputStream in = jar.getInputStream(je)) {
                    in.transferTo(zos);
                }
                zos.closeEntry();
                count++;
            }

            if (count == 0) {
                throw new IOException("No entries found under '" + SOURCE_DIR + "' in plugin JAR.");
            }
        }
    }
}
