package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.models.Stats;

/**
 *
 * @author Achi Jones
 */
public class Team {
    public List<Player> players;
    public int wins;
    public int losses;

    public List<Game> gameSchedule;

    public int games;
    public int pointsFor;
    public int pointsAga;
    public String name;
    public boolean[] startersIn;

    public volatile Strategy offStrat;
    public volatile Strategy defStrat;

    public Team( String name, List<Player> players ) {
        this.players = players;
        this.name = name;
        wins = 0;
        losses = 0;
        games = 0;
        gameSchedule = new ArrayList<>();
        startersIn = new boolean[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = true;
        }

        setOffStrat(Strategy.Strats.DRIBBLE_DRIVE);
        setDefStrat(Strategy.Strats.MAN_TO_MAN);
    }
    
    public Team( String name, PlayerGen gen ) {
        this.name = name;
        wins = 0;
        losses = 0;
        games = 0;
        gameSchedule = new ArrayList<>();
        players = new ArrayList<>(10);

        // Set so all the starters are in for now
        startersIn = new boolean[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = true;
        }

        // Make the players
        for (int i = 0; i < 5; ++i) {
            Player a = gen.genPlayer(i+1);
            Player b = gen.genPlayer(i+1);
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
            if (remainingTime + players.get(position).gmStats.secondsPlayed > players.get(position).getPlayingTime()*60) {
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
            if (remainingTime + players.get(position+5).gmStats.secondsPlayed < players.get(position).getPlayingTime()*60
                    || Math.random() < 0.3 || remainingTime <= 120) {
                // Sub starter in
                startersIn[position] = true;
                Player tmp = players.get(position);
                players.set(position, players.get(position+5));
                players.set(position+5, tmp);
            }
        }
    }
    
    public void subPlayers( double remainingTime ) {
        // sub players based on game time

        for (int i = 0; i < 5; ++i)
            subPosition(remainingTime, i);

        for (int i = 0; i < players.size(); ++i) {
            if (i < 5) players.get(i).onCourt = true;
            else players.get(i).onCourt = false;
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
        sum[0] = "Game";
        Game gm = gameSchedule.get(gameNumber);

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
}
