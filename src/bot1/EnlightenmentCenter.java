package bot1;

import battlecode.common.*;

import java.awt.*;
import java.util.*;
import java.lang.Math;

//import static bot1.RobotPlayer.rc;

public class EnlightenmentCenter extends RobotPlayer {

    public static Vector<bot> NeutEnl = new Vector<bot>();
    public static Vector<Integer> Muck = new Vector<Integer>();
    public static Vector<dum> EnMuck = new Vector<dum>();
    public static Vector<dum> Polst = new Vector<dum>(); //contains the politician strongholds in the position where the Round number of their presence is present
    public static Vector<dum> slan = new Vector<dum>(); //contains the scandalerer strongholds in the position where the Round number of their presence is present
    public static Vector<bot> EnEnl = new Vector<bot>();
    public static Vector<bot> MyEnl = new Vector<bot>();
    public static int x, y;//pseudo variable to be used now and then

    public void decode(int t) {
        x = t / 65;
        y = t % 65;
    }

    public static RobotInfo sen[];

    public static void runEnlightenmentCenter() throws GameActionException {
        System.out.println("Trying to run");
        sen = rc.senseNearbyRobots();
        UpdateData();
        System.out.println("Data Updated");
        SensorList();
        System.out.println("Data Collected");
        int bidh = bidAmount();
        if (rc.canBid(bidh)) {
            rc.bid(bidh);
        }
        System.out.println("Bidding possible");
        DeployUnits();
        System.out.println("Deployed");
        Flagset();
        System.out.println("Flags given");
    }


    public static Team mine;
    public static int create;
    public static MapLocation selfi;

    public static void setup(){
        System.out.println("Start of EC constructor");
        mine = rc.getTeam();
        create = rc.getRoundNum();
        selfi = rc.getLocation();
        System.out.println("Got Team Info");
        RobotInfo sen[] = rc.senseNearbyRobots();
        int u;
        u = sen.length;

        for (int i = 0; i < u; i++) {
            RobotInfo a = sen[i];
            bot z = new bot();
            dum o = new dum();
            if (a.type == RobotType.ENLIGHTENMENT_CENTER) {
                if (a.team == mine.opponent()) {

                    z.co = a.location;
                    z.tea = 1;
                    z.type = 0;
                    z.id = a.ID;

                    EnEnl.add(z);
                } else if (a.team == mine) {
                    z.co = a.location;
                    z.tea = 0;
                    z.type = 0;
                    z.id = a.ID;

                    MyEnl.add(z);
                } else {
                    z.co = a.location;
                    z.tea = 2;
                    z.type = 0;
                    z.id = a.ID;

                    NeutEnl.add(z);
                }
            }
            if (a.type == RobotType.MUCKRAKER) {
                if (a.team == mine.opponent()) {
                    o.co = a.location;
                    o.roun = rc.getRoundNum();
                    EnMuck.add(o);
                }
            }
            if (a.type == RobotType.POLITICIAN) {
                if (a.team == mine.opponent()) {
                    o.co = a.location;
                    o.roun = rc.getRoundNum();
                    Polst.add(o);
                }
            }
            if (a.type == RobotType.SLANDERER) {
                o.co = a.location;
                o.roun = rc.getRoundNum();
                slan.add(o);
            }
        }
        System.out.println("End of Setup");
    }

//    public static void setup(){
//        System.out.println("I'm Setup!");
//    }
    public static class bot {
        int id;
        int type;               //0 for Enlighten, 1 for Poli, 2 for Slan, 3 for Muck
        MapLocation co;
        int tea;               //0 for self, 1 for opponent, 2 for neutral
    }

    public static class dum {
        MapLocation co;
        int roun;
    }

    public static class Coor {
        int x;
        int y;
    }

    public static Coor Conv(int r, int theta, MapLocation rel) {
        Coor res = new Coor();
        res.x = (int) Math.round(r * Math.sin((double) (theta * (3.14 / 2.0))) + rel.x);
        res.y = (int) Math.round(r * Math.cos((double) (theta * (3.14 / 2.0))) + rel.y);
        return res;
    }


