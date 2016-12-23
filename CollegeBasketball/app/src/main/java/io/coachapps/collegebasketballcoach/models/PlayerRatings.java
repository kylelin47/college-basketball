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
}
