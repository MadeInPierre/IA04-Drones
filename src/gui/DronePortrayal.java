package gui;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;

import javax.swing.ImageIcon;

import agents.drone.DroneAgent;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.CircledPortrayal2D;
import sim.portrayal.simple.ImagePortrayal2D;
import sim.portrayal.simple.LabelledPortrayal2D;

public class DronePortrayal extends CircledPortrayal2D {

	public DronePortrayal(String image, Paint labelPaint, boolean onlyLabelWhenSelected, Paint circlePaint) {
		super(new LabelledPortrayal2D(new ImagePortrayal2D(new ImageIcon(image), 1), 0, 20, 0, 0.5,
				new Font("SansSerif", Font.BOLD, 15), LabelledPortrayal2D.ALIGN_CENTER, null, labelPaint,
				onlyLabelWhenSelected) {
			@Override
			public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
				DroneAgent drone = (DroneAgent) object;

				this.label = "(" + drone.getID() + ") ";
				this.label += drone.getDroneState() + " | " + drone.getFlyingState();
				super.draw(object, graphics, info);
			}
		}, circlePaint, false);
	}

	@Override
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info) {
		DroneAgent drone = (DroneAgent) object;

		// cool animation
		this.scale += 0.005f;
		if (scale > 3)
			scale = 0.5;

		if (drone.isLeader())
			this.setCircleShowing(true);
		else
			this.setCircleShowing(false);

		super.draw(object, graphics, info);
	}
}
