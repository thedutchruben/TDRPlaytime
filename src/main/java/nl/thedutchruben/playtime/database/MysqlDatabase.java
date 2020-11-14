package nl.thedutchruben.playtime.database;

public class MysqlDatabase extends Storage{

    public void setup(){
        String ex = "CREATE TABLE IF NOT EXIST `playtime` (\n" +
                "  `uuid` varchar(32),\n" +
                "  `name` varchar(16),\n" +
                "  `time` long\n" +
                ");\n";
    }

    @Override
    public void stop() {

    }

    @Override
    public Long getPlayTimeByUUID(String uuid) {
        return null;
    }

    @Override
    public Long getPlayTimeByName(String name) {
        return null;
    }

    @Override
    public void savePlayTime(String uuid) {

    }
}
