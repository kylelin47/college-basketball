package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.LeagueResults;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
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
            case 6: return "Pro";
            case 7: return "HoF";
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
            case 6: return "Pro";
            case 7: return "HoF";
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
    public static String getRankStr(int num) {
        if (num == 11) {
            return "11th";
        } else if (num == 12) {
            return "12th";
        } else if (num == 13) {
            return "13th";
        } else if (num%10 == 1) {
            return num + "st";
        } else if (num%10 == 2) {
            return num + "nd";
        } else if (num%10 == 3){
            return num + "rd";
        } else {
            return num + "th";
        }
    }
    public static ArrayList<String> getTeamStatsCSVs(String teamName, League league, Context context, int year) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(year);
        YearlyTeamStats statsOfSelectedTeam = null;
        for (YearlyTeamStats stats : currentTeamStats) {
            if (stats.team.equals(teamName)) {
                statsOfSelectedTeam = stats;
                break;
            }
        }
        Team team = league.getTeam(teamName);
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

        if (team != null) {
            teamStatsCSVs.add(team.pollScore + ",Poll Votes," + getRankStr(team.pollRank));
            teamStatsCSVs.add(team.prestige + ",Prestige," + getRankStr(league.getPrestigeRank(teamName)));
        }


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
                return right.opp_points < left.opp_points ? 1 : left.opp_points == right.opp_points ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("OPPG") + ",Opp Points Per Game," +
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

        /*
        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fgm < left.fgm ? 1 : left.fgm == right.fgm ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("OFGMPG") + ",Opp FGM Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fga < left.fga ? 1 : left.fga == right.fga ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("OFGAPG") + ",Opp FGA Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));
        */

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.getOFGP() < left.getOFGP() ? 1 : left.getOFGP() == right.getOFGP() ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getOFGPStr() + "%,Opp Field Goal Percentage," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        /*
        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePM < left.threePM ? 1 : left.threePM == right.threePM ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("O3FGMPG") + ",Opp 3FGM Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePA < left.threePA ? 1 : left.threePA == right.threePA ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("O3FGAPG") + ",Opp 3FGA Per Game," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));
        */

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.getO3FGP() < left.getO3FGP() ? 1 : left.getO3FGP() == right.getO3FGP() ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getO3FGPStr() + "%,Opp 3 Point Percentage," +
                getRankStr(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        return teamStatsCSVs;
    }

    public static ArrayList<String> getTeamRankingsCSVs(League league, Context context, int year,
                                    final String category, boolean higherIsBetter) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(year);

        HashMap<String, Team> nameMap = new HashMap<>();
        List<Team> sortedTeamList = new ArrayList<>();
        for (Team t : league.getAllTeams()) {
            nameMap.put(t.getName(), t);
            sortedTeamList.add(t);
        }

        if (category.equals("Poll Votes")) {
            Collections.sort(sortedTeamList, new Comparator<Team>() {
                @Override
                public int compare(Team a, Team b) {
                    return b.pollScore - a.pollScore;
                }
            });
            ArrayList<String> teamRankingsCSV = new ArrayList<>();
            for (int i = 0; i < sortedTeamList.size(); ++i) {
                teamRankingsCSV.add(getRankStr(i+1) + "," +
                        sortedTeamList.get(i).getNameWLStr() + "," +
                        (sortedTeamList.get(i).pollScore/10));
            }

            return teamRankingsCSV;

        } else {

            if (higherIsBetter) {
                Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
                    @Override
                    public int compare(YearlyTeamStats a, YearlyTeamStats b) {
                        return b.getPG(category) < a.getPG(category) ?
                                -1 : a.getPG(category) == b.getPG(category) ? 0 : 1;
                    }
                });
            } else {
                Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
                    @Override
                    public int compare(YearlyTeamStats a, YearlyTeamStats b) {
                        return b.getPG(category) < a.getPG(category) ?
                                1 : a.getPG(category) == b.getPG(category) ? 0 : -1;
                    }
                });
            }

            ArrayList<String> teamRankingsCSV = new ArrayList<>();
            for (int i = 0; i < currentTeamStats.size(); ++i) {
                teamRankingsCSV.add(getRankStr(i + 1) + "," +
                        nameMap.get(currentTeamStats.get(i).team).getRankNameWLStr() + "," +
                        DataDisplayer.round(currentTeamStats.get(i).getPG(category), 2));
            }

            return teamRankingsCSV;
        }
    }

    public static ArrayList<String> getGamePreviewComparison(Context context, int year, Game gm) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(year);

        YearlyTeamStats homeStats = null;
        YearlyTeamStats awayStats = null;
        for (YearlyTeamStats teamStats : currentTeamStats) {
            if (teamStats.team.equals(gm.getHome().getName())) {
                homeStats = teamStats;
            } else if (teamStats.team.equals(gm.getAway().getName())) {
                awayStats = teamStats;
            }
        }

        if (homeStats != null && awayStats != null) {
            ArrayList<String> stats = new ArrayList<>();
            stats.add(gm.getAway().getName() + ", ," + gm.getHome().getName());
            stats.add(awayStats.getPGDisplay("PPG") + ",Points Per Game," + homeStats.getPGDisplay("PPG"));
            stats.add(awayStats.getPGDisplay("OPPG") + ",Opp Points Per Game," + homeStats.getPGDisplay("OPPG"));
            stats.add(awayStats.getPGDisplay("FG%") + ",Field Goal Percentage," + homeStats.getPGDisplay("FG%"));
            stats.add(awayStats.getPGDisplay("OFG%") + ",Opp Field Goal Percentage," + homeStats.getPGDisplay("OFG%"));
            stats.add(awayStats.getPGDisplay("3FG%") + ",3 Point Percentage," + homeStats.getPGDisplay("3FG%"));
            stats.add(awayStats.getPGDisplay("O3FG%") + ",Opp 3 Point Percentage," + homeStats.getPGDisplay("O3FG%"));
            stats.add(awayStats.getPGDisplay("RPG") + ",Rebounds Per Game," + homeStats.getPGDisplay("RPG"));
            stats.add(awayStats.getPGDisplay("APG") + ",Assists Per Game," + homeStats.getPGDisplay("APG"));
            return stats;
        } else {
            return new ArrayList<>();
        }
    }

    public static List<String> getCSVChampions(LeagueResults leagueResults, League league) {
        List<String> list = new ArrayList<>();
        list.add("National Champions," + league.getTeam(leagueResults.championTeamName).getRankNameWLStr());
        list.add("Cowboy Champions," + league.getTeam(leagueResults.cowboyChampTeamName).getRankNameWLStr());
        list.add("Lakes Champions," + league.getTeam(leagueResults.lakesChampTeamName).getRankNameWLStr());
        list.add("Mountains Champions," + league.getTeam(leagueResults.mountainsChampTeamName).getRankNameWLStr());
        list.add("North Champions," + league.getTeam(leagueResults.northChampTeamName).getRankNameWLStr());
        list.add("Pacific Champions," + league.getTeam(leagueResults.pacificChampTeamName).getRankNameWLStr());
        list.add("South Champions," + league.getTeam(leagueResults.southChampTeamName).getRankNameWLStr());
        return list;
    }

    public static String[] getAllCategories() {
        return new String[]{
            "PPG", "APG", "RPG", "SPG", "BPG", "TPG", "FGMPG", "FGAPG", "FG%",
                    "3FGMPG", "3FGAPG", "3FG%", "FGAPG", //"FTMPG", "FTAPG",
            "OPPG", "OAPG", "ORPG", "OSPG", "OBPG", "OTPG", "OFGMPG", "OFGAPG", "OFG%",
                    "O3FGMPG", "O3FGAPG", "O3FG%", "OFTMPG", "OFTAPG"};
    }

    public static String getDescriptionCategory(String cat) {
        switch (cat) {
            case "PPG":
                return "Points Per Game";
            case "APG":
                return "Assists Per Game";
            case "RPG":
                return "Rebounds Per Game";
            case "SPG":
                return "Steals Per Game";
            case "BPG":
                return "Blocks Per Game";
            case "TPG":
                return "Turnovers Per Game";
            case "FGMPG":
                return "Field Goals Made Per Game";
            case "FGAPG":
                return "Field Goal Attempts Per Game";
            case "FG%":
                return "Field Goal Percentage";
            case "3FGMPG":
                return "3 Pointers Made Per Game";
            case "3FGAPG":
                return "3 Point Attempts Per Game";
            case "3FG%":
                return "3 Point Percentage";
            case "FTMPG":
                return "Free Throws Made Per Game";
            case "FTAPG":
                return "Free Throw Attempts Per Game";
            case "OPPG":
                return "Opp Points Per Game";
            case "OAPG":
                return "Opp Assists Per Game";
            case "ORPG":
                return "Opp Rebounds Per Game";
            case "OSPG":
                return "Opp Steals Per Game";
            case "OBPG":
                return "Opp Blocks Per Game";
            case "OTPG":
                return "Opp Turnovers Per Game";
            case "OFGMPG":
                return "Opp Field Goals Made Per Game";
            case "OFGAPG":
                return "Opp Field Goal Attempts Per Game";
            case "OFG%":
                return "Opp Field Goal Percentage";
            case "O3FGMPG":
                return "Opp 3 Pointers Made Per Game";
            case "O3FGAPG":
                return "Opp 3 Point Attempts Per Game";
            case "O3FG%":
                return "Opp 3 Point Percentage";
            case "OFTMPG":
                return "Opp Free Throws Made Per Game";
            case "OFTAPG":
                return "Opp Free Throw Attempts Per Game";
            default:
                return "ERROR";
        }
    }

    public static void colorizeRatings(TextView textV) {
        String letter;
        if (textV.getText().toString().split(" ").length == 2) {
            letter = textV.getText().toString().split(" ")[1];
        } else {
            letter = textV.getText().toString();
        }
        // The last index is always the rating: A+, C, etc
        if (letter.equals("A") || letter.equals("A+")) {
            textV.setTextColor(Color.parseColor("#006600"));
        } else if (letter.equals("B") || letter.equals("B+")) {
            textV.setTextColor(Color.parseColor("#00b300"));
        } else if (letter.equals("C") || letter.equals("C+")) {
            textV.setTextColor(Color.parseColor("#e68a00"));
        } else if (letter.equals("D") || letter.equals("D+")) {
            textV.setTextColor(Color.parseColor("#cc3300"));
        } else if (letter.equals("F") || letter.equals("F+")) {
            textV.setTextColor(Color.parseColor("#990000"));
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
