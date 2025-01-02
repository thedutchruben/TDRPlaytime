package nl.thedutchruben.playtime.core.storage;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains the SQL statements that are used to create the tables in the database.
 * The statements are formatted based on the table prefix and the database type.
 */
public class SqlStatements {

    /**
     * Get the SQL statements for creating the tables in the database.
     *
     * @param tablePrefix The prefix for the table names.
     * @param mysql       Whether the database is MySQL or SQLite.
     * @return The SQL statements for creating the tables in the database.
     */
    public static List<String> getStatements(String tablePrefix, boolean mysql) {
        List<String> statements = new ArrayList<>();
        String prefix = "CREATE TABLE IF NOT EXISTS `" + tablePrefix;
        statements.add(String.format("%splaytime` (\n  `uuid` varchar(36),\n  `name` varchar(16),\n  `time` BIGINT \n);\n", prefix));
        statements.add(String.format("%smilestones` (\n  `name` varchar(40),\n  `data` TEXT \n);\n", prefix));
        statements.add(String.format("%srepeating_milestones` (\n  `name` varchar(40),\n  `data` TEXT \n);\n", prefix));
        statements.add(String.format("%splaytime_milestones` (\n  `uuid` varchar(36),\n  `milestone` varchar(40),\n  `claimed` BOOLEAN \n);\n", prefix));

        String playtimeHistory = mysql
                ? String.format("%splaytime_history` (\n  `id` INT NOT NULL AUTO_INCREMENT , \n  `uuid` VARCHAR(40) NOT NULL \n,  `event` VARCHAR(40) NOT NULL , \n  `start_time` BIGINT NOT NULL , \n  `date` DATETIME NOT NULL , \n  PRIMARY KEY (`id`)) ENGINE = InnoDB;", prefix)
                : String.format("CREATE TABLE IF NOT EXISTS `%splaytime_history` (\n" +
                "  `id` INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                "  `uuid` VARCHAR(40),\n" +
                "  `time` BIGINT,\n" +
                "  `event` VARCHAR(40),\n" +
                "  `date` DATETIME\n" +
                ");", tablePrefix);
        statements.add(playtimeHistory);

        return statements;
    }
}