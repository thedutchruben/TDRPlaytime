package nl.thedutchruben.playtime.core.storage;

import java.util.ArrayList;
import java.util.List;

public class SqlStatements {

    public static List<String> getStatements(String tablePrefix, boolean mysql) {
        List<String> statements = new ArrayList<>();
        String prefix = "CREATE TABLE IF NOT EXISTS `" + tablePrefix;
        statements.add(prefix + "playtime` (\n  `uuid` varchar(36),\n  `name` varchar(16),\n  `time` BIGINT \n);\n");
        statements.add(prefix + "milestones` (\n  `name` varchar(40),\n  `data` TEXT \n);\n");
        statements.add(prefix + "repeating_milestones` (\n  `name` varchar(40),\n  `data` TEXT \n);\n");
        statements.add(prefix + "playtime_milestones` (\n  `uuid` varchar(36),\n  `milestone` varchar(40),\n  `claimed` BOOLEAN \n);\n");

        String playtimeHistory = mysql
                ? prefix + "playtime_history` (\n  `id` INT NOT NULL AUTO_INCREMENT , \n  `uuid` VARCHAR(40) NOT NULL , \n  `start_time` BIGINT NOT NULL , \n  `end_time` BIGINT NOT NULL , \n  `date` DATE NOT NULL , \n  PRIMARY KEY (`id`)) ENGINE = InnoDB;"
                : "CREATE TABLE IF NOT EXISTS \"" + tablePrefix + "playtime_history\" (\n  `id` INTEGER,\n  `uuid` VARCHAR(40),\n  `start_time` BIGINT,\n  `end_time` BIGINT,\n  `date` DATE,\n  PRIMARY KEY(`id` AUTOINCREMENT)\n);";
        statements.add(playtimeHistory);

        return statements;
    }
}