    public static void UpdateData() throws GameActionException {
        int k = rc.getRoundNum();
        int j = Muck.size();
        for (int i = 0; i < j; i++) {
            int u = Muck.get(i);
            RobotInfo mu = rc.senseRobot(u);
            if (rc.canGetFlag(u)) {

                int z = rc.getFlag(u);
                int l = z;
                l = l >> 12;
                z = (z - (l << 12));

                x = (z >> 6);
                y = z - ((z >> 6) << 6);
                MapLocation p;
                p = mu.getLocation();
                p = p.translate(x, y);
                if (rc.isLocationOccupied(p)) {
                    RobotInfo s = rc.senseRobotAtLocation(p);
                    if (s.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        bot f = new bot();
                        f.id = s.ID;
                        f.type = 0;
                        f.co = p;
                        if (s.getTeam() == mine) {
                            f.tea = 0;
                            MyEnl.add(f);
                        } else if (s.getTeam() == mine.opponent()) {
                            f.tea = 1;
                            EnEnl.add(f);
                        } else {
                            f.tea = 2;
                            NeutEnl.add(f);
                        }
                    } else {
                        dum e = new dum();
                        e.co = p;
                        e.roun = rc.getRoundNum();
                        Polst.add(e);
                        EnMuck.add(e);
                    }
                } else {
                    dum e = new dum();
                    e.co = p;
                    e.roun = rc.getRoundNum();
                    Polst.add(e);
                    EnMuck.add(e);
                }
                x = l >> 6;
                y = l - ((l >> 6) << 6);
                p = mu.getLocation();
                p = p.translate(x, y);
                if (rc.isLocationOccupied(p)) {
                    RobotInfo s = rc.senseRobotAtLocation(p);
                    if (s.getType() == RobotType.ENLIGHTENMENT_CENTER) {
                        bot f = new bot();
                        f.id = s.ID;
                        f.type = 0;
                        f.co = p;
                        if (s.getTeam() == mine) {
                            f.tea = 0;
                            MyEnl.add(f);
                        } else if (s.getTeam() == mine.opponent()) {
                            f.tea = 1;
                            EnEnl.add(f);
                        } else {
                            f.tea = 2;
                            NeutEnl.add(f);
                        }
                    } else {
                        dum e = new dum();
                        e.co = p;
                        e.roun = rc.getRoundNum();
                        Polst.add(e);
                        EnMuck.add(e);
                    }
                } else {
                    dum e = new dum();
                    e.co = p;
                    e.roun = rc.getRoundNum();
                    Polst.add(e);
                    EnMuck.add(e);
                }

            }
        }


        int u = sen.length;
        for (int i = 0; i < u; i++) {
            RobotInfo a = sen[i];
            dum o = new dum();
            if (a.type == RobotType.MUCKRAKER) {
                if (a.team == mine.opponent()) {
                    o.co = a.location;
                    o.roun = rc.getRoundNum();
                    EnMuck.add(o);
                }
            }
            if (a.type == RobotType.POLITICIAN) {
                if (a.team == mine.opponent()) {
                    o.co = a.location;
                    o.roun = rc.getRoundNum();
                    Polst.add(o);
                }
            }
            if (a.type == RobotType.SLANDERER && a.team == mine.opponent()) {
                o.co = a.location;
                o.roun = rc.getRoundNum();
                slan.add(o);
            }
            if (a.team == mine) {
                if (rc.canGetFlag(a.ID)) {
                    int t = rc.getFlag(a.ID);
                    t = t - GameConstants.MIN_FLAG_VALUE;
                    int l = (t >> 3);
                    t = t - (l << 3);
                    Coor toa = Conv(l, t, a.location.translate(-selfi.x, -selfi.y));
                    o.co = selfi.translate(toa.x, toa.y);
                    o.roun = rc.getRoundNum();
                    slan.add(o);
                    Polst.add(o);
                }
            }
        }


    }


    public static int Manhdist(MapLocation a, MapLocation b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }//Compute the Manhattan Distance

    public static void SensorList() {
        int k = rc.getRoundNum();
        while (Polst.size() > 0) {
            dum z = Polst.get(0);
            if (z.roun - k > 10 && Polst.size() > 10) {
                Polst.remove(0);
            } else break;
        }
        while (EnMuck.size() > 0) {
            dum z = EnMuck.get(0);
            if (z.roun - k > 10 && EnMuck.size() > 10) {
                EnMuck.remove(0);
            } else break;
        }
        while (slan.size() > 0) {
            dum z = slan.get(0);
            if (z.roun - k > 10 && slan.size() > 10) {
                slan.remove(0);
            } else break;
        }
    }


