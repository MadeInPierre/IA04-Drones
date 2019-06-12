package gui;

import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import environment.Environment;
import main.Constants;
import sim.field.continuous.Continuous2D;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.FieldPortrayal2D;
import sim.util.Double2D;

import java.awt.*;

public class CollisionSensorPortrayal extends FieldPortrayal2D {
    Environment environment;

    public CollisionSensorPortrayal(Environment environment) {
        super();

        this.environment = environment;
    }

    @Override
    public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
        super.draw(object, graphics, info);

        Continuous2D field = (Continuous2D) object;

        Stroke stroke = graphics.getStroke();
        Paint paint = graphics.getPaint();
        graphics.setStroke(new BasicStroke(3));

        for (Object agent : field.allObjects) {
            if (agent.getClass() == DroneAgent.class) {
                DroneAgent drone = (DroneAgent) agent;
                Double2D position = environment.getDronePos(drone);
                float droneAngle = environment.getDroneAngle(drone);

                CollisionsSensor[] sensors = drone.getCollisionSensors();

                for (CollisionsSensor sensor : sensors) {
                    float sensorAbsoluteAngle = droneAngle + sensor.getAngle();

                    float distance = (float) sensor.getDistance(environment, null);
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
