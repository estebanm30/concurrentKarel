public class Stop {
    int nStop;
    int[] entrancePos;
    int[] dropPos;
    String[] wayToPrincipal;
    String[] directionList;
    int maxRobots;

    public Stop(int nStop, int[] entrancePos, String[] directionList, String[] wayToPrincipal) {
        this.nStop = nStop;
        this.entrancePos = entrancePos;
        this.directionList = directionList;
        this.wayToPrincipal = wayToPrincipal;

        switch (nStop) {
            case 1:
                this.dropPos = new int[] { 15, 6 };
                this.maxRobots = 1;
                break;
            case 2:
                this.dropPos = new int[] { 13, 7 };
                this.maxRobots = 1;
                break;
            case 3:
                this.dropPos = new int[] { 8, 8 };
                this.maxRobots = 1;
                break;

            default:
                break;
        }

    }

}