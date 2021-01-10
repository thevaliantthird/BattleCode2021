package bot1;

import battlecode.common.*;

import java.util.HashMap;
import java.util.Map;

import static battlecode.common.Direction.*;

public class Slanderer extends RobotPlayer {

    public static int sourceID; // ID of the source Enlightenment Center
    public static int roundCreated;    // round in which it is built
    public static int currentRound;
    public static Direction directionToEC;  // shortest approximate direction of the EC from the current location of slanderer


    public static int dangerDetectedInitial = 0;   // to be set to 1 if slanderer detects any enemy around it while encircling the EC and then it is supposed to move opposite to it till it reaches a safe place
    public static Team enemy;
    public static Direction moveInDir;    // direction in which the slanderer is supposed to move at the current time
    public static MapLocation sourceLoc;    // maplocation of the source EC
    public static int initialMovementDone = 0;   // to check whether the most initial movement of the slanderer has been completed or not
    public static int eastMovementDone = 0;       // checks whether it has moved Eastwards by 9 sq.units just after creation or not
    public static int northMovementDone = 0;     // similar to above variable
    public static int dangerDetectedAfterwards = 0; // danger detected after 50 rounds

    public static int enemy_loc_x = 0;  //x coordinates of enemy detected
    public static int enemy_loc_y = 0;   // y coordinate of enemy detected
    public static int flag_x;    // average of x coordinates of all the enemies detected
    public static int flag_y;   // avg of y coordinates of all the enemies detected
    public static int count_enemy = 0; // count of enemy within the sensor radius of slanderer
    public static int avgDistSquared;    // distance squared of slanderer from the avg location of enemies
    public static Direction directionOfEnemy;    // direction of avg map location from current location of slanderer
    public static int flag_dir;     // direction part of the flag to be raised after sensing an enemy
    public static int flagValue;

    public static Team homeTeam;
    public static int directionDecided;    // determines whether a direction can be decided to move in, according to the flag received
    public static int flag_fellow;    // flag received by team member
    public static int distPartOfFlag;     // part of flag_fellow signifying the distance
    public static int directionPartOfFlag;    // part of flag_fellow signifying the direction
    public static Direction passiveSuggestedDirection;    // direction suggested according to the fellow_flag
    public static int gotFirstFlag = 0;

    static Map<Direction, Integer> directionToNumberMap;

    public static void setup() throws GameActionException  // called only once i.e. when the slanderer is first created to setup constants
    {
        directionToNumberMap = new HashMap<>();
        for (int i = 0; i < 8; ++i){
            directionToNumberMap.put(directions[i], i);
        }
        sourceLoc = rc.getLocation().add(WEST);
        enemy = rc.getTeam().opponent();
        homeTeam = rc.getTeam();
        sourceID = rc.senseRobotAtLocation(rc.getLocation().add(WEST)).ID;
        roundCreated = rc.getRoundNum();


    }


    public static void dangerDetectedAndMove() throws GameActionException {

        if (rc.getRoundNum() - roundCreated < 50)
            dangerDetectedInitial = 1;
        else
            dangerDetectedAfterwards = 1;
        // if danger is detected within the sensor radius, move in a direction opposite to the enemy direction
        // Enemy location in increasing order
        moveInDirectionSmart(rc.getLocation().directionTo(rc.senseNearbyRobots(rc.getLocation(),rc.getType().sensorRadiusSquared, enemy)[0].location).opposite());

    }

