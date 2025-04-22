/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package projectClasses;
 
/**
 * 
 * @author Albin
 */
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Config {
    private static int gridSize;
    private static int shots;
    private static int islands;
    private static int traps;
    private static boolean[][] occupiedCells;
    // "ship:<color>", "island" o "trap"
    private static String[][] cellInfo;
    private static List<int[]> islandPositions = new ArrayList<>();
    private static List<int[]> trapPositions = new ArrayList<>();
    private static String playerName;

    public static void setPlayerName(String name) {
        playerName = name;
    }

    public static String getPlayerName() {
        return playerName;
    }
    public static void setDifficulty(String difficulty) {
        switch (difficulty) {
            case "Easy":
                gridSize = 10;
                shots = 100;
                islands = 0;
                traps = 0;
                break;
            case "Medium":
                gridSize = 15;
                shots = 20;
                islands = 6;
                traps = 3;
                break;
            case "Hard":
                gridSize = 20;
                shots = 15;
                islands = 12;
                traps = 9;
                break;
             case "Hairol":
                gridSize = 22;
                shots = 12;
                islands = 14;
                traps = 14;
                break;
            default:
                throw new IllegalArgumentException("Invalid difficulty level");
        }
        occupiedCells = new boolean[gridSize][gridSize];
        cellInfo = new String[gridSize][gridSize];
    }

    // Set ships position
    public static void setOccupiedCells(boolean[][] occupied) {
        occupiedCells = occupied;
        // Generate islands and traps
        generateIslandsAndTraps();
    }
    
    // read info->ship color
    public static void setCellInfo(String[][] info) {
        cellInfo = info;
    }

    // add islands and traps
    private static void generateIslandsAndTraps() {
        Random random = new Random();
        islandPositions.clear();
        trapPositions.clear();

        // add islands
        for (int i = 0; i < islands; i++) {
            int row, col;
            boolean valid;
            do {
                row = random.nextInt(gridSize - 1);
                col = random.nextInt(gridSize);
                valid = (cellInfo[row][col] == null) && (cellInfo[row+1][col] == null);
            } while (!valid);

            occupiedCells[row][col] = true;
            occupiedCells[row+1][col] = true;
            cellInfo[row][col] = "island";
            cellInfo[row+1][col] = "island";
            islandPositions.add(new int[]{row, col});
            islandPositions.add(new int[]{row+1, col});
        }

        // Add traps
        for (int i = 0; i < traps; i++) {
            int row, col;
            boolean valid;
            do {
                row = random.nextInt(gridSize);
                col = random.nextInt(gridSize);
                valid = (cellInfo[row][col] == null);
            } while (!valid);

            occupiedCells[row][col] = true;
            cellInfo[row][col] = "trap";
            trapPositions.add(new int[]{row, col});
        }
    }

    public static List<int[]> getIslandPositions() {
        return islandPositions;
    }

    public static List<int[]> getTrapPositions() {
        return trapPositions;
    }

    public static int getGridSize() {
        return gridSize;
    }

    public static int getShots() {
        return shots;
    }

    public static boolean[][] getOccupiedCells() {
        return occupiedCells;
    }

    public static String[][] getCellInfo() {
        return cellInfo;
    }

    public static int getIslandCount() {
        return islands;
    }

    public static int getTrapCount() {
        return traps;
    }
    
}
