package main;

import sim.display.Console;
import sim.engine.SimState;
import environment.Environment;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting the simulation");
        new Environment();
    }

    public static void runWithoutUI(String[] args) {
    }
}

