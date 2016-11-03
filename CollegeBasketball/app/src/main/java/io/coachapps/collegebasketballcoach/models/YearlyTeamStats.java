package io.coachapps.collegebasketballcoach.models;

public class YearlyTeamStats {
    public String team;
    public int year;
    public int wins;
    public int losses;
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
    };
    public YearlyTeamStats(String team, int year, int wins, int losses, Stats stats) {
        this.team = team;
        this.year = year;
        this.wins = wins;
        this.losses = losses;
        this.points = stats.points;
        this.assists = stats.assists;
        this.rebounds = stats.defensiveRebounds + stats.offensiveRebounds;
        this.steals = stats.steals;
        this.blocks = stats.blocks;
        this.turnovers = stats.turnovers;
        this.fgm = stats.fieldGoalsMade;
        this.fga = stats.fieldGoalsAttempted;
        this.threePM = stats.threePointsMade;
        this.threePA = stats.threePointsAttempted;
        this.ftm = stats.freeThrowsMade;
        this.fta = stats.fieldGoalsAttempted;
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
            case "3FGMPG":
                return getPG(threePM);
            case "3FGAPG":
                return getPG(threePA);
            case "FTMPG":
                return getPG(ftm);
            case "FTAPG":
                return getPG(fta);
        }
        return -1;
    }
    private float getPG(float total) {
        return wins + losses == 0 ? 0 : total/(wins + losses);
    }

    public double getFGP() {
        return (fga == 0 ? 0 : (double)fgm/fga);
    }

    public double get3FGP() {
        return (threePA == 0 ? 0 : (double)threePM/threePA);
    }

    public String getFGPStr() {
        return (fga == 0 ? "0" : String.format("%.1f", 100*((double)fgm/fga)));
    }

    public String get3FGPStr() {
        return (threePA == 0 ? "0" : String.format("%.1f", 100*((double)threePM/threePA)));
    }
}
