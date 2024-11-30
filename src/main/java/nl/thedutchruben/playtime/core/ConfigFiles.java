package nl.thedutchruben.playtime.core;

public enum ConfigFiles {
    CONFIG("config.yml"),
    STORAGE("storage.yml");

    private final String fileName;

    ConfigFiles(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }
}
