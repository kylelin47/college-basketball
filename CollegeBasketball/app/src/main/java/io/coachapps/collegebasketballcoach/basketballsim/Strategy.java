package io.coachapps.collegebasketballcoach.basketballsim;

/**
 * Strategy class. Encapsulates the different types of strategies and their bonuses.
 * Created by jojones on 10/11/16.
 */

public class Strategy {

    public enum Strats {
        DRIBBLE_DRIVE("Dribble Drive",
                "Encourages your PG to drive and pass out to his teammates for open shots. Relies on good passing from your PG."),
        MOTION("Motion",
                "Offense filled with passing, screening, and cutting. Can be a thing of beauty, if your players are smart enough."),
        RUN_AND_GUN("Run and Gun",
                "Encourages cross-court passes and a fast pace to get easy shots. Of course, sometimes those risks can lead to turnovers."),
        TRIANGLE("Triangle",
                "Relies on smart play from your PG and C in order to take smart shots. Generally a conservative, slower paced offense."),
        MAN_TO_MAN("Man to Man",
                "The simplest defense of all, where each player guards an opposing player."),
        ONE_THREE_ONE_ZONE("1-3-1 Zone",
                "Riskier defense that encourages trapping and getting steals. However, with only one man on the inside, it can give up easy dunks and lay-ups."),
        TWO_THREE_ZONE("2-3 Zone",
                "The most common zone defense, which places emphasis on interior defense at the risk of giving up perimeter shots.");

        private String name;
        private String description;

        Strats(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public static Strats[] getOffStrats() {
            return new Strats[]{DRIBBLE_DRIVE, MOTION, RUN_AND_GUN, TRIANGLE};
        }

        public static Strats[] getDefStrats() {
            return new Strats[]{MAN_TO_MAN, ONE_THREE_ONE_ZONE, TWO_THREE_ZONE};
        }

    }

    public final Strats strat;

    private final Team team;

    private final double insideBonus;
    private final double midrangeBonus;
    private final double outsideBonus;
    private final double stealBonus;

    public Strategy(Strats type, Team t) {
        team = t;
        strat = type;

        switch (type) {

            // Offense
            case DRIBBLE_DRIVE:
                insideBonus = -2;
                midrangeBonus = 3 * (double)t.getPG().getPass()/100;
                outsideBonus = 5 * (double)t.getPG().getPass()/100;
                stealBonus = -5;
                break;
            case MOTION:
                double iq = getCollectiveIQ()/100;
                insideBonus = 4 * iq;
                midrangeBonus = 3 * iq;
                outsideBonus = 3 * iq;
                stealBonus = 8;
                break;
            case RUN_AND_GUN:
                // Bonuses are only applied some of the time
                insideBonus = 25;
                midrangeBonus = 25;
                outsideBonus = 25;
                stealBonus = 20;
                break;
            case TRIANGLE:
                double iqPGC = (double)(team.getPG().getBBallIQ() + team.getC().getBBallIQ())/200;
                insideBonus = 4 * iqPGC;
                midrangeBonus = 2 * iqPGC;
                outsideBonus = 1 * iqPGC;
                stealBonus = -8;
                break;

            // Defense
            case MAN_TO_MAN:
                insideBonus = 2;
                midrangeBonus = 2;
                outsideBonus = 2;
                stealBonus = 0;
                break;
            case ONE_THREE_ONE_ZONE:
                insideBonus = -8;
                midrangeBonus = 0;
                outsideBonus = 0;
                stealBonus = 20;
                break;
            case TWO_THREE_ZONE:
                insideBonus = 5;
                midrangeBonus = 0;
                outsideBonus = -4;
                stealBonus = 0;
                break;

            // Should never get here
            default:
                insideBonus = 0;
                midrangeBonus = 0;
                outsideBonus = 0;
                stealBonus = 0;
                break;
        }
    }

    public String getName() {
        return strat.getName();
    }

    public double getCollectiveIQ() {
        int iq = 0;
        for (Player p : team.players) {
            iq += p.getBBallIQ();
        }

        return (double) iq/team.players.size();
    }

    public double getCollectivePassing() {
        int pass = 0;
        for (int i = 0; i < 5; ++i) {
            pass += team.players.get(i).getPass();
        }
        return (double)pass/5;
    }

    public double getInsideBonus() {
        if (strat == Strats.RUN_AND_GUN) {
            if (Math.random() < 0.25) return insideBonus;
            else return 0;
        } else {
            return insideBonus;
        }
    }

    public double getMidrangeBonus() {
        if (strat == Strats.RUN_AND_GUN) {
            if (Math.random() < 0.25) return midrangeBonus;
            else return 0;
        } else {
            return midrangeBonus;
        }
    }

    public double getOutsideBonus() {
        if (strat == Strats.RUN_AND_GUN) {
            if (Math.random() < 0.25) return outsideBonus;
            else return 0;
        } else {
            return outsideBonus;
        }
    }

    public double getStealBonus() {
        return stealBonus;
    }
}
