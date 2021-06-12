package byow.Core;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import edu.princeton.cs.introcs.StdDraw;

import java.awt.Font;
import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.Date;

import static edu.princeton.cs.introcs.StdDraw.mouseX;
import static edu.princeton.cs.introcs.StdDraw.mouseY;

public class Engine {
    /* BACKEND VARIABLES */
    private World world;
    private long seed;
    private boolean justEnteredColon = false;
    private boolean enteringNewSeed = false;
    private String gameMode;

    /* FRONTEND VARIABLES */
    private int width = 40;
    private int height = 40;
    private TERenderer ter = new TERenderer();
    private  boolean inWorld = false;
    private boolean restrictedSight = false;
    private String mouseTile = "hello";
    private String dateTime;


    public Engine() {
        /* backend setup for the engine*/
        world = World.readSavedWorld();
        seed = 0;
        dateTime = formatDate(new Date());
    }

    /**
     * Method used for exploring a fresh world. This method should handle all inputs,
     * including inputs from the main menu.
     */
    public void interactWithKeyboard() {
        gameMode = "keyboard";

        frontEndSetup();
        displayStartup();

        while (true) {
            if (StdDraw.hasNextKeyTyped()) {
                dealWithKey(StdDraw.nextKeyTyped());
            }

            /* constantly checking if the mouse has moved to a different tile to update HUD */
            if (inWorld && (newMouseTile() || newDate())) {
                updateUI();
            }
        }
    }

    /**
     * Method used for autograding and testing your code. The input string will be a series
     * of characters (for example, "n123sswwdasdassadwas", "n123sss:q", "lwww". The engine should
     * behave exactly as if the user typed these characters into the engine using
     * interactWithKeyboard.
     * <p>
     * Recall that strings ending in ":q" should cause the game to quite save. For example,
     * if we do interactWithInputString("n123sss:q"), we expect the game to run the first
     * 7 commands (n123sss) and then quit and save. If we then do
     * interactWithInputString("l"), we should be back in the exact same state.
     * <p>
     * In other words, both of these calls:
     * - interactWithInputString("n123sss:q")
     * - interactWithInputString("lww")
     * <p>
     * should yield the exact same world state as:
     * - interactWithInputString("n123sssww")
     *
     * @param input the input string to feed to your program
     * @return the 2D TETile[][] representing the state of the world
     */
    public TETile[][] interactWithInputString(String input) {
        gameMode = "string";

        for (char key: input.toCharArray()) {
            dealWithKey(key);
        }
        return world.getTiles();
    }



    /** processes the single key passed in*/
    private void dealWithKey(char key) {

        /* this block checks if a colon was the last key entered; if this is the case,
        then only a 'q' or 'Q' will do anything*/
        if (justEnteredColon && (key == 'q' || key == 'Q')) {
            saveWorld();
            exitGame();
            return;
        } else if  (justEnteredColon) {
            /* nothing happens if a colon is entered but a q does not follow */
            justEnteredColon = false;
            return;
        }

        switch (key) {
            case 'v':
            case 'V':
                restrictedSight = !restrictedSight;
                break;
            case 'n':
            case 'N':
                enteringNewSeed = true;
                displaySeed();
                break;
            case 'l':
            case 'L':
                inWorld = true;
                initializeRenderer();
                break;
            case ':':
                justEnteredColon = true;
                break;
            case 'Q':
            case 'q':
                if (!inWorld) {
                    exitGame();
                }
                break;
            case 'w':
            case 'W':
                world.moveUp();
                break;
            case 'a':
            case 'A':
                world.moveLeft();
                break;
            case 's':
            case 'S':
                if (enteringNewSeed) {
                    world = new World(seed);
                    enteringNewSeed = false;
                    inWorld = true;
                    initializeRenderer();
                } else {
                    world.moveDown();
                }
                break;
            case 'd':
            case 'D':
                world.moveRight();
                break;
            default:
                dealWithNumber(key);
        }

        if (inWorld)  {
            updateUI();
        }
    }

    private void dealWithNumber(char key) {
        if (enteringNewSeed) {
            String keyString = Character.toString(key);
            seed = seed * 10  + Long.parseLong(keyString);
            displaySeed();
        }
    }

