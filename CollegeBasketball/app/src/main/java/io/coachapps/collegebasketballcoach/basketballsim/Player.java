package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Achi Jones
 */
public class Player {
    
    public String name;
    public String attributes;
    // attributes = "3p ID DNK CHU WALL"
    public int games_played;
    public int[] stats_gm;
    public int[] stats_tot;
    // stats = { 0 points, 1 fga, 2 fgm, 3 3ga, 4 3gm, 5 ass, 6 reb, 7 stl, 8 blk, 9 ofa, 10 ofm, (11 MSM) }
    public int[] ratingsArray;
    // ratings = { 0 Position,    1 Overall, 2 Int_S,  3 Mid_S,  4 Out_S, 5 Passing, 
    //             6 Handling,    7 Steal,   8 Block,  9 Int_D, 10 Out_D, 
    //            11 Rebounding, 12 Usage,  13 Ins_T, 14 Mid_T, 15 Out_T }
    
    public Player( String name, int[] ratings ) {
        stats_gm = new int[11];
        stats_tot = new int[12];
        games_played = 0;
        ratingsArray = ratings;
        this.name = name;
        for (int i = 0; i < 10; ++i) {
            stats_gm[i] = 0;
            stats_tot[i] = 0;
        }
        stats_tot[11] = 0;
        attributes = "";
    }
    
    public Player( String name, int[] ratings, String att ) {
        stats_gm = new int[11];
        stats_tot = new int[12];
        games_played = 0;
        ratingsArray = ratings;
        this.name = name;
        for (int i = 0; i < 10; ++i) {
            stats_gm[i] = 0;
            stats_tot[i] = 0;
        }
        stats_tot[11] = 0;
        attributes = att;
    }
    
    //Ratings getters
    public int getPosition() {
        return ratingsArray[0];
    }
    public int getOverall() {
        return ratingsArray[1];
    }
    public int getIntS() {
        return ratingsArray[2];
    }
    public int getMidS() {
        return ratingsArray[3];
    }
    public int getOutS() {
        return ratingsArray[4];
    }
    public int getPass() {
        return ratingsArray[5];
    }
    public int getHand() {
       return ratingsArray[6]; 
    }
    public int getStl() {
       return ratingsArray[7]; 
    }
    public int getBlk() {
       return ratingsArray[8]; 
    }
    public int getIntD() {
       return ratingsArray[9]; 
    }
    public int getOutD() {
       return ratingsArray[10]; 
    }
    public int getReb() {
       return ratingsArray[11]; 
    }
    public int getUsage() {
       return ratingsArray[12]; 
    }
    public double getInsT() {
       return (double)ratingsArray[13]/1000; 
    }
    public double getMidT() {
       return (double)ratingsArray[14]/1000; 
    }
    public double getOutT() {
       return (double)ratingsArray[15]/1000; 
    }
    
    public int getPlayingTime() {
        //playing time in minutes
        return 25 + getOverall()/8;
    }
    
    //Stats getters/setters
    // stats = { 0 points, 1 fga, 2 fgm, 3 3ga, 4 3gm, 5 ass, 6 reb, 7 stl, 8 blk, 9 ofa, 10 ofm }
    public void addPts(int points) {
        stats_gm[0] += points;
    }
    public void addFGA() {
        stats_gm[1]++;
    }
    public void addFGM() {
        stats_gm[2]++;
    }
    public void add3GA() {
        stats_gm[3]++;
    }
    public void add3GM() {
        stats_gm[4]++;
    }
    public void addAss() {
        stats_gm[5]++;
    }
    public void addReb() {
        stats_gm[6]++;
    }
    public void addStl() {
        stats_gm[7]++;
    }
    public void addBlk() {
        stats_gm[8]++;
    }
    public void addOFA() {
        stats_gm[9]++;
    }
    public void addOFM() {
        stats_gm[10]++;
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
        for (int i = 0; i <= 10; ++i) {
            stats_tot[i] += stats_gm[i];
            stats_gm[i] = 0;
        }
        games_played++;
    }
    // stats = { 0 points, 1 fga, 2 fgm, 3 3ga, 4 3gm, 5 ass, 6 reb, 7 stl, 8 blk, 9 ofa, 10 ofm }
    public double getPPG() {
        return (double)((int)((double)stats_tot[0]/games_played * 10))/10;
    }
    public double getFGP() {
        if (stats_tot[1] > 0 ) {
            return (double) ((int) ((double) stats_tot[2] / stats_tot[1] * 1000)) / 10;
        } else return 0;
    }
    public double get3GP() {
        if ( stats_tot[3] > 0 ) {
            return (double)((int)((double)stats_tot[4]/stats_tot[3] * 1000))/10;
        } else return 0;
    }
    public double getAPG() {
        return (double)((int)((double)stats_tot[5]/games_played * 10))/10;
    }
    public double getRPG() {
        return (double)((int)((double)stats_tot[6]/games_played * 10))/10;
    }
    public double getSPG() {
        return (double)((int)((double)stats_tot[7]/games_played * 10))/10;
    }
    public double getBPG() {
        return (double)((int)((double)stats_tot[8]/games_played * 10))/10;
    }
    public double getOFP() {
        return (double)((int)((double)stats_tot[10]/stats_tot[9] * 1000))/10;
    }
    public int getMSM() {
        return (int)((double)stats_tot[11]/(games_played));
    }
    public double getFGAPG() {
        return (double)((int)((double)stats_tot[1]/games_played * 10))/10;
    }
    public double get3GAPG() {
        return (double)((int)((double)stats_tot[3]/games_played * 10))/10;
    }
    public double getFGMPG() {
        return (double)((int)((double)stats_tot[2]/games_played * 10))/10;
    }
    public double get3GMPG() {
        return (double)((int)((double)stats_tot[4]/games_played * 10))/10;
    }
    
    public static String getPositionStr(int pos) {
        if (pos == 1) return "PG";
        if (pos == 2) return "SG";
        if (pos == 3) return "SF";
        if (pos == 4) return "PF";
        return "C";
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
