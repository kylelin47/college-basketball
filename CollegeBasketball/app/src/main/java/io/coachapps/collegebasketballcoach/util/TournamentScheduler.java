package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.GameDao;

public class TournamentScheduler {
    private static final int MAX_GAMES_BEFORE_MARCH_MADNESS = 31;
    // teams with highest wins are first
    private class MarchMadnessSeeder implements Comparator<Team> {
        @Override
        public int compare(Team left, Team right) {
            int leftWins = getNonMarchMadnessWins(left);
            int rightWins = getNonMarchMadnessWins(right);
            return rightWins - leftWins;
            //return right.pollScore - left.pollScore;
        }
        private int getNonMarchMadnessWins(Team team) {
            int wins = 0;
            for (Game game : team.gameSchedule) {
                if (game.gameType != Game.GameType.MARCH_MADNESS &&
                        game.getWinner() == team) {
                    wins++;
                }
            }
            return wins;
        }
    }

    private class ConferenceTournamentSeeder implements Comparator<Team> {
        @Override
        public int compare(Team left, Team right) {
            int leftWins = getRegularSeasonWins(left);
            int rightWins = getRegularSeasonWins(right);
            return rightWins - leftWins;

        }
        private int getRegularSeasonWins(Team team) {
            int regularSeasonWins = 0;
            for (Game game : team.gameSchedule) {
                if (!(game.gameType.isTournament()) &&
                        game.gameType == Game.GameType.REGULAR_SEASON && game.getWinner() == team) {
                    regularSeasonWins++;
                }
            }
            return regularSeasonWins;
        }
    }

    private Context context;
    public TournamentScheduler(Context context) {
        this.context = context;
    }

    private Game getLastSeasonalGame(List<Team> teams) {
        Team team = teams.get(0);
        for (int i = team.gameSchedule.size() - 1; i >= 0; i--) {
            Game game = team.gameSchedule.get(i);
            if (!(game.gameType.isTournament())) {
                return game;
            }
        }
        return null;
    }

    public List<Game> scheduleConferenceTournament(List<Team> teams) {
        List<Game> games = new ArrayList<>();
        if (teams.size() != 10) {
            Log.e("TournamentScheduler", "Conference passed with not 10 teams");
            teams = teams.subList(0, 10);
        }
        teams = new ArrayList<>(teams);
        Collections.sort(teams, new ConferenceTournamentSeeder());
        for (int i = 0; i < teams.size(); i++) {
            teams.get(i).conferenceSeed = i + 1;
        }
        teams = teams.subList(0, 8);
        GameDao gameDao = new GameDao(context);
        Game lastSeasonalGame = getLastSeasonalGame(teams);
        int year = lastSeasonalGame.getYear();
        int[] order = new int[]{1, 8, 4, 5, 3, 6, 2, 7};
        games.addAll(scheduleTournament(createBrackets(teams, order), year, gameDao, Game.GameType
                .TOURNAMENT_GAME));
        return games;
    }

    public List<Game> scheduleMarchMadness(Map<League.Conference, List<Game>> conferenceGames,
                                           List<Team> teamsInLeague) {
        List<Team> marchMadnessTeams = getMarchMadnessTeams(conferenceGames, teamsInLeague);
        for (int i = 0; i < marchMadnessTeams.size(); i++) {
            marchMadnessTeams.get(i).madnessSeed = i + 1;
        }
        int[] order = new int[] {1, 32, 16, 17, 9, 24, 8, 25, 4, 29, 13, 20, 12, 21, 5, 28, 2,
                31, 15, 18, 10, 23, 7, 26, 3, 30, 14, 19, 11, 22, 6, 27};
        int year = conferenceGames.get(League.Conference.COWBOY).get(0).getYear();
        for (Team team : marchMadnessTeams) {
            while (team.gameSchedule.size() < MAX_GAMES_BEFORE_MARCH_MADNESS) {
                team.gameSchedule.add(null);
            }
        }
        return scheduleTournament(createBrackets(marchMadnessTeams, order), year, Game.GameType
                .MARCH_MADNESS);
    }

    // gets the teams in march madness, in seed order
    private List<Team> getMarchMadnessTeams(Map<League.Conference, List<Game>> conferenceGames,
                                            List<Team> teamsInLeague) {
        List<Team> conferenceWinners = new ArrayList<>();
        for (League.Conference conference : League.Conference.values()) {
            List<Game> games = conferenceGames.get(conference);
            conferenceWinners.add(games.get(games.size() - 1).getWinner());
        }
        Comparator<Team> seeder = new MarchMadnessSeeder();
        Collections.sort(conferenceWinners, seeder);
        Set<Team> marchMadnessTeams = new LinkedHashSet<>(conferenceWinners);
        Collections.sort(teamsInLeague, seeder);
        for (Team team : teamsInLeague) {
            marchMadnessTeams.add(team);
            if (marchMadnessTeams.size() == 32) break;
        }
        ArrayList<Team> listTeams = new ArrayList<>(marchMadnessTeams);
        Collections.sort(listTeams, seeder);
        return listTeams;
    }

    public List<Game> scheduleTournament(List<Team> teams, int year, Game.GameType gameType) {
        return scheduleTournament(teams, year, new GameDao(context), gameType);
    }

    private List<Game> scheduleTournament(List<Team> teams, int year, GameDao gameDao, Game
            .GameType gameType) {
        if (teams.size() <= 1) return new ArrayList<>();
        if (!((teams.size() & (teams.size() - 1)) == 0)) {
            Log.e("scheduleTournament", "Tournaments require power-of-2 number of teams");
            Log.e("scheduleTournament", "Tournament has " + teams.size() + " entrants");
            teams = teams.subList(0, 8);
        }
        List<Game> games = new ArrayList<>(teams.size()/2);
        for (int i = 0; i < teams.size(); i+=2) {
            Team home = teams.get(i);
            Team away = teams.get(i+1);
            games.add(LeagueEvents.scheduleGame(home, away, year, gameDao, gameType));
        }
        if (allGamesPlayed(games)) {
            for (int i = 0; i < teams.size()/2; i++) {
                teams.set(i, games.get(i).getWinner());
            }
            games.addAll(scheduleTournament(teams.subList(0, teams.size()/2), year, gameDao,
                    gameType));
        }
        return games;
    }

    private boolean allGamesPlayed(List<Game> games) {
        for (Game game : games) {
            if (!game.hasPlayed()) return false;
        }
        return true;
    }

    /**
     * @param teams seeded list of teams
     * @param order top-down order of games for first round
     * @return bracketed teams
     */
    private List<Team> createBrackets(List<Team> teams, int[] order) {
        List<Team> bracketedTeams = new ArrayList<>(teams.size());
        for (int seed : order) {
            bracketedTeams.add(teams.get(seed - 1));
        }
        return bracketedTeams;
    }
}
