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
        public static final String IS_PLAYER = "isPlayer";
    }

    public static abstract class YearlyTeamStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "yearlyTeamStats";
        public static final String TEAM = "team";
        public static final String YEAR = "year";
        public static final String WINS = "wins";
        public static final String LOSSES = "losses";
    }

    public static abstract class YearlyPlayerStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "yearlyPlayerStats";
        public static final String PLAYER = "player";
        public static final String YEAR = "year";
        public static final String POINTS = "points";
        public static final String MINUTES = "minutes";
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
    }

    public static abstract class LeagueResultsEntry implements BaseColumns {
        public static final String TABLE_NAME = "leagueResults";
        public static final String YEAR = "year";
        public static final String CHAMPION = "champion";
        public static final String MVP = "mvp";
        public static final String DPOY = "dpoy";
    }

    public static abstract class BoxScoreEntry implements BaseColumns {
        public static final String TABLE_NAME = "boxScore";
        public static final String PLAYER = "player";
        public static final String GAME = "game";
        public static final String POINTS = "points";
        public static final String REBOUNDS = "rebounds";
        public static final String ASSISTS = "assists";
        public static final String FIELD_GOALS_MADE = "fgm";
    }
}
