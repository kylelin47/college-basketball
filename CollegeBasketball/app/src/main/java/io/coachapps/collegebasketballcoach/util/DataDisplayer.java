package io.coachapps.collegebasketballcoach.util;

public class DataDisplayer {
    public static String getHeight(int heightInInches) {
        return heightInInches/12 + "'" + heightInInches % 12 + "\"";
    }
    public static String getYear(int year) {
        switch (year) {
            case 0: return "Recruit";
            case 1: return "Freshman";
            case 2: return "Sophomore";
            case 3: return "Junior";
            case 4: return "Senior";
            case 5: return "Graduate";
            default: return "Unknown";
        }
    }
    public static String getYearAbbreviation(int year) {
        switch (year) {
            case 0: return "Re";
            case 1: return "Fr";
            case 2: return "Soph";
            case 3: return "Jr";
            case 4: return "Sr";
            case 5: return "Gr";
            default: return "Unknown";
        }
    }
    public static String getWeight(int weightInPounds) {
        return weightInPounds + " lbs.";
    }
    public static String getPosition(int position) {
        switch (position) {
            case 1: return "Point Guard";
            case 2: return "Shooting Guard";
            case 3: return "Small Forward";
            case 4: return "Power Forward";
            case 5: return "Center";
            default: return "Unknown";
        }
    }
    public static String getPositionAbbreviation(int position) {
        switch (position) {
            case 1: return "PG";
            case 2: return "SG";
            case 3: return "SF";
            case 4: return "PF";
            case 5: return "C";
            default: return "N/A";
        }
    }
    public static String getFieldGoalPercentage(int fgm, int fga) {
        return String.format("%.1f", (double)(100*fgm)/fga);
    }
}