    public static void encircleEnlightenmentCenter() throws GameActionException {

        if (dangerDetectedInitial == 1)    // if danger is detected before first 50 rounds, no need to encircle the EC, first priority is to run
            return;

        // following are two if blocks to encircle the source EC for first 50 rounds
        if (initialMovementDone == 0) {
            if (rc.getLocation().distanceSquaredTo(sourceLoc) == 9)
                eastMovementDone = 1;
            if (eastMovementDone == 0)
                rc.move(EAST);
            if (rc.getLocation().distanceSquaredTo(sourceLoc) == 18)
                northMovementDone = 1;
            if (eastMovementDone == 1 && northMovementDone == 0)
                rc.move(NORTH);
            if (northMovementDone == 1 && eastMovementDone == 1)
                initialMovementDone = 1;


        }

        if (initialMovementDone == 1) {
            directionToEC = rc.getLocation().directionTo(sourceLoc);
            if (rc.getLocation().distanceSquaredTo(sourceLoc) == 18)
                moveInDir = directionToEC.rotateRight();
            if (rc.canMove(moveInDir))
                rc.move(moveInDir);

        }

    }


    public static void moveToSafeZone() throws GameActionException {               //function to govern the behaviour of slanderer for rounds between 50 to 300


        if (gotFirstFlag == 0 && rc.canGetFlag(sourceID))   // to be executed if either the slanderer has just finished the 50th round or if he has detected some danger in its surroundings
        {
            int ECFlag = rc.getFlag(sourceID) >> 16;
            for (int i = 7; i >= 0; i--) {
                if ((ECFlag >> i) != Math.pow(2, 8 - i) - 1)    // Decoding the flag received by the Enlightenment Center
                {
                    switch (i) {
                        case 7:
                            moveInDir = NORTH;
                            directionDecided = 1;
                            break;
                        case 6:
                            moveInDir = NORTHEAST;
                            directionDecided = 1;
                            break;
                        case 5:
                            moveInDir = EAST;
                            directionDecided = 1;
                            break;
                        case 4:
                            moveInDir = SOUTHEAST;
                            directionDecided = 1;
                            break;
                        case 3:
                            moveInDir = SOUTH;
                            directionDecided = 1;
                            break;
                        case 2:
                            moveInDir = SOUTHWEST;
                            directionDecided = 1;
                            break;
                        case 1:
                            moveInDir = WEST;
                            directionDecided = 1;
                            break;
                        case 0:
                            moveInDir = NORTHWEST;
                            directionDecided = 1;
                            break;

                    }

                }
                if (directionDecided == 1)
                    break;
            }
            gotFirstFlag = 1;
        }


//
//        if (rc.canMove(moveInDir))
//            rc.move(moveInDir);
//
        moveInDirectionSmart(moveInDir);

    }


    public static void setFlagForTeam() throws GameActionException {
        //to set flag when the slanderer detects an enemy unit



        for (RobotInfo robot : rc.senseNearbyRobots(rc.getLocation(), rc.getType().sensorRadiusSquared, enemy)) {
            enemy_loc_x += robot.location.x;
            enemy_loc_y += robot.location.y;
            count_enemy++;
        }

        flag_x = enemy_loc_x / count_enemy;
        flag_y = enemy_loc_y / count_enemy;
        count_enemy = 0;
        MapLocation avgLoc = new MapLocation(flag_x, flag_y);
        avgDistSquared = rc.getLocation().distanceSquaredTo(avgLoc);
        directionOfEnemy = rc.getLocation().directionTo(avgLoc);

        switch (directionOfEnemy) {

            case NORTH:
                flag_dir = 0;
                break;
            case NORTHEAST:
                flag_dir = 1;
                break;
            case EAST:
                flag_dir = 2;
                break;
            case SOUTHEAST:
                flag_dir = 3;
                break;
            case SOUTH:
                flag_dir = 4;
                break;
            case SOUTHWEST:
                flag_dir = 5;
                break;
            case WEST:
                flag_dir = 6;
                break;
            case NORTHWEST:
                flag_dir = 7;
                break;

        }

        flagValue = (avgDistSquared >> 3) + flag_dir;
        if (rc.canSetFlag(flagValue))
            rc.setFlag(flagValue);


    }


