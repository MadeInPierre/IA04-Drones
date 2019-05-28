package environment;

import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import agents.drone.DroneAgent;
import main.Constants;
import sim.field.grid.DoubleGrid2D;

public class SignalManager {
    private double[][] qualityMap;
    private float step;
    private DoubleGrid2D qualityField;

    public SignalManager(float width, float height, float step, String signalImage) {
        int cellsW = (int) Math.ceil(width / step);
        int cellsH = (int) Math.ceil(height / step);
        this.qualityMap = new double[cellsW][cellsH];
        this.step = step;
        initializeMap(signalImage);
        System.out.println("Signal map initialized with " + cellsW + " x " + cellsH + " cells.");
        System.out.println(this.qualityMap[0][0]);
    }

    private float getQualityAtPoint(Point2D position) {
        int x = (int) Math.ceil(position.getX() / this.step);
        int y = (int) Math.ceil(position.getY() / this.step);
        return (float) this.qualityMap[x][y];
    }
    
    private void initializeMap(String image) {
    	File imgPath = new File(image);
		BufferedImage img;
		try {
			img = ImageIO.read(imgPath);
			assert img.getWidth() == qualityMap.length;
			assert img.getHeight() == qualityMap[0].length;
			for (int x = 0; x < img.getWidth(); x++) {
				for (int y = 0; y < img.getHeight(); y++) {
					int r = (img.getRGB(x, y)>>16) & 0xff;
					// map 0->255 to signal
					this.qualityMap[x][y] = Constants.MIN_SIGNAL_QUALITY + (1 - (float) r / 255f) * (Constants.MAX_SIGNAL_QUALITY - Constants.MIN_SIGNAL_QUALITY);
				}
			}
		} catch (IOException e) {
			System.err.println("Unable to load signal map image !");
			e.printStackTrace();
		}
    }
    
    private float getPathLoss(float lossExponent, float distance) {
    	return (float) (10 * lossExponent * Math.log10(distance));
    }
    
    private float getExponentLoss(Point2D pos1, Point2D pos2) {
    	int n_points = (int) (pos1.distance(pos2) / this.step * 10);
    	Point2D tmpPoint = new Point2D.Float();
    	float qualitySum = 0f;
    	for(int i = 0; i < n_points; i++) {
    		tmpPoint.setLocation(pos1.getX() + i * pos2.getX() / n_points, pos1.getY() + i * pos2.getY() / n_points);
    		qualitySum += getQualityAtPoint(tmpPoint);
    	}
    	
    	return qualitySum / n_points;
    }
    
    public float getSignalLoss(Point2D pos1, Point2D pos2) {
    	float lossExponent = getExponentLoss(pos1, pos2);
    	float distance = (float) pos1.distance(pos2);
    	return getPathLoss(lossExponent, distance);
    }
    
    public Map<DroneAgent, Float> getDronesInRange(Point2D dronePos, Map<DroneAgent, Point2D> allDrones) {
    	Map<DroneAgent, Float> ret = new HashMap<DroneAgent, Float>();
    	for (Map.Entry<DroneAgent, Point2D> entry : allDrones.entrySet()) {
    		float strength = Constants.EMITTER_SIGNAL_STRENGTH - getSignalLoss(dronePos, entry.getValue());
    	    if (strength >= Constants.MINIMUM_SIGNAL_STRENGTH)
    	    	ret.put(entry.getKey(), strength);
    	}
    	return ret;
    }
    
    public DoubleGrid2D getSignalField() {
    	return new DoubleGrid2D(this.qualityMap);
    }
}
