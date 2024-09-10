import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;

import java.util.*;

public class ConcurrentKarel implements Directions {
  public static Map<String, Semaphore> positionSemaphores = Collections.synchronizedMap(new HashMap<>());
  public static Map<String, Semaphore> positionStopSemaphores = Collections.synchronizedMap(new HashMap<>());
  public static Stop[] stopsArr = new Stop[6];
  public static int totalBeepers = 1000;
  public static String[] wayBackToBeepers = { "North", "East", "North", "West", "South", "East", "North" };
  public static int[] principalPos = new int[] { 10, 10 };
  public static BayStop4 bayArr[] = new BayStop4[4];
  public static int maxRobots1 = 8;
  public static int maxRobots2 = 9;
  public static int maxRobots3 = 1;
  public static int maxRobots4 = 1;
  

  public static void main(String[] args) {

    /* Variables */
    int numRobots = 15;

    createStops();
    /* World Setup */
    Thread[] arr = new Thread[2];
    arr = setUpWprld(numRobots);

    /* Robots */

    for (Thread robot : arr) {
      robot.start();
    }

  }

  public static void createStops() {
    int[] beepersPos = { 8, 19 };
    String[] directionsStop0 = { "East", "North" };
    String[] wayToPrincipal0 = null;
    Stop stop0 = new Stop(0, beepersPos, directionsStop0, wayToPrincipal0);
    stopsArr[0] = stop0;

    int[] exitStop1 = { 18, 5 };
    String[] directionsStop1 = { "West", "South", "West", "North", "East" };
    String[] wayToPrincipal1 = { "East", "South", "West", "South", "East", "North", "West" };
    Stop stop1 = new Stop(1, exitStop1, directionsStop1, wayToPrincipal1);
    stopsArr[1] = stop1;

    int[] exitStop2 = { 10, 8 };
    String[] directionsStop2 = { "West", "South", "West", "North", "East", "South", "West", "South", "East", "North",
        "West" };
    String[] wayToPrincipal2 = { "West", "South", "East" };
    Stop stop2 = new Stop(2, exitStop2, directionsStop2, wayToPrincipal2);
    stopsArr[2] = stop2;

    int[] exitStop3 = { 6, 7 };
    String[] directionsStop3 = { "West", "South", "West", "North", "East", "South", "West", "South", "East", "North",
        "West", "South", "East" };
    String[] wayToPrincipal3 = { "East" };
    Stop stop3 = new Stop(3, exitStop3, directionsStop3, wayToPrincipal3);
    stopsArr[3] = stop3;

    int[] exitStop4 = { 10, 16 };
    String[] directionsStop4 = { "West", "South", "West", "North", "East", "South", "West", "South", "East", "North",
        "West" };
    String[] wayToPrincipal4 = { "West" };
    Stop stop4 = new Stop(4, exitStop4, directionsStop4, wayToPrincipal4);
    stopsArr[4] = stop4;

    int[] exitStopParking = { 2, 19 };
    String[] directionsStopPark = { "West", "South", "West", "North", "East", "South", "West", "South", "East", "North",
        "West", "South", "East", "North", "East", "North", "West", "South", "East", "North" };
    String[] wayToPrincipalParking = null;
    Stop stopPark = new Stop(5, exitStopParking, directionsStopPark, wayToPrincipalParking);
    stopsArr[5] = stopPark;
  }

  public static void createBays() {
    BayStop4 b1 = new BayStop4(1, new int[] { 11, 15 }, new String[] { "West", "South", "East" });
    BayStop4 b2 = new BayStop4(2, new int[] { 15, 15 }, new String[] { "East", "South", "West", "North" });
    BayStop4 b3 = new BayStop4(3, new int[] { 16, 12 }, new String[] { "North", "West", "South" });
    BayStop4 b4 = new BayStop4(4, new int[] { 12, 18 }, new String[] { "South", "West", "North" });

    bayArr[0] = b1;
    bayArr[1] = b2;
    bayArr[2] = b3;
    bayArr[3] = b4;
  }

