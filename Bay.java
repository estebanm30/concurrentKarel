import java.util.concurrent.Semaphore;

public class Bay {
    public int nBay;
    public int[] pos = new int[2];
    public String direction;
    public Semaphore mySemaphore = new Semaphore(1);
    public Bay nextBay;
    public Bay prevBay;

    public Bay(int nBay, String direction, Bay nextBay, Bay prevBay) {
        this.direction = direction;
        this.nextBay = nextBay;
        this.prevBay = prevBay; 
    }
}
