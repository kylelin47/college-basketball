package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.FullGameResults;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;

/**
 * League Events utility class. Will be used to perform various league activities,
 * such as scheduling games, picking All-Americans, etc.
 * Created by Achi Jones on 10/22/2016.
 */

public class LeagueEvents {
    public static void scheduleSeason(List<Team> teams, Context context, int year) {
        List<Team> shuffledTeams = new ArrayList<>(teams);
        Collections.shuffle(shuffledTeams, new Random(year));
        int robinRounds = shuffledTeams.size() - 1;
        int halfRobin = shuffledTeams.size()/2;
        GameDao gameDao = new GameDao(context);
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                Team home = shuffledTeams.get((r + g) % robinRounds);
                Team away;
                if ( g == 0 ) {
                    away = shuffledTeams.get(robinRounds);
                } else {
                    away = shuffledTeams.get((robinRounds - g + r) % robinRounds);
                }
               scheduleGame(home, away, year, gameDao, Game.GameType.REGULAR_SEASON);
            }
        }
    }

    static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType gameType) {
        int week = home.gameSchedule.size();
        GameModel gameModel = null;
        if (home.gameSchedule.size() == 0 || home.gameSchedule.get(week - 1).hasPlayed()) {
            gameModel = gameDao.getGame(year, week, home.getName(), away.getName());
        }
        Game gameToSchedule;
        if (gameModel == null) {
            gameToSchedule = new Game(home, away, year);
        } else {
            gameToSchedule = new Game(home, away, gameModel);
        }
        gameToSchedule.schedule(week, gameType);
        return gameToSchedule;
    }

    public static List<Game> scheduleConferenceTournament(List<Team> teams, Context context) {
        TournamentScheduler tournamentScheduler = new TournamentScheduler(context);
        return tournamentScheduler.scheduleConferenceTournament(teams);
    }

    public static void playTournamentRound(List<Game> tournamentGames, Simulator sim, boolean
            simUserGame, String userTeamName) {
        if (tournamentGames == null) return;
        List<Team> winners = playGames(tournamentGames, sim, simUserGame, userTeamName);
        TournamentScheduler tournamentScheduler = new TournamentScheduler(sim.context);
        Game lastGame = tournamentGames.get(tournamentGames.size() - 1);
        if (simUserGame) {
            tournamentGames.addAll(
                    tournamentScheduler.scheduleTournament(winners, lastGame.getYear()));
        }
    }

    public static boolean tryToFinishTournament(List<Game> tournamentGames, Context context) {
        if (tournamentGames == null || tournamentGames.size() <= 4) {
            return false;
        }
        Game championshipGame = tournamentGames.get(tournamentGames.size() - 1);
        Game previousGame = tournamentGames.get(tournamentGames.size() - 2);
        if (championshipGame.hasPlayed() && championshipGame.getWeek() > previousGame.getWeek()) {
            LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(context);
            leagueResultsEntryDao.save(championshipGame.getYear(), championshipGame.getWinner().name,
                    0, 0);
            return true;
        }
        return false;
    }

    public static boolean playRegularSeasonGame(List<Team> teams, Simulator sim,
                                                boolean simUserGame, String userTeamName) {
        int week = determineLastUnplayedRegularSeasonWeek(teams);
        Log.i("LeagueEvents","Week = " + week);
        List<Game> games = new ArrayList<>();
        if (week == Integer.MAX_VALUE) return false;
        for (Team t : teams) {
            games.add(t.gameSchedule.get(week));
        }
        playGames(games, sim, simUserGame, userTeamName);
        return true;
    }

    // returns the winners
    private static List<Team> playGames(List<Game> games, Simulator sim, boolean simUserGame,
                                        String userTeamName) {
        List<BoxScore> boxScores = new ArrayList<>();
        List<GameModel> gamesToSave = new ArrayList<>();
        List<Team> winners = new ArrayList<>();
        for (Game game : games) {
            if (!game.hasPlayed() && (simUserGame ||
                    (!game.getAway().getName().equals(userTeamName) &&
                            !game.getHome().getName().equals(userTeamName)))) {
                FullGameResults fgr = game.playGame(sim);
                boxScores.addAll(fgr.boxScores);
                gamesToSave.add(fgr.game);
                winners.add(game.getWinner());
            }
        }
        saveGameResults(boxScores, gamesToSave, sim.context);
        return winners;
    }

    private static void saveGameResults(List<BoxScore> boxScores, List<GameModel> games, Context
            context) {
        GameDao gd = new GameDao(context);
        gd.save(games);
        BoxScoreDao bsd = new BoxScoreDao(context);
        bsd.save(boxScores);
    }

    public static FullGameResults getGameResult(Team home, Team away, int year, int week) {
        List<BoxScore> boxScores = new ArrayList<>();
        for (Player p : home.players) {
            boxScores.add(p.getGameBoxScore(year, week, home.getName()));
        }
        for (Player p : away.players) {
            boxScores.add(p.getGameBoxScore(year, week, away.getName()));
        }

        home.resetLineup();
        away.resetLineup();

        Stats homeStats = new Stats();
        for (Player player : home.players) {
            homeStats.add(player.gmStats);
        }
        Stats awayStats = new Stats();
        for (Player player : away.players) {
            awayStats.add(player.gmStats);
        }
        GameModel gameResult = new GameModel(home.name, away.name, year, week, homeStats,
                awayStats);
        return new FullGameResults(boxScores, gameResult);
    }

    public static GameModel saveGameResult(Context context, Team home, Team away, int year, int
            week) {
        FullGameResults fgr = getGameResult(home, away, year, week);
        saveGameResults(fgr.boxScores, Arrays.asList(fgr.game), context);
        return fgr.game;
    }
    public static int determineLastUnplayedRegularSeasonWeek(List<Team> teams) {
        int minWeek = Integer.MAX_VALUE;
        for (Team t : teams) {
            for (Game game : t.gameSchedule) {
                if (game.gameType == Game.GameType.REGULAR_SEASON && !game.hasPlayed() && game.getWeek() < minWeek) {
                    minWeek = game.getWeek();
                }
            }
        }
        return minWeek;
    }
}
