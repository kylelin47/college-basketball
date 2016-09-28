package io.coachapps.collegebasketballcoach.basketballsim;

import android.content.Context;

import java.util.List;

import io.coachapps.collegebasketballcoach.db.BoxScoreDao;

/**
 * Has all the code responsible for simulating games.
 * Pretty sure this class should be static :^)
 * @author Achi Jones
 */
public class Simulator {

    public Context context;

    public Simulator(Context c) {
        context = c;
    }
    
    public void playSeason( List<Team> league ) {
        //each team plays 4 games against each other team, and then is normalized to 82 games
        int inner_itr = 0;
        int outer_itr = 0;
        int games = 0;
        while ( outer_itr < league.size() ) {
            inner_itr = outer_itr + 1;
            while ( inner_itr < league.size() ) {
                for (int g = 0; g < 20; g++) {
                    playGame( league.get(inner_itr), league.get(outer_itr) );
                    playGame( league.get(outer_itr), league.get(inner_itr) );
                    games += 2;
                }
                inner_itr++;
            }
            outer_itr++;
        }
        
    }
    
    public void playGame( Team home, Team away ) {
        boolean poss_home = true;
        boolean poss_away = false;
        double gametime = 0;
        double max_gametime = 2880;
        int hscore = 0; //score of home team
        int ascore = 0; //score of away team
        boolean playing = true;
        
        int home_tot_outd = home.getPG().getOutD() + home.getSG().getOutD() + home.getSF().getOutD() + 
                           home.getPF().getOutD() + home.getC().getOutD();
        int away_tot_outd = away.getPG().getOutD() + away.getSG().getOutD() + away.getSF().getOutD() + 
                           away.getPF().getOutD() + away.getC().getOutD();
        double hspeed = 6 - (home_tot_outd - away_tot_outd)/8;
        double aspeed = 6 - (away_tot_outd - home_tot_outd)/8;
        //detect mismatches and add to total
        int[] matches_h = detectMismatch(home, away);
        for (int i = 0; i < 5; ++i) {
            //home.playersArray[i].stats_tot[11] += matches_h[i]; // add mismatch to total
        }
        int[] matches_a = detectMismatch(away, home);
        for (int i = 0; i < 5; ++i) {
            //away.playersArray[i].stats_tot[11] += matches_a[i]; // add mismatch to total
        }
        
        while (playing) {
            if (poss_home) {
                hscore += runPlay(home, away, matches_h);
                poss_away = true;
                poss_home = false;
                gametime += hspeed + 19 * Math.random();
                home.subPlayers( gametime );
                matches_h = detectMismatch(home, away);
            } else if (poss_away) {
                ascore += runPlay(away, home, matches_a);
                poss_away = false;
                poss_home = true;
                gametime += aspeed + 19 * Math.random();
                away.subPlayers( gametime );
                matches_a = detectMismatch(away, home);
            }
            //check if game has ended, or go to OT if needed
            if ( gametime > max_gametime ) {
                gametime = max_gametime;
                if ( hscore != ascore ) {
                    playing = false;
                } else {
                    poss_home = true;
                    poss_away = false;
                    max_gametime += 300;
                }
            }
        }

        BoxScoreDao bsd = new BoxScoreDao(context);
        
        //add each players stats to his career total
        for (int p = 0; p < 10; ++p) {
            home.players.get(p).addGameStatsToTotal();
            away.players.get(p).addGameStatsToTotal();

            //bsd.save(home.playersArray[p].getGameBoxScore());
            //bsd.save(away.playersArray[p].getGameBoxScore());
        }
        
        home.pointsFor += hscore;
        home.pointsAga += ascore;
        away.pointsFor += ascore;
        away.pointsAga += hscore;
        
        if ( hscore > ascore ) {
            home.wins++;
            away.losses++;
            home.games++;
            away.games++;
        } else {
            home.losses++;
            away.wins++;
            home.games++;
            away.games++;
        }
        
    }

