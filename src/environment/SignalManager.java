package environment;

import java.awt.Point;
import java.awt.geom.Point2D;

public class SignalManager {
    private float[][] qualityMap;
    private float step;

    public SignalManager(float width, float height, float step) {
        int cellsW = (int) Math.ceil(width / step);
        int cellsH = (int) Math.ceil(height / step);
        this.qualityMap = new float[cellsW][cellsH];
        this.step = step;
        initializeMap();
        System.out.println("Signal map initialized with " + cellsW + " x " + cellsH + " cells.");
    }

    private Point getGridLocation(Point2D position) {
        int x = (int) Math.ceil(position.getX() / this.step);
        int y = (int) Math.ceil(position.getY() / this.step);
        return new Point(x, y);
    }

    private void initializeMap() {

    }
}
