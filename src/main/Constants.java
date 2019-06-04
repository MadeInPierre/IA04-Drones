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
    public static final float DRONE_SPEED             = .3f;	// map units per step
    public static final float DRONE_MAXIMUM_SIGNAL_LOSS = 40f; //dB
	public static final float HISTORY_DURATION        = 3f;   	// duration in steps of the drones' position history
}
