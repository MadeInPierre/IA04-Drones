package main;

public class Constants {
	// map dimensions in arbitrary units
	public static final float MAP_WIDTH  = 48f;
	public static final float MAP_HEIGHT = 36f;

	// signal
	public static final float SIGNAL_MAP_STEP 	 = .1f;
	public static final float MIN_SIGNAL_LOSS 	 = 2f;
	public static final float MAX_SIGNAL_LOSS 	 = 6f;
	public static final float SIGNAL_QUALITY_STD = .2f;
	public static final String SIGNAL_IMAGE 	 = "img/signal.bmp";
	
    // collisions
    public static final float COLLISION_MAP_STEP = .1f;
    public static final String COLLISION_IMAGE   = "img/collision.bmp";

	// drone stats
    public static final float DRONE_MAXIMUM_SIGNAL_LOSS  = 35f;  //dB, loss before signal is completely lost
    public static final float DRONE_IDEAL_SIGNAL_LOSS    = 20f;  //dB, loss goal using in KeepDistanceBehavior
    public static final float DRONE_DANGER_SIGNAL_LOSS   = 25f;  //dB, loss before triggering a new direction search

    public static final float DRONE_SPEED             = .1f;	// map units per step
	public static final float DRONE_COLLISION_SENSOR_RANGE = 5f;
	public static final float DRONE_COLLISION_SENSOR_MINIMUM_DISTANCE = 0.01f;	// Epsilon distance in collision sensor vector calculation
	public static final float DRONE_COLLISION_SENSOR_WEIGHT = 0.01f;	// Weight of the collision sensor vector for avoiding walls
	public static final float HISTORY_DURATION        = 3f;   	// duration in steps of the drones' position history
	
	public static final float KEEP_DIST_GOAL_SIGNAL_TOLERANCE = 2f;
}
