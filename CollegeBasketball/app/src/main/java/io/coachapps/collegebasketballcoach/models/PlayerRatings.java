package io.coachapps.collegebasketballcoach.models;

import java.io.Serializable;

public class PlayerRatings implements Serializable {
    public int position;
    public int potential;
    public int bballIQ;
    public int heightInInches;
    public int weightInPounds;
    public int insideShooting;
    public int midrangeShooting;
    public int outsideShooting;
    public int passing;
    public int handling;
    public int steal;
    public int block;
    public int insideDefense;
    public int perimeterDefense;
    public int rebounding;
    public int usage;
    // Variables needed for when users customize lineup/minutes
    public int lineupMinutes;
    public int lineupPosition;

    // Offense: Ins Mid Out Usage
    public int getAggregateOffense() {
        return (insideShooting + midrangeShooting + outsideShooting)/3;
    }

    // Fundamentals: Pass Hand IQ Steal
    public int getAggregateFundamentals() {
        return (passing*3 + handling + bballIQ*2 + rebounding*2)/8;
    }

    // Defense: Block Ind Perd Rebound
    public int getAggregateDefense() {
        return (block + insideDefense*3 + perimeterDefense*3 + steal)/8;
    }

    public String getBestAttribute() {
        int maxAttr = 94;
        String strAttr = "";
        if (potential > maxAttr) {
            maxAttr = potential;
            strAttr = "Potential";
        }
        if (bballIQ > maxAttr) {
            maxAttr = bballIQ;
            strAttr = "BBall IQ";
        }
        if (insideShooting > maxAttr) {
            maxAttr = insideShooting;
            strAttr = "Inside Shooting";
        }
        if (midrangeShooting > maxAttr) {
            maxAttr = midrangeShooting;
            strAttr = "Midrange Shooting";
        }
        if (outsideShooting > maxAttr) {
            maxAttr = outsideShooting;
            strAttr = "Outside Shooting";
        }
        if (passing > maxAttr) {
            maxAttr = passing;
            strAttr = "Passing";
        }
        if (steal > maxAttr) {
            maxAttr = steal;
            strAttr = "Stealing";
        }
        if (block > maxAttr) {
            maxAttr = block;
            strAttr = "Blocking";
        }
        if (insideDefense > maxAttr) {
            maxAttr = insideDefense;
            strAttr = "Inside Defense";
        }
        if (perimeterDefense > maxAttr) {
            maxAttr = perimeterDefense;
            strAttr = "Perimeter Defense";
        }
        if (rebounding > maxAttr) {
            maxAttr = rebounding;
            strAttr = "Rebounding";
        }
        return strAttr;
    }
}
