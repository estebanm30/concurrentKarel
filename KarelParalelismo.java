import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;

import apple.laf.JRSUIConstants.Direction;

import java.util.*;


public class KarelParalelismo implements Directions {
  public static ArrayList<int[]> posUsed = new ArrayList<int[]>();
  public static int totalBeepers;

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
    /* Robots */
    Thread[] threadsArr = setUpRobots(r, n, e, rand);
    for (Thread robot : threadsArr) {
      robot.start();
    }
  }

  static void setUpWprld(int r, Random rand) {
    KarelParalelismo.totalBeepers = r * 100;
    // World.setBeeperColor(Color.DARK_GRAY);
    World.showSpeedControl(true);
    World.setSize(8, 10); // 8 calles 10 avenidas
    World.placeNSWall(1, 10, 8);
    World.placeEWWall(8, 1, 10);
    for (int i = 0; i < KarelParalelismo.totalBeepers; i++) { // i < r*100
      int randStreet = rand.nextInt(1, 9);
      int randAvenue = rand.nextInt(3, 11);
      World.placeBeepers(randStreet, randAvenue, 1);
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
  public int limitBeepers;
  public int currBeepers;
  public int[] initialPos = new int[2];
  public int[] actualPos = new int[2];
  public Semaphore semaphore = new Semaphore(1);


  public ParalelRobot(int Street, int Avenue, Direction direction, int beepers, Color color, int limitBeepers) {
    super(Street, Avenue, direction, beepers, color);
    this.limitBeepers = limitBeepers;
    this.initialPos[0] = Street;
    this.initialPos[1] = Avenue;
    this.actualPos[0] = Street;
    this.actualPos[1] = Avenue;
    World.setupThread(this);
  }

  void work() {
    while (KarelParalelismo.totalBeepers > 0) {
      while (this.frontIsClear() && this.currBeepers < this.limitBeepers) {
        this.c_move();
        if (this.checkCorner() && this.nextToABeeper()) {
          int arr[] = { this.actualPos[1], this.actualPos[0] };
          KarelParalelismo.posUsed.add(arr);
          while (this.nextToABeeper() && this.currBeepers < this.limitBeepers && this.actualPos[1] != 1) {
            this.pick();
          }
          KarelParalelismo.posUsed.remove(arr);
          if (this.currBeepers == this.limitBeepers) {
            this.goDeliver();
            this.turnEast();
          }
        }
      }
      if (this.currBeepers == this.limitBeepers) {
        this.goDeliver();
        this.navegate();
      } else {
        this.navegate();
      }
    }
    goDeliver();
    goBack();
  }

  void pick() {
    this.pickBeeper();
    this.currBeepers += 1;
    KarelParalelismo.totalBeepers -= 1;
  }

  void turnRandom() {
    Random rand = new Random();
    int num = rand.nextInt(0, 4);
    for (int i = 0; i < num; i++) {
      this.turnLeft();
    }
  }

  void navegate() {
    if (this.facingEast()) {
      this.turnLeft();
      if (!this.frontIsClear()) {
        this.turnLeft();
        this.turnLeft();
        this.c_move();
        if (this.checkCorner() && this.nextToABeeper()) {
          int arr[] = { this.actualPos[1], this.actualPos[0] };
          KarelParalelismo.posUsed.add(arr);
          while (this.nextToABeeper() && this.currBeepers < this.limitBeepers && this.actualPos[1] != 1) {
            this.pick();
          }
          KarelParalelismo.posUsed.remove(arr);
        }
      } else {
        this.c_move();
        if (this.checkCorner() && this.nextToABeeper()) {
          int arr[] = { this.actualPos[1], this.actualPos[0] };
          KarelParalelismo.posUsed.add(arr);
          while (this.nextToABeeper() && this.currBeepers < this.limitBeepers && this.actualPos[1] != 1) {
            this.pick();
          }
          KarelParalelismo.posUsed.remove(arr);
        }
        this.turnLeft();
      }

    } else if (this.facingNorth()) {
      this.turnLeft();
    } else if (this.facingWest()) {
      this.turnRight();
      if (!this.frontIsClear()) {
        this.turnRight();
      }
      this.c_move();
      if (this.checkCorner() && this.nextToABeeper()) {
        int arr[] = { this.actualPos[1], this.actualPos[0] };
        KarelParalelismo.posUsed.add(arr);
        while (this.nextToABeeper() && this.currBeepers < this.limitBeepers && this.actualPos[1] != 1) {
          this.pick();
        }
        KarelParalelismo.posUsed.remove(arr);
      }
      this.turnRight();
    } else if (this.facingSouth()) {
      this.turnLeft();
      if (!this.frontIsClear()) {
        this.turnLeft();
        this.turnLeft();
      }
    }
  }

  void turnEast() {
    while (!this.facingEast()) {
      this.turnLeft();
    }
  }

  void turnRight() {
    this.turnLeft();
    this.turnLeft();
    this.turnLeft();
  }

  void goBack() {
    int streetDiff = this.actualPos[0] - this.initialPos[0];
    int avenueDiff = this.actualPos[1] - this.initialPos[1];

    if (avenueDiff != 0) {
      if (this.actualPos[1] < this.initialPos[1]) {
        while (!this.facingEast()) {
          this.turnLeft();
        }
        while (this.actualPos[1] != this.initialPos[1]) {
          this.move();
          this.calculateDirections();
        }
      }
    }

    if (streetDiff != 0) {
      if (this.actualPos[0] > this.initialPos[0]) {
        while (!this.facingSouth()) {
          this.turnLeft();
        }
        while (this.actualPos[0] != this.initialPos[0]) {
          this.move();
          this.calculateDirections();
        }
      } else {
        while (!this.facingNorth()) {
          this.turnLeft();
        }
        while (this.actualPos[0] != this.initialPos[0]) {
          this.move();
          this.calculateDirections();
        }
      }
    }
    this.turnOff();
  }

  void goDeliver() {
    int streetDiff = this.actualPos[0] - this.limitBeepers;
    int avenueDiff = this.actualPos[1] - 1;

    if (avenueDiff != 0) {
      if (this.actualPos[1] > 1) {
        while (!this.facingWest()) {
          this.turnLeft();
        }
        while (this.actualPos[1] != 1) {
          this.move();
          this.calculateDirections();
        }
      }

    }

    if (streetDiff != 0) {
      if (this.actualPos[0] > this.limitBeepers) {
        while (!this.facingSouth()) {
          this.turnLeft();
        }
        while (this.actualPos[0] != this.limitBeepers) {
          this.move();
          this.calculateDirections();
        }
      } else {
        while (!this.facingNorth()) {
          this.turnLeft();
        }
        while (this.actualPos[0] != this.limitBeepers) {
          this.move();
          this.calculateDirections();
        }
      }
    }
    while (this.anyBeepersInBeeperBag()) {
      this.putBeeper();
      this.currBeepers -= 1;
    }

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

  void c_move() {
    this.calculateDirections();
    this.move();
  }

  boolean checkCorner() {
    for (int i = 0; i < KarelParalelismo.posUsed.size(); i++) {
      if (KarelParalelismo.posUsed.get(i)[0] == this.actualPos[1]
          && KarelParalelismo.posUsed.get(i)[1] == this.actualPos[0]) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void run() {
    work();
  }
}
