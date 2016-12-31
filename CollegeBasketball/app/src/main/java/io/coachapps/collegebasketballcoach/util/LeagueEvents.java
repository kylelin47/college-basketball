package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseIntArray;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.db.TeamDao;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.AwardTeamModel;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.FullGameResults;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.TeamStats;
import io.coachapps.collegebasketballcoach.models.ThreeAwardTeams;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

/**
 * League Events utility class. Will be used to perform various league activities,
 * such as scheduling games, picking All-Americans, etc.
 * Created by Achi Jones on 10/22/2016.
 */

public class LeagueEvents {
    public static void scheduleSeason(League league, Context context, int year) {
        int seed = league.getPlayerTeam().getName().hashCode() + year;
        Random random = new Random(seed);
        RegularSeasonScheduler scheduler = new RegularSeasonScheduler();
        for (League.Conference conference : league.getConferences()) {
            List<Team> shuffledTeams = new ArrayList<>(league.getConference(conference));
            Collections.shuffle(shuffledTeams, random);
            scheduler.halfRobinScheduling(shuffledTeams, year, false);
            scheduler.halfRobinScheduling(shuffledTeams, year, true);
            for (Team team : shuffledTeams) {
                Collections.shuffle(team.gameSchedule, new Random(seed));
            }
        }
        scheduler.scheduleOutOfConference(league, year, random);
        Set<Game> visited = new HashSet<> ();
        GameDao gameDao = new GameDao(context);
        for (Team team : league.getAllTeams()) {
            for (int i = 0; i < team.gameSchedule.size(); i++) {
                Game game = team.gameSchedule.get(i);
                if (i > 0 && !team.gameSchedule.get(i - 1).hasPlayed()) break;
                if (!visited.contains(game)) {
                    visited.add(game);
                    GameModel gameModel = gameDao.getGame(year, i, game.getHome().getName(),
                            game.getAway().getName());
                    if (gameModel == null) break;
                    game.apply(gameModel);
                }
            }
        }
    }

