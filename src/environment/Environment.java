package environment;

import main.Constants;

public class Environment {
    SignalManager signalManager;

    // TODO: singleton
    public Environment() {
        signalManager = new SignalManager(Constants.MAP_WIDTH, Constants.MAP_HEIGHT, Constants.SIGNAL_MAP_STEP);

        System.out.println("Environment is initialized.");
    }
}
