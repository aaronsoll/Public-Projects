package byow.lab12;
import org.junit.Test;
import static org.junit.Assert.*;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/**
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    public static TETile[][] addHexagon(int s, int bottomLeftX, int bottomLeftY, TETile tile, TETile[][] world) {
        for (int y = 0; y < s; y++) {
            for (int x = 0; x < s + (2 * y); x++) {
                if (tileExists(bottomLeftX - y + x, bottomLeftY + y)) {
                    world[bottomLeftX - y + x][bottomLeftY + y] = tile;
                }
                if (tileExists(bottomLeftX - y + x, bottomLeftY + 2 * s - 1 - y)) {
                    world[bottomLeftX - y + x][bottomLeftY + 2 * s - 1 - y] = tile;
                }
            }
        }

        return world;
    }

    public static boolean tileExists(int x, int y) {
        return (x >= 0  && x < WIDTH && y >= 0 && y < HEIGHT);
    }

    public static void manyHexagi(int s, int bottomLeftX, int bottomLeftY, int rows, int columns) {
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        for (int x = 0; x < WIDTH; x += 1) {
            for (int y = 0; y < HEIGHT; y += 1) {
                world[x][y] = Tileset.NOTHING;
            }
        }

        /* implement rows and columns */




        ter.renderFrame(world);
    }
}
