package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.PlayerOverallComp;

/**
 *
 * @author Achi Jones
 */
public class Team {
    public List<Player> players;
    public int wins;
    public int losses;
    public int seed;

    public List<Game> gameSchedule;

    public String name;
    private boolean isUserTeam;
    public int prestige;
    public boolean[] startersIn;

    public volatile Strategy offStrat;
    public volatile Strategy defStrat;

    public Team( String name, List<Player> players, int prestige, boolean isUserTeam ) {
        this.players = players;
        this.name = name;
        this.prestige = prestige;
        this.isUserTeam = isUserTeam;
        wins = 0;
        losses = 0;
        gameSchedule = new ArrayList<>();
        startersIn = new boolean[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = true;
        }

        setOffStrat(Strategy.Strats.DRIBBLE_DRIVE);
        setDefStrat(Strategy.Strats.MAN_TO_MAN);
        try {
            resetLineup();
        } catch (Exception e) {
            // uh
        }
    }
    
    public Team( String name, int prestige, PlayerGen gen, boolean isUserTeam ) {
        this.name = name;
        this.prestige = prestige;
        this.isUserTeam = isUserTeam;
        wins = 0;
        losses = 0;
        gameSchedule = new ArrayList<>();
        players = new ArrayList<>(10);

        // Set so all the starters are in for now
        startersIn = new boolean[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = true;
        }

        // Make the players
        for (int i = 0; i < 5; ++i) {
            Player a = gen.genPlayer(i+1, prestige, 1+(int)(Math.random()*4));
            Player b = gen.genPlayer(i+1, prestige, 1+(int)(Math.random()*4));
            if (a.getOverall() > b.getOverall()) {
                players.add(i, a);
                players.add(b);
            } else {
                players.add(i, b);
                players.add(a);
            }
        }

        setOffStrat(Strategy.Strats.DRIBBLE_DRIVE);
        setDefStrat(Strategy.Strats.MAN_TO_MAN);
        try {
            resetLineup();
        } catch (Exception e) {
            // uh
        }
    }

    /**
     * Resets the lineup for the team.
     * Makes sure that a PG SG SF PF C lineup is in place, with all the best players in place.
     */
    public void resetLineup() {
        if (isUserTeam) {
            sortPlayersLineupPosition();
        } else {
            sortPlayersOvrPosition();
        }
    }

    public void sortPlayersOvrPosition() {
        if (players.size() >= 10) {
            Object[] playerArr = new Object[players.size()];
            for (int i = 0; i < 5; ++i) {
                pickStarterBenchPosition(i + 1, players, playerArr);
            }

            players.clear();
            for (Object obj : playerArr) {
                players.add((Player) obj);
            }

            int[] backUpMinutes = new int[5];
            for (int i = 0; i < 10; ++i) {
                players.get(i).setLineupPosition(i);
                if (i < 5) {
                    backUpMinutes[i] = 40 - players.get(i).getPlayingTime();
                    players.get(i).setLineupMinutes(players.get(i).getPlayingTime());
                } else {
                    players.get(i).setLineupMinutes(backUpMinutes[i-5]);
                }
            }
        }
    }

    public void sortPlayersLineupPosition() {
        Collections.sort(players, new Comparator<Player>() {
            @Override
            public int compare(Player left, Player right) {
                return right.getLineupPosition() < left.getLineupPosition() ?
                        1 : left.getLineupPosition() == right.getLineupPosition() ? 0 : -1;
            }
        });
    }

    private void pickStarterBenchPosition(int position, List<Player> playerList, Object[] playerArr) {
        ArrayList<Player> posPlayers = new ArrayList<>();
        for (Player p : playerList) {
            if (p.getPosition() == position) {
                posPlayers.add(p);
            }
        }

        Collections.sort(posPlayers, new PlayerOverallComp());
        playerArr[position-1] = posPlayers.get(0);
        playerArr[position-1+5] = posPlayers.get(1);
    }

    public void beginNewGame() {
        for (Player player : players) {
            player.gmStats = new Stats();
        }
    }

    public void setOffStrat(Strategy.Strats type) {
        offStrat = new Strategy(type, this);
    }

    public void setDefStrat(Strategy.Strats type) {
        defStrat = new Strategy(type, this);
    }

    public Strategy getOffStrat() {
        return offStrat;
    }

    public Strategy getDefStrat() {
        return defStrat;
    }

