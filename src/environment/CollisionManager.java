package environment;

import main.Constants;
import sim.field.grid.DoubleGrid2D;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
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
        System.out.println(collisionMap[0][0]);
        System.out.println("Collision map initialized with " + cellsW + " x " + cellsH + " cells.");
    }

    private void initializeMap(String image) {
        File imgPath = new File(image);
        BufferedImage img;
        try {
            img = ImageIO.read(imgPath);
            assert img.getWidth() == collisionMap.length;
            assert img.getHeight() == collisionMap[0].length;
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

    public boolean isColliding(Point2D p) {
        int x = (int) (p.getX() / step);
        int y = (int) (p.getY() / step);

        return collisionMap[x][y];
    }

    public Point2D firstPathPointColliding(Point2D p1, Point2D p2) {
        int n_points = (int) (p1.distance(p2) / this.step * 10);
        Point2D tmpPoint = new Point2D.Float();
        boolean colliding = false;

        for (int i = 0; i < n_points; i++) {
            tmpPoint.setLocation(p1.getX() + i * p2.getX() / n_points, p1.getY() + i * p2.getY() / n_points);
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
