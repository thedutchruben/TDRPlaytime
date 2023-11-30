package nl.thedutchruben.playtime.core.translations;

public enum Messages {



    private String path;

    Messages(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
