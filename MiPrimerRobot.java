import kareltherobot.*;
import java.awt.Color;
import java.util.*;



public class MiPrimerRobot implements Directions 
{
    public static ArrayList<int[]> posUsed = new ArrayList<int[]>();
    public static void main(String [] args)
    {
        World.showSpeedControl(true);
        World.readWorld("mundo2.kwld");
        World.setVisible(true);        

        Racer first = new Racer(1, 1, East, 0);
        Racer second = new Racer(1, 2, East, 0);
        Racer third = new Racer(1, 3, East, 0);



        Thread firstRobot = new Thread(first);
        Thread secondRobot = new Thread(second);
        Thread thirdRobot = new Thread(third);
        firstRobot.start();
        secondRobot.start();
        thirdRobot.start();

    }
}

class Racer extends Robot implements Runnable{
    private int street;
    private int avenue;
    private Stack<String> path = new Stack<String>();

    public Racer(int Street, int Avenue,  Direction direction, int beepers){
        super(Street, Avenue, direction, beepers);
        World.setupThread(this);
        this.avenue = Avenue;
        this. street = Street;
    }
    public void setAvenue(int avenue) {
        this.avenue = avenue;
    }

    public void setStreet(int street) {
        this.street = street;
    }

    public int getAvenue() {
        return this.avenue;
    }
    public int getStreet() {
        return this.street;
    }

    public void setPath(String direction) {
        this.path.push(direction);
    }

    public Stack<String> getPath() {
        return this.path;
    }

    public void returnToStart(){
        while (!getPath().empty()) {
            String nextMove = getPath().pop();
            System.out.println(nextMove+" next move");
            switch (nextMove) {
                case "west":
                    while (!facingWest()) {
                        turnLeft();
                    }
                    move();
                    break;
                case "east":
                    while (!facingEast()) {
                        turnLeft();
                    }
                    move();
                    break;
                case "north":
                    while (!facingNorth()) {
                        turnLeft();
                    }
                    move();
                    break;
                case "south":
                    while (!facingSouth()) {
                        turnLeft();
                    }
                    move();
                    break;
                default:
                    break;
            }
        }
    }

    public void setNewDirection(){
        if (facingEast()){
            setAvenue(getAvenue()+1);
            setPath("west");
        } else if (facingNorth()) {
            setStreet(getStreet()+1);
            setPath("south");
        } else if (facingSouth()) {
            setStreet(getStreet()-1);
            setPath("north");
        } else{
            setAvenue(getAvenue()-1);
            setPath("east");
        }
    }

    public boolean checkCorner(){
        System.out.println( MiPrimerRobot.posUsed);
        for(int i = 0; i < MiPrimerRobot.posUsed.size(); i++ ){
            if(MiPrimerRobot.posUsed.get(i)[0]==getAvenue() && MiPrimerRobot.posUsed.get(i)[1]==getStreet()){
                return false;
            }
        }
        System.out.println("hola");
        return true;
    }

    public void race(){
        
    while(! nextToABeeper()){
        move();
        setNewDirection();
        System.out.println(getStreet() + " " + getAvenue());
        
    }
    if (checkCorner()) {
        int arr[] = {getAvenue(),getStreet()};
        MiPrimerRobot.posUsed.add(arr);
        while( nextToABeeper() ){
            pickBeeper();
        }
        MiPrimerRobot.posUsed.remove(arr);
    }

    turnLeft();
    setNewDirection();

    move();
    setNewDirection();
    while(! nextToABeeper() ){
        move();
        setNewDirection();
        
    }
    if (nextToABeeper() && checkCorner()) {
        int arr[] = {getAvenue(),getStreet()};
        MiPrimerRobot.posUsed.add(arr);
        while( nextToABeeper() ){
            pickBeeper();
        }
        MiPrimerRobot.posUsed.remove(arr);
    }
    move();
    setNewDirection();
    move();
    setNewDirection();

    while (anyBeepersInBeeperBag()) {
        putBeeper();
    }
    move();
    setNewDirection();
    move();

    returnToStart();

    turnOff();
    }

    public void run(){
    race();
    }
}