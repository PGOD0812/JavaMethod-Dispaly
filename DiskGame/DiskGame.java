// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a XMUT102 assignment.
// You may not distribute it in any other way without permission.

/* Code for XMUT102 - 2024T2, Assignment 7
 * Name: Peng Lu
 * Username: Peng Lu
 * ID: 2312409147
 */

import ecs100.*;
import java.awt.Color;
import java.util.*;
import java.nio.file.*;
import java.io.*;

/**
 * A game in which the player has to blow up disks spread out over a shooting range.
 * The game starts with a collection of randomly placed small disks on the upper half
 * of the graphics pane, and a gun at the bottom.
 * The gun is fixed in the middle of a horizontal line below the shooting range, and
 * shoots in any direction with an angle of 180 degrees.
 * The player fires the gun with the mouse, by releasing the mouse within the firing zone
 * limited by an arc surrounding the upper part of the gun. That will give the direction of the shot.
 * If a shot hits a disk, it will damage it.
 * If the disk is damaged sufficiently, it will explode and maybe damage surrounding disks,
 * if they are within range (Challenge).
 * The player has a limited number of shots, and the goal is to cause the maximum damage.
 * Each disk can report its score (based on how much damage it received - the greater the damage,
 * the greater the score), and the score for the game is the sum of the scores for each disk.  
 * The game is over when either the player has run out of shots or all the disks have exploded.
 */

public class DiskGame{
    // Constants for the game geometry: the disks in the shooting range should
    // all be in the rectangle starting at (0,0) with a width of 500 and a height of 150
    // The gun should be on the line at y = 300
    private static final double GAME_WIDTH = 500;
    private static final double SHOOTING_RANGE_Y = 150; // lowest point that a disk can be
    private static final double GUN_X = GAME_WIDTH/2;   // current x position of the gun
    private static final double GUN_Y = 300;
    private static final double SHOOTING_CIRCLE = GUN_Y-SHOOTING_RANGE_Y;

    //Constants for game logic
    private static final int DEFAULT_NUMBER_OF_SHOTS = 30;
    private static final int DEFAULT_NUMBER_OF_DISKS = 30;
    private int numShots = DEFAULT_NUMBER_OF_SHOTS;
    private int numDisks = DEFAULT_NUMBER_OF_DISKS;

    //Fields for the game state
    private double score = 0;                         // current score
    private int shotsRemaining = this.numShots;       // How many shots are left

    private ArrayList <Disk> disks = new ArrayList<Disk>(); // The list of disks

    /**
     * Sets up the user interface:
     * Set up the buttons and the mouselistener
     */
    public void setupGUI(){
        /*# YOUR CODE HERE */
        UI.setMouseListener(this::doMouse);
        UI.addSlider("Number of Disks", 2,60,31, this::setNumDisks);
        UI.addSlider("Number of Shots", 2,60,31, this::setNumShots);
        UI.addButton("Restart", this::startGame );
        UI.addButton("Load game", this::loadGame );
        UI.addButton("Save game", this::saveGame );
        UI.addButton("Quit", UI::quit);
        UI.setDivider(0.16);
    }

    /**
     * Set the number of disks for the next game
     * Hint: Remember to cast to an int
     */
    public void setNumDisks(double value){
        /*# YOUR CODE HERE */
        numDisks = (int)value;
    }

    /**
     * Set the number of shots for the next game
     * Hint: Remember to cast to an int
     */
    public void setNumShots(double value){
        /*# YOUR CODE HERE */
        numShots = (int)value;
    }

    /**
     * Set the fields of the game to their initial values,
     * Create a new list of disks
     * redraw the game
     */
    public void startGame(){ 
        this.shotsRemaining = this.numShots;
        this.score = 0;
        this.initialiseDisks();
        this.redraw();
    }

    /**
     * Make a new ArrayList of disks with new disks at random positions
     * within the shooting range.
     * Remember to use the CONSTANTS
     * Completion: ensure than none of them are overlapping.
     */
    public void initialiseDisks(){
        /*# YOUR CODE HERE */
        disks.clear();
        while (disks.size() < numDisks)
        {
            double x =Math.random() * GAME_WIDTH;
            double y = Math.random() * SHOOTING_RANGE_Y;
            boolean standard = false;
            Disk newDisk = new Disk(x,y);
            for(Disk n : disks)
            {
                if(newDisk.isOverlapping(n))
                {
                    standard = true;
                }
            }
            if(!standard)
            {
                disks.add(newDisk);
            }
        }
    }

