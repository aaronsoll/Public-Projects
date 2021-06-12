package byow.Core;

import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static byow.Core.ReadWriteUtils.*;

/**
 * Main world instance for a game
 */
public class World implements Serializable {
    private static final File CWD = new File(System.getProperty("user.dir"));
    private static final File WORLDOBJ = join(CWD, "world.txt");

    private final int desiredNumFloorTiles;
    private final int maxRoomRadius;
    private final TETile[][] tiles;
    private final long seed;
    private final Random random;
    private final ArrayList<int[]> roomList = new ArrayList<>();
    private int numFloorTiles;
    private final int width;
    private final int height;
    private int[] avatarPos;



    /* GENERAL FUNCTIONS */

    public World(long input) {
        //attribute assignment
        seed = input;
        random = new Random(seed);
        width = 70 + random.nextInt(30);
        height = 40 + random.nextInt(15);
        tiles = new TETile[width][height];
        maxRoomRadius = width / 18;
        desiredNumFloorTiles = (int) (width * height * 0.28);

        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                layNothing(x, y);
            }
        }

        createPath();
        createRooms();
        removeUselessWalls();
        avatarPos = placeAvatarRandomly();
    }

    public static World readSavedWorld() {
        if (WORLDOBJ.exists()) {
            return readObject(WORLDOBJ, World.class);
        }
        return null;
    }

    public void save() {
        writeObject(WORLDOBJ, this);
    }



    /* PATH CREATION FUNCTIONS */

    private void createPath() {
        int xStart = random.nextInt(width - 10) + 5;
        int yStart = random.nextInt(height - 10) + 5;
        layHallway(xStart, yStart);
        int direction = random.nextInt(3);
        createNextPath(xStart, yStart, direction);
    }

    /**
     * Recursively generates pathways
     */
    private void createNextPath(int x, int y, int direction) {
        int[] nextXY = new int[]{};
        int[] endXY = new int[]{};

        //Check if we've got enough floor space
        if (numFloorTiles > (desiredNumFloorTiles)) {
            return;
        }

        //Creates tiles
        nextXY = getNextXY(x, y, direction);
        //Checks if going to hit boundary and forces turn if so
        if (nextTileBoundary(x, y, direction)) {
            //Corner condition:
            // cornerType: 0 if forward + left are boundary, 1 if forward + right are
            // boundary, 2 if not corner
            int cornerType = getCornerType(x, y, direction);
            if (cornerType == 0) {
                createNextPath(x, y, turnRight(direction));
            }
            if (cornerType == 1) {
                createNextPath(x, y, turnLeft(direction));
            }
            if (cornerType == 2) {
                handleTurn(x, y, direction);
            }
            return;
        }
        layHallway(nextXY[0], nextXY[1]);
        numFloorTiles += 1;
        endXY = nextXY;
        //Series of if statements to handle deviations, making calls to other helper functions
        if (turnOccurs()) {
            handleTurn(endXY[0], endXY[1], direction);
            return;
        } else if (branchOccurs()) {
            handleTurn(endXY[0], endXY[1], direction);
        }
        if (roomOccurs()) {
            handleRoom(endXY);
        }
        //Calls itself recursively
        createNextPath(endXY[0], endXY[1], direction);
    }

    private void handleTurn(int x, int y, int direction) {
        //Decide if left or right turn. 0 = right, 1 = left
        int turnDirection = random.nextInt(2);
        int newDirection = 69;
        //Right turn
        if (turnDirection == 0) {
            newDirection = turnRight(direction);
        }
        //Left turn
        if (turnDirection == 1) {
            newDirection = turnLeft(direction);
        }
        createNextPath(x, y, newDirection);
    }



    /* FUNCTIONS THAT DEAL  WITH ROOMS */

    private void handleRoom(int[] location) {
        roomList.add(location);
    }

    /**
     * iterates through all room coordinates from roomList, creating a randomly-sized room
     * at each spot
     */
    private void createRooms() {
        //This is now a int[] array list with (x,y) entries
        for (int[] room : roomList) {
            int x = room[0];
            int y = room[1];
            int rightDist = randRightDist(x);
            int leftDist = randLeftDist(x);
            int upDist = randUpDist(y);
            int downDist = randDownDist(y);

            for (int i = x - leftDist; i <= x + rightDist; i++) {
                for (int j = y - downDist; i <= y + upDist; i++) {
                    layWall(i, j);
                }
            }

            for (int i = x - leftDist + 1; i < x + rightDist; i++) {
                for (int j = y - downDist + 1; j < y + upDist; j++) {
                    layHallway(i, j);
                }
            }
        }
    }


    private int randRightDist(int x) {
        int distToEdge = width - x - 2;
        int biggestAllowed = Math.min(distToEdge, random.nextInt(maxRoomRadius));
        return biggestAllowed;
    }

    private int randLeftDist(int x) {
        int distToEdge = x - 1;
        int biggestAllowed = Math.min(distToEdge, random.nextInt(maxRoomRadius));
        return biggestAllowed;
    }

    private int randUpDist(int y) {
        int distToEdge = height - y - 2;
        int biggestAllowed = Math.min(distToEdge, random.nextInt(maxRoomRadius));
        return biggestAllowed;
    }

    private int randDownDist(int y) {
        int distToEdge = y - 1;
        int biggestAllowed = Math.min(distToEdge, random.nextInt(maxRoomRadius));
        return biggestAllowed;
    }


    /* FUNCTIONS THAT INTERACT DIRECTLY WITH TILESET */

    private boolean validTile(int x, int y) {
        return (exists(x, y) && !tiles[x][y].equals(Tileset.FLOOR)
                && !tiles[x][y].equals(Tileset.AVATAR));
    }

    private boolean isFloor(int x, int y) {
        return (tiles[x][y].description().equals("floor"));
    }

    private void layNothing(int x, int y) {
        tiles[x][y] = Tileset.NOTHING;
    }

    private void layWall(int x, int y) {
        if (validTile(x, y)) {
            tiles[x][y] = Tileset.WALL;
        }
    }

    private int[] placeAvatarRandomly() {
        boolean placed = false;
        int xRand = 0;
        int yRand = 0;
        while (!placed) {
            xRand = random.nextInt(width);
            yRand = random.nextInt(height);
            if (isFloor(xRand, yRand)) {
                tiles[xRand][yRand] = Tileset.AVATAR;
                placed = true;
            }
        }
        return new int[]{xRand, yRand};
    }

    private void placeAvatar(int x, int y) {
        tiles[x][y] = Tileset.AVATAR;
        avatarPos = new int[]{x, y};
    }

    private void layHallway(int x, int y) {
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                if (exists(x + i, y + j)) {
                    layWall(x + i, y + j);
                }
            }
        }
        layFloor(x, y);
    }

    private void layFloor(int x, int y) {
        tiles[x][y] = Tileset.FLOOR;
    }

    private void removeUselessWalls() {
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (uselessWall(i, j)) {
                    layNothing(i, j);
                }
            }
        }
    }

    private boolean uselessWall(int x, int y) {
        if (tiles[x][y] != Tileset.WALL) {
            return false;
        }
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (tiles[x + i][y + j].description().equals("floor")) {
                    return false;
                }
            }
        }
        return true;
    }

    public TETile[][] getVisibleTiles() {
        boolean turnBlack = true;
        TETile[][] curTiles = this.getTiles();
        TETile[][] output = new TETile[width][height];
        List<int[]> visTilesXY = getVisTileArray();
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                int[] coordinates = new int[]{x, y};
                for (int j = 0; j < visTilesXY.size(); j += 1) {
                    int[] listItem = visTilesXY.get(j);
                    if (visTilesXY.get(j)[0] == coordinates[0]
                            && visTilesXY.get(j)[1] == coordinates[1]) {
                        turnBlack = false;
                    }
                }
                if (turnBlack) {
                    output[x][y] = Tileset.NOTHING;
                }
                if (!turnBlack) {
                    output[x][y] = curTiles[x][y];
                }
                turnBlack = true;
            }
        }
        return output;
    }

    /* MINOR HELPER FUNCTIONS */
    //Checks what type of corner and returns 0 if forward and left are boundary,
    // 1 if forward and right
    //returns 2 if not a corner
    private List<int[]> getVisTileArray() {
        int avatarX = avatarPos[0];
        int avatarY = avatarPos[1];
        List<int[]> output = new ArrayList<int[]>();
        int n = 5;
        output = addNAboveBelow(output, n, avatarX, avatarY);
        n -= 1;
        //for loop steps right and left 5
        for (int i = 1; i <= 5; i += 1) {
            output = addNAboveBelow(output, n, avatarX + i, avatarY);
            output = addNAboveBelow(output, n, avatarX - i, avatarY);
            n -= 1;
        }
        return output;
    }

    private List<int[]> addNAboveBelow(List<int[]> inputArray, int n, int x, int y) {
        List<int[]> outputArray = inputArray;
        outputArray.add(new int[]{x, y});
        for (int i = 1; i <= n; i += 1) {
            outputArray.add(new int[]{x, y + i});
        }
        for (int i = 1; i <= n; i += 1) {
            outputArray.add(new int[]{x, y - i});
        }
        return outputArray;
    }

    private int getCornerType(int x, int y, int direction) {
        if (direction == 0) {
            if (y <= 1) {
                return 1;
            }
            if (y >= height - 2) {
                return 0;
            }
        }
        if (direction == 2) {
            if (y <= 1) {
                return 0;
            }
            if (y >= height - 2) {
                return 1;
            }
        }
        if (direction == 1) {
            if (x <= 1) {
                return 1;
            }
            if (x >= width - 2) {
                return 0;
            }
        }
        if (direction == 3) {
            if (x <= 1) {
                return 0;
            }
            if (x >= width - 2) {
                return 1;
            }
        }
        return 2;
    }

    private int turnLeft(int direction) {
        if (direction == 3) {
            return 0;
        }
        return direction + 1;
    }

    private int turnRight(int direction) {
        if (direction == 0) {
            return 3;
        }
        return direction - 1;
    }

    /**
     * Helper function to check if next tile is going to be Boundary
     */
    private boolean nextTileBoundary(int x, int y, int direction) {
        int[] nextXY = getNextXY(x, y, direction);
        x = nextXY[0];
        y = nextXY[1];
        return x <= 1 || x >= width - 2 || y <= 1 || y >= height - 2;
    }

    /**
     * Helper function to get next x and y values for given current x y and direction
     */
    private int[] getNextXY(int curX, int curY, int direction) {
        switch (direction) {
            case 0:
                int[] newXY1 = {curX + 1, curY};
                return newXY1;
            case 1:
                int[] newXY2 = {curX, curY + 1};
                return newXY2;
            case 2:
                int[] newXY3 = {curX - 1, curY};
                return newXY3;
            case 3:
                int[] newXY4 = {curX, curY - 1};
                return newXY4;
            default:
                return null;
        }
    }

    private boolean exists(int x, int y) {
        return (x < width && x >= 0 && y < height && y >= 0);
    }



    /* EXTERNAL INTERACTION FUNCTIONS (PUBLIC) */

    public void moveRight() {
        int x = avatarPos[0] + 1;
        int y = avatarPos[1];
        if (exists(x, y) && isFloor(x, y)) {
            layFloor(avatarPos[0], avatarPos[1]);
            placeAvatar(x, y);
        }
    }

    public void moveLeft() {
        int x = avatarPos[0] - 1;
        int y = avatarPos[1];
        if (exists(x, y) && isFloor(x, y)) {
            layFloor(avatarPos[0], avatarPos[1]);
            placeAvatar(x, y);
        }
    }

    public void moveUp() {
        int x = avatarPos[0];
        int y = avatarPos[1] + 1;
        if (exists(x, y) && isFloor(x, y)) {
            layFloor(avatarPos[0], avatarPos[1]);
            placeAvatar(x, y);
        }
    }

    public void moveDown() {
        int x = avatarPos[0];
        int y = avatarPos[1] - 1;
        if (exists(x, y) && isFloor(x, y)) {
            layFloor(avatarPos[0], avatarPos[1]);
            placeAvatar(x, y);
        }
    }

    public TETile[][] getTiles() {
        return tiles;
    }

    public int[] getDims() {
        int[] dims = new int[]{width, height};
        return dims;
    }

    /* PROBABILITY FUNCTIONS: */

    private boolean turnOccurs() {
        return (RandomUtils.uniform(random, 100) < Probabilities.TURN);
    }

    private boolean roomOccurs() {
        return (RandomUtils.uniform(random, 100) < Probabilities.ROOM);
    }

    private boolean branchOccurs() {
        return (RandomUtils.uniform(random, 100) < Probabilities.BRANCH);
    }
}
