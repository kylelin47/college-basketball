package io.coachapps.collegebasketballcoach.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * Settings class, keeps track of application settings
 * Created by jojones on 12/30/16.
 */

public class Settings {

    public static final String SETTINGS_FILE_NAME = "settings";
    public static final String RECORDS_FILE_NAME = "records";
    public static final String TEAM_RECORDS_FILE_NAME = "team_records";

    // Difficulty, 0 easy, 1 normal, 2 hard
    private int difficulty;
    public boolean enableToasts;

    public Settings(File settingsFile) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(settingsFile));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplit = line.split(" ");
                System.out.println(line);
                if (lineSplit.length == 2) {
                    if (lineSplit[0].equals("Difficulty")) {
                        difficulty = Integer.parseInt(lineSplit[1]);
                    } else if (lineSplit[0].equals("Toasts")) {
                        enableToasts = lineSplit[1].equals("1");
                    }
                }
            }
        } catch (Exception e) {
            // Whoops
            difficulty = 1;
            enableToasts = true;
        }
    }

    public void saveSettings(File settingsFile) {
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(settingsFile), "utf-8"))) {
            writer.write("Difficulty " + difficulty + "\n");
            writer.write("Toasts " + (enableToasts ? 1 : 0));
        } catch (Exception e) {
            System.out.println(e.toString());
        }
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean areToastsEnabled() {
        return enableToasts;
    }

    public void setEnableToasts(boolean enableToasts) {
        this.enableToasts = enableToasts;
    }
}
