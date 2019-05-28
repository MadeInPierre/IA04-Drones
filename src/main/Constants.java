package main;

public class Constants {
    // map dimensions in arbitrary units
    public static final float MAP_WIDTH = 30f;
    public static final float MAP_HEIGHT = 30f;

    // signal stuff
    public static final float SIGNAL_MAP_STEP = .1f;
    public static final float MIN_SIGNAL_QUALITY = 2f;
    public static final float MAX_SIGNAL_QUALITY = 6f;
    public static final String SIGNAL_IMAGE = "img/signal.bmp";
    
    // drone stats
    public static final float MINIMUM_SIGNAL_STRENGTH = -80f; // dBm
    public static final float EMITTER_SIGNAL_STRENGTH = 20f; // dBm
}