    public String getName() { return name; }

    public String getAbbr() { return name.substring(0,3).toUpperCase(); }

    public int getPrestige() {
        return prestige;
    }
    
    public void addPlayer( Player player ) {
        //add player (used by AI)
        if ( players.get( player.getPosition() - 1 ) == null ) {
            // no starter yet
            players.set( player.getPosition() - 1, player );
        } else {
            // put in bench
            players.set( player.getPosition() - 1 + 5, player);
        }

    }

    public int getPosTotals(int position) {
        int count = 0;
        for (Player p : players) {
            if (p.getPosition() == position) count++;
        }
        return count;
    }
    
    public Player getPG() {
        return players.get(0);
    }
    
    public Player getSG() {
        return players.get(1);
    }
    
    public Player getSF() {
        return players.get(2);
    }
    
    public Player getPF() {
        return players.get(3);
    }
    
    public Player getC() {
        return players.get(4);
    }

    public void addTimePlayed( int seconds ) {
        for (int i = 0; i < 5; i++) {
            players.get(i).addTimePlayed(seconds);
        }
    }

    private void subPosition( double remainingTime, int position ) {
        if (startersIn[position] && remainingTime > 120) {
            // See if should be subbed out (starters stay in for last 2 min)
            if (remainingTime + players.get(position).gmStats.secondsPlayed > players.get(position).getLineupMinutes()*60) {
                // More time than needed to play, can sub out
                if (Math.random() < 0.3) {
                    // Sub
                    startersIn[position] = false;
                    Player tmp = players.get(position);
                    players.set(position, players.get(position+5));
                    players.set(position+5, tmp);
                }
            }
        } else {
            // Bench is in
            if (remainingTime + players.get(position+5).gmStats.secondsPlayed < players.get(position).getLineupMinutes()*60
                    || Math.random() < 0.3 || remainingTime <= 120) {
                // Sub starter in
                startersIn[position] = true;
                Player tmp = players.get(position);
                players.set(position, players.get(position+5));
                players.set(position+5, tmp);
            }
        }
    }
    
    void subPlayers( double remainingTime ) {
        // sub players based on game time

        for (int i = 0; i < 5; ++i)
            subPosition(remainingTime, i);

        for (int i = 0; i < players.size(); ++i) {
            players.get(i).onCourt = (i < 5);
        }
    }
    
    public void selectPlayer(List<Player> players) {
        //assumes arraylist is sorted by overall
        for (int p = 0; p < players.size(); ++p) {
            if ( this.players.get(0) != null && this.players.get(1) != null && this.players.get(2) != null
                    && this.players.get(3) != null && this.players.get(4) != null ) {
                // starters all selected, need bench
                if ( this.players.get( players.get(p).getPosition() - 1 + 5 ) == null ) {
                    //dont have bench guy in this position yet
                    Player selectedPlayer = players.get(p);
                    addPlayer(selectedPlayer);
                    players.remove(selectedPlayer);
                    System.out.println(name + " selected " + selectedPlayer.name + " for bench.");
                    return;
                }
            } else {
                if ( this.players.get( players.get(p).getPosition() - 1 ) == null ) {
                    //dont have starter in this position yet
                    Player selectedPlayer = players.get(p);
                    addPlayer(selectedPlayer);
                    players.remove(selectedPlayer);
                    System.out.println(name + " selected " + selectedPlayer.name);
                    return;
                }
            }
        }
        System.out.println(name + " DIDN'T PICK ENOUGH PEOPLE!");
    }

    public String[] getGameSummaryStr(int gameNumber) {
        String[] sum = new String[3];
        Game gm = gameSchedule.get(gameNumber);
        if (gm.tournamentGame) {
            sum[0] = "Tournament";
        } else {
            sum[0] = "Reg. Season";
        }
        if (gm.hasPlayed()) {
            if (gm.getWinner() == this) {
                sum[1] = gm.getWinnerString();
            } else {
                sum[1] = gm.getLoserString();
            }
        } else {
            sum[1] = "---";
        }

        if (gm.getHome() == this) {
            sum[2] = "vs " + gm.getAway().getAbbr();
        } else {
            sum[2] = "@ " + gm.getHome().getAbbr();
        }
        return sum;
    }

    public void beginNewSeason() {
        this.wins = 0;
        this.losses = 0;
        this.seed = 0;
        this.gameSchedule.clear();
    }
}
