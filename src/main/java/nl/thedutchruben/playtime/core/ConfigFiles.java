package nl.thedutchruben.playtime.core;

import lombok.Getter;

@Getter
public enum ConfigFiles {
    CONFIG("config.yml"),
    STORAGE("storage.yml");

    private final String fileName;

    ConfigFiles(String fileName) {
        this.fileName = fileName;
    }

}