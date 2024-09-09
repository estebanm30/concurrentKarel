import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;

import java.util.*;

public class ParalelRobot extends Robot implements Runnable {

    public int[] initialPos = new int[2];
    public int[] actualPos = new int[2];
    public Stop stop;

    public ParalelRobot(int Street, int Avenue, Direction direction, int beepers, Color color, int streetDest,
            int avenueDest, Stop stop) {
        super(Street, Avenue, direction, beepers, color);
        this.initialPos[0] = Street;
        this.initialPos[1] = Avenue;
        this.actualPos[0] = Street;
        this.actualPos[1] = Avenue;
        this.stop = stop;
        World.setupThread(this);

        String pos = this.actualPos[0] + "," + this.actualPos[1];
        try {
            ConcurrentKarel.positionSemaphores.get(pos).acquire();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void work() {
        goTo(3, 17);
        goToStop(); // Robot go to beepers
        while (ConcurrentKarel.totalBeepers != 0) {
            this.pick(); // Pick beeper and set random stop
            goToStop();
            goToPrincipal(this.stop.wayToPrincipal, ConcurrentKarel.principalAvenue); // Go to principal avenue in
                                                                                      // avenue 10
            goToPrincipal(ConcurrentKarel.wayBackToBeepers, 100000); // Go from principal to beepers
        }
        this.stop = ConcurrentKarel.stopsArr[5]; // Parking stop
        goToStop();
        goTo(7, 12);
        goTo(4, 18);
    }

    void park(){

    }

    void calculateDirections() {
        String facing;
        if (this.facingEast()) {
            this.actualPos[1] += 1;
            facing = "east";
        } else if (this.facingWest()) {
            this.actualPos[1] -= 1;
            facing = "west";
        } else if (this.facingNorth()) {
            this.actualPos[0] += 1;
            facing = "north";
        } else if (this.facingSouth()) {
            this.actualPos[0] -= 1;
            facing = "south";
        }
    }

    void c_move() {
        String prevPost = this.actualPos[0] + "," + this.actualPos[1];
        this.calculateDirections();
        String nextPositionKey = this.actualPos[0] + "," + this.actualPos[1];
        try {
            ConcurrentKarel.positionSemaphores.get(nextPositionKey).acquire();
            this.move();
            ConcurrentKarel.positionSemaphores.get(prevPost).release();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void turnDirection(String direction) {
        if (direction.equals("East")) {
            while (!this.facingEast()) {
                this.turnLeft();
            }
        } else if (direction == "West") {
            while (!this.facingWest()) {
                this.turnLeft();
            }
        } else if (direction == "South") {
            while (!this.facingSouth()) {
                this.turnLeft();
            }
        } else {
            while (!this.facingNorth()) {
                this.turnLeft();
            }
        }
    }

    void enterStop() throws InterruptedException {
        int nStop = this.stop.nStop;
        String nextPos = "";
        switch (nStop) {
            case 1:
                System.out.println(this.actualPos[0] + "," + (int) (this.actualPos[1] + 1));
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] + 1);
                try {
                    if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == 0) {
                        this.wait();
                    }
                } catch (Exception e) {
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).acquire();
                break;
            case 2:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] - 1);
                try {
                    if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == 0) {
                        this.wait();
                    }
                } catch (Exception e) {
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).acquire();
                break;
            case 3:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] + 1);
                try {
                    if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == 0) {
                        this.wait();
                    }
                } catch (Exception e) {
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).acquire();
                break;

            default:
                break;
        }
    }

    void leaveStop() {
        int nStop = this.stop.nStop;
        String nextPos = "";
        switch (nStop) {
            case 1:
                nextPos = this.actualPos[0] + "," + (this.actualPos[1]);
                if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == this.stop.maxRobots) {
                    this.notifyAll();
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).release();
                break;
            case 2:
                nextPos = this.actualPos[0] + "," + this.actualPos[1];
                if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == this.stop.maxRobots) {
                    this.notifyAll();
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).release();
                break;
            case 3:
                nextPos = this.actualPos[0] + "," + this.actualPos[1];
                if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == this.stop.maxRobots) {
                    this.notifyAll();
                }
                ConcurrentKarel.positionStopSemaphores.get(nextPos).release();
                break;

            default:
                break;
        }
    }

    void goToStop() {
        int i = 0;
        while (!((this.stop.entrancePos[0] == this.actualPos[0]) && (this.stop.entrancePos[1] == this.actualPos[1]))) {
            String direction = this.stop.directionList[i];
            turnDirection(direction);
            while (this.frontIsClear()
                    && !((this.stop.entrancePos[0] == this.actualPos[0])
                            && (this.stop.entrancePos[1] == this.actualPos[1]))) {
                c_move();
            }
            i++;
        }
        try {
            enterStop();
            if (this.stop.nStop != 0) {
                goTo(this.stop.dropPos[0], this.stop.dropPos[1]);
                this.putBeeper();
                stopRoute();
            }
        } catch (Exception e) {
        } finally {
            leaveStop();
        }

    }

    void stopRoute() {
        int nStop = this.stop.nStop;
        switch (nStop) {
            case 1:
                goTo(17, 3);
                goTo(16, 5);
                goTo(18, 6);
                break;
            case 2:
                goTo(10, 4);
                goTo(11, 5);
                goTo(12, 6);
                goTo(10, 7);
                break;
            case 3:
                goTo(6, 8);
                break;

            default:
                break;
        }
    }

    void goToPrincipal(String[] directions, int limit) {
        int i = 0;
        while (!((limit == this.actualPos[1])) && i < directions.length) {
            String direction = directions[i];
            turnDirection(direction);
            while (this.frontIsClear()
                    && !((limit == this.actualPos[1]))) {
                c_move();
            }
            i++;
        }
    }

    void goTo(int streetDest, int avenueDest) {
        int streetDiff = this.actualPos[0] - streetDest;
        int avenueDiff = this.actualPos[1] - avenueDest;

        if (avenueDiff != 0) {
            if (this.actualPos[1] < avenueDest) {
                while (!this.facingEast()) {
                    this.turnLeft();
                }
                while (this.actualPos[1] != avenueDest) {
                    this.c_move();
                }
            } else {
                while (!this.facingWest()) {
                    this.turnLeft();
                }
                while (this.actualPos[1] != avenueDest) {
                    this.c_move();
                }
            }
        }

        if (streetDiff != 0) {
            if (this.actualPos[0] > streetDest) {
                while (!this.facingSouth()) {
                    this.turnLeft();
                }
                while (this.actualPos[0] != streetDest) {
                    this.c_move();
                }
            } else {
                while (!this.facingNorth()) {
                    this.turnLeft();
                }
                while (this.actualPos[0] != streetDest) {
                    this.c_move();
                }
            }
        }
    }

    void pick() {
        this.pickBeeper();
        ConcurrentKarel.totalBeepers -= 1;
        Random rand = new Random();
        int r = rand.nextInt(1, 5); // todo hasta 4
        this.stop = ConcurrentKarel.stopsArr[r];
    }

    @Override
    public void run() {
        work();
    }
}