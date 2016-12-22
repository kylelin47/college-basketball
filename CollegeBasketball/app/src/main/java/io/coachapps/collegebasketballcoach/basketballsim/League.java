package io.coachapps.collegebasketballcoach.basketballsim;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;
import io.coachapps.collegebasketballcoach.util.TournamentScheduler;

public class League {
    public enum Conference {
        COWBOY("Cowboy"),
        LAKES("Lakes"),
        MOUNTAINS("Mountains"),
        NORTH("North"),
        PACIFIC("Pacific"),
        SOUTH("South");
        private String value;
        Conference(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return value;
        }
    }
    private static final Random RANDOM = new Random();
    private Map<Conference, List<Team>> league = new HashMap<>();
    private Team playerTeam;
    private List<String> orderedConferenceNames;
    private List<Conference> orderedConferences;
    private List<Team> allTeams;
    private TournamentScheduler tournamentScheduler;
    private Map<Conference, List<Game>> conferenceTournament;
    private List<Game> marchMadness;

    public League(String playerTeamName, Context context, PlayerGen playerGen, int difficulty, int conference) {
        String[] teamNames = context.getResources().getStringArray(R.array.team_names);
        Conference[] conferences = Conference.values();
        for (int i = 0; i < conferences.length; i++) {
            for (int j = i * 10; j < i* 10 + 10; j++) {
                if (teamNames[j].equals(playerTeamName)) {
                    teamNames[j] = "Chef Boyardees";
                }
                addTeam(new Team(teamNames[j], (int) (Math.random() * 100), playerGen, false,
                        conferences[i].name()));
            }
        }
        addPlayerTeam(playerTeamName, playerGen, difficulty, conference);
        sortAll();
    }
    public League(List<Team> allTeams) {
        for (Team team : allTeams) {
            if (team.isPlayer()) playerTeam = team;
            addTeam(team);
        }
        sortAll();
    }

    public List<Game> getMarchMadnessGames() {
        return marchMadness;
    }

    public void scheduleConferenceTournament(Context context) {
        if (conferenceTournament != null) return;
        if (tournamentScheduler == null) tournamentScheduler = new TournamentScheduler(context);
        conferenceTournament = new HashMap<>();
        for (Map.Entry<Conference, List<Team>> teams : league.entrySet()) {
            conferenceTournament.put(teams.getKey(), tournamentScheduler
                    .scheduleConferenceTournament(teams.getValue()));
        }
    }

    public void scheduleMarchMadness(Context context) {
        if (marchMadness != null) return;
        if (tournamentScheduler == null) tournamentScheduler = new TournamentScheduler(context);
        marchMadness = tournamentScheduler.scheduleMarchMadness(conferenceTournament, getAllTeams());
    }

    public boolean conferenceTournamentFinished() {
        if (conferenceTournament == null) return false;
        for (List<Game> conferenceGames : conferenceTournament.values()) {
            if (conferenceGames.size() !=  7 || !conferenceGames.get(conferenceGames.size() - 1).hasPlayed()) {
                return false;
            }
        }
        return true;
    }

    public Game getConfChampionshipGame(Conference conference) {
        List<Game> confGames = conferenceTournament.get(conference);
        return confGames.get(confGames.size() - 1);
    }

    public List<Game> getTournamentGames(Conference conference) {
        if (conferenceTournament == null) return null;
        return conferenceTournament.get(conference);
    }

    public List<Conference> getConferences() {
        if (orderedConferences == null) {
            orderedConferences = new ArrayList<>(Arrays.asList(Conference.values()));
            orderedConferences.remove(Conference.valueOf(playerTeam.conference));
            orderedConferences.add(0, Conference.valueOf(playerTeam.conference));
        }
        return orderedConferences;
    }
    public List<String> getConferenceNames() {
        if (orderedConferenceNames == null) {
            List<Conference> orderedConferences = getConferences();
            orderedConferenceNames = new ArrayList<>();
            for (Conference conference : orderedConferences) {
                orderedConferenceNames.add(conference.toString());
            }
        }
        return orderedConferenceNames;
    }

    public List<Team> getConference(Conference conference) {
        return league.get(conference);
    }

    public Team getPlayerTeam() {
        return playerTeam;
    }

    public Conference getTeamConference(String teamName) {
        Set<Map.Entry<Conference, List<Team>>> entrySet = league.entrySet();
        for (Map.Entry<Conference, List<Team>> entry : entrySet) {
            for (Team team : entry.getValue()) {
                if (team.getName().equals(teamName)) {
                    return entry.getKey();
                }
            }
        }
        Log.e("League", "Could not find team with name " + teamName);
        return null;
    }