    /**
     * Respond to the mouse
     */
    public void doMouse(String action, double x, double y){
        /*# YOUR CODE HERE */
        if(action.equals("released")){
            if(isWithinFiringZone(x,y)){
            fireShot(x,y);
        UI.printMessage("Your score is: " + score + " and you have " + shotsRemaining + " shots remaining");
        }
            else
            {
                UI.printMessage("Given point was not within the firing zone");
            }
        }
    }

    /**
     * Is the given position within the firing zone
     */
    public boolean isWithinFiringZone(double x, double y){
        // an easy approximation is to pretend it is the enclosing rectangle.
        // It is nicer to do a little bit of geometry and get it right
        return (x >= GUN_X-SHOOTING_CIRCLE/2) && (y >= GUN_Y-SHOOTING_CIRCLE/2)
        && (x <= GUN_X + SHOOTING_CIRCLE/2) && (y <= GUN_Y);
    }

    /**
     * The core mechanic of the game is to fire a shot.
     * - Update the number of shots remaining.
     * - Move the shot up the screen in the correct direction from the gun, step by step, until 
     *   it either goes off the screen or hits a disk.
     *   The shot is constantly redrawn as a line from the gun to its current position.
     * - If the shot hits a disk,
     *   - it damages the disk, 
     *   - If the disk is now broken, then
     *     it will damage its neighbours
     *     (ie, all the other disks within range will be damaged also)
     *   - it exits the loop.
     * - Redraw the game
     * - Finally, update the score,
     * - If the game is now over,  print out the score 
     * (You should define additional methods - don't do it all in one big method!)
     */
    public void fireShot(double x, double y){
        this.shotsRemaining--; //We now have one less shot left
        double shotPosX = GUN_X; //The shot starts at the top of the gun
        double shotPosY = GUN_Y; //The shot starts at the top of the gun
        // Calculate the step_X value for a step_y of -1
        double step_X = (GUN_X-x)/(y-GUN_Y);
        UI.setColor(Color.black);
        while (!this.isShotOffScreen(shotPosX, shotPosY)){ 
            shotPosY -= 1;
            shotPosX += step_X;
            UI.drawLine(GUN_X, GUN_Y, shotPosX, shotPosY);
            //check if it hits a disk... 
            /*# YOUR CODE HERE */
            boolean crush = false;
           /* for(Disk n : disks)
            {
                if (n.isOn(shotPosX, shotPosY))
                {
                    n.damage();
                    if (n.isBroken())
                    {
                        n.explode();
                        crush=true;
                    }
                }
            }*/
            if(getHitDisk(shotPosX,shotPosY) != null)
            {
                Disk disk1 = getHitDisk(shotPosX,shotPosY);
                disk1.damage();
                //UI.println( disk1.toString());
                crush =true;
                if(disk1.isBroken())
                {
                    damageNeighbours(disk1);
                }
            }
            UI.sleep(1);
            if (crush)
            {
                break;
            }
        }
        this.redraw();
        this.updateScore();
        //If game is over, print out the score
        if ((this.haveAllDisksExploded() || this.shotsRemaining < 1)){
            UI.println("Your score: " + this.score);
        }
    }

    /**
     * Is the shot out of the screen
     */
    public boolean isShotOffScreen(double x, double y) {
        return (x < 0 || y < 0 || x > GAME_WIDTH);
    }    

    /**
     * Does the given shot hit a disk? If yes, return that disk. Else return null
     * Useful when firing a shot
     * Hint: use the isOn method of the Disk class
     */
    public Disk getHitDisk(double shotX, double shotY){
        /*# YOUR CODE HERE */
        for(Disk n : disks)
        {
            if (n.isOn(shotX, shotY))
            {
                return n;
            }
        }
        return null;  // this is to make your code compile until you write this method.
    }

    /**
     * Inflict damage on all the neighbours of the given disk
     * (ie, all disks that are within range of the disk, and are not already broken)
     * Note, it should not inflict more damage on the given disk.
     * Useful when firing a shot
     * Hint: make use of Disk class methods
     *
     * For the CHALLENGE, this should be able to cause a chain reaction 
     *  so that neighbours that are damaged to their limit will explode and
     *  damage their neighbours, ....
     */
    public void damageNeighbours(Disk disk){
        /*# YOUR CODE HERE */
        ArrayList<Disk> diskDamaged = new ArrayList<>();
        for(Disk temDisk : disks)
        {
            if(disk.isWithinRange(temDisk))
            {
                if(!(temDisk.isBroken()) && temDisk != disk)
                {
                    diskDamaged.add(temDisk);
                }
            }
        }
        disk.explode();
        for(Disk temDisk : diskDamaged)
        {
            temDisk.damage();
            if(temDisk.isBroken())
            {
                damageNeighbours(temDisk);
            }
        }
    }

