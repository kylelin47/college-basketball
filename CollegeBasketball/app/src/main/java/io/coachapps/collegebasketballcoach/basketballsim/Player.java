package io.coachapps.collegebasketballcoach.basketballsim;

import java.io.Serializable;
import java.util.Comparator;

import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;
import io.coachapps.collegebasketballcoach.models.Stats;

/**
 * Player class used in the application layer.
 * @author Achi Jones
 */
public class Player implements Serializable {
    
    public String name;
    public String attributes;
    // 0 is to be recruited, 1-2-3-4 are college years, 5 is done
    public int year;

    private int overall;

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
    }

    public void setLineupPosition(int pos) {
        ratings.lineupPosition = pos;
    }

    public int getLineupPosition() {
        return ratings.lineupPosition;
    }

    public void setLineupMinutes(int minutes) {
        ratings.lineupMinutes = minutes;
    }

    public int getLineupMinutes() {
        return ratings.lineupMinutes;
    }

    public int getPlayingTime() {
        //playing time in minutes
        return 18 + getOverall()/6;
    }

    public BoxScore getGameBoxScore(int year, int week, String teamName) {
        return new BoxScore(id, year, week, gmStats, teamName);
    }
    public int getId() {
        return id;
    }
    //Ratings getters
    public int getPosition() {
        return ratings.position;
    }
    public int getBBallIQ() {
        return ratings.bballIQ;
    }
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
    public int getPotential() {
        return ratings.potential;
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
    public void addTO() {
        gmStats.turnovers++;
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

    public int getCompositeShooting() {
        return (getIntS() + getMidS() + getOutS())/3;
    }

    public int getCompositeDefense() {
        return (getIntD()*3 + getOutD()*2 + getBlk() + getStl())/7;
    }

    public int getCompositePassing() {
        return (getPass()*4 + getHand())/5;
    }

    public int getCompositeRebounding() {
        return getReb();
    }
}
