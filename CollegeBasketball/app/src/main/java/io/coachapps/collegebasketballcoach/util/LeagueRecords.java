package io.coachapps.collegebasketballcoach.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Class for storing and interacting with league records.
 * Created by jojones on 1/12/17.
 */

public class LeagueRecords {

    public static final String TEAM_SEASON_PPG = "Team Points Per Game";
    public static final String TEAM_SEASON_OPPG = "Team Opp Points Per Game";
    public static final String TEAM_SEASON_FGP = "Team Field Goal Percentage";
    public static final String TEAM_SEASON_OFGP = "Team Opp Field Goal Percentage";
    public static final String TEAM_SEASON_APG = "Team Assists Per Game";
    public static final String TEAM_SEASON_RPG = "Team Rebounds Per Game";
    public static final String TEAM_SEASON_SPG = "Team Steals Per Game";
    public static final String TEAM_SEASON_BPG = "Team Blocks Per Game";

    public static final String PLAYER_SEASON_POINTS = "Season Total Points";
    public static final String PLAYER_SEASON_ASSISTS = "Season Total Assists";
    public static final String PLAYER_SEASON_REBOUNDS = "Season Total Rebounds";
    public static final String PLAYER_SEASON_STEALS = "Season Total Steals";
    public static final String PLAYER_SEASON_BLOCKS = "Season Total Blocks";
    public static final String PLAYER_SEASON_FGM = "Season Total Field Goals";
    public static final String PLAYER_SEASON_3GM = "Season Total 3 Pointers";
    public static final String PLAYER_SEASON_FGP = "Season Field Goal Percentage";
    public static final String PLAYER_SEASON_3FGP = "Season 3 Point Percentage";

    public static final String PLAYER_CAREER_POINTS = "Career Total Points";
    public static final String PLAYER_CAREER_ASSISTS = "Career Total Assists";
    public static final String PLAYER_CAREER_REBOUNDS = "Career Total Rebounds";
    public static final String PLAYER_CAREER_STEALS = "Career Total Steals";
    public static final String PLAYER_CAREER_BLOCKS = "Career Total Blocks";
    public static final String PLAYER_CAREER_FGM = "Career Total Field Goals";
    public static final String PLAYER_CAREER_3GM = "Career Total 3 Pointers";
    public static final String PLAYER_CAREER_FGP = "Career Field Goal Percentage";
    public static final String PLAYER_CAREER_3FGP = "Career 3 Point Percentage";

    public static final String[] ALL_RECORDS = {
            TEAM_SEASON_PPG,
            TEAM_SEASON_OPPG,
            TEAM_SEASON_FGP,
            TEAM_SEASON_OFGP,
            TEAM_SEASON_APG,
            TEAM_SEASON_RPG,
            TEAM_SEASON_SPG,
            TEAM_SEASON_BPG,
            PLAYER_SEASON_POINTS,
            PLAYER_SEASON_ASSISTS,
            PLAYER_SEASON_REBOUNDS,
            PLAYER_SEASON_STEALS,
            PLAYER_SEASON_BLOCKS,
            PLAYER_SEASON_FGM,
            PLAYER_SEASON_3GM,
            PLAYER_SEASON_FGP,
            PLAYER_SEASON_3FGP,
            PLAYER_CAREER_POINTS,
            PLAYER_CAREER_ASSISTS,
            PLAYER_CAREER_REBOUNDS,
            PLAYER_CAREER_STEALS,
            PLAYER_CAREER_BLOCKS,
            PLAYER_CAREER_FGM,
            PLAYER_CAREER_3GM,
            PLAYER_CAREER_FGP,
            PLAYER_CAREER_3FGP
    };

    public static final String[] ALL_SEASON_ECORDS = {
            TEAM_SEASON_PPG,
            TEAM_SEASON_OPPG,
            TEAM_SEASON_FGP,
            TEAM_SEASON_OFGP,
            TEAM_SEASON_APG,
            TEAM_SEASON_RPG,
            TEAM_SEASON_SPG,
            TEAM_SEASON_BPG,
            PLAYER_SEASON_POINTS,
            PLAYER_SEASON_ASSISTS,
            PLAYER_SEASON_REBOUNDS,
            PLAYER_SEASON_STEALS,
            PLAYER_SEASON_BLOCKS,
            PLAYER_SEASON_FGM,
            PLAYER_SEASON_3GM,
            PLAYER_SEASON_FGP,
            PLAYER_SEASON_3FGP
    };


    public class Record {
        String description; // Should be one of the above
        String holder; // Team Name or Player ID
        int year;
        double number;

        public Record(String[] line) {
            // type, description, holder, year, number
            if (line.length == 4) {
                description = line[0];
                holder = line[1];
                year = Integer.parseInt(line[2]);
                number = Double.parseDouble(line[3]);
            }
        }

        public Record(String description, String holder, int year, double number) {
            this.description = description;
            this.holder = holder;
            this.year = year;
            this.number = number;
        }

        public void update(String holder, int year, double number) {
            this.holder = holder;
            this.year = year;
            this.number = number;
        }

        public String toFileLine() {
            return description + ">" + holder + ">" + year + ">" + number;
        }

        public boolean isPlayerRecord() {
            return !description.contains("Team");
        }

        public boolean isTeamRecord() {
            return description.contains("Team");
        }

        public String getDescription() {
            return description;
        }

        public String getHolder() {
            return holder;
        }

        public int getYear() {
            return year;
        }

        public double getNumber() {
            return number;
        }
    }

    private HashMap<String, Record> recordMap;

    public LeagueRecords(File recordsFile) {
        recordMap = new HashMap<>();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(recordsFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplit = line.split(">");
                System.out.println(line);
                if (lineSplit.length == 4) {
                    recordMap.put(lineSplit[0], new Record(lineSplit));
                }
            }
        } catch (Exception e) {
            // Whoops
        }
    }

    public void saveRecords(File recordsFile) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(recordsFile), "utf-8"))) {
            for (String record : ALL_RECORDS) {
                Record r = getRecord(record);
                if (r != null) writer.write(r.toFileLine() + "\n");
            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public List<Record> getAllRecords() {
        List<Record> records = new ArrayList<>();
        for (String s : ALL_RECORDS) {
            if (recordMap.containsKey(s)) records.add(recordMap.get(s));
        }
        return records;
    }

    public Record getRecord(String record) {
        return recordMap.get(record);
    }

    public boolean checkRecord(String description, String holder, int year, double number) {
        if (recordMap.containsKey(description)) {
            if (description.contains("Opp")) {
                if (recordMap.get(description).number > number) {
                    // New Record!
                    Log.i("LeagueRecords", "Broken " + description + ", holder = " + holder);
                    recordMap.get(description).update(holder, year, number);
                    return true;
                }
            } else {
                if (recordMap.get(description).number < number) {
                    // New Record!
                    Log.i("LeagueRecords", "Broken " + description + ", holder = " + holder);
                    recordMap.get(description).update(holder, year, number);
                    return true;
                }
            }
            return false;
        } else {
            Log.i("LeagueRecords", "Adding new record for " + description + ", holder = " + holder);
            recordMap.put(description, new Record(description, holder, year, number));
            return true;
        }
    }

}
