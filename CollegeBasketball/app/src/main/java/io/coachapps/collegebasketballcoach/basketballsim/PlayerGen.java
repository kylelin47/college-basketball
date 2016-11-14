package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.models.PlayerRatings;

/**
 * Class that encapsulates the player generation engine.
 * @author Achi Jones
 */
public class PlayerGen {
    
    String[] listFirstNames;
    String[] listLastNames;
    int currID;
    
    public PlayerGen(String firstCSV, String lastCSV, int year) {
        //get list of names from file
        listFirstNames = firstCSV.split(",");
        listLastNames = lastCSV.split(",");
        currID = 1000 * (year - 2016) + 1;
    }
    
    private String getRandName() {
        // get random name from list and remove it so it won't be used again
        int firstName = (int)(Math.random() * listFirstNames.length);
        String fName = listFirstNames[firstName].trim();
        int lastName = (int)(Math.random() * listLastNames.length);
        String lName = listLastNames[lastName].trim();
        return ( fName + " " + lName );        
    }
    
    public ArrayList<Player> genRecruits( int number ) {
        // generate number of players, with an equal number of each position
        ArrayList<Player> PlayerList = new ArrayList<>();
        int basePrestige = 25;
        int randPrestige = 75;
        for (int i = 0; i < number/5; ++i) {
            Player genPG = genPlayer(1, basePrestige + (int)(Math.random()*randPrestige), 1);
            Player genSG = genPlayer(2, basePrestige + (int)(Math.random()*randPrestige), 1);
            Player genSF = genPlayer(3, basePrestige + (int)(Math.random()*randPrestige), 1);
            Player genPF = genPlayer(4, basePrestige + (int)(Math.random()*randPrestige), 1);
            Player genC  = genPlayer(5, basePrestige + (int)(Math.random()*randPrestige), 1);
            PlayerList.add(genPG);
            PlayerList.add(genSG);
            PlayerList.add(genSF);
            PlayerList.add(genPF);
            PlayerList.add(genC);
        }
        
        return PlayerList;
    }
    
