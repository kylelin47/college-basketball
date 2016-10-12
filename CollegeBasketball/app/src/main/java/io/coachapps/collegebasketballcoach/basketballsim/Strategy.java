package io.coachapps.collegebasketballcoach.basketballsim;

/**
 * Strategy class. Encapsulates the different types of strategies and their bonuses.
 * Created by jojones on 10/11/16.
 */

public class Strategy {

    public enum Strats {
        DRIBBLE_DRIVE("Dribble Drive"),
        MOTION("Motion"),
        RUN_AND_GUN("Run and Gun"),
        TRIANGLE("Triangle"),
        MAN_TO_MAN("Man to Man"),
        ONE_THREE_ONE_ZONE("1-3-1 Zone"),
        TWO_THREE_ZONE("2-3 Zone"),
        DOUBLE_TEAM("Double Team");

        private String name;

        Strats(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public static Strats[] getOffStrats() {
            return new Strats[]{DRIBBLE_DRIVE, MOTION, RUN_AND_GUN, TRIANGLE};
        }

        public static Strats[] getDefStrats() {
            return new Strats[]{MAN_TO_MAN, ONE_THREE_ONE_ZONE, TWO_THREE_ZONE, DOUBLE_TEAM};
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
                insideBonus = -3;
                midrangeBonus = 3 * (double)t.getPG().getPass()/100;
                outsideBonus = 5 * (double)t.getPG().getPass()/100;
                stealBonus = 0;
                break;
            case MOTION:
                double iq = getCollectiveIQ()/100;
                insideBonus = 3 * iq;
                midrangeBonus = 3 * iq;
                outsideBonus = 3 * iq;
                stealBonus = 15;
                break;
            case RUN_AND_GUN:
                // Bonuses are only applied some of the time
                insideBonus = 25;
                midrangeBonus = 25;
                outsideBonus = 25;
                stealBonus = 20;
                break;
            case TRIANGLE:
                double iqPGC = (double)(team.getPG().getBBallIQ() + team.getC().getBBallIQ())/100;
                insideBonus = 5 * iqPGC;
                midrangeBonus = 2 * iqPGC;
                outsideBonus = 2 * iqPGC;
                stealBonus = -10;
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
            case DOUBLE_TEAM:
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
        return insideBonus;
    }

    public double getMidrangeBonus() {
        return midrangeBonus;
    }

    public double getOutsideBonus() {
        return outsideBonus;
    }

    public double getStealBonus() {
        return stealBonus;
    }
}
