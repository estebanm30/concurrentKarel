import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;


import java.util.*;

public class ConcurrentKarel implements Directions {
  public static Map<String, Semaphore> positionSemaphores = Collections.synchronizedMap(new HashMap<>());

  public static void main(String[] args) {

    /* Variables */
    Random rand = new Random();
    int r = 0;
    int n = 0;
    boolean e = false;
    /* Arguments */
    try {
      if (args.length > 0) {
        if (args[0].equals("-r")) {
          r = Integer.parseInt(args[1]);
          if (r != 1 && r != 2 && r != 4) {
            throw new Exception("Argument -r need to be 1, 2 or 4");
          }
        }
        if (args.length >= 3 && args[2].equals("-e")) {
          e = true;
        }
      } else {
        throw new Exception("Argument -r not found");
      }
    } catch (Exception exe) {
      System.err.println(exe);
      System.exit(0);
    }
    /* World Setup */
    setUpWprld(r, rand);

    ParalelSemaphore semaphore1 = new ParalelSemaphore(1, 3, 7000);
    ParalelSemaphore semaphore2 = new ParalelSemaphore(2, 6, 7000);
    ParalelSemaphore semaphore3 = new ParalelSemaphore(4, 8, 7000);
    Thread semaphoreThread1 = new Thread(semaphore1);
    Thread semaphoreThread2 = new Thread(semaphore2);
    Thread semaphoreThread3 = new Thread(semaphore3);
    semaphoreThread1.start();
    semaphoreThread2.start();
    semaphoreThread3.start();

    /* Robots */
    Thread[] threadsArr = setUpRobots(r, n, e, rand);
    for (Thread robot : threadsArr) {
      robot.start();
    }
    ParalelRobot newRobot = new ParalelRobot(1, 9, West, 0, new Color((int) (Math.random() * 0x1000000)), n);
    Thread newThread = new Thread(newRobot);
    newThread.start();
    
  }

  static void setUpWprld(int r, Random rand) {
    // World.setBeeperColor(Color.DARK_GRAY);
    World.showSpeedControl(true);
    World.setSize(8, 10); // 8 calles 10 avenidas
    World.placeNSWall(1, 10, 8);
    World.placeEWWall(8, 1, 10);

    for (int street = 1; street <= 8; street++) {
      for (int avenue = 1; avenue <= 10; avenue++) {
        String key = street + "," + avenue;
        ConcurrentKarel.positionSemaphores.put(key, new Semaphore(1));
      }
    }
    World.setVisible(true);

  }



  static Thread[] setUpRobots(int r, int n, boolean e, Random rand) {
    if (e) {
      while (true) {
        int tmp = rand.nextInt(1, 9);
        if (tmp % 2 == 0) {
          n = tmp;
          System.out.println("Sirenas a recoger: " + n);
          break;
        }
      }
    }
    Thread[] threadsArr = new Thread[r];
    for (int i = 0; i < r; i++) {
      if (e) {
        ParalelRobot robot = new ParalelRobot(i + 1, 2, East, 0, new Color((int) (Math.random() * 0x1000000)), n);
        Thread robotThread = new Thread(robot);
        threadsArr[i] = robotThread;
      } else {
        while (true) {
          int tmp = rand.nextInt(1, 9);
          if (tmp % 2 == 0) {
            System.out.println("Sirenas a recoger robot " + (i + 1) + ": " + tmp);
            ParalelRobot robot = new ParalelRobot(i + 1, 2, East, 0, new Color((int) (Math.random() * 0x1000000)), tmp);
            Thread robotThread = new Thread(robot);
            threadsArr[i] = robotThread;
            break;
          }
        }
      }
    }
    return threadsArr;
  }
}

class ParalelRobot extends Robot implements Runnable {

  public int[] initialPos = new int[2];
  public int[] actualPos = new int[2];

  public ParalelRobot(int Street, int Avenue, Direction direction, int beepers, Color color, int limitBeepers) {
    super(Street, Avenue, direction, beepers, color);
    this.initialPos[0] = Street;
    this.initialPos[1] = Avenue;
    this.actualPos[0] = Street;
    this.actualPos[1] = Avenue;
    World.setupThread(this);

    String pos = this.actualPos[0] + "," + this.actualPos[1];
    try {
      ConcurrentKarel.positionSemaphores.get(pos).acquire();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  void work() {

    Random rand = new Random();
    while (true) {
      int tmp = rand.nextInt(1, 3);
      goTo(8, 8);
      goTo(2, 4);
      
    }
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


  @Override
  public void run() {
    work();
  }
}

class ParalelSemaphore implements Runnable {

  int street;
  int avenue;
  int timeMillis;

  public ParalelSemaphore(int street, int avenue, int timeMillis){
  this.street = street;
  this.avenue = avenue;
  this.timeMillis = timeMillis;
  }

  void semaphoreControl(int street, int avenue, int timeMillis){
    String posSemaphore = street + "," + avenue;
    while (true) {
      try {
        ConcurrentKarel.positionSemaphores.get(posSemaphore).acquire();
        System.out.println("lo tengo " + ConcurrentKarel.positionSemaphores.get(posSemaphore).availablePermits());
        Thread.sleep(timeMillis);
        ConcurrentKarel.positionSemaphores.get(posSemaphore).release();
        System.out.println("lo suelto " + ConcurrentKarel.positionSemaphores.get(posSemaphore).availablePermits());
        Thread.sleep(timeMillis);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  @Override
  public void run() {
    semaphoreControl(this.street, this.avenue, this.timeMillis);
  }
}
