import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.Random;

public class ParalelRobot extends Robot implements Runnable {

    public int[] actualPos = new int[2];
    public Stop stop;
    int takenPermits;
    Random rand = new Random();

    public ParalelRobot(int Street, int Avenue, Direction direction, int beepers, Color color, int streetDest,
            int avenueDest, Stop stop) {
        super(Street, Avenue, direction, beepers, color);
        this.actualPos[0] = Street;
        this.actualPos[1] = Avenue;
        this.stop = stop;
        World.setupThread(this);

        String pos = this.actualPos[0] + "," + this.actualPos[1];
        try {
            ConcurrentKarel.cMoveSemaphore.acquire();
            ConcurrentKarel.positionsUsed.add(pos);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            ConcurrentKarel.cMoveSemaphore.release();
        }

    }

    void work() {
        goTo(3, 17); // Robot to exit parking
        goToStop(); // Robot to beepers
        while (ConcurrentKarel.totalBeepers != 0) {
            this.pick(); // Pick beeper and set random stop
            goToStop(); // Go to random stop
            followDirections(this.stop.wayToPrincipal, ConcurrentKarel.principalPos[1],
                    ConcurrentKarel.principalPos[0]); // Go to principal avenue
            followDirections(ConcurrentKarel.wayBackToBeepers, 100000, 0); // Go from principal to beepers
        }
        this.stop = ConcurrentKarel.stopsArr[5]; // Parking stop
        goToStop(); // Go to parking stop
        // Line up in parking
        goTo(7, 12);
        goTo(4, 18);
    }

    void calculateDirections() {
        if (this.facingEast()) {
            this.actualPos[1] += 1;
        } else if (this.facingWest()) {
            this.actualPos[1] -= 1;
        } else if (this.facingNorth()) {
            this.actualPos[0] += 1;
        } else if (this.facingSouth()) {
            this.actualPos[0] -= 1;
        }
    }

