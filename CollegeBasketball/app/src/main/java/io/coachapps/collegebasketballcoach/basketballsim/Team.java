package io.coachapps.collegebasketballcoach.basketballsim;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Achi Jones
 */
public class Team {
    public List<Player> players;
    public int wins;
    public int losses;

    int games;
    int pointsFor;
    int pointsAga;
    String name;
    boolean[] startersIn;

    volatile Strategy offStrat;
    volatile Strategy defStrat;

    public Team( String name, List<Player> players ) {
        this.players = players;
        this.name = name;
        wins = 0;
        losses = 0;
        games = 0;
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
    
    public int getWins82() {
        return (int)( wins * (float)82/games );
    }
    
    public int getLosses82() {
        return 82 - getWins82();
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
    
    //getters for team stats per game
    public double getPPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getPPG();
        }
        return res;
    }
    public double getFGAPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getFGAPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double get3GAPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).get3GAPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double getFGP() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getFGP() * (players.get(p).getFGAPG() / getFGAPG());
        }
        return (double)((int)(res * 10))/10;
    }
    public double get3GP() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).get3GP() * (players.get(p).get3GAPG() / get3GAPG());
        }
        return (double)((int)(res * 10))/10;
    }
    public double getRPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getRPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double getAPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getAPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double getSPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getSPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double getBPG() {
        double res = 0.0;
        for (int p = 0; p < 10; p++) {
            res += players.get(p).getBPG();
        }
        return (double)((int)(res*10))/10;
    }
    public double getOFP() {
        int tot_ofa = 0;
        int tot_ofm = 0;
        for (int p = 0; p < 10; p++) {
            //tot_ofa += playersArray[p].stats_tot[9];
            //tot_ofm += playersArray[p].stats_tot[10];
        }
        System.out.println(name + " " + tot_ofm + "/" + tot_ofa);
        return (double)((int)((float)tot_ofm/tot_ofa * 1000))/10;
    }
    
    public double getPointDiff() {
        double pd = (double)(pointsFor - pointsAga)/games;
        return (double)((int)(pd*10))/10;
    }
    
    public String getPDStr() {
        double pd =  getPointDiff();
        if ( pd >= 0 ) {
            return "+" + pd;
        } else {
            return "" + pd;
        }
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
}
