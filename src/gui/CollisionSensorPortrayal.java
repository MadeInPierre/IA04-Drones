package gui;

import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import environment.Environment;
import main.Constants;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;

import java.awt.*;

public class CollisionSensorPortrayal extends FieldPortrayal2D {

    public CollisionSensorPortrayal() {
        super();
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        super.draw(object, graphics, info);
        Continuous2D field = (Continuous2D) object;
        if (field == null)
        	return;

        Stroke stroke = graphics.getStroke();
        Paint paint = graphics.getPaint();
        graphics.setStroke(new BasicStroke(3));
        Bag allObjects = field.getAllObjects();
        if (allObjects == null)
        	return;
        for (Object agent : allObjects) {
            if (agent.getClass() == DroneAgent.class) {
                DroneAgent drone = (DroneAgent) agent;
                Double2D position = Environment.get().getDronePos(drone);
                float droneAngle = Environment.get().getDroneAngle(drone);

                CollisionsSensor[] sensors = drone.getCollisionSensors();

                for (CollisionsSensor sensor : sensors) {
                    float sensorAbsoluteAngle = droneAngle + sensor.getAngle();

                    float distance = (float) sensor.getDistance(null);
                    distance = Math.min(distance < 0 ? 0 : distance, sensor.getRange());

                    graphics.setPaint(
                            distance < 0 ?
                                    new Color(0.5f, 0.5f, 0.5f, 0.5f) :
                                    new Color(1f, 1f, 1f, 0.5f));

                    graphics.drawLine(
                            (int) (info.draw.width * position.x / Constants.MAP_WIDTH),
                            (int) (info.draw.height * position.y / Constants.MAP_HEIGHT),
                            (int) (info.draw.width * (position.x + distance * Math.cos(sensorAbsoluteAngle)) / Constants.MAP_WIDTH),
                            (int) (info.draw.height * (position.y + distance * Math.sin(sensorAbsoluteAngle)) / Constants.MAP_HEIGHT));
                }
            }
        }

        graphics.setStroke(stroke);
        graphics.setPaint(paint);
    }
}
