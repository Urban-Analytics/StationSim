/* Created by Micahel Adcock on 17/04/2018.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package StationSim;

import sim.engine.SimState;
import sim.field.continuous.Continuous2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.Interval;

import java.awt.*;

/**
 * Simulates a very simple train station with a given number of entrances and exits which are set through
 * GUI or command line arguments. People move from the entrance to a given exit, interacting on the way.
 */
public class Station extends SimState{

    private static final long serialVersionUID = 1;

    private Experimenter experimenter; // For running experiments for the Transitions work

    // Representations of simulation space
    private double areaWidth = 200.0;
    private double areaHeight = 100.0;
    int wallWidth = 2;
    double wallHeight;
    public Continuous2D area = new Continuous2D(1.0, areaWidth, areaHeight);
    public Continuous2D doorways = new Continuous2D(1.0, areaWidth, areaHeight);
    public Continuous2D walls = new Continuous2D(1.0, areaWidth, areaHeight);

    // Default values for parameters
    private int numPeople;
    private int numEntrances = 3;
    private int numExits = 2;
    private double exitProb = 0.5;
    private int exitInterval = 30;
    private int entranceInterval;
    private int entranceSize = 5;
    private int exitSize = 20;
    private int personSize = 1; // sort bug here
    public int addedCount;
    private boolean writeResults = false;

    // Store  exit agents for passing to constructors
    private Bag exits;
    private Bag entrances;

    //Analysis agent
    private Analysis analysis;

    // Run with default settings only
    private boolean hideParameters = false;

    public Station(long seed) {
        super(seed);
    }

    // Lots of getters and setters use hideParameters variable to hide parameters from GUI
    public int getNumPeople() {
        return numPeople;
    }

    public String desNumPeople() {
        return "Total number of people to enter simulation split evenly between all entrances";
    }

    public void setNumPeople(int people) {
        numPeople = people;
    }

    public Object domNumPeople() {
        return new Interval(1, 10000);
    }

    public boolean hideNumPeople() {
        return hideParameters;
    }

    public int getNumEntrances() {
        return numEntrances;
    }

    public void setNumEntrances(int entrances) {
        numEntrances = entrances;
    }

    public boolean hideNumEntrances() {
        return hideParameters;
    }

    public Object domNumEntrances() {
        return new Interval(2, 10);
    }

    public int getNumExits() {
        return numExits;
    }

    public void setNumExits(int exits) {
        numExits = exits;
    }

    public boolean hideNumExits() {
        return hideParameters;
    }

    public Object domNumExits() {
        return new Interval(2, 10);
    }

    public int getEntranceSize() {
        return entranceSize;
    }

    public void setEntranceSize(int size) {
        entranceSize = size;
    }

    public boolean hideEntranceSize() {
        return hideParameters;
    }

    public Object domEntranceSize() {
        return new Interval(1, 10);
    }

    public String desEntranceSize() {
        return "Number of people who can come through each entrance per step";
    }

    public int getExitSize() {
        return exitSize;
    }

    public void setExitSize(int size) {
        exitSize = size;
    }

    public String desExitSize() {
        return "Number of people who can pass through each exit per step";
    }

    public boolean hideExitSize() {
        return hideParameters;
    }

    public Object domExitSize() {
        return new Interval(1, 10);
    }
    
    public double getExitProb() {
        return exitProb;
    }

    public void setExitProb(double exitProb) {
        this.exitProb = exitProb;
    }

    public boolean hideExitProb() {
        return hideParameters;
    }

    public Object domExitProb() {
        return new Interval(0.01, 1.0);
    }

    public String desExitProb() {
        return "Probability that person will choose exit assigned to them";
    }

    public int getPersonSize() {
        return personSize;
    }

    public void setPersonSize(int size) {
        personSize = size;
    }

    // Set to always true at the moment as there is a bug when peron size is changed
    public boolean hidePersonSize() {
        return true;
    }

    public Bag getEntrances() {
        return entrances;
    }

    public boolean hideEntrances() {
        return true;
    }

    public double getAreaWidth() {
        return areaWidth;
    }

    public boolean hideAreaWidth() {
        return true;
    }

    public int getWallWidth() {
        return wallWidth;
    }

    public boolean hideWallWidth() {
        return true;
    }

    public Bag getExits() {
        Bag exitsCopy = new Bag();
        exitsCopy.addAll(exits);
        return exitsCopy;
    }

    public boolean hideExits() {
        return true;
    }

    public int getExitInterval() {
        return exitInterval;
    }

    public void setExitInterval(int exitInterval) {
        this.exitInterval = exitInterval;
    }

    public boolean hideExitInterval() {
        return hideParameters;
    }

    public String desExitInterval() {
        return "How often the exits can remove people (in num of steps)";
    }

    public Object domExitInterval() {
        return new Interval(1, 100);
    }

    public int getEntranceInterval() {
        return entranceInterval;
    }

