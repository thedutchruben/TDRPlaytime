package nl.thedutchruben.playtime.core;

import lombok.Getter;
import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import net.byteflux.libby.logging.LogLevel;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class DependencyLoader {

    public static void load(Plugin plugin) {
        plugin.getLogger().info("Loading dependencies");

        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);
        // setup repo's
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addRepository("https://maven.thedutchservers.com/releases");
        libraryManager.setLogLevel(LogLevel.INFO);

        //build array with all the dependencies as liberies
        List<Library> libraries = new ArrayList<>();
        for (Dependency dependency : Dependency.values()) {
            Library.Builder builder = Library.builder()
                    .groupId(dependency.getGroupId()) // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                    .artifactId(dependency.getArtifactID())
                    .version(dependency.getVersion());

            if (dependency.getRelocation() != null) {
                builder.relocate(dependency.getGroupId(), dependency.getRelocation());
            }
            libraries.add(builder.build());
        }
        // Load all the dependencies
        for (Library library : libraries) {
            libraryManager.loadLibrary(library);
        }
    }

    @Getter
    private enum Dependency {
        BSON("org{}mongodb", "bson", "5.2.1", null),
        MONGODB("org{}mongodb", "mongodb-driver-sync", "5.2.1", null),
        MONGODB_CORE("org{}mongodb", "mongodb-driver-core", "5.2.1", null),
        MCCORE("nl{}thedutchruben", "mccore", "1.4.9", null),
        BSTATS("org{}bstats", "bstats-bukkit", "3.0.2", "nl{}thedutchruben{}playtime{}bstats"),
        BSTATS_BASE("org{}bstats", "bstats-base", "3.0.2", "nl{}thedutchruben{}playtime{}bstats"),
        HIKARI_CP("com{}zaxxer", "HikariCP", "6.0.0", null);

        public final String version;
        public final String relocation;
        private final String groupId;
        private final String artifactID;

        Dependency(String groupId, String artifactID, String version, String relocation) {
            this.groupId = groupId;
            this.artifactID = artifactID;
            this.version = version;
            this.relocation = relocation;
        }
    }
}