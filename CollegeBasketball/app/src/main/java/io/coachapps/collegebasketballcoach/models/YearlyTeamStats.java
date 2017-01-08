package io.coachapps.collegebasketballcoach.models;

import io.coachapps.collegebasketballcoach.util.DataDisplayer;

public class YearlyTeamStats {
    public String team;
    public int year;
    public int wins;
    public int losses;
    public String summary;
    public int points;
    public int assists;
    public int rebounds;
    public int steals;
    public int blocks;
    public int turnovers;
    public int fgm;
    public int fga;
    public int threePM;
    public int threePA;
    public int ftm;
    public int fta;
    public int opp_points;
    public int opp_assists;
    public int opp_rebounds;
    public int opp_steals;
    public int opp_blocks;
    public int opp_turnovers;
    public int opp_fgm;
    public int opp_fga;
    public int opp_threePM;
    public int opp_threePA;
    public int opp_ftm;
    public int opp_fta;

    public YearlyTeamStats(String team){
        this.team = team;
        this.wins = 0;
        this.losses = 0;
        this.assists = 0;
        this.rebounds = 0;
        this.steals = 0;
        this.blocks = 0;
        this.turnovers = 0;
        this.fgm = 0;
        this.fga = 0;
        this.threePM = 0;
        this.threePA = 0;
        this.ftm = 0;
        this.fta = 0;
        this.opp_points = 0;
        this.opp_assists = 0;
        this.opp_rebounds = 0;
        this.opp_steals = 0;
        this.opp_blocks = 0;
        this.opp_turnovers = 0;
        this.opp_fgm = 0;
        this.opp_fga = 0;
        this.opp_threePM = 0;
        this.opp_threePA = 0;
        this.opp_ftm = 0;
        this.opp_fta = 0;
    };
    public YearlyTeamStats(String team, int year, int wins, int losses, TeamStats stats) {
        this.team = team;
        this.year = year;
        this.wins = wins;
        this.losses = losses;
        this.points = stats.stats.points;
        this.assists = stats.stats.assists;
        this.rebounds = stats.stats.defensiveRebounds + stats.stats.offensiveRebounds;
        this.steals = stats.stats.steals;
        this.blocks = stats.stats.blocks;
        this.turnovers = stats.stats.turnovers;
        this.fgm = stats.stats.fieldGoalsMade;
        this.fga = stats.stats.fieldGoalsAttempted;
        this.threePM = stats.stats.threePointsMade;
        this.threePA = stats.stats.threePointsAttempted;
        this.ftm = stats.stats.freeThrowsMade;
        this.fta = stats.stats.freeThrowsAttempted;
        this.opp_points = stats.oppStats.points;
        this.opp_assists = stats.oppStats.assists;
        this.opp_rebounds = stats.oppStats.defensiveRebounds + stats.oppStats.offensiveRebounds;
        this.opp_steals = stats.oppStats.steals;
        this.opp_blocks = stats.oppStats.blocks;
        this.opp_turnovers = stats.oppStats.turnovers;
        this.opp_fgm = stats.oppStats.fieldGoalsMade;
        this.opp_fga = stats.oppStats.fieldGoalsAttempted;
        this.opp_threePM = stats.oppStats.threePointsMade;
        this.opp_threePA = stats.oppStats.threePointsAttempted;
        this.opp_ftm = stats.oppStats.freeThrowsMade;
        this.opp_fta = stats.oppStats.freeThrowsAttempted;
    }
    public String getPGDisplay(String abbreviation) {
        return String.format("%.1f", getPG(abbreviation));
    }
    public float getPG(String abbreviation) {
        switch (abbreviation) {
            case "PPG":
                return getPG(points);
            case "APG":
                return getPG(assists);
            case "RPG":
                return getPG(rebounds);
            case "SPG":
                return getPG(steals);
            case "BPG":
                return getPG(blocks);
            case "TPG":
                return getPG(turnovers);
            case "FGMPG":
                return getPG(fgm);
            case "FGAPG":
                return getPG(fga);
            case "FG%":
                return (float)(DataDisplayer.round(getFGP()*100, 2));
            case "3FGMPG":
                return getPG(threePM);
            case "3FGAPG":
                return getPG(threePA);
            case "3FG%":
                return (float)(DataDisplayer.round(get3FGP()*100, 2));
            case "FTMPG":
                return getPG(ftm);
            case "FTAPG":
                return getPG(fta);
            case "FT%":
                return (float)(DataDisplayer.round(getFTP()*100, 2));
            case "OPPG":
                return getPG(opp_points);
            case "OAPG":
                return getPG(opp_assists);
            case "ORPG":
                return getPG(opp_rebounds);
            case "OSPG":
                return getPG(opp_steals);
            case "OBPG":
                return getPG(opp_blocks);
            case "OTPG":
                return getPG(opp_turnovers);
            case "OFGMPG":
                return getPG(opp_fgm);
            case "OFGAPG":
                return getPG(opp_fga);
            case "OFG%":
                return (float)(DataDisplayer.round(getOFGP()*100, 2));
            case "O3FGMPG":
                return getPG(opp_threePM);
            case "O3FGAPG":
                return getPG(opp_threePA);
            case "O3FG%":
                return (float)(DataDisplayer.round(getO3FGP()*100, 2));
            case "OFTMPG":
                return getPG(opp_ftm);
            case "OFTAPG":
                return getPG(opp_fta);
        }
        return -1;
    }
    private float getPG(float total) {
        return wins + losses == 0 ? 0 : total/(wins + losses);
    }

    public double getFGP() {
        return (fga == 0 ? 0 : (double)fgm/fga);
    }

    public double getFTP() {
        return (fta == 0 ? 0 : (double)ftm/fta);
    }

    public double get3FGP() {
        return (threePA == 0 ? 0 : (double)threePM/threePA);
    }

    public double getOFGP() {
        return (opp_fga == 0 ? 0 : (double)opp_fgm/opp_fga);
    }

    public double getO3FGP() {
        return (opp_threePA == 0 ? 0 : (double)opp_threePM/opp_threePA);
    }

    public String getFGPStr() {
        return (fga == 0 ? "0" : String.format("%.1f", 100*((double)fgm/fga)));
    }

    public String get3FGPStr() {
        return (threePA == 0 ? "0" : String.format("%.1f", 100*((double)threePM/threePA)));
    }

    public String getFTPStr() {
        return (fta == 0 ? "0" : String.format("%.1f", 100*((double)ftm/fta)));
    }

    public String getOFGPStr() {
        return (opp_fga == 0 ? "0" : String.format("%.1f", 100*((double)opp_fgm/opp_fga)));
    }

    public String getO3FGPStr() {
        return (opp_threePA == 0 ? "0" : String.format("%.1f", 100*((double)opp_threePM/opp_threePA)));
    }
}
