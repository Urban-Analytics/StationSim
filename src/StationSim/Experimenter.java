package StationSim;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * For running experiments, specifically looking at ABM state transitions
 * Created by nick on 14/06/2018.
 */
public class Experimenter implements Steppable {

    private enum TRANSITION {HIGH, LOW};

    static TRANSITION transition = TRANSITION.HIGH; // start high

    private static final int HIGH_INTERVAL = 8;
    private static final int  LOW_INTERVAL = Integer.MAX_VALUE;

    private static final int CHANGE_REGULARITY = 800; // How often to switch low/high

    /**
     * Constructor can be used to mess around with things on model initialisation.
     *
     */
    public Experimenter(Station s) {
        s.setNumPeople(200000);
        s.setEntranceInterval(HIGH_INTERVAL);

    }

    public void step(SimState state) {

        Station station = (Station) state;

        // Get the iteration and decide what to do
        long iter = state.schedule.getSteps();

        //System.out.println(iter+": "+ ((Station) state).getEntranceInterval() );

        // Every X iterations, change the interval
        if (iter % CHANGE_REGULARITY == 0 & iter > 0) {
            System.out.println(iter+":\tSwitching from :"+this.transition);
            if (Experimenter.transition == TRANSITION.HIGH) {
                Experimenter.transition = TRANSITION.LOW;
                station.setEntranceInterval(LOW_INTERVAL);
                System.out.println("\tEntranceInterval: "+station.getEntranceInterval());
            }
            else {
                Experimenter.transition = TRANSITION.HIGH;
                station.setEntranceInterval(HIGH_INTERVAL);
                System.out.println("\tEntranceInterval: "+station.getEntranceInterval());
            }
            System.out.println("\tNow:"+this.transition+"\n");

        }

        // System.out.print(s.getEntranceInterval()+"\t");

    }
}
