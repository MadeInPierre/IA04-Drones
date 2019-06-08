package agents.drone.behaviors;

import agents.Communicator;
import sim.util.Double3D;
import java.util.ArrayList;
import java.util.Collections;

public class SeekDirectionBehavior extends FlyingBehavior {
	private static final float CIRCLE_RADIUS  = 2f;
	private static final float N_GOTO_STEPS   = 20;  // Number of steps before reaching circle or center positions 
	private static final float N_CIRCLE_STEPS = 100; // Number of steps before making a full circle. One signal measure per step.
	
	private enum SeekState {
		GOTO_CIRCLE, // Going to the circle's first point position
		IN_CIRCLE,   // Making the turn and collecting signals 
		GOTO_CENTER, // Going back to center position when circle finished 
		FINISHED,    // Mark as finished for FlyingManager
	}
	private SeekState seekState = SeekState.GOTO_CIRCLE;
	
	private int step = 0;
	private ArrayList<Float> strengths;
	
	public SeekDirectionBehavior() {
		strengths = new ArrayList<Float>();
	}
	
	// Make a full circle pattern
	public Double3D stepTransform(Communicator com) {
		Double3D transform = new Double3D();
		
		switch(seekState) {
		case GOTO_CIRCLE: {
			double stepDist = (double)CIRCLE_RADIUS / N_GOTO_STEPS;
			transform = transform.add(new Double3D(stepDist, 0, 0));
			step++;
			
			if(step == N_GOTO_STEPS) {
				step = 0;
				transform = transform.add(new Double3D(0, 0, Math.PI / 2));
				seekState = SeekState.IN_CIRCLE;
			}
			break;
		}
		case IN_CIRCLE: {
			float s = 0; // TODO get leader's signal
			if(step == 13) s = 1; // TODO tmp for testing 
			strengths.add(s); // get current signal measure
			
			double da = 2 * Math.PI / N_CIRCLE_STEPS;
			double tx = CIRCLE_RADIUS - CIRCLE_RADIUS * Math.cos(da);
			double ty = CIRCLE_RADIUS * Math.sin(da);
			transform = transform.add(new Double3D(tx, ty, da));
			step++; 
			if(step == N_CIRCLE_STEPS) {
				step = 0;
				transform = transform.add(new Double3D(0, 0, Math.PI / 2));
				seekState = SeekState.GOTO_CENTER;
			}
			break;
		}
		case GOTO_CENTER: {
			double stepDist = (double)CIRCLE_RADIUS / N_GOTO_STEPS;
			transform = transform.add(new Double3D(stepDist, 0, 0));
			step++;
			
			if(step == N_GOTO_STEPS) {
				step = 0;
				transform = transform.add(new Double3D(0, 0, Math.PI));
				seekState = SeekState.FINISHED;
				
				// Choose new direction 
				float max = Collections.max(strengths);
				int i_max = strengths.indexOf(max);
				transform = transform.add(new Double3D(0, 0, i_max * 2 * Math.PI / N_CIRCLE_STEPS));
				
			}
			break;
		}
		case FINISHED: {
			transform = transform.subtract(new Double3D(0.05, 0, 0));
			break;
		}
		}
		
		//System.out.println("[SeekDirectionBehaviour] chose to move by (" + transform.getX() + ", " + transform.getY() + ", " + transform.getZ() + "), step = " + step);
		return transform;
	}
}
