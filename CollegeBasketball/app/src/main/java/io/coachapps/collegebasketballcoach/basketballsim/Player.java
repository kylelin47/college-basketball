package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;
import io.coachapps.collegebasketballcoach.models.Stats;

/**
 * Player class used in the application layer.
 * @author Achi Jones
 */
public class Player {
    
    public String name;
    public String attributes;
    // 0 is to be recruited, 1-2-3-4 are college years, 5 is done
    public int year;
    public int games_played;

    private int overall;
    private double insideTend;
    private double midrangeTend;
    private double outsideTend;

    public PlayerRatings ratings;
    public Stats gmStats;
    private int id;

    public boolean onCourt;
    
    public Player(String name, PlayerRatings ratings, String att, int id, int year) {
        this.name = name;
        this.attributes = att;
        this.id = id;
        this.ratings = ratings;
        this.year = year;

        overall = (int) Math.round( Math.pow(getIntS(), 1.3) + Math.pow(getMidS(), 1.3) + Math.pow(getOutS(), 1.3) + Math.pow(getPass(), 1.1) + getHand() +
                Math.pow(getStl(), 1.1) + Math.pow(getBlk(), 1.1) + Math.pow(getIntD(), 1.2) + Math.pow(getOutD(), 1.2) + Math.pow(getReb(), 1.2) );
        overall = (100*overall)/2500;

        gmStats = new Stats();
        games_played = 0;
    }

    public BoxScore getGameBoxScore() {
        return new BoxScore(id, 2016, gmStats);
    }
    public int getId() {
        return id;
    }
    //Ratings getters
    public int getPosition() {
        return ratings.position;
    }
    public int getBBallIQ() { return ratings.bballIQ; }
    public int getOverall() {
        return overall;
    }
    public int getIntS() {
        return ratings.insideShooting;
    }
    public int getMidS() {
        return ratings.midrangeShooting;
    }
    public int getOutS() {
        return ratings.outsideShooting;
    }
    public int getPass() {
        return ratings.passing;
    }
    public int getHand() {
        return ratings.handling;
    }
    public int getStl() {
        return ratings.steal;
    }
    public int getBlk() {
        return ratings.block;
    }
    public int getIntD() {
        return ratings.insideDefense;
    }
    public int getOutD() {
        return ratings.perimeterDefense;
    }
    public int getReb() {
        return ratings.rebounding;
    }
    public int getUsage() {
        return ratings.usage;
    }
    public double getInsT() {
        double factor = 1.8;
        return Math.pow(getIntS(), factor) / (Math.pow(getIntS(), factor) + Math.pow(getMidS(), factor) + Math.pow(getOutS(), factor));
    }
    public double getMidT() {
        double factor = 1.8;
        return Math.pow(getMidS(), factor) / (Math.pow(getIntS(), factor) + Math.pow(getMidS(), factor) + Math.pow(getOutS(), factor));
    }
    public double getOutT() {
        double factor = 1.8;
        return Math.pow(getOutS(), factor) / (Math.pow(getIntS(), factor) + Math.pow(getMidS(), factor) + Math.pow(getOutS(), factor));
    }


    public int getPlayingTime() {
        //playing time in minutes
        return 18 + getOverall()/6;
    }
    
    //Stats getters/setters
    // stats = { 0 points, 1 fga, 2 fgm, 3 3ga, 4 3gm, 5 ass, 6 reb, 7 stl, 8 blk, 9 ofa, 10 ofm }
    public void addPts(int points) {
        gmStats.points += points;
    }
    public void addFGA() {
        gmStats.fieldGoalsAttempted++;
    }
    public void addFGM() {
        gmStats.fieldGoalsMade++;
    }
    public void add3GA() {
        gmStats.threePointsAttempted++;
    }
    public void add3GM() {
        gmStats.threePointsMade++;
    }
    public void addAss() {
        gmStats.assists++;
    }
    public void addReb() {
        gmStats.defensiveRebounds++;
    }
    public void addStl() {
        gmStats.steals++;
    }
    public void addBlk() {
        gmStats.blocks++;
    }
    public void addTimePlayed(int seconds) { gmStats.secondsPlayed += seconds; }
    public void addOFA() {
        //stats_gm[9]++;
    }
    public void addOFM() {
        //stats_gm[10]++;
    }
    public void make3ptShot() {
        addPts(3);
        addFGM();
        addFGA();
        add3GA();
        add3GM();
    }
    
