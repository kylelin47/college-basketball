package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
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
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
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
    //bit of a lie
    private static final List<Integer> OUT_OF_CONFERENCE_WEEKS = Arrays.asList(3, 5, 7, 9, 11, 13,
            15, 17, 19, 21);

    public static void scheduleSeason(League league, Context context, int year) {
        int seed = league.getPlayerTeam().getName().hashCode() + year;
        Random random = new Random(seed);
        for (League.Conference conference : league.getConferences()) {
            List<Team> shuffledTeams = new ArrayList<>(league.getConference(conference));
            Collections.shuffle(shuffledTeams, random);
            halfRobinScheduling(shuffledTeams, year, false);
            halfRobinScheduling(shuffledTeams, year, true);
            for (Team team : shuffledTeams) {
                Collections.shuffle(team.gameSchedule, new Random(seed));
            }
        }
        scheduleOutOfConference(league, year, random);
        Set<Game> visited = new HashSet<> ();
        GameDao gameDao = new GameDao(context);
        for (League.Conference conference : league.getConferences()) {
            for (Team team : league.getConference(conference)) {
                for (Game game : team.gameSchedule) {
                    if (!game.hasPlayed() && !visited.contains(game)) {
                        visited.add(game);
                        GameModel gameModel = gameDao.getGame(year, game.getWeek(), game.getHome().getName(),
                                game.getAway().getName());
                        if (gameModel == null) break;
                        game.apply(gameModel);
                    }
                }
            }
        }
    }

    private static void scheduleOutOfConference(League league, int year, Random
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
        List<Game> games = new ArrayList<>();
        for (int r = 0; r < robinRounds; ++r) {
            for (int g = 0; g < halfRobin; ++g) {
                List<Team> conferenceA = shuffledTeams.get((r + g) % robinRounds);
                List<Team> conferenceB;
                if ( g == 0 ) {
                    conferenceB = shuffledTeams.get(robinRounds);
                } else {
                    conferenceB = shuffledTeams.get((robinRounds - g + r) % robinRounds);
                }
                scheduleOutOfConferenceGames(conferenceA, conferenceB, year, week, games, random);
            }
            week+=2;
        }
        for (Game game : games) {
            game.reschedule();
        }
    }

    private static void scheduleOutOfConferenceGames(List<Team> conferenceA, List<Team>
            conferenceB, int year, int weekIndex, List<Game> games, Random random) {
        for (int i = 0; i < conferenceA.size(); i++) {
            Team teamA = conferenceA.get(i);
            for (int j = 0; j < 2; j++) {
                Team teamB = conferenceB.get(i - (i % 2) + j);
                int week;
                if (i % 2 == 1) {
                    week = OUT_OF_CONFERENCE_WEEKS.get(weekIndex + ((j + 1) % 2));
                } else {
                    week = OUT_OF_CONFERENCE_WEEKS.get(weekIndex + j);
                }
                if (random.nextBoolean()) {
                    games.add(scheduleGame(teamA, teamB, year, null, Game.GameType
                            .OUT_OF_CONFERENCE, week));
                } else {
                    games.add(scheduleGame(teamB, teamA, year, null, Game.GameType
                            .OUT_OF_CONFERENCE, week));
                }

            }
        }
    }

    private static void halfRobinScheduling(List<Team> teams, int year, boolean
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
                scheduleGame(home, away, year, null, Game.GameType.REGULAR_SEASON);
            }
        }
    }

    static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType) {
        return scheduleGame(home, away, year, gameDao, gameType, home.gameSchedule.size());
    }

    // Set GameDao to null if you don't want to try and retrieve the past
    private static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType, int week) {
        GameModel gameModel = null;
        if (gameDao != null && (home.gameSchedule.size() == 0 || home.gameSchedule.get(week - 1)
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

    public static boolean tryToFinishTournament(List<Game> tournamentGames,
                                                Context context, League league) {
        if (tournamentGames == null || tournamentGames.size() <= 4) {
            return false;
        }
        Game championshipGame = tournamentGames.get(tournamentGames.size() - 1);
        Game previousGame = tournamentGames.get(tournamentGames.size() - 2);
        if (championshipGame.hasPlayed() && championshipGame.getWeek() > previousGame.getWeek()) {
            List<ThreeAwardTeams> awardTeams = getAllAwardTeams(context, league, championshipGame.getYear());
            LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(context);
            leagueResultsEntryDao.save(championshipGame.getYear(),
                    championshipGame.getWinner().name, 0, 0, awardTeams);
            return true;
        }
        return false;
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
                if (!game.gameType.isTournament() && !game.hasPlayed() && game.getWeek() <
                        minWeek) {
                    minWeek = game.getWeek();
                }
            }
        }
        return minWeek;
    }

    public static List<ThreeAwardTeams> getAllAwardTeams(Context context, League league, int year) {
        YearlyPlayerStatsDao yps = new YearlyPlayerStatsDao(context);
        List<YearlyPlayerStats> playerStatsList = yps.getAllYearlyPlayerStats(year);
        Log.i("LeagueEvents", "playerStatsList size = " + playerStatsList.size());

        // Sort all stats by points+assists+rebounds
        Collections.sort(playerStatsList, new Comparator<YearlyPlayerStats>() {
            @Override
            public int compare(YearlyPlayerStats a, YearlyPlayerStats b) {
                return (b.playerStats.points + b.playerStats.defensiveRebounds +
                        b.playerStats.offensiveRebounds + b.playerStats.assists) -
                           (a.playerStats.points + a.playerStats.defensiveRebounds +
                            a.playerStats.offensiveRebounds + a.playerStats.assists);
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
        HashMap<Integer, Integer> idPositionMap = new HashMap<>();
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
            if (idPositionMap.containsKey(pStats.playerId)) {
                int position = idPositionMap.get(pStats.playerId);
                for (AwardTeamModel team : awardTeamList) {
                    if (team.getIdPosition(position) == 0) {
                        team.setIdPosition(position, pStats.playerId);
                        break;
                    }
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
}
