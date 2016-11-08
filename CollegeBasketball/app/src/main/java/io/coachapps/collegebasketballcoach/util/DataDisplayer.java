package io.coachapps.collegebasketballcoach.util;

import android.content.Context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;

public class DataDisplayer {
    private static final String[] grades = {"F", "F+", "D", "D+", "C", "C+", "B", "B+", "A", "A+"};
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
            case 2: return "So";
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
    public static String getLetterGrade(int num) {
        int ind = (num-50)/5;
        if (ind > 9) ind = 9;
        if (ind < 0) ind = 0;
        return grades[ind];
    }
    public static String getRankStr(int rank) {
        switch (rank) {
            case 1: return "1st";
            case 2: return "2nd";
            case 3: return "4th";
            default: return rank + "th";
        }
    }
    public static ArrayList<String> getTeamStatsCSVs(String teamName, Context context, int year) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(year);
        YearlyTeamStats statsOfSelectedTeam = null;
        for (YearlyTeamStats stats : currentTeamStats) {
            if (stats.team.equals(teamName)) {
                statsOfSelectedTeam = stats;
                break;
            }
        }
        ArrayList<String> teamStatsCSVs = new ArrayList<>();
        teamStatsCSVs.add(",,Rank");
        if (statsOfSelectedTeam == null)  {
            teamStatsCSVs.add("0 - 0,Wins - Losses,N/A");
            teamStatsCSVs.add("0.0,Points Per Game,N/A");
            teamStatsCSVs.add("0.0,Assists Per Game,N/A");
            teamStatsCSVs.add("0.0,Rebounds Per Game,N/A");
            teamStatsCSVs.add("0.0,Steals Per Game,N/A");
            teamStatsCSVs.add("0.0,Blocks Per Game,N/A");
            teamStatsCSVs.add("0.0,Turnovers Per Game,N/A");
            teamStatsCSVs.add("0.0,FGM Per Game,N/A");
            teamStatsCSVs.add("0.0,FGA Per Game,N/A");
            teamStatsCSVs.add("0.0,3FGM Per Game,N/A");
            teamStatsCSVs.add("0.0,3FGA Per Game,N/A");
            return teamStatsCSVs;
        }
        int highestIndex = currentTeamStats.indexOf(statsOfSelectedTeam);

        while (highestIndex >= 0 && currentTeamStats.get(highestIndex).wins == statsOfSelectedTeam.wins) {
            highestIndex--;
        }
        teamStatsCSVs.add(statsOfSelectedTeam.wins + " - " + statsOfSelectedTeam.losses + ",Wins " +
                "- Losses," + getRankStr(highestIndex + 2));

        // This is disgusting

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.points < left.points ? -1 : left.points == right.points ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("PPG") + ",Points Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.assists < left.assists ? -1 : left.assists == right.assists ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("APG") + ",Assists Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.rebounds < left.rebounds ? -1 : left.rebounds == right.rebounds ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("RPG") + ",Rebounds Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.steals < left.steals ? -1 : left.steals == right.steals ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("SPG") + ",Steals Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.blocks < left.blocks ? -1 : left.blocks == right.blocks ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("BPG") + ",Blocks Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.turnovers < left.turnovers ? 1 : left.turnovers == right.turnovers ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("TPG") + ",Turnovers Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fgm < left.fgm ? -1 : left.fgm == right.fgm ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("FGMPG") + ",FGM Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fga < left.fga ? -1 : left.fga == right.fga ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("FGAPG") + ",FGA Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.getFGP() < left.getFGP() ? -1 : left.getFGP() == right.getFGP() ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getFGPStr() + "%,Field Goal Percentage," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePM < left.threePM ? -1 : left.threePM == right.threePM ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("3FGMPG") + ",3FGM Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePA < left.threePA ? -1 : left.threePA == right.threePA ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("3FGAPG") + ",3FGA Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.get3FGP() < left.get3FGP() ? -1 : left.get3FGP() == right.get3FGP() ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.get3FGPStr() + "%,3 Point Percentage," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        return teamStatsCSVs;
    }
}