    public void addGameStatsToTotal() {
        //Add stats from each game to total count
        //for (int i = 0; i <= 10; ++i) {
        //    stats_tot[i] += stats_gm[i];
        //    stats_gm[i] = 0;
        //}
        games_played++;
    }
    // stats = { 0 points, 1 fga, 2 fgm, 3 3ga, 4 3gm, 5 ass, 6 reb, 7 stl, 8 blk, 9 ofa, 10 ofm }
    public double getPPG() {
        return (double)((int)((double)gmStats.points/games_played * 10))/10;
    }
    public double getFGP() {
        //if (stats_tot[1] > 0 ) {
        //    return (double) ((int) ((double) stats_tot[2] / stats_tot[1] * 1000)) / 10;
        //} else return 0;
        return 50.0;
    }
    public double get3GP() {
        //if ( stats_tot[3] > 0 ) {
        //    return (double)((int)((double)stats_tot[4]/stats_tot[3] * 1000))/10;
        //} else return 0;
        return 40.0;
    }
    public double getAPG() {
        return (double)((int)((double)gmStats.assists/games_played * 10))/10;
    }
    public double getRPG() {
        return (double)((int)((double)gmStats.defensiveRebounds/games_played * 10))/10;
    }
    public double getSPG() {
        return (double)((int)((double)gmStats.steals/games_played * 10))/10;
    }
    public double getBPG() {
        return (double)((int)((double)gmStats.blocks/games_played * 10))/10;
    }
    public double getOFP() {
        //return (double)((int)((double)stats_tot[10]/stats_tot[9] * 1000))/10;
        return 50.0;
    }
    public int getMSM() {
        //return (int)((double)stats_tot[11]/(games_played));
        return 100;
    }
    public double getFGAPG() {
        return (double)((int)((double)gmStats.fieldGoalsAttempted/games_played * 10))/10;
    }
    public double get3GAPG() {
        return (double)((int)((double)gmStats.threePointsAttempted/games_played * 10))/10;
    }
    public double getFGMPG() {
        return (double)((int)((double)gmStats.fieldGoalsMade/games_played * 10))/10;
    }
    public double get3GMPG() {
        return (double)((int)((double)gmStats.threePointsMade/games_played * 10))/10;
    }

    public Map<String, Integer> getRatingsMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("IntS", getIntS());
        map.put("MidS", getMidS());
        map.put("OutS", getOutS());
        map.put("Pass", getPass());
        map.put("Stl", getStl());
        map.put("PerD", getOutD());
        map.put("Reb", getReb());
        map.put("Blk", getBlk());
        map.put("IntD", getIntD());

        return map;
    }

    public List<String> getRatingsCSVs() {
        ArrayList<String> list = new ArrayList<>();
        list.add("IntS,"+getIntS());
        list.add("MidS,"+getMidS());
        list.add("OutS,"+getOutS());

        list.add("Pass,"+getPass());
        list.add("Stl,"+getStl());
        list.add("PerD,"+getOutD());

        list.add("Reb,"+getReb());
        list.add("Blk,"+getBlk());
        list.add("IntD,"+getIntD());

        list.add("InsT,"+getInsT());
        list.add("MidT,"+getMidT());
        list.add("OutT,"+getOutT());

        list.addAll(getAvgStatsCSV());

        return list;
    }

    public List<String> getAvgStatsCSV() {
        ArrayList<String> list = new ArrayList<>();
        list.add("GP,82");
        list.add("GS,82");
        list.add("MPG,"+getPlayingTime());

        list.add("USG,"+getUsage());
        list.add("MSM,"+getMSM());
        list.add("POS,"+getPosition());

        list.add("PPG,"+getPPG());
        list.add("RPG,"+getRPG());
        list.add("APG,"+getAPG());

        list.add("SPG,"+getSPG());
        list.add("BPG,"+getBPG());
        list.add("OFG%,"+getOFP());

        list.add("FGM,"+getFGMPG());
        list.add("FGA,"+getFGAPG());
        list.add("FG%,"+getFGP());

        list.add("3GM,"+get3GMPG());
        list.add("3GA,"+get3GAPG());
        list.add("3FG%,"+get3GP());

        return list;
    }
    
    
}