    /**
     * Run a play between the offense and defense.
     * @param offense the team with the ball
     * @param defense the opposing team
     * @param matches array of mismatches, used to calculate who to pass the ball to
     * @return number of points scored by the offense (0, 2, or 3)
     */
    public int runPlay( Team offense, Team defense, int[] matches ) {
        
        int off_tot_outd = offense.getPG().getOutD() + offense.getSG().getOutD() + offense.getSF().getOutD() + 
                           offense.getPF().getOutD() + offense.getC().getOutD();
        int def_tot_outd = defense.getPG().getOutD() + defense.getSG().getOutD() + defense.getSF().getOutD() + 
                           defense.getPF().getOutD() + defense.getC().getOutD();
        int fastbreak_possibility = off_tot_outd - def_tot_outd;
        
        int totPasses = 0;
        int offPoss = 1;
        Player whoPoss = getBallCarrier(offense);
        Player whoDef = defense.players.get( whoPoss.getPosition() - 1 );
        Player assister = whoPoss;
         
        while ( offPoss == 1 ) {
            if ( (int)(6*Math.random()) + totPasses < 5 || (totPasses == 0 && Math.random() < 0.97) ) {
                //pass the ball
                totPasses++;
                if ( potSteal(whoPoss, whoDef) ) {
                    //ball stolen
                    whoDef.addStl();
                    return 0;
                }
                //get receiver of pass
                assister = whoPoss;
                whoPoss = intelligentPass(whoPoss, offense, defense, matches);
                whoDef = defense.players.get( whoPoss.getPosition() - 1 );
            } else if ( fastbreak_possibility * Math.random() > 60 ) {
                //punish all-bigs lineup, they give up fast break points
                //System.out.println("FAST BREAK");
                whoPoss.addPts(2);
                whoPoss.addFGA();
                whoPoss.addFGM();
                whoDef.addOFA();
                whoDef.addOFM();
                if ( assister == whoPoss ) {
                    return 2;
                } else {
                    if ( Math.pow(assister.getPass()/14, 2.4) * Math.random() > 20 ) {
                        assister.addAss();
                    }
                    return 2;
                }
            } else {
                //whoPoss will shoot the ball
                int points = takeShot(whoPoss, whoDef, defense, assister);
                if ( points > 0 ) {
                    //made the shot!
                    if ( assister == whoPoss ) { //can't pass to yourself
                        return points;
                    } else {
                        if ( Math.pow(assister.getPass()/14, 2.4) * Math.random() > 20 ) {
                            assister.addAss(); //give assist
                        }
                        return points;
                    }
                } else {
                    //miss, scramble for rebound
                    int[] rebAdvArr = new int[3];
                    for (int r = 0; r < 3; ++r) {
                        //calculate each players rebounding advantage
                        rebAdvArr[r] = offense.players.get(r+2).getReb() - defense
                                .players.get(r+2).getReb();
                    }
                    double rebAdv = 0.175 * (rebAdvArr[ (int)(Math.random() * 3) ] + rebAdvArr[ (int)(Math.random() * 3) ]);
                    if ( Math.random()*100 + rebAdv > 25 ) {
                        //defensive rebound
                        Player rebounder = findRebounder(defense);
                        rebounder.addReb();
                        return 0; //exit without scoring any points
                    } else {
                        //offensive rebound
                        Player rebounder = findRebounder(offense);
                        rebounder.addReb();
                        whoPoss = rebounder;
                        totPasses = 2;
                        //goes back into loop to try another play
                    }
                }
            } 
        }
        return 0;
    }

