package io.coachapps.collegebasketballcoach.models;

import java.util.List;

public class FullGameResults {
    public List<BoxScore> boxScores;
    public GameModel game;
    public FullGameResults(List<BoxScore> boxScores, GameModel game) {
        this.boxScores = boxScores;
        this.game = game;
    }
}