    public Player genPlayer( int position, int prestige, int year ) {
        int def_rat    = 75;
        int height     = 78;
        int weight     = 200;
        int speed      = def_rat;
        int age        = 25;
        int int_s      = def_rat;
        int mid_s      = def_rat;
        int out_s      = def_rat;
        int passing    = def_rat;
        int handling   = def_rat;
        int steal      = def_rat;
        int block      = def_rat;
        int int_d      = def_rat;
        int out_d      = def_rat;
        int rebounding = def_rat;
        int extra_usage = 0;
        String name = getRandName();
        String att = "";
        
        if ( position == 1 ) {
            // Point Guard
            height -= 3*Math.random() + 3;
            weight -= 30*Math.random();
            speed += 5*Math.random() + 5;
            int_s -= 8*Math.random() + 8;
            mid_s += 10*Math.random() - 5;
            out_s += 10*Math.random() - 5;
            passing += 10*Math.random() + 5;
            handling += 10*Math.random();
            steal += 10*Math.random() - 2;
            block -= 20*Math.random() + 20;
            int_d -= 8*Math.random() + 8;
            out_d += 10*Math.random() - 5;
            rebounding -= 20*Math.random() + 10;
        } else if ( position == 2 ) {
            // Shooting Guard
            height += 4*Math.random() - 3;
            weight += 30*Math.random() - 15;
            speed += 6*Math.random();
            int_s += 16*Math.random() - 8;
            mid_s += 13*Math.random() - 5;
            out_s += 13*Math.random() - 5;
            passing += 10*Math.random();
            handling += 10*Math.random() - 2;
            steal += 10*Math.random() - 5;
            block -= 20*Math.random() + 10;
            int_d -= 5*Math.random() + 5;
            out_d += 10*Math.random() - 5;
            rebounding -= 10*Math.random() + 5;
        } else if ( position == 3 ) {
            // Small Forward
            height += 6*Math.random() - 2;
            weight += 40*Math.random() - 10;
            speed += 16*Math.random() - 8;
            int_s += 20*Math.random() - 8;
            mid_s += 20*Math.random() - 10;
            out_s += 20*Math.random() - 10;
            passing += 20*Math.random() - 10;
            handling += 20*Math.random() - 10;
            steal += 20*Math.random() - 10;
            block += 15*Math.random() - 5;
            int_d += 15*Math.random() - 5;
            out_d += 15*Math.random() - 5;
            rebounding += 15*Math.random() - 5;
        } else if ( position == 4 ) {
            // Power Forward
            height += 6*Math.random() + 1;
            weight += 40*Math.random() + 20;
            speed += 15*Math.random() - 15;
            int_s += 20*Math.random() - 5;
            mid_s += 16*Math.random() - 8;
            out_s += 12*Math.random() - 6;
            passing += 20*Math.random() - 15;
            handling += 20*Math.random() - 20;
            steal += 20*Math.random() - 15;
            block += 20*Math.random() - 5;
            int_d += 20*Math.random() - 5;
            out_d += 10*Math.random() - 8;
            rebounding += 20*Math.random() - 5;
        } else if ( position == 5 ) {
            // Center
            height += 8*Math.random() + 2;
            weight += 40*Math.random() + 40;
            speed += 20*Math.random() - 30;
            int_s += 10*Math.random() + 5;
            mid_s += 20*Math.random() - 15;
            out_s += 30*Math.random() - 45;
            passing += 20*Math.random() - 20;
            handling += 30*Math.random() - 40;
            steal += 30*Math.random() - 40;
            block += 10*Math.random() + 5;
            int_d += 10*Math.random() + 5;
            out_d += 20*Math.random() - 20;
            rebounding += 12*Math.random() + 5;
        }
        
        // Assign attributes to boost or lower stats
        int choice;
        for (int i = 0; i < 5; i++) {
            choice = (int) (Math.random() * 20);
            if (choice == 0) {
                //Passer
                passing += 15*Math.random() + 5;
                att += "Ps ";
            } else if (choice == 1) {
                //Off Weapon
                out_s += 5*Math.random() + 5;
                mid_s += 6*Math.random() + 6;
                int_s += 7*Math.random() + 7;
                att += "OW ";
            } else if (choice == 2) {
                //Blocker
                block += 10*Math.random() + 5;
                att += "Bl ";
            } else if (choice == 3) {
                //Tall (does nothing?)
                height += 2;
                //att += "Tll ";
            } else if (choice == 4) {
                //Short (does nothing?)
                height -= 2;
                //att += "Sht ";
            } else if (choice == 5) {
                //On-Ball Defense
                steal += 5*Math.random() + 5;
                out_d += 5*Math.random() + 5;
                att += "Ob ";
            } else if (choice == 6) {
                //Rebounding
                rebounding +=10*Math.random() + 5;
                height += 1;
                att += "Rb ";
            } else if (choice == 7) {
                //Fumbler
                handling -= 5*Math.random() + 5;
                passing -= 5*Math.random() + 5;
                att += "Fm ";
            } else if (choice == 8) {
                //Fatty
                weight += 30;
                //att += "Fa ";
            } else if (choice == 9) {
                //Slow
                speed -= 15;
                //att += "Sl ";
            } else if (choice == 10) {
                //No Threes
                out_s -= 10*Math.random() + 15;
                if (out_s < 0) out_s = 0;
                att += "n3 ";
            } else if (choice == 11) {
                //Dunker
                int_s += 10*Math.random() + 10;
                att += "Dn ";
            } else if (choice == 12) {
                //Defensive Liability
                steal -= 5*Math.random() - 5;
                block -= 5*Math.random() - 5;
                out_d -= 5*Math.random() - 5;
                int_d -= 5*Math.random() - 5;
                att += "Dl ";
            } else if (choice == 13) {
                //Offensive Liability
                int_s -= 5*Math.random() - 5;
                mid_s -= 5*Math.random() - 5;
                out_s -= 5*Math.random() - 5;
                att += "Ol ";
            } else if (choice == 14) {
                //Mid Range Jesus
                mid_s += 12*Math.random() + 5;
                att += "Md ";
            } else if (choice == 15) {
                //The Wall
                int_d += 10*Math.random() + 5;
                block += 5*Math.random() + 5;
                att += "Wa ";
            } else if (choice == 16) {
                //3pt Specialist
                out_s += 5*Math.random() + 8;
                passing -= 5*Math.random() + 5;
                int_s -= 5*Math.random() + 5;
                mid_s -= 5*Math.random() + 5;
                att += "3p ";
            } else if (choice == 17) {
                //Chucker
                if (extra_usage == 0) {
                    extra_usage += 5000;
                    att += "Ch ";
                }
            } else if (choice == 18 && Math.random() < 0.1) {
                //Whole Package (rare)
                extra_usage += 3;
                int_s += 5*Math.random() + 5;
                mid_s += 5*Math.random() + 5;
                out_s += 5*Math.random() + 5;
                passing += 5*Math.random() + 5;
                handling += 5*Math.random() + 5;
                steal += 5*Math.random() + 5;
                block += 5*Math.random() + 5;
                int_d += 5*Math.random() + 5;
                out_d += 5*Math.random() + 5;
                att += "XX ";
            } else if (choice == 19) {
                //The Thief
                steal += 10*Math.random() + 5;
                att += "St ";
            }
        }

        // Better player for higher prestige
        int prestigeFactor = 2*(prestige-50)/10;
        int_s      += prestigeFactor*Math.random();
        mid_s      += prestigeFactor*Math.random();
        out_s      += prestigeFactor*Math.random();
        passing    += prestigeFactor*Math.random();
        handling   += prestigeFactor*Math.random();
        steal      += prestigeFactor*Math.random();
        block      += prestigeFactor*Math.random();
        int_d      += prestigeFactor*Math.random();
        out_d      += prestigeFactor*Math.random();
        rebounding += prestigeFactor*Math.random();
        
        int usage = (int) (Math.round( Math.pow(int_s, 2) + Math.pow(mid_s, 2) + Math.pow(out_s, 2) ) + extra_usage)/1000;
        /*
        double factor = 1.8;
        int ins_t = (int) (1000*Math.pow(int_s, factor) / (Math.pow(int_s, factor) + Math.pow(mid_s, factor) + Math.pow(out_s, factor)) );
        int mid_t = (int) (1000*Math.pow(mid_s, factor) / (Math.pow(int_s, factor) + Math.pow(mid_s, factor) + Math.pow(out_s, factor)) );
        int out_t = (int) (1000*Math.pow(out_s, factor) / (Math.pow(int_s, factor) + Math.pow(mid_s, factor) + Math.pow(out_s, factor)) );
        */

        PlayerRatings ratings = new PlayerRatings();

        ratings.potential = 50 + (int)(Math.random()*50);
        ratings.bballIQ = 50 + (int)(Math.random()*50);
        ratings.position = position;
        ratings.insideShooting = int_s;
        ratings.midrangeShooting = mid_s;
        ratings.outsideShooting = out_s;
        ratings.passing = passing;
        ratings.handling = handling;
        ratings.steal = steal;
        ratings.block = block;
        ratings.insideDefense = int_d;
        ratings.perimeterDefense = out_d;
        ratings.rebounding = rebounding;
        ratings.usage = usage;
        ratings.weightInPounds = weight;
        ratings.heightInInches = height;

        int numYearsAdvance = year - 1;
        for (int i = 0; i < numYearsAdvance; ++i) {
            advanceYearRatings(ratings);
        }
        
        return new Player(name, ratings, att, currID++, year);
    }

