package main;

public class Constants {
	// map dimensions in arbitrary units
	public static final float MAP_WIDTH = 48f;
	public static final float MAP_HEIGHT = 36f;

	// signal stuff
	public static final float SIGNAL_MAP_STEP = .1f;
	public static final float MIN_SIGNAL_LOSS = 2f;
	public static final float MAX_SIGNAL_LOSS = 6f;
	public static final float SIGNAL_QUALITY_STD = .2f;
	public static final String SIGNAL_IMAGE = "img/signal.bmp";
	
    // collisions
    public static final float COLLISION_MAP_STEP = .1f;
    public static final String COLLISION_IMAGE = "img/collision.bmp";

	// drone stats
	public static final float MINIMUM_SIGNAL_STRENGTH = -80f; // dBm
	public static final float EMITTER_SIGNAL_STRENGTH = 20f; // dBm
}