    /**
     * Are all the disks exploded?
     * Useful for telling whether the game is over.
     */
    public boolean haveAllDisksExploded(){
        /*# YOUR CODE HERE */
        for(Disk n : disks)
        {
            if(!n.isBroken())
            {
                return false;
            }
        }
       return true;  // this is to make your code compile until you write this method.
    }

    /**
     * Update the score field, by summing the scores of each disk
     * Score is 150 for exploded disks, 50 for disks with 2 hits, and 20 for disks with 1 hit.
     */
    public void updateScore(){
        // Hint: Each Disk can report how many points they are worth:
        // Iterate through the ArrayList, adding up the total score of the disks.
        /*# YOUR CODE HERE */
        int temScore = 0;
        for (Disk disk : disks)
        {
            temScore += disk.score();
        }
        score = temScore;
    }

    /**
     *  Redraws the game:
     *  - the boundary of the shooting range (done for you)
     *  - the shooting zone in gray (done for you)
     *  - the gun in black (done for you)
     *  - the disks
     *  - the pile of remaining shot (Completion)
     * 
     */
    public void redraw(){
        UI.clearGraphics();
        //Redraw the boundary of the shooting range
        UI.setColor(Color.black);
        UI.drawRect(0,0, GAME_WIDTH, GUN_Y);
        UI.setColor(Color.gray);
        UI.drawLine(0, SHOOTING_RANGE_Y, GAME_WIDTH, SHOOTING_RANGE_Y);

        // Redraw the shooting zone in gray
        UI.setColor(Color.lightGray);
        UI.fillArc(GUN_X-SHOOTING_CIRCLE/2, GUN_Y-SHOOTING_CIRCLE/2, SHOOTING_CIRCLE, SHOOTING_CIRCLE, 0, 180);

        // Redraw the gun in black
        UI.setColor(Color.black);
        UI.fillRect(GUN_X-5, GUN_Y-5, 10, 10);

        // Redraw the disks, and
        // For the Completion, the pile of remaining rounds in red
        /*# YOUR CODE HERE */
        for(Disk n : disks)
        {
            n.draw();
        }
        UI.setColor(Color.red);
        for (int i = 0; i < shotsRemaining; i++) {
            UI.fillRect(  0, GUN_Y  -i *5 - 4, 4 , 4 ); // 调整位置和间距
        }
    }

    /**
     * Ask the user for a file to open,
     * then read all the game attributes
     * (which must mirror what was saved in the saveGame method)
     * re-create the game
     */    
    public void loadGame(){ 
        this.disks = new ArrayList <Disk>();
        /*# YOUR CODE HERE */
        try
        {
            String name = UIFileChooser.open("Name of file");
            if (name == null) return;
            Scanner scanner = new Scanner(Path.of(name));
            double score = scanner.nextDouble();
            int shotRemains = scanner.nextInt();
            this.score = score;
            this.shotsRemaining = shotRemains;
            while(scanner.hasNext())
            {
                double x = scanner.nextDouble();
                double y = scanner.nextDouble();
                int damage = scanner.nextInt();
                disks.add(new Disk(x,y,damage));
            }
            scanner.close();
            redraw();
        }catch (IOException e) {
            System.out.println("The error is " + e);
        }
    }

    /**
     * Ask the user to select a file and save the current game to the selected file
     * You need to save:
     * - The current score and the number of remaining shots
     * - The coordinates and the damage of each disk
     *   Hint: use the toString method
     */
    public void saveGame(){ 
        /*# YOUR CODE HERE */
        try
        {
            String name = UIFileChooser.save("Save");
            if (name == null) return;
            PrintStream printStream = new PrintStream(name);
            printStream.println(score);
            printStream.println(shotsRemaining);
            for(Disk n : disks)
            {
                printStream.println(n.toString());
            }
            printStream.close();
        }catch (IOException e)
        {
            System.out.println("The error is " + e);
        }
    }

    public static void main(String[] args){
        DiskGame dg = new DiskGame();
        dg.setupGUI();
        dg.startGame();
    }

}
