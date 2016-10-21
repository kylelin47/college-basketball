package io.coachapps.collegebasketballcoach.models;

public class YearlyTeamStats {
    public String team;
    public int year;
    public int wins;
    public int losses;
    public int points;
    public int assists;
    public int rebounds;
    public YearlyTeamStats(String team){
        this.team = team;
        this.wins = 0;
        this.losses = 0;
        this.assists = 0;
        this.rebounds = 0;
    };
    public YearlyTeamStats(String team, int year, int wins, int losses, Stats stats) {
        this.team = team;
        this.year = year;
        this.wins = wins;
        this.losses = losses;
        this.points = stats.points;
        this.rebounds = stats.defensiveRebounds + stats.offensiveRebounds;
    }
    public float getPPG() {
        return getPG(points);
    }
    public float getAPG() {
        return getPG(assists);
    }
    public float getRPG() {
        return getPG(rebounds);
    }
    private float getPG(float total) {
        return wins + losses == 0 ? 0 : total/(wins + losses);
    }
}
