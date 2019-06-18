package main;

public class Constants {
	// map dimensions in arbitrary units
	public static final float MAP_WIDTH  = 48f;
	public static final float MAP_HEIGHT = 36f;

	// signal
	public static final float SIGNAL_MAP_STEP 	 = .1f;
	public static final float MIN_SIGNAL_LOSS 	 = 2f;
	public static final float MAX_SIGNAL_LOSS 	 = 6f;
	public static final float SIGNAL_QUALITY_STD = .5f;
	public static final String SIGNAL_IMAGE 	 = "img/signal.bmp";
	
    // collisions
    public static final float COLLISION_MAP_STEP = .1f;
    public static final String COLLISION_IMAGE   = "img/collision.bmp";

	// drone stats
    public static final float DRONE_RTH_SIGNAL_LOSS      = 8f;  //dB, loss before landing near the base during rth
    public static final float DRONE_IDEAL_SIGNAL_LOSS    = 20f;  //dB, loss goal using in KeepDistanceBehavior
    public static final float DRONE_ARMED_SIGNAL_LOSS    = 25f;  //dB, threshold between armed and flying
    public static final float DRONE_DANGER_SIGNAL_LOSS   = 32f;  //dB, loss before triggering a disconnection scenario
    public static final float DRONE_MAXIMUM_SIGNAL_LOSS  = 35f;  //dB, loss before signal is completely lost

    public static final float N_DRONES = 6;
    public static final float DRONE_SPEED                             = .1f / 3f;	// Map units per step
	public static final float DRONE_COLLISION_SENSOR_RANGE 			  = 5f; 		// Distance in map units
	public static final float DRONE_COLLISION_SENSOR_TRIGGER_DISTANCE = 1.5f;		// Distance at which correction will start to be applied
	public static final float DRONE_COLLISION_SENSOR_MINIMUM_DISTANCE = 0.01f;		// Epsilon distance in collision sensor vector calculation
	public static final float DRONE_COLLISION_SENSOR_WEIGHT 		  = 0.9f;		// Weight of the collision sensor vector for avoiding walls
	public static final int   HISTORY_DURATION        				  = 1000;   		// Duration in steps of the drones' position history
	
	public static final float KEEP_DIST_GOAL_SIGNAL_TOLERANCE = 2f;
	public static final int   DRONE_MAX_INBOX_MSGS = 50;
}
