import kareltherobot.*;
import java.awt.Color;
import java.util.concurrent.Semaphore;

import java.util.*;

public class ConcurrentKarel implements Directions {
  public static Map<String, Semaphore> positionSemaphores = Collections.synchronizedMap(new HashMap<>());
  public static Map<String, Semaphore> positionStopSemaphores = Collections.synchronizedMap(new HashMap<>());
  public static Stop[] stopsArr = new Stop[5];
  public static int totalBeepers = 1000;
  public static String[] wayBackToBeepers = {"North", "East","North", "West", "South", "East", "North"};
  public static int principalAvenue = 10;


  public static void main(String[] args) {

    /* Variables */
    int numRobots = 10;

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
    String[] wayToPrincipal1 = {"East", "South", "West", "South", "East"};
    Stop stop1 = new Stop(1, exitStop1, directionsStop1, wayToPrincipal1);
    stopsArr[1] = stop1;

    int[] exitStop2 = { 10, 8 };
    String[] directionsStop2 = { "West", "South", "West", "North", "East","South", "West", "South", "East", "North", "West" };
    String[] wayToPrincipal2 = {"West", "South", "East"};
    Stop stop2 = new Stop(2, exitStop2, directionsStop2, wayToPrincipal2);
    stopsArr[2] = stop2;

    int[] exitStop3 = { 6, 7 };
    String[] directionsStop3 = { "West", "South", "West", "North", "East","South", "West", "South", "East", "North", "West" ,"South", "East" };
    String[] wayToPrincipal3 = {"East"};
    Stop stop3 = new Stop(3, exitStop3, directionsStop3, wayToPrincipal3);
    stopsArr[3] = stop3;

    int[] exitStopParking = { 2, 19 };
    String[] directionsStopPark = {  "West", "South", "West", "North", "East", "South", "West", "South", "East", "North", "West","South", "East", "North", "East", "North", "West", "South", "East", "North" };
    String[] wayToPrincipalParking = null;
    Stop stopPark = new Stop(5, exitStopParking, directionsStopPark, wayToPrincipalParking);
    stopsArr[4] = stopPark;

    // Todo Stop 4

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
    key = 18 + "," + 6;
    ConcurrentKarel.positionStopSemaphores.put(key, new Semaphore(1));

    key = 10+ "," + 7;
    ConcurrentKarel.positionStopSemaphores.put(key, new Semaphore(1));

    key = 6+ "," + 8;
    ConcurrentKarel.positionStopSemaphores.put(key, new Semaphore(1));

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
