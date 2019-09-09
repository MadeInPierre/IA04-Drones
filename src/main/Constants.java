package main;

import sim.util.Double3D;

public class Constants {
	// Map images
//	public static final String IMAGE   		   = "img/simple_tunnel.jpg";
//	public static final String SIGNAL_IMAGE    = "img/simple_tunnel_signal.jpg";
//	public static final String COLLISION_IMAGE = "img/simple_tunnel_collision.jpg";
//	public static final Double3D SPAWN_POS = new Double3D(3, 5, 0); // start
//  public static final Double3D SPAWN_POS = new Double3D(6, 15, -1.5792); // 90 angle
	
//	public static final String IMAGE   		   = "img/maps/map_misc.jpg";
//	public static final String SIGNAL_IMAGE    = "img/maps/map_misc_signal_90challenge.png";
//	public static final String COLLISION_IMAGE = "img/maps/map_misc_collisions.png";
//	public static final Double3D SPAWN_POS = new Double3D(42.5, 4.5, 0);
	
//	public static final String IMAGE   		   = "img/cave.jpg";
//	public static final String SIGNAL_IMAGE    = "img/signal.bmp";
//  public static final String COLLISION_IMAGE = "img/collision.bmp";
//	public static final Double3D SPAWN_POS = new Double3D(3, 33.5, (float)Math.PI / -4);
	
	public static final String IMAGE   		   = "img/maps/cliff.png";
	public static final String SIGNAL_IMAGE    = "img/maps/cliff_collisions.png";
	public static final String COLLISION_IMAGE = "img/maps/cliff_collisions.png";
	public static final Double3D SPAWN_POS = new Double3D(3, 33.5, (float)Math.PI / -4);
  
	
	// Map dimensions in arbitrary units
	public static final float MAP_WIDTH  				= 56f;
	public static final float MAP_HEIGHT 				= 36f; 

	// Signal
	public static final float SIGNAL_MAP_STEP 			= .1f; // map units, signal map evaluation resolution
	// Normal path loss
	public static final float SIGNAL_MIN_LOSS 			= 3f;  // dB
	public static final float SIGNAL_MAX_LOSS 			= 8f;  // dB
	public static final float SIGNAL_WALL_LOSS 			= 12f; // dB
	public static final float SIGNAL_STD_LOSS 			= 4f;  // dB
	// Shadowing loss
	public static final float SIGNAL_SHADOWING_LOSS 	= 10f; // no unit, empirical
	// Multipath loss
	public static final float SIGNAL_MULTIPATH_LOSS_AMP = 5f;  // dB
	public static final float SIGNAL_MULTIPATH_LOSS_PER = 3f; // dB
	// Random noise
	public static final float SIGNAL_RANDOM_LOSS_STD 	= 8f;  // dB
		
    // Collisions
    public static final float COLLISION_MAP_STEP 		= .1f; // map units, collision search resolution

	// Drone loss
    public static final float DRONE_RTH_SIGNAL_LOSS     = 40f;  //dB, loss before landing near the base during rth
    public static final float DRONE_IDEAL_SIGNAL_LOSS   = 50; //dB, loss goal using in KeepDistanceBehavior
    public static final float DRONE_ARMED_SIGNAL_LOSS   = 60f; //dB, threshold between armed and flying
    public static final float DRONE_DANGER_SIGNAL_LOSS  = 90f; //dB, loss before triggering a disconnection scenario
    public static final float DRONE_MAXIMUM_SIGNAL_LOSS = 100f; //dB, loss before signal is completely lost
    public static final float DRONE_BEST_SIGNAL_LOSS    = 35f; //dB, loss before drone can't go closer to the other drone
    // Signal filtering
    public static final float DRONE_EXPECTED_SIGNAL_STD = 5f; // dB, Drone tries to ignore this amount of Gaussian noise
    public static final int   DRONE_SIGNAL_MEAN_STEPS 	= 10; // Number of signal measures we take the mean from

    // Drone general params
    public static final float N_DRONES 								  = 5;
    public static final float DRONE_SPEED                             = .05f;	// Map units per step
    public static final float DRONE_TURN_SPEED                        = .5f;         // No unit
	public static final float DRONE_COLLISION_SENSOR_RANGE 			  = 3f; 		// Distance in map units
	public static final float DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE = 3.2f;			// Distance at which correction will start to be applied
	public static final float DRONE_COLLISION_SENSOR_MINIMUM_DISTANCE = 0.01f;		// Epsilon distance in collision sensor vector calculation
	public static final float DRONE_COLLISION_SENSOR_WEIGHT 		  = 0.9f;		// Weight of the collision sensor vector for avoiding walls
	public static final int   HISTORY_DURATION        				  = 500;   		// Duration in steps of the drones' position history
	
	public static final float KEEP_DIST_GOAL_SIGNAL_TOLERANCE 		  = 2f;
	public static final int   DRONE_MAX_INBOX_MSGS 					  = 50;
}