    private void saveWorld() {
        if (world != null) {
            world.save();
        }
    }




    /* FRONTEND FUNCTIONS (these functions will only do anything if in keyboard mode) */

    private void frontEndSetup() {
        StdDraw.setCanvasSize(this.width * 16, this.height * 16);
        StdDraw.setXscale(0, this.width);
        StdDraw.setYscale(0, this.height);
        StdDraw.clear(Color.BLACK);
    }

    /** updates the screen every time a button is pressed in interactWithKeyboard() mode */
    private void updateUI() {
        if (gameMode.equals("keyboard") && !restrictedSight) {
            StdDraw.clear();
            ter.renderFrame(world.getTiles());
            updateHUD();
            updateDateTime();
        } else if (gameMode.equals("keyboard") && restrictedSight) {
            TETile [][] restrictedWorld = world.getVisibleTiles();
            StdDraw.clear();
            ter.renderFrame(restrictedWorld);
            updateHUD();
            updateDateTime();
        }
    }

    private void updateHUD() {
        Font smallFont = new Font("MONACO", Font.PLAIN, 14);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(new Color(255, 255, 255));
        int[] dims = world.getDims();
        StdDraw.textLeft(1, dims[1] - .3, mouseTile);
        StdDraw.text(dims[0] / 2, dims[1] - .3, "Toggle Visibility (V)");
        StdDraw.show();
    }

    private void updateDateTime() {
        Font smallFont = new Font("MONACO", Font.PLAIN, 14);
        StdDraw.setFont(smallFont);
        StdDraw.setPenColor(new Color(255, 255, 255));
        int[] dims = world.getDims();
        StdDraw.textRight(dims[0] - 1, dims[1] - .3, formatDate(new Date()));
        StdDraw.show();
    }

    private void exitGame() {
        if (gameMode.equals("keyboard")) {
            System.exit(0);
        }
    }


    private  void initializeRenderer() {
        if (gameMode.equals("keyboard")) {
            int[] dims = world.getDims();
            ter.initialize(dims[0], dims[1] + 1);
        }
    }

    /** runs the UI during startup */
    private void displayStartup() {
        if (gameMode.equals("keyboard")) {
            StdDraw.clear();
            Font bigFont = new Font("SERIF", Font.BOLD, 36);
            Font smallFont = new Font("SERIF", Font.PLAIN, 24);
            StdDraw.setFont(bigFont);
            StdDraw.setPenColor();
            StdDraw.text(width / 2, height * 2 / 3, "CS61B: THE GAME");
            StdDraw.setFont(smallFont);
            StdDraw.text(width / 2, height * 6 / 20, "New Game (N)");
            StdDraw.text(width / 2, height * 5 / 20, "Load Game (L)");
            StdDraw.text(width / 2, height * 4 / 20, "Quit (Q)");
            StdDraw.show();
        }
    }

    private void displaySeed() {
        if (gameMode.equals("keyboard")) {
            StdDraw.clear();
            Font bigFont = new Font("SERIF", Font.BOLD, 36);
            Font smallFont = new Font("SERIF", Font.PLAIN, 24);
            StdDraw.setFont(bigFont);
            StdDraw.setPenColor();
            StdDraw.text(width / 2, height * 2 / 3, "Enter Seed:");
            if (seed != 0) {
                StdDraw.text(width / 2, height * 3 / 5, Long.toString(seed));
            }
            StdDraw.setFont(smallFont);
            StdDraw.text(width / 2, height * 1 / 4, "Press S to begin.");
        }
    }

    private boolean newMouseTile() {
        if (!currentMouseTile().equals(mouseTile)) {
            mouseTile = currentMouseTile();
            return true;
        }
        return false;
    }

    private String currentMouseTile() {
        int x = (int) mouseX();
        int y = (int) mouseY();
        int worldWidth = world.getDims()[0];
        int worldHeight = world.getDims()[1];
        if (y >= worldHeight) {
            return world.getTiles()[x][worldHeight - 1].description();
        }
        return world.getTiles()[x][y].description();
    }

    private boolean newDate() {
        if (!formatDate(new Date()).equals(dateTime)) {
            dateTime = formatDate(new Date());
            return true;
        }
        return false;
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
        return formatter.format(date);
    }
}
