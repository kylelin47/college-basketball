package io.coachapps.collegebasketballcoach.util;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.GameDao;

class TournamentScheduler {
    private Context context;
    public TournamentScheduler(Context context) {
        this.context = context;
    }
    private int determineYear(List<Team> teams) {
        List<Game> schedule = teams.get(0).gameSchedule;
        return schedule.get(schedule.size() - 1).getYear();
    }

    private Game scheduleTournamentGame(Team home, Team away, int year, int week, GameDao gameDao) {
        Game game = LeagueEvents.scheduleGame(home, away, year, week, gameDao);
        game.tournamentGame = true;
        return game;
    }

    List<Game> scheduleConferenceTournament(List<Team> teams) {
        List<Game> games = new ArrayList<>();
        if (teams.size() != 10) {
            Log.e("TournamentScheduler", "Conference passed with not 10 teams");
            teams = teams.subList(0, 10);
        }
        teams = seedTeams(teams);
        GameDao gameDao = new GameDao(context);
        int year = determineYear(teams);
        List<Game> schedule = teams.get(0).gameSchedule;
        int week = schedule.get(schedule.size() - 1).getWeek() + 1;
        // do play in rounds
        Game sevenSeed = scheduleTournamentGame(teams.get(6), teams.get(9), year, week, gameDao);
        Game eightSeed = scheduleTournamentGame(teams.get(7), teams.get(8), year, week, gameDao);
        games.add(sevenSeed);
        games.add(eightSeed);
        if (eightSeed.hasPlayed() && sevenSeed.hasPlayed()) {
            teams.set(6, sevenSeed.getWinner());
            teams.set(7, eightSeed.getWinner());
            teams = teams.subList(0, 8);
            games.addAll(scheduleTournament(createBrackets(teams), year, week + 1, gameDao));
        }
        return games;
    }
    List<Game> scheduleTournament(List<Team> teams, int year, int firstWeek) {
        return scheduleTournament(teams, year, firstWeek, new GameDao(context));
    }

    private List<Game> scheduleTournament(List<Team> teams, int year, int firstWeek, GameDao
            gameDao) {
        if (!((teams.size() & (teams.size() - 1)) == 0)) {
            Log.e("scheduleTournament", "Tournaments require power-of-2 number of teams");
            Log.e("scheduleTournament", "Tournament has " + teams.size() + " entrants");
            teams = teams.subList(0, 8);
        }
        List<Game> games = new ArrayList<>(teams.size()/2);
        for (int i = 0; i < teams.size(); i+=2) {
            Team home = teams.get(i);
            Team away = teams.get(i+1);
            games.add(scheduleTournamentGame(home, away, year, firstWeek, gameDao));
        }
        if (allGamesPlayed(games)) {
            for (int i = 0; i < teams.size()/2; i++) {
                teams.set(i, games.get(i).getWinner());
            }
            games.addAll(scheduleTournament(teams.subList(0, teams.size()/2), year, firstWeek + 1,
                    gameDao));
        }
        return games;
    }

    private boolean allGamesPlayed(List<Game> games) {
        for (Game game : games) {
            if (!game.hasPlayed()) return false;
        }
        return true;
    }

    private List<Team> seedTeams(List<Team> teams) {
        List<Team> seededTeams = new ArrayList<>(teams);
        Collections.sort(seededTeams, new Comparator<Team>() {
            @Override
            public int compare(Team left, Team right) {
                return right.wins < left.wins ? -1 : left.wins == right.wins ? 0 : 1;

            }
        });
        return seededTeams;
    }
    // given a seeded list of teams (teams.get(n) is seed n+1), rearrange so teams that play
    // against each other are adjacent
    private List<Team> createBrackets(List<Team> teams) {
        List<Team> bracketedTeams = new ArrayList<>(teams.size());
        for (int i = 0; i < teams.size()/2; i++) {
            Team home = teams.get(i);
            Team away = teams.get(teams.size() - 1 - i);
            bracketedTeams.add(home);
            bracketedTeams.add(away);
        }
        return bracketedTeams;
    }
}
