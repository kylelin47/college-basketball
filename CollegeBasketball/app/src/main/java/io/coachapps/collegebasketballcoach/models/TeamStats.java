package io.coachapps.collegebasketballcoach.models;

import java.io.Serializable;

public class TeamStats implements Serializable {
    public Stats stats;
    public Stats oppStats;

    public TeamStats() {
        stats = new Stats();
        oppStats = new Stats();
    }

    public TeamStats(Stats stats, Stats oppStats) {
        this.stats = stats;
        this.oppStats = oppStats;
    }

    public void add(Stats s, boolean isFor) {
        if (isFor) {
            stats.add(s);
        } else {
            oppStats.add(s);
        }
    }
}
