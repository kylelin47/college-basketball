package io.coachapps.collegebasketballcoach.db;

import android.provider.BaseColumns;

public final class Schemas {
    private Schemas() {}
    public static abstract class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String TEAM = "teamName";
        public static final String NAME = "name";
        public static final String YEAR = "year";
        public static final String RATINGS = "ratings";
    }

    public static abstract class TeamEntry implements BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String NAME = "name";
        public static final String CONFERENCE = "conference";
        public static final String PRESTIGE = "prestige";
        public static final String IS_PLAYER = "isPlayer";
    }

    public static abstract class YearlyTeamStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "yearlyTeamStats";
        public static final String TEAM = "team";
        public static final String YEAR = "year";
        public static final String WINS = "wins";
        public static final String LOSSES = "losses";
        public static final String POINTS = "points";
        public static final String ASSISTS = "assists";
        public static final String REBOUNDS = "rebounds";
        public static final String STEALS = "steals";
        public static final String BLOCKS = "blocks";
        public static final String TURNOVERS = "turnovers";
        public static final String FGM = "fgm";
        public static final String FGA = "fga";
        public static final String THREEPM = "threePM";
        public static final String THREEPA = "threePA";
        public static final String FTM = "ftm";
        public static final String FTA = "fta";
        public static final String OPP_POINTS = "opp_points";
        public static final String OPP_ASSISTS = "opp_assists";
        public static final String OPP_REBOUNDS = "opp_rebounds";
        public static final String OPP_STEALS = "opp_steals";
        public static final String OPP_BLOCKS = "opp_blocks";
        public static final String OPP_TURNOVERS = "opp_turnovers";
        public static final String OPP_FGM = "opp_fgm";
        public static final String OPP_FGA = "opp_fga";
        public static final String OPP_THREEPM = "opp_threePM";
        public static final String OPP_THREEPA = "opp_threePA";
        public static final String OPP_FTM = "opp_ftm";
        public static final String OPP_FTA = "opp_fta";
    }

    public static abstract class YearlyPlayerStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "yearlyPlayerStats";
        public static final String PLAYER = "player";
        public static final String YEAR = "year";
        public static final String POINTS = "points";
        public static final String OFFENSIVE_REBOUNDS = "offensiveRebounds";
        public static final String DEFENSIVE_REBOUNDS = "defensiveRebounds";
        public static final String ASSISTS = "assists";
        public static final String STEALS = "steals";
        public static final String BLOCKS = "blocks";
        public static final String TURNOVERS = "turnovers";
        public static final String FOULS = "fouls";
        public static final String FGM = "fgm";
        public static final String FGA = "fga";
        public static final String THREE_POINTS_MADE = "threePM";
        public static final String THREE_POINTS_ATTEMPTED = "threePA";
        public static final String FTA = "fta";
        public static final String FTM = "ftm";
        public static final String GAMES_PLAYED = "gamesPlayed";
        public static final String SECONDS_PLAYED = "secondsPlayed";
    }

    public static abstract class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String HOME_TEAM = "homeTeam";
        public static final String AWAY_TEAM = "awayTeam";
        public static final String HOME_STATS = "homeStats";
        public static final String AWAY_STATS = "awayStats";
        public static final String YEAR = "year";
        public static final String WEEK = "week";
        public static final String IS_PLAYED = "isPlayed";
    }

    public static abstract class LeagueResultsEntry implements BaseColumns {
        public static final String TABLE_NAME = "leagueResults";
        public static final String YEAR = "year";
        public static final String CHAMPION = "champion";
        public static final String COWBOY_CHAMPION = "cowboyChampion";
        public static final String LAKES_CHAMPION = "lakesChampion";
        public static final String MOUNTAINS_CHAMPION = "mountainsChampion";
        public static final String NORTH_CHAMPION = "northChampion";
        public static final String PACIFIC_CHAMPION = "pacificChampion";
        public static final String SOUTH_CHAMPION = "southChampion";
        public static final String MVP = "mvp";
        public static final String DPOY = "dpoy";
        public static final String ALL_AMERCANS = "allAmericans";
        public static final String ALL_COWBOY = "allCowboy";
        public static final String ALL_LAKES = "allLakes";
        public static final String ALL_MOUNTAINS = "allMountains";
        public static final String ALL_NORTH = "allNorth";
        public static final String ALL_PACIFIC = "allPacific";
        public static final String ALL_SOUTH = "allSouth";
    }

    public static abstract class BoxScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "boxScore";
        public static final String PLAYER = "player";
        public static final String YEAR = "year";
        public static final String WEEK = "week";
        public static final String STATS = "stats";
        public static final String TEAM_NAME = "teamName";
    }
}
