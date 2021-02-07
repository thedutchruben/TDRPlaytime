package nl.thedutchruben.playtime.database;

import lombok.SneakyThrows;
import nl.thedutchruben.playtime.Playtime;
import nl.thedutchruben.playtime.utils.FileManager;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MysqlDatabase extends Storage{
    private Connection connection;
    private FileManager.Config config = Playtime.getInstance().getFileManager().getConfig("database.yml");

    public void setup(){
        try {
            connection = DriverManager.getConnection(
                    "jdbc:mysql://"+config.get().getString("mysql.hostname")+":"+config.get().getInt("mysql.port")+"/" + config.get().getString("mysql.database")
                    ,config.get().getString("mysql.user"),config.get().getString("mysql.password"));

            String ex = "CREATE TABLE IF NOT EXISTS `playtime` (\n" +
                    "  `uuid` varchar(40),\n" +
                    "  `name` varchar(20),\n" +
                    "  `time` BIGINT \n" +
                    ");\n";

            try(PreparedStatement preparedStatement = connection.prepareStatement(ex)) {
                preparedStatement.execute();
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }

    @Override
    public void stop() {
        try {
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @SneakyThrows
    @Override
    public long getPlayTimeByUUID(String uuid) {
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT `time` FROM `playtime` WHERE `uuid` = ?")) {
            preparedStatement.setString(1,uuid);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return resultSet.getLong("time");
                }
            }
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `playtime`(`uuid`, `name`, `time`) VALUES (?,?,?)")) {
            preparedStatement.setString(1,uuid);
            preparedStatement.setString(2, Bukkit.getPlayer(UUID.fromString(uuid)).getName());
            preparedStatement.setLong(3, 0L);
            preparedStatement.execute();
        }
        return 0;
    }

    @SneakyThrows
    @Override
    public long getPlayTimeByName(String name) {
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT `time` FROM `playtime` WHERE `name` = ?")) {
            preparedStatement.setString(1,name);
            try (ResultSet resultSet = preparedStatement.executeQuery()){
                if(resultSet.next()){
                    return resultSet.getLong("time");
                }
            }
        }
        try(PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO `playtime`(`uuid`, `name`, `time`) VALUES (?,?,?)")) {
            preparedStatement.setString(1,Bukkit.getOfflinePlayer(name).getUniqueId().toString());
            preparedStatement.setString(2, name);
            preparedStatement.setLong(3, 0L);
            preparedStatement.execute();
        }
        return 0;
    }

    @SneakyThrows
    @Override
    public void savePlayTime(String uuid, long playtime) {
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `playtime` SET `uuid`=?,`time`=? WHERE `uuid` = ?")) {
            preparedStatement.setString(1,uuid);
            preparedStatement.setLong(2, playtime);
            preparedStatement.setString(3,uuid);
            preparedStatement.execute();
        }
    }

    @SneakyThrows
    @Override
    public Map<String, Long> getTopTenList() {
        Map<String,Long> topList = new HashMap<>();
        try(PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM `playtime`ORDER BY `time` DESC  LIMIT 10 ")) {

           ResultSet resultSet = preparedStatement.executeQuery();
           while (resultSet.next()){
                topList.put(resultSet.getString("name"),resultSet.getLong("time"));
           }
        }
        return topList;
    }

    @SneakyThrows
    @Override
    public void reset(String uuid) {
        try(PreparedStatement preparedStatement = connection.prepareStatement("UPDATE `playtime` SET `time`=? WHERE `name` = ?")) {
            preparedStatement.setLong(1, 0);
            preparedStatement.setString(2,uuid);
            preparedStatement.execute();
        }
    }


}