    /**
     * Make the shooter take a shot, choosing where to shoot it from.
     * Player gets a bonus for getting a pass from a good assister.
     * @param shooter
     * @param defender
     * @param defense
     * @param assister
     * @return
     */
    public int takeShot( Player shooter, Player defender, Team defense, Player assister ) {
        int assBonus = 0;
        if ( assister != shooter ) {
            //shooter gets bonus for having a good passer
            assBonus = (int)((float)(assister.getPass() - 75)/5);
        }
        
        double selShot = Math.random();
        //get intelligent tendencies based on mismatches
        double intelOutT = shooter.getOutT();
        double intelMidT = shooter.getMidT();
        int mismMid = shooter.getMidS() - (int)( (float)defender.getOutD()/2 + (float)defender.getIntD()/2 );
        if ( Math.abs(mismMid) > 30 ) {
            intelMidT += (int)((float)mismMid / 8);
        }
        int mismOut = shooter.getOutS() - defender.getOutD();
        if ( Math.abs(mismOut) > 30 ) {
            intelOutT += (int)((float)mismOut / 8);
        }
        //System.out.println(shooter.name + " has " + shooter.getOutS() + "outS and " + shooter.getOutT() + "outT and " + intelOutT + "intelOutT");
        if ( selShot < intelOutT && intelOutT >= 0 && shooter.getOutS() > 50 ) {
            //3 point shot
            double chance = 22 + (float)shooter.getOutS()/3 + assBonus - (float)defender.getOutD()/6;
            if ( chance > Math.random()*100 ) {
                //made the shot!
                shooter.make3ptShot();
                defender.addOFA();
                defender.addOFM();
                return 3;
            } else {
                shooter.addFGA();
                shooter.add3GA();
                defender.addOFA();
                return 0;
            }
        } else if ( selShot < intelMidT && intelMidT >= 0 ) {
            //mid range shot
            int defMidD = (int)( defender.getOutD()*0.5 + defender.getIntD()*0.5 );
            double chance = 30 + (float)shooter.getOutS()/3 + assBonus - (float)defMidD/7;
            if ( chance > Math.random()*100 ) {
                //made the shot!
                shooter.addPts(2);
                shooter.addFGA();
                shooter.addFGM();
                defender.addOFA();
                defender.addOFM();
                return 2;
            } else {
                shooter.addFGA();
                defender.addOFA();
                return 0;
            }
        } else {
            //inside shot, layup, dunk
            
            //check for block
            if ( Math.random() < 0.35 ) {
                int blk = defender.getBlk()-75;
                if (blk < 0) {
                    blk = 0;
                }
                if ( Math.random() * Math.pow(blk, 0.75) > 5 || Math.random() < 0.02 ) {
                    //blocked!
                    shooter.addFGA();
                    defender.addOFA();
                    defender.addBlk();
                    return 0;
                } 
            }

            // Add a defense bonus if a non PF/C is laying up.
            // There is help defense from the opposing PF/C so the shot is harder.
            float defenseBonus = 0;
            if (shooter.getPosition() == 4) {
                defenseBonus = (float)defense.getC().getIntD()/25;
            } else if (shooter.getPosition() != 5) {
                defenseBonus = (float)defense.getPF().getIntD()/25 + (float)defense.getC().getIntD()/25;
            }

            double chance = 35 + (float)shooter.getIntS()/3 + assBonus - (float)defender.getIntD()/14 - defenseBonus;
            if ( chance > Math.random() * 100 ) {
                //made the shot!
                shooter.addPts(2);
                shooter.addFGA();
                shooter.addFGM();
                defender.addOFA();
                defender.addOFM();
                return 2;
            } else {
                shooter.addFGA();
                defender.addOFA();
                return 0;
            }
        } 
    }

    /**
     * Calculate mismatches of the two teams.
     * @param offense team on offense
     * @param defense team on defense
     * @return int array of mismatches
     */
    public int[] detectMismatch( Team offense, Team defense ) {
        int[] mismatches = new int[5];
        for ( int i = 0; i < 5; ++i ) {
            mismatches[i] = calcMismatch( offense.players.get(i), defense.players.get(i) );
        }
        return mismatches;
    }

    /**
     * Calculate individual mismatch.
     * @param shooter player who's shooting
     * @param defender player who's defending
     * @return int value of the mismatch
     */
    public int calcMismatch( Player shooter, Player defender ) {
        double intMis = ( 2 * shooter.getIntS() - defender.getIntD() ) * shooter.getInsT();
        double midMis = ( 2 * shooter.getMidS() - (float)(defender.getIntD() + defender.getOutD()) ) * shooter.getMidT();
        double outMis = ( 2 * shooter.getOutS() - defender.getOutD() ) * shooter.getOutT();
        return (int)(Math.pow( shooter.getUsage()*(intMis + midMis + outMis), 1.3 ) / 100);
    }

