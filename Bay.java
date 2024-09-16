import java.util.concurrent.Semaphore;

public class Bay {
    public int nBay;
    public Semaphore mySemaphore = new Semaphore(1);

    public Bay(int nBay) {
        this.nBay = nBay;
    }
}