  static Thread[] setUpWprld(int numRobots) {
    World.setBeeperColor(Color.DARK_GRAY);
    World.showSpeedControl(true);
    String key = "";
    for (int street = 1; street <= 20; street++) {
      for (int avenue = 1; avenue <= 20; avenue++) {
        key = street + "," + avenue;
        ConcurrentKarel.positionSemaphores.put(key, new Semaphore(1));
      }
    }
    key = stopsArr[1].posSemaphoreEntrance[0] + "," + stopsArr[1].posSemaphoreEntrance[1];
    Semaphore sem1e = new Semaphore(maxRobots1);
    stopsArr[1].semaphoreEntrance = sem1e;
    ConcurrentKarel.positionStopSemaphores.put(key, sem1e); // Stop 1
    key = stopsArr[1].posSemaphoreExit[0] + "," + stopsArr[1].posSemaphoreExit[1];
    Semaphore sem1ex = new Semaphore(1);
    stopsArr[1].semaphoreExit = sem1ex;
    ConcurrentKarel.positionStopSemaphores.put(key, sem1ex); 

    key = stopsArr[2].posSemaphoreEntrance[0] + "," + stopsArr[2].posSemaphoreEntrance[1];
    Semaphore sem2e = new Semaphore(maxRobots2);
    stopsArr[2].semaphoreEntrance = sem2e;
    ConcurrentKarel.positionStopSemaphores.put(key, sem2e); // Stop 2
    key = stopsArr[2].posSemaphoreExit[0] + "," + stopsArr[2].posSemaphoreExit[1];
    Semaphore sem2ex = new Semaphore(1);
    stopsArr[2].semaphoreExit = sem2ex;
    ConcurrentKarel.positionStopSemaphores.put(key, sem2ex);


    key = stopsArr[3].posSemaphoreEntrance[0] + "," + stopsArr[3].posSemaphoreEntrance[1];
    Semaphore sem3e = new Semaphore(maxRobots3);
    stopsArr[3].semaphoreEntrance = sem3e;
    ConcurrentKarel.positionStopSemaphores.put(key, sem3e); // Stop 3
    key = stopsArr[3].posSemaphoreExit[0] + "," + stopsArr[3].posSemaphoreExit[1];
    Semaphore sem3ex = new Semaphore(1);
    stopsArr[3].semaphoreExit = sem3ex;
    ConcurrentKarel.positionStopSemaphores.put(key, sem3ex);

    key = stopsArr[4].posSemaphoreEntrance[0] + "," + stopsArr[4].posSemaphoreEntrance[1];
    Semaphore sem4e = new Semaphore(maxRobots4);
    stopsArr[4].semaphoreEntrance = sem4e;
    ConcurrentKarel.positionStopSemaphores.put(key, sem4e); // Stop 4
    key = stopsArr[4].posSemaphoreExit[0] + "," + stopsArr[4].posSemaphoreExit[1];
    Semaphore sem4ex = new Semaphore(1);
    stopsArr[4].semaphoreExit = sem4ex;
    ConcurrentKarel.positionStopSemaphores.put(key, sem4ex);

    int currStreetPark = 7;
    int currAvenuePark = 12;
    Thread[] threadsArr = new Thread[numRobots];

    for (int i = 0; i < numRobots; i++) {
      if (currAvenuePark > 18) {
        currStreetPark--;
        currAvenuePark = 12;
      }
      ParalelRobot robot = new ParalelRobot(currStreetPark, currAvenuePark, South, 0,
          new Color((int) (Math.random() * 0x1000000)), 3, 18, stopsArr[0]);
      Thread robotThread = new Thread(robot);
      threadsArr[i] = robotThread;
      currAvenuePark++;
    }

    World.readWorld("PracticaOperativos.kwld");
    World.setVisible(true);

    return threadsArr;

  }
}
