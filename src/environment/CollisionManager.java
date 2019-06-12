package environment;

import main.Constants;
import sim.field.grid.DoubleGrid2D;
import sim.util.Double2D;

import javax.imageio.ImageIO;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class CollisionManager {
    private boolean[][] collisionMap;
    private float step;

    public CollisionManager(float width, float height, float step, String collisionImage) {
        int cellsW = (int) Math.ceil(width / step);
        int cellsH = (int) Math.ceil(height / step);
        this.collisionMap = new boolean[cellsW][cellsH];
        this.step = step;
        initializeMap(collisionImage);
        System.out.println("Collision map initialized with " + cellsW + " x " + cellsH + " cells.");
    }

    private void initializeMap(String image) {
        File imgPath = new File(image);
        BufferedImage img;
        try {
            img = ImageIO.read(imgPath);
            
            if (img.getWidth() != collisionMap.length || img.getHeight() != collisionMap[0].length) {
				Image tmp = img.getScaledInstance(collisionMap.length, collisionMap[0].length, Image.SCALE_DEFAULT);
			    BufferedImage newImg = new BufferedImage(collisionMap.length, collisionMap[0].length, img.getType());
			    Graphics2D g2d = newImg.createGraphics();
			    g2d.drawImage(tmp, 0, 0, null);
			    g2d.dispose();
			    img = newImg;
			}
            
            for (int x = 0; x < img.getWidth(); x++) {
                for (int y = 0; y < img.getHeight(); y++) {
                    this.collisionMap[x][y] = ((img.getRGB(x, y) >> 16) & 0xff) >= 128;
                }
            }
        } catch (IOException e) {
            System.err.println("Unable to load collision map image !");
            e.printStackTrace();
        }
    }

    public boolean isColliding(Double2D p) {
        int x = ((int) (p.x / step) + collisionMap.length) % collisionMap.length;
        int y = ((int) (p.y / step) + collisionMap[0].length) % collisionMap[0].length;

        return collisionMap[x][y];
    }

    public Double2D firstPathPointColliding(Double2D p1, Double2D p2) {
        int n_points = (int) (p1.distance(p2) / this.step * 10);
        Double2D tmpPoint = new Double2D();
        boolean colliding = false;

        for (int i = 0; i < n_points; i++) {
            Double2D p2mp1 = p2.subtract(p1);
            tmpPoint = new Double2D(p1.x + i * p2mp1.x / n_points, p1.y + i * p2mp1.y / n_points);
            colliding |= isColliding(tmpPoint);

            if (colliding) { break; }
        }

        return colliding ? tmpPoint : null;
    }

    public DoubleGrid2D getCollisionMap() {
        int x = collisionMap.length;
        int y = collisionMap[0].length;

        DoubleGrid2D grid = new DoubleGrid2D(x, y);

        for (int i = 0; i < x; i++) {
            for (int j = 0; j < y; j++) {
                grid.set(i, j, collisionMap[i][j] ? 1 : 0);
            }
        }

        return grid;
    }
}
