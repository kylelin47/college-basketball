package io.coachapps.collegebasketballcoach.models;

import java.io.Serializable;

public class AwardTeamModel implements Serializable {
    public int pg = 0;
    public int sg = 0;
    public int sf = 0;
    public int pf = 0;
    public int c = 0;

    public AwardTeamModel() {}

    public AwardTeamModel(int pg, int sg, int sf, int pf, int c) {
        this.pg = pg;
        this.sg = sg;
        this.sf = sf;
        this.pf = pf;
        this.c = c;
    }

    public int getIdPosition(int position) {
        switch (position) {
            case 1: return pg;
            case 2: return sg;
            case 3: return sf;
            case 4: return pf;
            case 5: return c;
        }
        return 0;
    }

    public void setIdPosition(int position, int id) {
        switch (position) {
            case 1: pg = id;
                break;
            case 2: sg = id;
                break;
            case 3: sf = id;
                break;
            case 4: pf = id;
                break;
            case 5: c = id;
                break;
        }
    }

}
