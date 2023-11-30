package nl.thedutchruben.playtime.core;

import net.byteflux.libby.BukkitLibraryManager;
import net.byteflux.libby.Library;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

public class DependencyLoader {

    public static void load(Plugin plugin) {
        System.out.println("Loading dependencies");

        BukkitLibraryManager libraryManager = new BukkitLibraryManager(plugin);
        // setup repo's
        libraryManager.addMavenCentral();
        libraryManager.addJitPack();
        libraryManager.addRepository("https://nexus.thedutchservers.com/repository/maven-public/");
        //build array with all the dependencies as liberies
        List<Library> libraries = new ArrayList<>();
        for (Dependency dependency : Dependency.values()) {
            Library.Builder builder = Library.builder()
                    .groupId(dependency.groupId) // "{}" is replaced with ".", useful to avoid unwanted changes made by maven-shade-plugin
                    .artifactId(dependency.artifactID)
                    .version(dependency.version);

            if (dependency.relocation != null) {
                builder.relocate(dependency.groupId, dependency.relocation);
            }
            libraries.add(builder.build());
        }
        // Load all the dependencies
        for (Library library : libraries) {
            libraryManager.loadLibrary(library);
        }

    }


    private enum Dependency {
        BSON("org{}mongodb", "bson", "4.11.1", null),
        MONGODB("org{}mongodb", "mongodb-driver-sync", "4.11.1", null),
        MONGODB_CORE("org{}mongodb", "mongodb-driver-core", "4.11.1", null),
        MCCORE("nl{}thedutchruben", "mccore", "1.4.1", null),
        BSTATS("org{}bstats", "bstats-bukkit", "3.0.2", "nl{}thedutchruben{}playtime{}bstats"),
        BSTATS_BASE("org{}bstats", "bstats-base", "3.0.2", "nl{}thedutchruben{}playtime{}bstats");

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