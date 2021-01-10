package bot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Muckraker extends RobotPlayer {

    // TODO: getType(), raiseFlag()

    // Sceptical about how the flag works
    // If the flag of the individual Muckraker is raised, then will have to save the flags in a separate list
    // and then work forward in raising the flags one at a time

    static final int actionR2 = rc.getType().actionRadiusSquared;
    static final int detectionR2 = rc.getType().detectionRadiusSquared;
    static final int sensorR2 = rc.getType().sensorRadiusSquared;


    static ArrayList<Integer> x_coordEC = new ArrayList<>(), y_coordEC = new ArrayList<>();
    static ArrayList<Integer> x_coord = new ArrayList<>(), y_coord = new ArrayList<>();

    // Neutral EC should be a priority if we find multiple EC I guess;
    // Precise location of only a limited amount of EC's can be sent at a time so we need to prioritize
    // say muckraker spots 6 EC, then we need to set a target location to pass on as it might happen like
    // sent 1 NEC and 2 EEC ? Bad Idea

    static ArrayList<Integer> typeofEC = new ArrayList<>();

    static Map<Direction, Integer> directionToNumberMap;

    private static int muckrakerCount = 0;
    private static int politicianCount = 0;
    private static int enlightenmentCenterCount = 0;

    static int roundCreated;


    private static int getBits(int n) {
        n = n >> 6;
        return n & ((1 << 9) - 1);
    }


    static Direction moveNext;

    // Set up muckraker
    public static void setup() throws GameActionException {
        directionToNumberMap = new HashMap<Direction, Integer>();
        directionToNumberMap = null;
        for (int i = 0; i < 8; ++i){
            directionToNumberMap.put(directions[i], i);
        }
        RobotInfo[] ecFinder = rc.senseNearbyRobots();
        RobotInfo parentEC = null;
        for (RobotInfo r : ecFinder) {
            if (r.type == RobotType.ENLIGHTENMENT_CENTER && r.team != rc.getTeam().opponent()) {
                parentEC = r;
                break;
            }
        }

        roundCreated = rc.getRoundNum();

        if (parentEC == null) throw new AssertionError();

        int location;

        if (rc.canGetFlag(parentEC.ID)) {

            location = rc.getFlag(parentEC.ID);
            int dirs = getBits(location); // get the 9 bits for direction

            ArrayList<Integer> directionsPassed = new ArrayList<>();

            for (int i = 0; i < 3; i++) {

                int dir = dirs & ((1 << 3) - 1);
                directionsPassed.add(dir);
                dirs >>= 3;

            }

            Random r = new Random();
//            int toMove = directionsPassed.get(r.nextInt(directionsPassed.size())); // get a random direction to move
            int toMove = 1;

            switch (toMove) {

                case 0:
                    moveNext = Direction.NORTH;
                    break;
                case 1:
                    moveNext = Direction.NORTHEAST;
                    break;
                case 2:
                    moveNext = Direction.EAST;
                    break;
                case 3:
                    moveNext = Direction.SOUTHEAST;
                    break;
                case 4:
                    moveNext = Direction.SOUTH;
                    break;
                case 5:
                    moveNext = Direction.SOUTHWEST;
                    break;
                case 6:
                    moveNext = Direction.WEST;
                    break;
                case 7:
                    moveNext = Direction.NORTHWEST;
                    break;

            }

        }


    }


    public static void getEnemyType() throws GameActionException {

        Team enemy = rc.getTeam().opponent();
        RobotInfo[] e = rc.senseNearbyRobots();

        for (RobotInfo r : e) {

            if (r.getType() == RobotType.ENLIGHTENMENT_CENTER &&
                    (r.getTeam() == enemy || r.getTeam() == Team.NEUTRAL)) {

                enlightenmentCenterCount += 1;

                if (r.getTeam() == Team.NEUTRAL) {
                    typeofEC.add(1);
                } else {
                    typeofEC.add(0);
                }


                //Location WRT the Muckraker
                x_coordEC.add((r.location.x - rc.getLocation().x) + 10); // No negatives this way
                y_coordEC.add((r.location.y - rc.getLocation().y) + 10); // No negatives this way

            } else if ((r.getType() == RobotType.MUCKRAKER || r.getType() == RobotType.POLITICIAN) &&
                    r.getTeam() == enemy) {

                if (r.getType() == RobotType.POLITICIAN) {
                    politicianCount += 1;
                } else {
                    muckrakerCount += 1;
                }

                x_coord.add(r.location.x);
                y_coord.add(r.location.y);

            } else {

                // If not Muckraker or Politician or EC then it'll be Slanderer, so we can expose him.
                exposeAction(r);

            }

        }

        moveOrFlag();

    }


    static int meanPosition(ArrayList<Integer> a) {

        int mean, sum = 0;
        for (int i : a)
            sum += i;

        mean = sum / a.size();

        return mean;

    }


    public static void raiseFlag() throws GameActionException {

        // If we have something in the x_coordEC arrayList then we need to send a flag precisely about
        // the location of the EC and whether it is enemy EC or Neutral EC

        // Else what we have to do is find the average position of politicians and muckraker (mean)
        // and send the information to EC

        int i = 0;

        if (x_coord.size() != 0) {

            // Mean density is only required as opponents will be moving

            int x_mean = meanPosition(x_coord) - rc.getLocation().x + 10;
            int y_mean = meanPosition(y_coord) - rc.getLocation().y + 10;

            /* TODO: Max 21 bits used */

            i = (x_mean << 3) + y_mean;

            x_coord.clear();
            y_coord.clear();

        } else if (x_coordEC.size() != 0) {
            int x, y; // To pass finally

            if (x_coordEC.size() >= 2) {

                /*
                 What to do
                 Say EC = [1,0,1,1,0,1], 4 neutral, 2 enemy
                 traverse for all 1's first: find all '1' pairs
                 transmit, and delete
                 continue;
                 find zeros then
                 bad idea, anyway
                 ???????????????????????????????????????
                */

                for (int j = 0; j < 2; j++) {

                    int pos;

                    // If neutral EC exists, do flag them in pairs, else try doing it for enemy EC
                    // If no ec found, break out; (Unnecessary but just to keep check as we send data in pairs)

                    if (typeofEC.contains(1)) {

                        pos = typeofEC.indexOf(1);

                    } else if (typeofEC.contains(0)) {

                        pos = typeofEC.indexOf(0);

                    } else {
                        break;
                    }

                    x = x_coordEC.get(pos);
                    y = x_coordEC.get(pos);

                    // For the first entry, add to i
                    // the next time shift 6 bits in i and add again

                    /*
                     * j = 0
                     * x = 101       y = 100
                     * i = 101100
                     *
                     * j = 1
                     * x = 110       y = 000
                     * i = 101100110000
                     *
                     * 12 bits consumed for one flag
                     * */

                    if (j == 0) {
                        i = (x << 3) + y;
                    } else {
                        i = (i << 6) + ((x << 3) + y);
                    }


                    // Remove it from the list as already flagged.
                    typeofEC.remove(pos);
                    x_coordEC.remove(pos);
                    y_coordEC.remove(pos);

                }


            } else {
                x = x_coordEC.get(0);
                y = y_coordEC.get(0);

                i = (x << 3) + y;
            }

        }

        if (rc.canSetFlag(i)) {
            rc.setFlag(i);
        }

    }


    public static void moveOrFlag() throws GameActionException {
        // While there is something to mark, don't move, but raise flags;
        getEnemyType();
        final int botsToFlagFor = politicianCount + muckrakerCount + enlightenmentCenterCount;

        if (botsToFlagFor >= 30) {
            raiseFlag();
        }

        moveInDirectionSmart(moveNext);

    }

    static void moveInDirectionSmart(Direction dir) throws GameActionException{
        int directionNumber = directionToNumberMap.get(dir);
        Direction[] candidateDirections = {
                directions[directionNumber],
                directions[(directionNumber + 1) % 8],
                directions[(directionNumber - 1) % 8],
                directions[(directionNumber + 2) % 8],
                directions[(directionNumber - 2) % 8]
        };

        double averagePassability = 0.0;
        int unoccupied = 0;
        for (Direction moveDir : candidateDirections){
            if (rc.canMove(moveDir)){
                averagePassability += rc.sensePassability(rc.getLocation().add(moveDir));
                unoccupied++;
            }
        }
        if (unoccupied == 0) unoccupied = 1; // preventing divide by zero error
        averagePassability /= unoccupied;

        for (Direction moveDir : candidateDirections){
            if (rc.canMove(moveDir) &&
                    (rc.sensePassability(rc.getLocation().add(moveDir)) >= averagePassability || averagePassability < 0.3)){
                rc.move(moveDir);
                break;
            }
        }

    }


    public static void muckrakerDirection() throws GameActionException {
//        EnlightenmentCenter.WhereToMove();
        if (moveNext != null && rc.getRoundNum() > roundCreated + 2) {
            tryMove(moveNext);
        }else{
            moveInDirectionSmart(moveNext);
        }
        System.out.println("I moved!");
    }


    static void exposeAction(RobotInfo robot) throws GameActionException {

        if (robot.type.canBeExposed()) {

            // It's a slanderer... go get them!
            if (rc.canExpose(robot.location)) {
                System.out.println("e x p o s e d");
                rc.expose(robot.location);
            }

        }

    }


    static void runMuckraker() throws GameActionException {

        Team enemy = rc.getTeam().opponent();

        for (RobotInfo robot : rc.senseNearbyRobots(actionR2, enemy)) {
            exposeAction(robot);
        }

        moveOrFlag();
        muckrakerDirection();
        System.out.println("I moved");
//
//        if (tryMove(muckrakerDirection()))
//            System.out.println("I moved!");
    }


//    public static void main(String[] args) {
//        System.out.println(Clock.getBytecodeNum());
//    }

}