    void cMove() {

        String actualPos = this.actualPos[0] + "," + this.actualPos[1];
        this.calculateDirections(); // calculates next direction, dosn't move
        String nextPositionKey = this.actualPos[0] + "," + this.actualPos[1];

        try {
            // Wait until the position is available
            ConcurrentKarel.waitForPosition(nextPositionKey);

            ConcurrentKarel.positionsUsed.add(nextPositionKey);
            this.move();

            // Use semaphore to handle concurrent move operations
            ConcurrentKarel.cMoveSemaphore.acquire();
            ConcurrentKarel.positionsUsed.remove(actualPos);

            // Notify other threads that the position is now available
            ConcurrentKarel.notifyPositionAvailable(nextPositionKey);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interruption status
            e.printStackTrace();
        } finally {
            ConcurrentKarel.cMoveSemaphore.release();
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
        String nextPos = "";
        switch (this.stop.nStop) {
            case 1:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] + 1);
                break;
            case 2:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] - 1);
                break;
            case 3:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] + 1);
                break;
            case 4:
                nextPos = this.actualPos[0] + "," + (int) (this.actualPos[1] - 1);
                break;
            default:
                return;
        }
        try {
            if (ConcurrentKarel.positionStopSemaphores.get(nextPos).availablePermits() == 0) {
                this.wait();
            }
        } catch (Exception e) {
        }
        this.stop.semaphoreEntrance.acquire();
        if (this.stop.nStop != 3 && this.stop.nStop != 4) {
            this.stop.semaphoreExit.acquire();
        }
    }

    void leaveStop() {
        if (this.stop.nStop != 0 && this.stop.nStop != 5) {
            String nextPos = "";
            nextPos = this.actualPos[0] + "," + (this.actualPos[1]);
            this.stop.cont++;
            if (this.stop.cont == this.stop.maxRobots) {
                ConcurrentKarel.positionStopSemaphores.get(nextPos).release(this.stop.maxRobots);
                this.stop.cont = 0;
            }

            if (this.stop.nStop != 3 && this.stop.nStop != 4) {
                this.stop.semaphoreEntrance.release(this.takenPermits);
                this.stop.semaphoreExit.release();
            }
        }
    }

    void goToStop() {
        int i = 0;
        while (!((this.stop.entrancePos[0] == this.actualPos[0]) && (this.stop.entrancePos[1] == this.actualPos[1]))
                && i < this.stop.directionList.length) {
            String direction = this.stop.directionList[i];
            turnDirection(direction);
            if (!this.frontIsClear()) {
                i++;
                continue;
            }
            cMove();
        }
        try {
            enterStop();
            if (this.stop.nStop == 4) {
                routeStop4();
            } else if (this.stop.nStop != 0) {
                goTo(this.stop.dropPos[0], this.stop.dropPos[1]);
                this.putBeeper();
                stopRoute();
            }
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            leaveStop();
        }

    }

    public void routeStop4() {
        try {
            ConcurrentKarel.bayArr[1].mySemaphore.acquire();
            goTo(11, 15);
            goTo(15, 11);
            goTo(15, 15);
            if (ConcurrentKarel.bayArr[2].mySemaphore.availablePermits() == 0) {
                goTo(15, 16);
            } else {
                ConcurrentKarel.bayArr[1].mySemaphore.release();
            }
            ConcurrentKarel.bayArr[2].mySemaphore.acquire();
            goTo(13, 15);
            goTo(16, 17);
            goTo(16, 12);
            if (ConcurrentKarel.bayArr[3].mySemaphore.availablePermits() == 0) {
                goTo(16, 11);
            } else {
                ConcurrentKarel.bayArr[2].mySemaphore.release();
            }
            ConcurrentKarel.bayArr[3].mySemaphore.acquire();
            goTo(17, 12);
            goTo(12, 18);
            if (ConcurrentKarel.bayArr[4].mySemaphore.availablePermits() == 0) {
                goTo(12, 17);
            } else {
                ConcurrentKarel.bayArr[3].mySemaphore.release();
            }
            ConcurrentKarel.bayArr[4].mySemaphore.acquire();
            goTo(9, 18);
            goTo(18, 19);
            this.putBeeper();
            // TODO: Hacer atomico el intento de coger los locks
            for (Bay bay : ConcurrentKarel.bayArr) {
                if (bay.mySemaphore.availablePermits() > 0) {
                    bay.mySemaphore.acquire();
                }
            }
            goTo(9, 19);
            goTo(12, 18);
            goTo(17, 18);
            ConcurrentKarel.bayArr[3].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[2].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[1].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[0].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[4].mySemaphore.release();
            goTo(16, 12);
            ConcurrentKarel.bayArr[2].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[1].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[0].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[3].mySemaphore.release();
            goTo(13, 17);
            goTo(15, 15);
            ConcurrentKarel.bayArr[1].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[0].mySemaphore.tryAcquire();
            ConcurrentKarel.bayArr[2].mySemaphore.release();
            goTo(11, 11);
            goTo(11, 15);
            goTo(10, 15);
            ConcurrentKarel.bayArr[1].mySemaphore.release();
            ConcurrentKarel.bayArr[0].mySemaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void stopRoute() {
        switch (this.stop.nStop) {
            case 1:
                try {
                    this.stop.semaphoreExit.release();
                    goTo(17, 3);
                    goTo(16, 5);
                    if (this.stop.semaphoreEntrance.availablePermits() > 0) {
                        this.takenPermits = this.stop.semaphoreEntrance.drainPermits();
                    }
                    this.stop.semaphoreExit.acquire();
                    goTo(18, 6);
                } catch (Exception e) {
                }
                break;
            case 2:
                try {
                    this.stop.semaphoreExit.release();
                    goTo(10, 4);
                    goTo(11, 5);
                    goTo(12, 6);
                    if (this.stop.semaphoreEntrance.availablePermits() > 0) {
                        this.takenPermits = this.stop.semaphoreEntrance.drainPermits();
                    }
                    this.stop.semaphoreExit.acquire();
                    goTo(10, 7);
                } catch (Exception e) {
                }
                break;
            case 3:
                goTo(6, 8);
                break;
            default:
                break;
        }
    }

    void followDirections(String[] directions, int avenue, int street) {
        int i = 0;
        while (!((avenue == this.actualPos[1]) && (street == this.actualPos[0])) && i < directions.length) {
            String direction = directions[i];
            turnDirection(direction);
            if (!this.frontIsClear()) {
                i++;
                continue;
            }
            cMove();
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
                    this.cMove();
                }
            } else {
                while (!this.facingWest()) {
                    this.turnLeft();
                }
                while (this.actualPos[1] != avenueDest) {
                    this.cMove();
                }
            }
        }

        if (streetDiff != 0) {
            if (this.actualPos[0] > streetDest) {
                while (!this.facingSouth()) {
                    this.turnLeft();
                }
                while (this.actualPos[0] != streetDest) {
                    this.cMove();
                }
            } else {
                while (!this.facingNorth()) {
                    this.turnLeft();
                }
                while (this.actualPos[0] != streetDest) {
                    this.cMove();
                }
            }
        }
    }

    void pick() {
        this.pickBeeper();
        ConcurrentKarel.totalBeepers -= 1;
        int r = this.rand.nextInt(1, 5);
        this.stop = ConcurrentKarel.stopsArr[r];
    }

    @Override
    public void run() {
        work();
    }
}