    public static int SurrPro() {

        int j = sen.length;
        int pro = 0;
        for (int i = 0; i < j; i++) {
            RobotInfo g = sen[i];
            if (g.team == mine.opponent() && g.type == RobotType.POLITICIAN) {
                pro += 6 - Manhdist(selfi, g.location) + 1;
            }
        }
        return pro;
    }

    public static int bidAmount() {

        int i = rc.getInfluence();
        int s = SurrPro();
        int ro = rc.getRoundNum();
        double k = Math.max(i / 10.0, 0.2 * Math.sqrt((double) ro));
        double fact = 1.0 / (1 + (Math.exp((double) -(1 / ((0.08 * x) + 1)))));

        return (int) Math.floor(k * fact);
    }

    public static void Flagset() throws GameActionException {
        int z = 0;
        int dir[] = new int[8];
        dir[0] = 0;
        dir[1] = 0;
        dir[2] = 0;
        dir[3] = 0;
        dir[4] = 0;
        dir[5] = 0;
        dir[6] = 0;
        dir[7] = 0;
        int j = sen.length;
        for (int i = 0; i < j; i++) {
            RobotInfo h = sen[i];
            if (h.team == mine.opponent()) {
                switch (selfi.directionTo(h.location)) {
                    case EAST:
                        dir[2]++;
                        break;
                    case NORTH:
                        dir[0]++;
                        break;
                    case WEST:
                        dir[6]++;
                        break;
                    case SOUTH:
                        dir[4]++;
                        break;
                    case NORTHEAST:
                        dir[1]++;
                        break;
                    case NORTHWEST:
                        dir[7]++;
                        break;
                    case SOUTHEAST:
                        dir[3]++;
                        break;
                    case SOUTHWEST:
                        dir[5]++;
                        break;
                    default:
                        break;

                }
            }
        }
        if (dir[0] > 0) z += 1;
        if (dir[1] > 0) z += 2;
        if (dir[2] > 0) z += 4;
        if (dir[3] > 0) z += 8;
        if (dir[4] > 0) z += 16;
        if (dir[5] > 0) z += 32;
        if (dir[6] > 0) z += 64;
        if (dir[7] > 0) z += 128;
        dir[0] = 0;
        dir[1] = 0;
        dir[2] = 0;
        dir[3] = 0;
        dir[4] = 0;
        dir[5] = 0;
        dir[6] = 0;
        dir[7] = 0;

        int N = 0, NE = 0, E = 0, SE = 0, S = 0, SW = 0, W = 0, NW = 0;
        z = z << 15;//6 bit for politician and 9 for muckraker
        j = slan.size();
        for (int i = 0; i < j; i++) {
            dum b = slan.get(i);
            switch (selfi.directionTo(b.co)) {
                case EAST:
                    dir[2]++;
                    E++;
                    break;
                case NORTH:
                    dir[0]++;
                    N++;
                    break;
                case WEST:
                    dir[6]++;
                    W++;
                    break;
                case SOUTH:
                    dir[4]++;
                    S++;
                    break;
                case NORTHEAST:
                    dir[1]++;
                    NE++;
                    break;
                case NORTHWEST:
                    dir[7]++;
                    NW++;
                    break;
                case SOUTHEAST:
                    dir[3]++;
                    SE++;
                    break;
                case SOUTHWEST:
                    dir[5]++;
                    SW++;
                    break;
                default:
                    break;
            }
        }
        Arrays.sort(dir);
        int T = 0;
        if (dir[0] == N) T = 0;
        if (dir[0] == NE) T = 1;
        if (dir[0] == E) T = 2;
        if (dir[0] == SE) T = 3;
        if (dir[0] == S) T = 4;
        if (dir[0] == SW) T = 5;
        if (dir[0] == W) T = 6;
        if (dir[0] == NW) T = 7;
        int R = 0;
        if (dir[1] == N) R = 0;
        if (dir[1] == NE) R = 1;
        if (dir[1] == E) R = 2;
        if (dir[1] == SE) R = 3;
        if (dir[1] == S) R = 4;
        if (dir[1] == SW) R = 5;
        if (dir[1] == W) R = 6;
        if (dir[1] == NW) R = 7;
        T = T + (R << 3);
        if (dir[1] == N) R = 0;
        if (dir[1] == NE) R = 1;
        if (dir[1] == E) R = 2;
        if (dir[1] == SE) R = 3;
        if (dir[1] == S) R = 4;
        if (dir[1] == SW) R = 5;
        if (dir[1] == W) R = 6;
        if (dir[1] == NW) R = 7;
        T = T + (R << 6);

        z += T;
        j = NeutEnl.size() + EnEnl.size();
        for (int i = 0; i < j; i++) {
            bot b;
            if (i < NeutEnl.size()) {
                b = NeutEnl.get(i);
            } else {
                b = EnEnl.get(i - NeutEnl.size());
            }
            switch (selfi.directionTo(b.co)) {
                case EAST:
                    dir[2]++;
                    E++;
                    break;
                case NORTH:
                    dir[0]++;
                    N++;
                    break;
                case WEST:
                    dir[6]++;
                    W++;
                    break;
                case SOUTH:
                    dir[4]++;
                    S++;
                    break;
                case NORTHEAST:
                    dir[1]++;
                    NE++;
                    break;
                case NORTHWEST:
                    dir[7]++;
                    NW++;
                    break;
                case SOUTHEAST:
                    dir[3]++;
                    SE++;
                    break;
                case SOUTHWEST:
                    dir[5]++;
                    SW++;
                    break;
                default:
                    break;
            }
        }
        Arrays.sort(dir);
        T = 0;
        if (dir[0] == N) T = 0;
        if (dir[0] == NE) T = 1;
        if (dir[0] == E) T = 2;
        if (dir[0] == SE) T = 3;
        if (dir[0] == S) T = 4;
        if (dir[0] == SW) T = 5;
        if (dir[0] == W) T = 6;
        if (dir[0] == NW) T = 7;
        R = -1;
        if (dir[1] == N) R = 0;
        if (dir[1] == NE) R = 1;
        if (dir[1] == E) R = 2;
        if (dir[1] == SE) R = 3;
        if (dir[1] == S) R = 4;
        if (dir[1] == SW) R = 5;
        if (dir[1] == W) R = 6;
        if (dir[1] == NW) R = 7;
        T = T + (R << 3);

        z += T;

        if (rc.canSetFlag(rc.getID())) {
            rc.setFlag(z + GameConstants.MIN_FLAG_VALUE);
        }


    }