    public void setEntranceInterval(int entranceInterval) {
        this.entranceInterval = entranceInterval;
    }

    /*
    public Object domEntranceInterval() {
        return new Interval(1, 100);
    }
    */

    public boolean hideEntranceInterval() {
        return hideParameters;
    }

    public String desEntranceInterval() {
        return "How often people enter simulation (in steps)";
    }

    public boolean getWriteResults() {
        return writeResults;
    }

    public void setWriteResults(boolean write) {
        writeResults = write;
    }

    /** Cleans up everything left from previous simulation and sets up new one.
     */
    @Override
    public void start() {
        super.start();
        area.clear();
        doorways.clear();
        walls.clear();
        addedCount = 0;

        // For runnning experiments
        this.experimenter = new Experimenter(this);
        schedule.scheduleRepeating(this.experimenter, 1, 1.0);

        createWalls();
        createExits();
        createEntrances();

        // This changes when number of exits of size of exits do
        wallHeight = (areaHeight / (numExits + 1)) - (exitSize * 2.0  * personSize / 2.0);

        // The sequencer will contain all the person objects on the schedule and order them in distance to exit.
        Sequencer sequencer = new Sequencer(this);
        schedule.scheduleRepeating(sequencer, 1, 1.0);

        // Analysis and outputs from the model are contained in this agent
        analysis = new Analysis(this);
        schedule.scheduleRepeating(analysis, 3, 1.0);

    }

    /** Call appropriate analysis methods and cleans up
     */
    @Override
    public void finish() {
        super.finish();
    }

    /** Creates entrance agents based on the number of entrances and size of entrances set in GUI.
     * The size of the entrance represents how many people can pass through it per step (i.e. the number
     * of person agents that are generated by an entrance per step). People are evenly assigned to the
     * entrances using integer division (remainers discarded). Entrances are evenly spaced on left side
     * of the simulation.
     */
    private void createEntrances() {
        double interval, y;

        entrances = new Bag();
        // Calculate even spacing of entrances
        interval = areaHeight / (numEntrances + 1);
        y = 0;

        // Create entrances
        for (int i = 0; i < numEntrances; i++) {
            y += interval;
            Double2D location = new Double2D(0, y);
            Entrance entrance =  new Entrance(
                    entranceSize, location, "Entrance: " + (i + 1),
                    this.getNumPeople() / numEntrances, personSize,
                    (Exit) exits.getValue(random.nextInt(numExits)), exitProb, this.getEntranceInterval());
            doorways.setObjectLocation(entrance, location);
            schedule.scheduleRepeating(entrance, 2, 1.0);
            entrances.add(entrance);
        }
    }

    /** Creates exit agents based on the number of exits and size of exits set in GUI.
     * The size of the entrance represents how many people can pass through it per step
     * (i.e. The number of person agents that can be removed from the simulation by an
     * exit per step). Exits are evenly spaced on right side of simulation.
     */
    private void createExits() {
        double intervalSpacer, y;
        Double2D position;

        exits = new Bag();
        // Calculate even spacing for exits
        intervalSpacer = areaHeight / (numExits + 1);
        y = 0;

        // Create exits
        for (int i = 0; i < numExits; i++) {
            y += intervalSpacer;
            position = new Double2D(areaWidth, y);
            Exit exit =  new Exit(exitSize, position, "Exit: " + (i + 1), exitInterval);
            doorways.setObjectLocation(exit, position);
            schedule.scheduleRepeating(exit, 0, 1.0);
            exits.add(exit);
        }
    }

    /** Setup walls on the right side of the simulation. These the position and size of these are
     * change based on the number of exits and size of the exits.
     */
    public void createWalls() {
        Double2D pos;
        Wall wall;

        for (int i = 0; i < numExits + 1; i++) {
            pos = new Double2D(areaWidth - (wallWidth), areaHeight / (numExits * 2 + 2) * (i * 2 + 1));
            wall = new Wall(pos, wallWidth / 2, wallHeight /2);
            walls.setObjectLocation(wall, wall.getLocation());

        }

        // back corners - These cover gaps in the top an bottom corners
        pos = new Double2D(areaWidth - (wallWidth), exitSize / 1.5);
        wall = new Wall(pos, wallWidth, exitSize + 1);
        walls.setObjectLocation(wall, wall.getLocation());

        pos = new Double2D(areaWidth - (wallWidth), areaHeight - (exitSize / 1.5));
        wall = new Wall(pos, wallWidth, exitSize + 1); //exitSize  * personSize / 2
        walls.setObjectLocation(wall, wall.getLocation());
    }

    /** Run simulation without GUI. Default values are automatically used ##### Fix this ########
     * @param args
     */
    public static void main(String[] args) {
        doLoop(Station.class, args);
        System.exit(0);
    }
}

// seed -460236115

// TO DO:
//
// Fix person number bug
// Allow arguments for simulation values to be passed into Station main()
//
// Start Bayesian methods for hackers