    public List<Team> getAllTeams() {
        if (allTeams == null) {
            allTeams = new ArrayList<>();
            for (List<Team> teamsInConference : league.values()) {
                allTeams.addAll(teamsInConference);
            }
        }
        return allTeams;
    }
    public List<Team> getPlayerConference() {
        if (playerTeam == null) return null;
        return league.get(Conference.valueOf(playerTeam.conference));
    }
    private void sortAll() {
        for (List<Team> teams : league.values()) {
            Collections.sort(teams, new Comparator<Team>() {
                @Override
                public int compare(Team team, Team t1) {
                    if (team.name.equals(playerTeam.name)) return -1;
                    if (t1.name.equals(playerTeam.name)) return 1;
                    return team.name.compareTo(t1.name);
                }
            });
        }
    }
    private void addPlayerTeam(String newTeamName, PlayerGen playerGen, int difficulty, int conference) {
        Conference[] conferences = Conference.values();
        Conference choice = conferences[conference];
        // difficulty 0 = ez, 1 = normal, 2 = hard
        playerTeam = new Team(newTeamName, 20 + 30*(2 - difficulty), playerGen, true, choice.name());
        List<Team> teamsInConference = league.get(choice);
        teamsInConference.set(RANDOM.nextInt(teamsInConference.size()), playerTeam);
    }

    private void addTeam(Team team) {
        Conference conference = Conference.valueOf(team.conference);
        if (league.containsKey(conference)) {
            league.get(conference).add(team);
        } else {
            List<Team> newList = new ArrayList<>();
            newList.add(team);
            league.put(conference, newList);
        }
    }

    public Team getTeam(String teamName) {
        for (Team t : getAllTeams()) {
            if (t.getName().equals(teamName)) return t;
        }
        Log.e("League", "Could not find team with that name!");
        return null;
    }

    public int getPrestigeRank(String teamName) {
        Team t = getTeam(teamName);
        if (t != null) {
            List<Team> sortedTeamList = new ArrayList<>();
            sortedTeamList.addAll(getAllTeams());
            Collections.sort(sortedTeamList, new Comparator<Team>() {
                @Override
                public int compare(Team a, Team b) {
                    return b.prestige - a.prestige;
                }
            });
            for (int i = 0; i < sortedTeamList.size(); ++i) {
                if (sortedTeamList.get(i).getName().equals(teamName)) return i+1;
            }
        }
        return -1;
    }

    private void assignPollRanks(List<YearlyTeamStats> currentTeamStats) {
        HashMap<String, Team> nameMap = new HashMap<>();
        List<Team> sortedTeamList = new ArrayList<>();
        for (Team t : getAllTeams()) {
            nameMap.put(t.getName(), t);
            sortedTeamList.add(t);
        }

        int WIN_WEIGHT = 200;
        int DIFF_WEIGHT = 5;
        int TALENT_WEIGHT = 20;
        int PRESTIGE_WEIGHT = 10;
        if (currentTeamStats.isEmpty()) {
            for (Team t : getAllTeams()) {
                t.pollScore = t.getTalent() * TALENT_WEIGHT + t.prestige * PRESTIGE_WEIGHT;
            }
        } else {
            for (YearlyTeamStats s : currentTeamStats) {
                Team t = nameMap.get(s.team);
                t.pollScore =
                        t.wins * WIN_WEIGHT +
                                (s.points - s.opp_points) * DIFF_WEIGHT +
                                t.getTalent() * TALENT_WEIGHT +
                                t.prestige * PRESTIGE_WEIGHT;
                for (Game g : t.gameSchedule) {
                    int count = 0;
                    if (g != null && g.hasPlayed() &&
                            g.gameType == Game.GameType.MARCH_MADNESS && g.getWinner() == t) {
                        // Won a march madness game
                        count++;
                    }
                    t.pollScore += count * WIN_WEIGHT * 3;
                    if (count == 5) t.pollScore += WIN_WEIGHT * 50;
                }
            }
        }

        Collections.sort(sortedTeamList, new Comparator<Team>() {
            @Override
            public int compare(Team a, Team b) {
                return b.pollScore - a.pollScore;
            }
        });
        for (int i = 0; i < sortedTeamList.size(); ++i) {
            sortedTeamList.get(i).pollRank = i+1;
        }
    }

    public void assignPollRanks(Context context, int year) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(context);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(year);
        assignPollRanks(currentTeamStats);
    }

    public int getNumConfTourneyGames() {
        int numConfGames = 0;
        for (Team t : getAllTeams()) {
            int teamConfGames = 0;
            Game g;
            for (int i = t.gameSchedule.size()-1; i >= 0; --i) {
                g = t.gameSchedule.get(i);
                if (g != null) {
                    if (g.gameType == Game.GameType.TOURNAMENT_GAME) {
                        teamConfGames++;
                    } else if (g.gameType == Game.GameType.REGULAR_SEASON) {
                        break;
                    }
                }
            }
            numConfGames = Math.max(numConfGames, teamConfGames);
        }

        return numConfGames;
    }

    public int getNumMarchMadnessGames() {
        int numMMGames = 0;
        for (Team t : getAllTeams()) {
            int teamMMGames = 0;
            Game g;
            for (int i = t.gameSchedule.size()-1; i >= 0; --i) {
                g = t.gameSchedule.get(i);
                if (g != null) {
                    System.out.println(t.getName() + " " + i + " : " + g.gameType.toString());
                    if (g.gameType == Game.GameType.MARCH_MADNESS) {
                        teamMMGames++;
                        System.out.println(t.getName() + " mm games: " + String.valueOf(teamMMGames));
                    } else if (g.gameType == Game.GameType.REGULAR_SEASON) {
                        break;
                    }
                }
            }
            numMMGames = Math.max(numMMGames, teamMMGames);
        }

        return numMMGames;
    }
}
