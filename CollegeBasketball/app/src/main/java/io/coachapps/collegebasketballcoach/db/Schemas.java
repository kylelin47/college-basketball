package io.coachapps.collegebasketballcoach.db;

import android.provider.BaseColumns;

public final class Schemas {
    // For ratings: store BLOB that consists of serialized Ratings class

    public Schemas() {}
    public static abstract class PlayerEntry implements BaseColumns {
        public static final String TABLE_NAME = "players";
        public static final String TEAM = "teamName";
        public static final String NAME = "name";
        public static final String YEAR = "year";
    }

    public static abstract class TeamEntry implements BaseColumns {
        public static final String TABLE_NAME = "teams";
        public static final String NAME = "name";
        public static final String CONFERENCE = "conference";
    }

    public static abstract class YearlyTeamStatsEntry implements BaseColumns {
        public static final String TABLE_NAME = "yearlyTeamStats";
        public static final String TEAM = "team";
        public static final String YEAR = "year";
        public static final String WINS = "wins";
        public static final String LOSSES = "losses";
    }

    public static abstract class GameEntry implements BaseColumns {
        public static final String TABLE_NAME = "games";
        public static final String HOME_TEAM = "homeTeam";
        public static final String AWAY_TEAM = "awayTeam";
        public static final String HOME_STATS = "homeStats";
        public static final String AWAY_STATS = "awayStats";
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