    public static int SlanFreq = 20;
    public static int PolFreq = 50;
    public static int MuckFreq = 10;
    public static Queue<RobotType> Depo = new PriorityQueue<>();

    // priority Slanderer, Politician, MuckRaker
    public static void DeployUnits() throws GameActionException {

        float a = (float) SlanFreq;
        float b = (float) rc.getInfluence();
        if(rc.getInfluence()>0) {
            SlanFreq = (int)(a * ((150.0/b)));
        }else{
            SlanFreq = 100;
        }
        if (SlanFreq == 0){
            SlanFreq = 3;
        }
//        System.out.println("Slanfreq known");
        System.out.println(SlanFreq);
        System.out.println(PolFreq);
        System.out.println(MuckFreq);
        if (((turnCount) % SlanFreq) == 0) {
            Depo.add(RobotType.SLANDERER);

        }
//        System.out.println("Slanderer depo");
        if (((turnCount) % PolFreq) == 0) {
            Depo.add(RobotType.POLITICIAN);
        }
//        System.out.println("Politician Depo");
        if (((turnCount) % MuckFreq) == 0) {
            Depo.add(RobotType.MUCKRAKER);
        }
//        System.out.println("At final depo");
        if (Depo.size() > 0) {
            RobotType d = Depo.remove();
            int inf = 5;
            switch (d) {
                case SLANDERER:
                    inf = 22;
                    break;
                case POLITICIAN:
                    inf = 15;
                    break;
                case MUCKRAKER:
                    inf = 5;
                    break;
                default:
                    break;
            }

            System.out.println("Before random direction");

            Random r = new Random();
            Direction toSend = directions[r.nextInt(directions.length)];
            int i = 0;
            while (!rc.canBuildRobot(d, toSend, inf) && i < 8) {
                toSend = directions[r.nextInt(directions.length)];
                i++;
            }
            System.out.println("Direction known");

            if (rc.canBuildRobot(d, toSend, inf))
                rc.buildRobot(d, toSend, inf);

        }
    }

}