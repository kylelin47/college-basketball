package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;
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
import io.coachapps.collegebasketballcoach.models.TeamStats;

/**
 * League Events utility class. Will be used to perform various league activities,
 * such as scheduling games, picking All-Americans, etc.
 * Created by Achi Jones on 10/22/2016.
 */

public class LeagueEvents {
    //bit of a lie
    private static final List<Integer> OUT_OF_CONFERENCE_WEEKS = Arrays.asList(3, 5, 7, 9, 11, 13,
            15, 17, 19, 21);

    public static void scheduleSeason(League league, Context context, int year) {
        GameDao gameDao = new GameDao(context);
        int seed = league.getPlayerTeam().getName().hashCode() + year;
        Random random = new Random(seed);
        for (League.Conference conference : league.getConferences()) {
            List<Team> shuffledTeams = new ArrayList<>(league.getConference(conference));
            Collections.shuffle(shuffledTeams, random);
            halfRobinScheduling(shuffledTeams, year, gameDao, false);
            halfRobinScheduling(shuffledTeams, year, gameDao, true);
            for (Team team : shuffledTeams) {
                Collections.shuffle(team.gameSchedule, new Random(seed));
            }
            for (Team team : shuffledTeams) {
                for (Game game : team.gameSchedule) {
                    if (!game.hasPlayed()) {
                        GameModel gameModel = gameDao.getGame(year, game.getWeek(), game.getHome().getName(),
                                game.getAway().getName());
                        if (gameModel == null) break;
                        game.apply(gameModel);
                    }
                }
            }
        }
        scheduleOutOfConference(league, year, gameDao, random);
    }

    private static void scheduleOutOfConference(League league, int year, GameDao gameDao, Random
            random) {
        List<League.Conference> conferences = league.getConferences();
        List<List<Team>> shuffledTeams = new ArrayList<>(conferences.size());
        for (League.Conference conference : conferences) {
            List<Team> teams = new ArrayList<>(league.getConference(conference));
            Collections.shuffle(teams, random);
            shuffledTeams.add(teams);
        }
        int robinRounds = conferences.size() - 1;
        int halfRobin = conferences.size()/2;
        int week = 0;
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                List<Team> conferenceA = shuffledTeams.get((r + g) % robinRounds);
                List<Team> conferenceB;
                if ( g == 0 ) {
                    conferenceB = shuffledTeams.get(robinRounds);
                } else {
                    conferenceB = shuffledTeams.get((robinRounds - g + r) % robinRounds);
                }
                scheduleOutOfConference(conferenceA, conferenceB, year, gameDao, week);
            }
            week+=2;
        }
    }

    private static void scheduleOutOfConference(List<Team> conferenceA, List<Team> conferenceB,
                                                int year, GameDao gameDao, int weekIndex) {
        boolean home = true;
        for (int i = 0; i < conferenceA.size(); i++) {
            for (int j = 0; j < 2; j++) {
                Team otherTeam = conferenceB.get(i - i % 2 + j);
                Team homeTeam = home ? conferenceA.get(i) : otherTeam;
                Team awayTeam = home ? otherTeam : conferenceA.get(i);
                home = !home;
                int week;
                if (i % 2 == 1) {
                    week = OUT_OF_CONFERENCE_WEEKS.get(weekIndex + ((j + 1) % 2));
                } else {
                    week = OUT_OF_CONFERENCE_WEEKS.get(weekIndex + j);
                }
                scheduleGame(homeTeam, awayTeam, year, gameDao, Game.GameType.OUT_OF_CONFERENCE,
                        week, true);
            }
        }
    }

    private static void halfRobinScheduling(List<Team> teams, int year, GameDao gameDao, boolean
            swapHomeAndAway) {
        int robinRounds = teams.size() - 1;
        int halfRobin = teams.size()/2;
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                Team home = teams.get((r + g) % robinRounds);
                Team away;
                if ( g == 0 ) {
                    away = teams.get(robinRounds);
                } else {
                    away = teams.get((robinRounds - g + r) % robinRounds);
                }
                if (swapHomeAndAway) {
                    Team temp = home;
                    home = away;
                    away = temp;
                }
                scheduleGame(home, away, year, gameDao, Game.GameType.REGULAR_SEASON, false);
            }
        }
    }

    static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType, boolean retrievePast) {
        return scheduleGame(home, away, year, gameDao, gameType, home.gameSchedule.size(), retrievePast);
    }

    private static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType, int week, boolean retrievePast) {
        GameModel gameModel = null;
        if (retrievePast && (home.gameSchedule.size() == 0 || home.gameSchedule.get(week - 1)
                .hasPlayed())) {
            gameModel = gameDao.getGame(year, week, home.getName(), away.getName());
        }
        Game gameToSchedule = new Game(home, away, year);
        gameToSchedule.apply(gameModel);
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
