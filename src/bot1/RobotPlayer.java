package bot1;

import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

    static final RobotType[] spawnableRobot = {
            RobotType.POLITICIAN,
            RobotType.SLANDERER,
            RobotType.MUCKRAKER,
    };

    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    static int turnCount;

    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
     **/
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;

        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        switch (rc.getType()) {
            case ENLIGHTENMENT_CENTER:
                EnlightenmentCenter.setup();
                break;
            case POLITICIAN:
                Politician.setup();
                break;
            case SLANDERER:
                Slanderer.setup();
                break;
            case MUCKRAKER:
                Muckraker.setup();
                break;

        }
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to freeze
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You may rewrite this into your own control structure if you wish.
//                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                switch (rc.getType()) {
                    case ENLIGHTENMENT_CENTER:
                        EnlightenmentCenter.runEnlightenmentCenter();
                        break;
                    case POLITICIAN:
                        Politician.runPolitician();
                        break;
                    case SLANDERER:
                        Slanderer.runSlanderer();
                        break;
                    case MUCKRAKER:
                        Muckraker.runMuckraker();
                        break;
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }

//    static Direction[] SpecificDirections = {
////            Muckraker.muckrakerDirection(),
//            Politician.politicianDirection(),
//            Slanderer.slandererDirection()
//    };

    static Direction randomDirection() {
        return directions[(int) (Math.random() * directions.length)];
    }

    static void runMuckraker() throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int actionRadius = rc.getType().actionRadiusSquared;
        for (RobotInfo robot : rc.senseNearbyRobots(actionRadius, enemy)) {
            if (robot.type.canBeExposed()) {
                // It's a slanderer... go get them!
                if (rc.canExpose(robot.location)) {
//                    System.out.println("e x p o s e d");
                    rc.expose(robot.location);
                    return;
                }
            }
        }
        if (tryMove(randomDirection()))
            System.out.println();
//            System.out.println("I moved!");
    }

    static void runEnlightenmentCenter() throws GameActionException {
        if (spawnableRobotIndex() != 3) {
            RobotType toBuild = spawnableRobot[spawnableRobotIndex()];
            int influence = 50;
            for (Direction dir : directions) {
                if (rc.canBuildRobot(toBuild, dir, influence)) {
                    rc.buildRobot(toBuild, dir, influence);
                } else {
                    break;
                }
            }
        }else{
            int bidAmt = (int) Math.ceil(rc.getInfluence () * ((double)turnCount/ 3000));
            if (rc.canBid(bidAmt)) {
                rc.bid(bidAmt);
            } else if (rc.canBid(1)) {
                rc.bid(1);
            }
        }
    }


    /**
     * 2: Muckraker
     * 1: Slanderer
     * 0: Politicians
     *
     * @return a specific RobotType
     */
    static int spawnableRobotIndex() {
//        return spawnableRobot[(int) (Math.random() * spawnableRobot.length)];
        // For the initial moves
            if (turnCount % 7 == 0) {
                return 2;
            } else if (turnCount % 17 == 0) {
                return 1;
            } else if (turnCount % 47 == 0) {
                return 0;
            }else {
                return 3;
            }
    }

    /**
     * Attempts to move in a given direction.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     */
    static boolean tryMove(Direction dir) throws GameActionException {
//        System.out.println("Conviction: " + rc.getConviction());
//        System.out.println("Influence: " + rc.getInfluence());
        System.out.println("I am trying to move " + dir + "; " + rc.isReady() + " " + rc.getCooldownTurns() + " " + rc.canMove(dir));
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        } else return false;
    }
}
