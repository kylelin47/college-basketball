package io.coachapps.collegebasketballcoach.models;

public class PlayerModel {

    public int id;
    public String name;
    public String team;
    public PlayerRatings ratings;

    public PlayerModel(int id, String name, String team, PlayerRatings ratings) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.ratings = ratings;
    }

}
