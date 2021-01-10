package bot1;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Politician extends RobotPlayer {

    static Map<Direction, Integer> directionToNumberMap;

    static boolean parentExists;
    static RobotInfo parentEnlightenmentCenter;
    static int parentFlag;

    static Direction moveDirection;

    public static void setup() throws GameActionException {

        directionToNumberMap = new HashMap<>();
        for (int i = 0; i < 8; ++i){
            directionToNumberMap.put(directions[i], i);
        }
        System.out.println("New politician created at" + rc.getLocation().x + ", " + rc.getLocation().y);


        setParentEnlightenmentCenter();
        if (parentExists) setParentFlag();
        setInitialMoveDirection();

    }

    static public void runPolitician() throws GameActionException{
        giveSpeechIfProfitable(0.5);
        int closestEnemyFlag = getClosestEnemyLocationFlag();
        int dist = getDist(closestEnemyFlag);
        int dirNum = getDirNum(closestEnemyFlag);

        if (dist != 0){
//            moveInDirectionNaive(directions[dirNum]);
            moveInDirectionSmart(directions[dirNum]);
        }else{
            moveInDirectionSmart(moveDirection);
        }
    }

    static void setParentEnlightenmentCenter(){
        RobotInfo[] adjacentRobotList = rc.senseNearbyRobots(2, rc.getTeam());
        parentExists = false;
        for (RobotInfo adjacentRobot : adjacentRobotList){
            if (adjacentRobot.type == RobotType.ENLIGHTENMENT_CENTER){
                parentExists = true;
                parentEnlightenmentCenter = adjacentRobot;
            }
        }
    }

    static void setParentFlag() throws GameActionException {
        parentFlag = rc.getFlag(parentEnlightenmentCenter.ID);
    }

    static Direction getDirectionFromFlag(int flag){
        int directionNumber = (flag % 8); //gets last 3 bits
        return directions[directionNumber];
    }
    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static void setInitialMoveDirection(){

        if (!parentExists) {
            moveDirection = randomDirection();
            return;
        }
        Direction dir1 = getDirectionFromFlag(parentFlag);
        Direction dir2 = getDirectionFromFlag(parentFlag/8); //shifted by 3 bits

        boolean randBool = ( (int) (Math.random() * 2) == 0 );
        moveDirection = randBool ? dir1 : dir2;
    }

    static void moveInDirectionNaive(Direction dir) throws GameActionException{
        if (rc.canMove(dir)) rc.move(dir);
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

    static public double speechProfitability(){
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        int conviction = rc.getConviction();
        int robotCount = rc.detectNearbyRobots(actionRadius).length;
        int enemyCount = rc.senseNearbyRobots(actionRadius, enemy).length;

        if (conviction < 10 || enemyCount == 0) return 0.0;

        double convictionPerEnemy = (double) (conviction - 10) / (double) robotCount;
        double halfPoint = 15;

        return Math.atan(convictionPerEnemy/halfPoint)*(2/Math.PI);
    }

    static public void giveSpeechIfProfitable(double threshold) throws GameActionException {
        double profitability = speechProfitability();
        int actionRadius = rc.getType().actionRadiusSquared;
        if (profitability >= threshold && rc.canEmpower(actionRadius)){
            rc.empower(actionRadius);
        }
    }

    static public int getFlagFromLocation(MapLocation robotLocation){
        MapLocation myLoc = rc.getLocation();
        int dist = myLoc.distanceSquaredTo(robotLocation);
        Direction dir = myLoc.directionTo(robotLocation);
        int dirNum = directionToNumberMap.get(dir);

        return (dist * 8) + dirNum; // 6bit + 3bit location format
    }

    static public int getDist(int flag){ return flag / 8; }
    static public int getDirNum(int flag){ return flag % 8; }

    static public ArrayList<Integer> getFriendFlagList() throws GameActionException {
        RobotInfo[] friendList = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam());
        ArrayList<Integer> flagList = new ArrayList<>();
        for (RobotInfo friend : friendList){
            if (friend.type == RobotType.POLITICIAN){
                flagList.add( rc.getFlag(friend.ID) );
            }
        }
        return flagList;
    }

    static public ArrayList<Integer> getSenseRadiusEnemyFlagList(){
        RobotInfo[] enemyList = rc.senseNearbyRobots(rc.getType().sensorRadiusSquared, rc.getTeam().opponent());
        ArrayList<Integer> flagList = new ArrayList<>();
        for (RobotInfo enemy : enemyList){
            flagList.add(getFlagFromLocation(enemy.getLocation()));
        }
        return flagList;
    }

    static public int getClosestEnemyLocationFlag() throws GameActionException{
        ArrayList<Integer> friendFlagList = getFriendFlagList();
        ArrayList<Integer> senseRadiusEnemyFlagList = getSenseRadiusEnemyFlagList();
        friendFlagList.addAll(senseRadiusEnemyFlagList);
        if (friendFlagList.size() == 0){ //no flag available
            return 0;
        }
        return Collections.min(friendFlagList); //in 6 + 3 bit system, min(total value) <=> min(dist)
    }

}