    public void advanceYearRatings(PlayerRatings ratings) {
        int potBonus = ratings.potential - 40;
        int div = 10;
        ratings.insideShooting   += (int)(Math.random()*potBonus)/div;
        ratings.midrangeShooting += (int)(Math.random()*potBonus)/div;
        ratings.outsideShooting  += (int)(Math.random()*potBonus)/div;
        ratings.passing          += (int)(Math.random()*potBonus)/div;
        ratings.handling         += (int)(Math.random()*potBonus)/div;
        ratings.steal            += (int)(Math.random()*potBonus)/div;
        ratings.block            += (int)(Math.random()*potBonus)/div;
        ratings.insideDefense    += (int)(Math.random()*potBonus)/div;
        ratings.perimeterDefense += (int)(Math.random()*potBonus)/div;
        ratings.rebounding       += (int)(Math.random()*potBonus)/div;

        if (Math.random()*100 < ratings.potential) {
            ratings.insideShooting   += (int)(Math.random()*potBonus)/div;
            ratings.midrangeShooting += (int)(Math.random()*potBonus)/div;
            ratings.outsideShooting  += (int)(Math.random()*potBonus)/div;
            ratings.passing          += (int)(Math.random()*potBonus)/div;
            ratings.handling         += (int)(Math.random()*potBonus)/div;
            ratings.steal            += (int)(Math.random()*potBonus)/div;
            ratings.block            += (int)(Math.random()*potBonus)/div;
            ratings.insideDefense    += (int)(Math.random()*potBonus)/div;
            ratings.perimeterDefense += (int)(Math.random()*potBonus)/div;
            ratings.rebounding       += (int)(Math.random()*potBonus)/div;
        }
    }
    
}