    public static void getTeamFlag() throws GameActionException {


        passiveSuggestedDirection = moveInDir;

        for (RobotInfo fellowRobot : rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, homeTeam)) {

            if(rc.canGetFlag(fellowRobot.ID))
                flag_fellow = rc.getFlag(fellowRobot.ID);



            if (fellowRobot.type == RobotType.ENLIGHTENMENT_CENTER) {

                flag_fellow = flag_fellow >> 16;
                for (int i = 7; i >= 0; i--) {
                    if ((flag_fellow >> i) != Math.pow(2, 8 - i) - 1)    // Decoding the flag received by the Enlightenment Center
                    {
                        switch (i) {
                            case 7:
                                passiveSuggestedDirection = NORTH;
                                directionDecided = 1;
                                break;
                            case 6:
                                passiveSuggestedDirection = NORTHEAST;
                                directionDecided = 1;
                                break;
                            case 5:
                                passiveSuggestedDirection = EAST;
                                directionDecided = 1;
                                break;
                            case 4:
                                passiveSuggestedDirection = SOUTHEAST;
                                directionDecided = 1;
                                break;
                            case 3:
                                passiveSuggestedDirection = SOUTH;
                                directionDecided = 1;
                                break;
                            case 2:
                                passiveSuggestedDirection = SOUTHWEST;
                                directionDecided = 1;
                                break;
                            case 1:
                                passiveSuggestedDirection = WEST;
                                directionDecided = 1;
                                break;
                            case 0:
                                passiveSuggestedDirection = NORTHWEST;
                                directionDecided = 1;
                                break;

                        }
                    }

                }

            }
            else if (fellowRobot.type != RobotType.MUCKRAKER) {
                distPartOfFlag = flag_fellow >> 3;
                directionPartOfFlag = flag_fellow - (distPartOfFlag << 3);

                switch (directionPartOfFlag) {

                    case 0:
                        passiveSuggestedDirection = NORTH.opposite();
                        directionDecided = 1;
                        break;
                    case 1:
                        passiveSuggestedDirection = NORTHEAST.opposite();
                        directionDecided = 1;
                        break;
                    case 2:
                        passiveSuggestedDirection = EAST.opposite();
                        directionDecided = 1;
                        break;
                    case 3:
                        passiveSuggestedDirection = SOUTHEAST.opposite();
                        directionDecided = 1;
                        break;
                    case 4:
                        passiveSuggestedDirection = SOUTH.opposite();
                        directionDecided = 1;
                        break;
                    case 5:
                        passiveSuggestedDirection = SOUTHWEST.opposite();
                        directionDecided = 1;
                        break;
                    case 6:
                        passiveSuggestedDirection = WEST.opposite();
                        directionDecided = 1;
                        break;
                    case 7:
                        passiveSuggestedDirection = NORTHWEST.opposite();
                        directionDecided = 1;
                        break;

                }

            }
            if (directionDecided == 1) {
                directionDecided = 0;
                break;
            }
        }
//
//        if(rc.canMove(passiveSuggestedDirection))
//            rc.move(passiveSuggestedDirection);

        moveInDirectionSmart(passiveSuggestedDirection);

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


    public static void runSlanderer() throws GameActionException {


        // If an enemy is detected within the sensor radius then set both the dangerDetectedVariables to 1, move in the opposite direction
//            and set an appropriate flag for the team
        if (rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, enemy).length != 0) {
            dangerDetectedAndMove();
            setFlagForTeam();
        }

        else {
            dangerDetectedAfterwards = 0;
            dangerDetectedInitial = 0;
        }

        if(rc.getRoundNum() - roundCreated <= 50)
            encircleEnlightenmentCenter();

        if (rc.getRoundNum() - roundCreated > 50 && rc.getRoundNum() - roundCreated < 300)
            moveToSafeZone();


        if (rc.getRoundNum() - roundCreated >= 50 && dangerDetectedInitial == 0 && dangerDetectedAfterwards == 0 && rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, homeTeam).length!=0)
            getTeamFlag();



    }


}