    /**
     * "Intelligently" pass the ball, taking into account mismatches and usages.
     * @param whoPoss who has the ball
     * @param offense team on offense
     * @param defense team on defense
     * @param matches array of mismatches
     * @return player who gets the ball
     */
    public Player intelligentPass( Player whoPoss, Team offense, Team defense, int[] matches ) {
        //pass intelligently
        int mismFactor = 18;
        double pgTen = offense.getPG().getUsage() + (float)matches[0]/mismFactor;
        double sgTen = offense.getSG().getUsage() + (float)matches[1]/mismFactor;
        double sfTen = offense.getSF().getUsage() + (float)matches[2]/mismFactor;
        double pfTen = offense.getPF().getUsage() + (float)matches[3]/mismFactor;
        double cTen = offense.getC().getUsage() + (float)matches[4]/mismFactor;
        
        double totTen = pgTen + sgTen + sfTen + pfTen + cTen;
        double whoPass = Math.random() * totTen;
        
        if ( whoPass < pgTen ) {
            return offense.getPG();
        } else if ( whoPass < pgTen + sgTen ) {
            return offense.getSG();
        } else if ( whoPass < pgTen + sgTen + sfTen ) {
            return offense.getSF();
        } else if ( whoPass < pgTen + sgTen + sfTen + pfTen ) {
            return offense.getPF();
        } else {
            return offense.getC();
        }
    }

    /**
     * Calculate the inital ball carrier when a team first brings the ball up the court.
     * @param t team on offense
     * @return player who will get ball
     */
    public Player getBallCarrier( Team t ) {
        double sfBall = t.getSF().getPass() * Math.random();
        double sgBall = t.getSG().getPass() * Math.random();
        double pgBall = t.getPG().getPass() * Math.random();
        if ( sfBall > sgBall && sfBall > pgBall ) {
            return t.getSF();
        } else if ( sgBall > sfBall && sgBall > pgBall ) {
            return t.getSG();
        } else {
            return t.getPG();
        }
    }

    /**
     * Find a rebounder from the team.
     * Based on each player's rebounding attribute.
     * @param t team getting the rebound
     * @return player who gets rebound
     */
    public Player findRebounder( Team t ) {
        double cnReb = t.getC().getReb() * Math.random();
        double pfReb = t.getPF().getReb() * Math.random();
        double sfReb = t.getSF().getReb() * Math.random();
        double sgReb = t.getSG().getReb() * Math.random();
        double pgReb = t.getPG().getReb() * Math.random();
        if ( pgReb > pfReb && pgReb > sfReb && pgReb > sgReb && pgReb > cnReb ) {
            return t.getPG();
        } else if ( sgReb > cnReb && sgReb > pfReb && sgReb > sfReb && sfReb > pgReb ) {
            return t.getSG();
        } else if ( sfReb > cnReb && sfReb > pfReb && sfReb > sgReb && sfReb > pgReb ) {
            return t.getSF();
        } else if ( pfReb > cnReb && pfReb > sfReb && pfReb > sgReb && pfReb > pgReb ) {
            return t.getPF();
        } else {
            return t.getC();
        }
    }

    /**
     * Check if the ball is being stolen
     * @param off player on offense
     * @param def player on defense
     * @return true if the ball was stolen, else false
     */
    public boolean potSteal( Player off, Player def ) {
        if ( Math.random() < 0.1 ) {
            int stl = def.getStl()-75;
            if (stl < 0) {
                stl = 0;
            }
            double chance = Math.random() * Math.pow(stl, 0.75);
            if ( chance > 5 || Math.random() < 0.1 ) {
                return true;
            } else return false;
        } else return false;
    }
    
    
    
}
