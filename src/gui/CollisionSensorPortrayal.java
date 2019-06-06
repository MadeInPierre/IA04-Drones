package gui;

import agents.drone.CollisionsSensor;
import agents.drone.DroneAgent;
import environment.Environment;
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

        for (Object agent : field.allObjects) {
            if (agent.getClass() == DroneAgent.class) {
                DroneAgent drone = (DroneAgent) agent;
                Double2D position = environment.getDronePos(drone);
                float droneAngle = environment.getDroneAngle(drone);

                CollisionsSensor[] sensors = drone.getCollisionSensors();

                for (CollisionsSensor sensor : sensors) {
                    float sensorAbsoluteAngle = droneAngle + sensor.getAngle();
                    float range = sensor.getRange();

                    graphics.drawLine((int) position.x, (int) position.y,
                            (int) (position.x + range * Math.cos(sensorAbsoluteAngle)),
                            (int) (position.y + range * Math.sin(sensorAbsoluteAngle)));
                }
            }
        }
    }
}
