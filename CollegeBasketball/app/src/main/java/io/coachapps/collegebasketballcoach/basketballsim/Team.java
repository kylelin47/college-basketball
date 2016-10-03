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
    int[] startersIn;
    int[] benchIn;

    public Team( String name, List<Player> players ) {
        this.players = players;
        this.name = name;
        wins = 0;
        losses = 0;
        games = 0;
        startersIn = new int[5];
        benchIn = new int[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = 1;
            benchIn[i] = 0;
        }
    }
    
    public Team( String name, PlayerGen gen ) {
        this.name = name;
        wins = 0;
        losses = 0;
        games = 0;
        players = new ArrayList<>(10);

        // Set so all the starters are in for now
        startersIn = new int[5];
        benchIn = new int[5];
        for (int i = 0; i < 5; ++i) {
            startersIn[i] = 1;
            benchIn[i] = 0;
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
    
    public void subPlayers( double time ) {
        // sub players based on game time
        time = time/60;
        //PG
        if ( startersIn[0] == 1 && benchIn[0] == 0 && time >= (double)getPG().getPlayingTime()/2 && time < 47 - (double)getPG().getPlayingTime()/2 ) {
            //sub out starting PG
            Player tmp = players.get(0);
            players.set(0, players.get(5));
            players.set(5, tmp);
            startersIn[0] = 0;
            benchIn[0] = 1;
        } else if ( startersIn[0] == 0 && benchIn[0] == 1 && time >= 48 - (double)getPG().getPlayingTime()/2 ) {
            //sub in starting PG
            Player tmp = players.get(0);
            players.set(0, players.get(5));
            players.set(5, tmp);
            startersIn[0] = 1;
            benchIn[0] = 0;
        }
        //SG
        if ( startersIn[1] == 1 && benchIn[1] == 0 && time >= (double)getSG().getPlayingTime()/2 && time < 47 - (double)getSG().getPlayingTime()/2 ) {
            //sub out starting SG
            Player tmp = players.get(1);
            players.set(1, players.get(6));
            players.set(6, tmp);
            startersIn[1] = 0;
            benchIn[1] = 1;
        } else if ( startersIn[1] == 0 && benchIn[1] == 1 && time >= 48 - (double)getSG().getPlayingTime()/2 ) {
            //sub in starting SG
            Player tmp = players.get(1);
            players.set(1, players.get(6));
            players.set(6, tmp);
            startersIn[1] = 1;
            benchIn[1] = 0;
        }
        //SF
        if ( startersIn[2] == 1 && benchIn[2] == 0 && time >= (double)getSF().getPlayingTime()/2 && time < 47 - (double)getSF().getPlayingTime()/2 ) {
            //sub out starting SF
            Player tmp = players.get(2);
            players.set(2, players.get(7));
            players.set(7, tmp);
            startersIn[2] = 0;
            benchIn[2] = 1;
        } else if ( startersIn[2] == 0 && benchIn[2] == 1 && time >= 48 - (double)getSF().getPlayingTime()/2 ) {
            //sub in starting SF
            Player tmp = players.get(2);
            players.set(2, players.get(7));
            players.set(7, tmp);
            startersIn[2] = 1;
            benchIn[2] = 0;
        }
        //PF
        if ( startersIn[3] == 1 && benchIn[3] == 0 && time >= (double)getPF().getPlayingTime()/2 && time < 47 - (double)getPF().getPlayingTime()/2 ) {
            //sub out starting PF
            Player tmp = players.get(3);
            players.set(3, players.get(8));
            players.set(8, tmp);
            startersIn[3] = 0;
            benchIn[3] = 1;
        } else if ( startersIn[3] == 0 && benchIn[3] == 1 && time >= 48 - (double)getPF().getPlayingTime()/2 ) {
            //sub in starting PF
            Player tmp = players.get(3);
            players.set(3, players.get(8));
            players.set(8, tmp);
            startersIn[3] = 1;
            benchIn[3] = 0;
        }
        //C
        if ( startersIn[4] == 1 && benchIn[4] == 0 && time >= (double)getC().getPlayingTime()/2 && time < 47 - (double)getC().getPlayingTime()/2 ) {
            //sub out starting C
            Player tmp = players.get(4);
            players.set(4, players.get(9));
            players.set(9, tmp);
            startersIn[4] = 0;
            benchIn[4] = 1;
        } else if ( startersIn[4] == 0 && benchIn[4] == 1 && time >= 48 - (double)getPF().getPlayingTime()/2 ) {
            //sub in starting C
            Player tmp = players.get(4);
            players.set(4, players.get(9));
            players.set(9, tmp);
            startersIn[4] = 1;
            benchIn[4] = 0;
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
