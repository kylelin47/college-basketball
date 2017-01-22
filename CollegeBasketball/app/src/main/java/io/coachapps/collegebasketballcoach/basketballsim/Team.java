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
    public int conferenceSeed;
    public int madnessSeed;
    public String conference;

    public List<Game> gameSchedule;

    public String name;
    private boolean isUserTeam;
    public int oldPrestige = 0;
    public int prestige;
    public int pollScore;
    public int pollRank;
    public int tourneySeed = 0;
    public int ovrTalent;
    public boolean[] startersIn;

    public volatile Strategy offStrat;
    public volatile Strategy defStrat;

    private static final int PRO_OVERALL = 90;
    private static final double PRO_CHANCE_USER = 0.4;
    private static final double PRO_CHANCE_CPU = 0.2;

    public Team( String name, List<Player> players, int prestige, boolean isUserTeam, String conference ) {
        this.players = players;
        this.name = name;
        this.prestige = prestige;
        this.isUserTeam = isUserTeam;
        this.conference = conference;
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
    
    public Team(String name, int prestige, PlayerGen gen, boolean isUserTeam, String conference) {
        this.name = name;
        this.prestige = prestige;
        this.isUserTeam = isUserTeam;
        this.conference = conference;
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

    public String getRankNameWLStr() {
        if (tourneySeed == 0) {
            return "#" + pollRank + " " + name + " (" + wins + "-" + losses + ")";
        } else {
            return  "[" + tourneySeed + "] " + name + " (" + wins + "-" + losses + ")";
        }
    }

    public String getPollRankNameWLStr() {
        return "#" + pollRank + " " + name + " (" + wins + "-" + losses + ")";
    }

    public String getNameWLStr() {
        return name + " (" + wins + "-" + losses + ")";
    }

    /**
     * Resets the lineup for the team.
     * Makes sure that a PG SG SF PF C lineup is in place, with all the best players in place.
     */
    public void resetLineup() {
        ovrTalent = calculateTalent();
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
            for (int i = 0; i < players.size(); ++i) {
                players.get(i).setLineupPosition(i);
                if (i < 5) {
                    backUpMinutes[i] = 40 - players.get(i).getPlayingTime();
                    players.get(i).setLineupMinutes(players.get(i).getPlayingTime());
                } else if (i < 10) {
                    players.get(i).setLineupMinutes(backUpMinutes[i-5]);
                } else {
                    players.get(i).setLineupMinutes(0);
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
        for (int i = 2; i < posPlayers.size(); ++i) {
            for (int j = 10; j < playerArr.length; ++j) {
                if (playerArr[j] == null) {
                    playerArr[j] = posPlayers.get(i);
                    break;
                }
            }
        }
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

    public int getPrestigeDiff() {
        if (isPlayer()) {
            return (3 * (wins+5) - prestige) / 3;
        } else {
            return (3 * wins - prestige) / 3;
        }
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

    public boolean isPlayer() {
        return isUserTeam;
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

    public Player getHighestUsagePlayerOnCourt() {
        int maxUsage = players.get(0).getUsage();
        Player maxUsagePlayer = players.get(0);
        for (int i = 1; i < 5; ++i) {
            if (players.get(i).getUsage() > maxUsage) {
                maxUsage = players.get(i).getUsage();
                maxUsagePlayer = players.get(i);
            }
        }
        return maxUsagePlayer;
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

    public String[] getGameSummaryStr(Game game) {
        String[] sum = new String[3];
        sum[0] = game.gameType.toString();
        if (game.hasPlayed()) {
            if (game.getWinner() == this) {
                sum[1] = game.getWinnerString() + " " + game.getOTString();
            } else {
                sum[1] = game.getLoserString() + " " + game.getOTString();
            }
        } else {
            sum[1] = "---";
        }

        if (game.getHome() == this) {
            if (game.gameType == Game.GameType.MARCH_MADNESS) {
                sum[2] = "vs [" + game.getAway().tourneySeed + "] " + game.getAway().getAbbr();
            } else {
                sum[2] = "vs #" + game.getAway().pollRank + " " + game.getAway().getAbbr();
            }
        } else {
            if (game.gameType == Game.GameType.MARCH_MADNESS) {
                sum[2] = "vs [" + game.getHome().tourneySeed + "] " + game.getHome().getAbbr();
            } else {
                sum[2] = "vs #" + game.getHome().pollRank + " " + game.getHome().getAbbr();
            }
        }
        return sum;
    }

    public String[] getGameSummaryFullStr(Game game) {
        String[] sum = new String[3];
        sum[0] = game.gameType.toString();
        if (game.hasPlayed()) {
            if (game.getWinner() == this) {
                sum[1] = game.getWinnerString();
            } else {
                sum[1] = game.getLoserString();
            }
        } else {
            sum[1] = "---";
        }

        if (game.getHome() == this) {
            sum[2] = "vs " + game.getAway().getNameWLStr();
        } else {
            sum[2] = "@ " + game.getHome().getNameWLStr();
        }
        return sum;
    }

    public int getNumGamesPlayed() {
        int num = 0;
        for (Game g : gameSchedule) {
            if (g == null) break;
            if (g.hasPlayed()) num++;
        }
        return num;
    }

    public int getNumMarchMadnessGamesWon() {
        int marchMadnessGamesWon = 0;
        for (Game g : gameSchedule) {
            if (g != null && g.gameType == Game.GameType.MARCH_MADNESS && g.getWinner() == this) {
                marchMadnessGamesWon++;
            }
        }
        return marchMadnessGamesWon;
    }

    public String getLastGameSummary() {
        Game lastGame = null;
        for (Game g : gameSchedule) {
            if (g.hasPlayed()) lastGame = g;
            else break;
        }
        String[] summary = getGameSummaryFullStr(lastGame);
        return summary[1] + " " + summary[2];
    }

    public int calculateTalent() {
        int talent = 0;
        for(int i = 0; i < Math.min(10, players.size()); ++i) {
            talent += (int)(Math.pow(players.get(i).getOverall(),1.25));
        }

        return talent/(Math.min(10, players.size()));
    }

    public int getOvrTalent() {
        return ovrTalent;
    }

    public int getWeakestPosition() {
        int weakestPosition = 1;
        int totalOverallPos = Integer.MAX_VALUE;
        int currTotal = 0;
        List<Player> currPosPlayers;
        for (int i = 1; i < 6; ++i) {
            currPosPlayers = new ArrayList<>();
            for (Player p : players) {
                if (p.getPosition() == i) currPosPlayers.add(p);
            }
            if (currPosPlayers.size() == 0) return i;
            Collections.sort(currPosPlayers, new PlayerOverallComp());
            currTotal = currPosPlayers.get(0).getOverall();
            if (currTotal < totalOverallPos) weakestPosition = i;
        }

        return weakestPosition;
    }

    public int getStrongestPosition() {
        int strongestPosition = 1;
        int totalOverallPos = 0;
        int currTotal = 0;
        List<Player> currPosPlayers;
        for (int i = 1; i < 6; ++i) {
            currPosPlayers = new ArrayList<>();
            for (Player p : players) {
                if (p.getPosition() == i) currPosPlayers.add(p);
            }
            if (currPosPlayers.size() == 0) return i;
            Collections.sort(currPosPlayers, new PlayerOverallComp());
            currTotal = currPosPlayers.get(0).getOverall();
            if (currTotal > totalOverallPos) strongestPosition = i;
        }

        return strongestPosition;
    }

    public void removeSeniorsAndAddYear() {
        List<Player> pros = new ArrayList<>();
        List<Player> seniors = new ArrayList<>();
        for (Player p : players) {
            p.year++;
            if (p.year == 5) seniors.add(p);
            else if (p.year > 2 && p.getOverall() >= PRO_OVERALL &&
                    ((isPlayer() && Math.random() < PRO_CHANCE_USER) ||
                            (!isPlayer() && Math.random() < PRO_CHANCE_CPU))) {
                p.year = 6;
                pros.add(p);
            }
        }
        for (Player p : seniors) {
            players.remove(p);
        }
        for (Player p : pros) {
            players.remove(p);
        }
    }

    public List<Player> recruitWalkOns(PlayerGen playerGen) {
        List<Player> walkOns = new ArrayList<>();
        for (int i = 1; i < 6; ++i) {
            while (getPosTotals(i) < 2) {
                int walkOnPrestige = 20;
                if (!isPlayer()) walkOnPrestige = prestige;
                Player p = playerGen.genPlayer(i, walkOnPrestige, 1);
                players.add(p);
                walkOns.add(p);
            }
        }
        return walkOns;
    }

    public boolean hasPlayer(Player p) {
        for (Player player : players) {
            if (player.getId() == p.getId()) return true;
        }
        return false;
    }

    /**
     * Recruits a player from a list of the recruits.
     * Assumes that the recruit list is sorted by overall rating.
     * Checks to see if player is in a position in need, then may recruit him.
     * @param recruits list of players
     * @return the player that was signed (if any)
     */
    public Player recruitPlayerFromList(List<Player> recruits) {
        // Have chance that this team passes on signing anyone
        double prestigeSkipChance = (double)prestige/2 + 0.05;
        if (Math.random() > prestigeSkipChance || players.size() >= 15) return null;

        // See if we don't have 2 players in any position
        boolean hasNeedsFilled = true;
        for (int i = 1; i < 6; ++i) {
            if (getPosTotals(i) < 2) {
                hasNeedsFilled = false;
                break;
            }
        }

        double chanceToSign = 0.25;
        if (!hasNeedsFilled) {
            for (Player p : recruits) {
                if (getPosTotals(p.getPosition()) < 2 && (p.getOverall() < (prestige/2 + 50) || prestige > 80)) {
                    // Need this position
                    if (Math.random() < chanceToSign) {
                        // Choose him
                        return p;
                    }
                }
            }
        } else {
            for (Player p : recruits) {
                if (getStrongestPosition() != p.getPosition() &&
                        getPosTotals(p.getPosition()) < 3 &&
                        (p.getOverall() < (prestige/2 + 50) || prestige > 80)) {
                    // He could be useful
                    if (Math.random() < chanceToSign/2) {
                        // Choose him
                        return p;
                    }
                }
            }
        }

        return null;
    }
}