    static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType) {
        return RegularSeasonScheduler.scheduleGame(home, away, year, gameDao, gameType, home
                .gameSchedule.size());
    }

    public static void playTournamentRound(League league, Simulator sim, boolean
            simUserGame, String userTeamName) {
        TournamentScheduler tournamentScheduler = new TournamentScheduler(sim.context);
        League.Conference playerConference = League.Conference.valueOf(league.getPlayerTeam()
                .conference);
        if (league.conferenceTournamentFinished()) {
            List<Game> tournamentGames = league.getMarchMadnessGames();
            List<Team> winners = playGames(tournamentGames, sim, simUserGame, userTeamName);
            Game lastGame = tournamentGames.get(tournamentGames.size() - 1);
            if (simUserGame) {
                tournamentGames.addAll(
                        tournamentScheduler.scheduleTournament(winners, lastGame.getYear(), Game
                                .GameType.MARCH_MADNESS));
            }
        } else {
            for (League.Conference conference : League.Conference.values()) {
                List<Game> tournamentGames = league.getTournamentGames(conference);
                if (tournamentGames == null) return;
                List<Team> winners = playGames(tournamentGames, sim, simUserGame, userTeamName);
                Game lastGame = tournamentGames.get(tournamentGames.size() - 1);
                if (simUserGame || playerConference != conference) {
                    tournamentGames.addAll(
                            tournamentScheduler.scheduleTournament(winners, lastGame.getYear(), Game.GameType.TOURNAMENT_GAME));
                }
            }
        }
    }

    public static boolean tryToFinishSeason(Context context, League league) {
        List<Game> marchMadness = league.getMarchMadnessGames();
        if (marchMadness == null || marchMadness.size() <= 4) {
            return false;
        }
        Game championshipGame = marchMadness.get(marchMadness.size() - 1);
        Game previousGame = marchMadness.get(marchMadness.size() - 2);
        if (championshipGame.hasPlayed() && championshipGame.getWeek() > previousGame.getWeek()) {
            // Assign awards to all the players and teams
            YearlyPlayerStatsDao yps = new YearlyPlayerStatsDao(context);
            List<YearlyPlayerStats> playerStatsList = yps.getAllYearlyPlayerStats(championshipGame.getYear());
            List<ThreeAwardTeams> awardTeams = getAllAwardTeams(playerStatsList, league);
            int mvpID = getMVP(playerStatsList);
            int dpoyID = getDPOY(playerStatsList);
            String[] champs = getChampions(league);
            LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(context);
            leagueResultsEntryDao.save(championshipGame.getYear(),
                    champs, mvpID, dpoyID, awardTeams);

            // Set new prestiges for all the teams and start the recruiting activity
            // 30 wins for 100 prestige, 10 wins for 0 prestige
            SQLiteDatabase db = DbHelper.getInstance(context).getReadableDatabase();
            TeamDao teamDao = new TeamDao(context);
            YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
            db.beginTransaction();
            try {
                for (Team t : league.getAllTeams()) {
                    int diff = t.getPrestigeDiff();
                    if (champs[0].equals(t.getName())) {
                        if (diff < 0) diff = 0;
                        diff += 10;
                    }
                    for (int i = 1; i < 7; ++i) {
                        if (champs[i].equals(t.getName())) diff += 4;
                    }
                    t.prestige += diff;
                    if (t.prestige < 5) t.prestige = 5;
                    if (t.prestige > 95) t.prestige = 95;
                    teamDao.updateTeam(t);
                    yearlyTeamStatsDao.updateSummary(LeagueEvents.getTeamSeasonSummaryStr(t),
                            t.getName(), leagueResultsEntryDao.getCurrentYear()-1);
                }
                db.setTransactionSuccessful();
            } finally {
                db.endTransaction();
            }

            File recruitingFile = new File(context.getFilesDir(), "current_state");
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(recruitingFile), "utf-8"))) {
                writer.write("RECRUITING");
            } catch (Exception e) {
                System.out.println(e.toString());
            }

            return true;
        }
        return false;
    }

    public static String[] getChampions(League league) {
        String[] champions = new String[7];

        List<Game> marchMadness = league.getMarchMadnessGames();
        if (marchMadness == null || marchMadness.size() <= 4) {
            return null;
        }
        Game championshipGame = marchMadness.get(marchMadness.size() - 1);

        champions[0] = championshipGame.getWinner().getName();
        champions[1] = league.getConfChampionshipGame(League.Conference.COWBOY).getWinner().getName();
        champions[2] = league.getConfChampionshipGame(League.Conference.LAKES).getWinner().getName();
        champions[3] = league.getConfChampionshipGame(League.Conference.MOUNTAINS).getWinner().getName();
        champions[4] = league.getConfChampionshipGame(League.Conference.NORTH).getWinner().getName();
        champions[5] = league.getConfChampionshipGame(League.Conference.PACIFIC).getWinner().getName();
        champions[6] = league.getConfChampionshipGame(League.Conference.SOUTH).getWinner().getName();

        return champions;
    }

    public static boolean playRegularSeasonGame(List<Team> teams, Simulator sim,
                                                boolean simUserGame, String userTeamName) {
        int week = determineLastUnplayedRegularSeasonWeek(teams);
        Log.i("LeagueEvents","Week = " + week);
        if (week == Integer.MAX_VALUE) return false;
        List<Game> games = new ArrayList<>();
        for (Team t : teams) {
            games.add(t.gameSchedule.get(week));
        }
        playGames(games, sim, simUserGame, userTeamName);
        return true;
    }

    private static List<Team> playGames(List<Game> games, Simulator sim, boolean simUserGame,
                                        String userTeamName) {
        List<BoxScore> boxScores = new ArrayList<>();
        List<GameModel> gamesToSave = new ArrayList<>();
        List<Team> winners = new ArrayList<>();
        for (Game game : games) {
            if (!game.hasPlayed() && (simUserGame || !game.hasTeam(userTeamName))) {
                FullGameResults fgr = game.playGame(sim);
                boxScores.addAll(fgr.boxScores);
                gamesToSave.add(fgr.game);
                winners.add(game.getWinner());
            }
        }
        saveGameResults(boxScores, gamesToSave, sim.context);
        for (Game gm : games) {
            if (simUserGame || !gm.hasTeam(userTeamName)) {
                gm.getHome().beginNewGame();
                gm.getAway().beginNewGame();
            }
        }
        return winners;
    }

    private static void saveGameResults(List<BoxScore> boxScores, List<GameModel> games, Context
            context) {
        GameDao gd = new GameDao(context);
        gd.save(games);
        BoxScoreDao bsd = new BoxScoreDao(context);
        bsd.save(boxScores);
    }

    public static FullGameResults getGameResult(Team home, Team away, int year, int week, int numOT) {
        List<BoxScore> boxScores = new ArrayList<>();
        for (Player p : home.players) {
            boxScores.add(p.getGameBoxScore(year, week, home.getName()));
        }
        for (Player p : away.players) {
            boxScores.add(p.getGameBoxScore(year, week, away.getName()));
        }

        home.resetLineup();
        away.resetLineup();

        TeamStats homeStats = new TeamStats();
        TeamStats awayStats = new TeamStats();
        for (Player player : home.players) {
            homeStats.add(player.gmStats, true);
            awayStats.add(player.gmStats, false);
        }
        for (Player player : away.players) {
            awayStats.add(player.gmStats, true);
            homeStats.add(player.gmStats, false);
        }
        GameModel gameResult = new GameModel(home.name, away.name,
                year, week, homeStats, awayStats, numOT);
        return new FullGameResults(boxScores, gameResult);
    }

    public static GameModel saveGameResult(Context context, Team home, Team away, int year, int
            week, int numOT) {
        FullGameResults fgr = getGameResult(home, away, year, week, numOT);
        saveGameResults(fgr.boxScores, Collections.singletonList(fgr.game), context);
        return fgr.game;
    }

    public static int determineLastUnplayedRegularSeasonWeek(List<Team> teams) {
        int minWeek = Integer.MAX_VALUE;
        for (Team t : teams) {
            for (Game game : t.gameSchedule) {
                if (game != null && !game.gameType.isTournament() && !game.hasPlayed() && game
                        .getWeek() < minWeek) {
                    minWeek = game.getWeek();
                }
            }
        }
        return minWeek;
    }

    private static int getMVP(List<YearlyPlayerStats> playerStatsList) {
        Collections.sort(playerStatsList, new Comparator<YearlyPlayerStats>() {
            @Override
            public int compare(YearlyPlayerStats a, YearlyPlayerStats b) {
                return b.getMVPScore() - a.getMVPScore();
            }
        });

        return playerStatsList.get(0).playerId;
    }

    private static int getDPOY(List<YearlyPlayerStats> playerStatsList) {
        Collections.sort(playerStatsList, new Comparator<YearlyPlayerStats>() {
            @Override
            public int compare(YearlyPlayerStats a, YearlyPlayerStats b) {
                return b.getDPOYScore() - a.getDPOYScore();
            }
        });

        return playerStatsList.get(0).playerId;
    }

    private static List<ThreeAwardTeams> getAllAwardTeams(List<YearlyPlayerStats> playerStatsList,
                                                         League league) {
        Log.i("LeagueEvents", "playerStatsList size = " + playerStatsList.size());

        // Sort all stats by points+assists+rebounds
        Collections.sort(playerStatsList, new Comparator<YearlyPlayerStats>() {
            @Override
            public int compare(YearlyPlayerStats a, YearlyPlayerStats b) {
                return b.getMVPScore() - a.getMVPScore();
            }
        });

        List<ThreeAwardTeams> threeAwardTeamsList = new ArrayList<>();
        for (int i = 0; i < 7; ++i) {
            threeAwardTeamsList.add(new ThreeAwardTeams());
        }

        // All Americans
        getAwardTeams(playerStatsList, league.getAllTeams(), threeAwardTeamsList, 0);
        // The All Conference Teams
        getAwardTeams(playerStatsList, league.getConference(League.Conference.COWBOY),
                threeAwardTeamsList, 1);
        getAwardTeams(playerStatsList, league.getConference(League.Conference.LAKES),
                threeAwardTeamsList, 2);
        getAwardTeams(playerStatsList, league.getConference(League.Conference.MOUNTAINS),
                threeAwardTeamsList, 3);
        getAwardTeams(playerStatsList, league.getConference(League.Conference.NORTH),
                threeAwardTeamsList, 4);
        getAwardTeams(playerStatsList, league.getConference(League.Conference.PACIFIC),
                threeAwardTeamsList, 5);
        getAwardTeams(playerStatsList, league.getConference(League.Conference.SOUTH),
                threeAwardTeamsList, 6);

        return threeAwardTeamsList;
    }

    private static void getAwardTeams(List<YearlyPlayerStats> playerStatsList,
                                                         List<Team> teams,
                                                         List<ThreeAwardTeams> threeAwardTeamsList,
                                                         int awardTeamPosition) {
        // Need a way to find positions given a player ID
        SparseIntArray idPositionMap = new SparseIntArray();
        for (Team t : teams) {
            for (Player p : t.players) {
                idPositionMap.put(p.getId(), p.getLineupPosition()%5+1);
            }
        }

        // Assign players to the award teams
        List<AwardTeamModel> awardTeamList = new ArrayList<>();
        for (int i = 0; i < 3; ++i) {
            awardTeamList.add(new AwardTeamModel());
        }

        for (YearlyPlayerStats pStats : playerStatsList) {
            // Only add the player to the award team if he is apart of the relevant teams
            int position = idPositionMap.get(pStats.playerId, -1);
            if (position == -1) continue;
            for (AwardTeamModel team : awardTeamList) {
                if (team.getIdPosition(position) == 0) {
                    team.setIdPosition(position, pStats.playerId);
                    break;
                }
            }
        }

        // Transfer award teams to the ThreeTeamAward list
        for (int i = 0; i < 3; ++i) {
            for (int pos = 1; pos < 6; ++pos) {
                threeAwardTeamsList.get(awardTeamPosition).get(i).setIdPosition(
                        pos, awardTeamList.get(i).getIdPosition(pos));
            }
        }
    }

    public static String getTeamSeasonSummaryStr(Team team) {
        // Should be like "Won Conf Championship, Made Sweet Sixteen"
        boolean madeConfTourny = false;
        int numConfWins = 0;
        boolean madeMarchMadness = false;
        int numMarchWins = 0;
        for (Game g : team.gameSchedule) {
            if (g != null) {
                if (g.gameType == Game.GameType.TOURNAMENT_GAME) {
                    madeConfTourny = true;
                    if (g.getWinner().getName().equals(team.getName())) {
                        numConfWins++;
                    }
                } else if (g.gameType == Game.GameType.MARCH_MADNESS) {
                    madeMarchMadness = true;
                    if (g.getWinner().getName().equals(team.getName())) {
                        numMarchWins++;
                    }
                }
            }
        }

        String confSum;
        if (!madeConfTourny) {
            confSum = "Missed Conf Tournament, ";
        } else {
            if (numConfWins == 3) confSum = "Won Conference, ";
            else if (numConfWins == 2) confSum = "Made Conf Finals, ";
            else if (numConfWins == 1) confSum = "Made Conf Semifinals, ";
            else confSum = "Made Conf Tournament, ";
        }

        String marchSum;
        if (!madeMarchMadness) {
            marchSum = "Missed March Madness";
        } else {
            // 32 16 8 4 2 1
            if (numMarchWins == 5) marchSum = "Won National Championship";
            else if (numMarchWins == 4) marchSum = "Made Tournament Final";
            else if (numMarchWins == 3) marchSum = "Made Final Four";
            else if (numMarchWins == 2) marchSum = "Made Elite Eight";
            else if (numMarchWins == 1) marchSum = "Made Sweet Sixteen";
            else marchSum = "Made March Madness";
        }

        return confSum + marchSum;
    }

    public static int getRegularSeasonWins(Team team) {
        int regularSeasonWins = 0;
        for (Game game : team.gameSchedule) {
            if (game != null && game.gameType != null &&
                    !game.gameType.isTournament() &&
                    game.gameType == Game.GameType.REGULAR_SEASON &&
                    game.getWinner() == team) {
                regularSeasonWins++;
            }
        }
        return regularSeasonWins;
    }
}
