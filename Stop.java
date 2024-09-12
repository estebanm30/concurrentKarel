import java.util.concurrent.Semaphore;

public class Stop {
    int nStop;
    int[] entrancePos;
    int[] dropPos;
    String[] wayToPrincipal;
    String[] directionList;
    int maxRobots;
    int[] posSemaphoreEntrance;
    int[] posSemaphoreExit;
    int cont = 0;
    Semaphore semaphoreEntrance;
    Semaphore semaphoreExit;

    public Stop(int nStop, int[] entrancePos, String[] directionList, String[] wayToPrincipal) {
        this.nStop = nStop;
        this.entrancePos = entrancePos;
        this.directionList = directionList;
        this.wayToPrincipal = wayToPrincipal;

        switch (nStop) {
            case 1:
                this.dropPos = new int[] { 15, 6 };
                this.maxRobots = ConcurrentKarel.maxRobots[0];
                this.posSemaphoreEntrance = new int[] {18,6};
                this.posSemaphoreExit = new int[]{16,6};
                break;
            case 2:
                this.dropPos = new int[] { 13, 7 };
                this.maxRobots = ConcurrentKarel.maxRobots[1];
                this.posSemaphoreEntrance = new int[]{10,7};
                this.posSemaphoreExit = new int[]{12,7};
                break;
            case 3:
                this.dropPos = new int[] { 8, 8 };
                this.maxRobots = ConcurrentKarel.maxRobots[2];
                this.posSemaphoreEntrance = new int[] {6 , 8};
                this.posSemaphoreExit = new int[]{8,8};
                break;
            case 4:
                this.dropPos = new int[] { 8, 8 };
                this.maxRobots = ConcurrentKarel.maxRobots[3];
                this.posSemaphoreEntrance = new int[] {10,15};
                this.posSemaphoreExit = new int[]{15,15};
                break;

            default:
                break;
        }

    }

}