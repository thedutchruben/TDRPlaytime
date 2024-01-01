package nl.thedutchruben.playtime.core.storage;

import java.util.ArrayList;
import java.util.List;

public class SqlStatements {


    public static List<String> getStatements(String tablePrefix,boolean mysql){
        List<String> strings = new ArrayList<>();
        strings.add("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "playtime` (\n" +
                "  `uuid` varchar(36),\n" +
                "  `name` varchar(16),\n" +
                "  `time` BIGINT \n" +
                ");\n");
        strings.add("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "milestones` (\n" +
                "  `name` varchar(40),\n" +
                "  `data` TEXT \n" +
                ");\n");
        strings.add( "CREATE TABLE IF NOT EXISTS `" + tablePrefix + "repeating_milestones` (\n" +
                "  `name` varchar(40),\n" +
                "  `data` TEXT \n" +
                ");\n");
        strings.add("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "playtime_milestones` (\n" +
                "  `uuid` varchar(36),\n" +
                "  `milestone` varchar(40),\n" +
                "  `claimed` BOOLEAN \n" +
                ");\n");
        if(mysql){
            strings.add("CREATE TABLE IF NOT EXISTS `" + tablePrefix + "playtime_history` (\n" +
                    "`id` INT NOT NULL AUTO_INCREMENT , \n" +
                    "`uuid` VARCHAR(40) NOT NULL , \n" +
                    "`start_time` BIGINT NOT NULL , \n" +
                    "`end_time` BIGINT NOT NULL , \n" +
                    "`date` DATE NOT NULL , \n" +
                    "PRIMARY KEY (`id`)) ENGINE = InnoDB;");
        }else{
            strings.add("CREATE TABLE IF NOT EXISTS \""+tablePrefix+"playtime_history\" (\n" +
                    "\t\"id\"\tINTEGER,\n" +
                    "\t\"uuid\"\tVARCHAR(40),\n" +
                    "\t\"start_time\"\tBIGINT,\n" +
                    "\t\"end_time\"\tBIGINT,\n" +
                    "\t\"date\"\tDATE,\n" +
                    "\tPRIMARY KEY(\"id\" AUTOINCREMENT)\n" +
                    ");");
        }

        return strings;
    }


}
