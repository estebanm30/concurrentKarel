import kareltherobot.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ConcurrentKarel implements Directions {
  public static int totalBeepers = 1000;
  public static final int[] maxRobots = new int[] { 8, 9, 1, 4 }; // maxRobots stop1, stop2, stop3, stop4
  public static final int[] principalPos = new int[] { 10, 10 };
  public static Stop[] stopsArr = new Stop[6];
  public static Bay bayArr[] = new Bay[5];
  private static final int numRobots = 13;
  public static final Semaphore cMoveSemaphore = new Semaphore(1);
  public static List<String> positionsUsed = Collections.synchronizedList(new ArrayList<>());
  private static final Lock positionLock = new ReentrantLock();
  private static final Condition positionAvailable = positionLock.newCondition();
  public static Map<String, Semaphore> positionStopSemaphores = Collections.synchronizedMap(new HashMap<>());
  public static final String[] wayBackToBeepers = { "North", "East", "North", "West", "South", "East", "North" };

  public static void main(String[] args) {

    /* Stops */
    createStops();

    /* Create bays of stop 4 */
    createBays();

    /* World Setup and robots creation */
    Thread[] arr = new Thread[2];
    arr = setUpWorld(numRobots);

    /* Start robots */
    for (Thread robot : arr) {
      robot.start();
    }

  }

  public static void notifyPositionAvailable(String position) {
    positionLock.lock();
    try {
      positionAvailable.signalAll(); // Notify waiting threads
    } finally {
      positionLock.unlock();
    }
  }

  public static boolean waitForPosition(String position) {
    positionLock.lock();
    try {
      while (positionsUsed.contains(position)) {
        positionAvailable.await(); // Wait for position to be available
      }
      return true;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt(); // Restore interruption status
      return false;
    } finally {
      positionLock.unlock();
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
    Bay b1 = new Bay(1);
    Bay b2 = new Bay(2);
    Bay b3 = new Bay(3);
    Bay b4 = new Bay(4);
    Bay b5 = new Bay(5);

    bayArr[0] = b1;
    bayArr[1] = b2;
    bayArr[2] = b3;
    bayArr[3] = b4;
    bayArr[4] = b5;
  }

  static void stopControlCreation() {
    String key = "";
    int j = 0;
    for (int i = 1; i <= 4; i++) {
      key = stopsArr[i].posSemaphoreEntrance[0] + "," + stopsArr[i].posSemaphoreEntrance[1];
      Semaphore semE = new Semaphore(maxRobots[j]);
      stopsArr[i].semaphoreEntrance = semE;
      ConcurrentKarel.positionStopSemaphores.put(key, semE);

      key = stopsArr[i].posSemaphoreExit[0] + "," + stopsArr[i].posSemaphoreExit[1];
      Semaphore semEx = new Semaphore(1);
      stopsArr[i].semaphoreExit = semEx;
      ConcurrentKarel.positionStopSemaphores.put(key, semEx);
      j++;
    }
  }

  static void createRobots(Thread[] threadsArr, int numRobots) {
    int currStreetPark = 7;
    int currAvenuePark = 12;

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
  }

  static Thread[] setUpWorld(int numRobots) {
    World.setBeeperColor(Color.DARK_GRAY);
    World.showSpeedControl(true);
    World.readWorld("PracticaOperativos.kwld");
    World.setVisible(true);

    stopControlCreation();
    Thread[] threadsArr = new Thread[numRobots];
    createRobots(threadsArr, numRobots);

    return threadsArr;

  }
}
