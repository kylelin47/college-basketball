package io.coachapps.collegebasketballcoach.util;

import java.util.Comparator;

import io.coachapps.collegebasketballcoach.basketballsim.Player;

/**
 * Comparator for players
 * Created by jojones on 11/13/16.
 */

public class PlayerOverallComp implements Comparator<Player> {
    @Override
    public int compare( Player a, Player b ) {
        return b.getOverall() - a.getOverall();
    }
}
