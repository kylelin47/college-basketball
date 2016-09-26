package io.coachapps.collegebasketballcoach.models;

public class Player {
    public int id;
    public String name;
    public String team;
    public PlayerRatings ratings;
    public Player(int id, String name, String team, PlayerRatings ratings) {
        this.id = id;
        this.name = name;
        this.team = team;
        this.ratings = ratings;
    }
}
