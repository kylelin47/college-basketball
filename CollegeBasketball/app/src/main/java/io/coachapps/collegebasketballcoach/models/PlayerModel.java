package io.coachapps.collegebasketballcoach.models;

import io.coachapps.collegebasketballcoach.basketballsim.Player;

public class PlayerModel {
    public int id;
    public String name;
    public String team;
    public int year;
    public PlayerRatings ratings;

    public PlayerModel(Player player, String teamName) {
        this.id = player.getId();
        this.name = player.name;
        this.team = teamName;
        this.ratings = player.ratings;
        this.year = player.year;
    }
}
