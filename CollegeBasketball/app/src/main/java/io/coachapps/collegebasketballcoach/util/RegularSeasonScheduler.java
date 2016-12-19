package io.coachapps.collegebasketballcoach.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.League;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.models.GameModel;

class RegularSeasonScheduler {
    //bit of a lie
    private static final List<Integer> OUT_OF_CONFERENCE_WEEKS = Arrays.asList(0, 1, 2, 3, 4, 9,
            13, 17, 19, 21);
    void scheduleOutOfConference(League league, int year, Random random) {
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

    private void scheduleOutOfConferenceGames(List<Team> conferenceA, List<Team>
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

    void halfRobinScheduling(List<Team> teams, int year, boolean
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
                scheduleGame(home, away, year, null, Game.GameType.REGULAR_SEASON, home
                        .gameSchedule.size());
            }
        }
    }

    // Set GameDao to null if you don't want to try and retrieve the past
    static Game scheduleGame(Team home, Team away, int year, GameDao gameDao, Game.GameType
            gameType, int week) {
        GameModel gameModel = null;
        if (gameDao != null) {
            gameModel = gameDao.getGame(year, week, home.getName(), away.getName());
        }
        Game gameToSchedule = new Game(home, away, year);
        gameToSchedule.apply(gameModel);
        gameToSchedule.schedule(week, gameType);
        return gameToSchedule;
    }

}
