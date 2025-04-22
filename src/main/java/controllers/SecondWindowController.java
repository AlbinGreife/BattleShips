package controllers;

import projectClasses.Config;
import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.ResourceBundle;

public class SecondWindowController implements Initializable {

    @FXML
    private GridPane gridPane;
    @FXML
    private Label shotsLabel;
    @FXML
    private Button backToUserButton;

    private final int CELL_SIZE = 40;
    private int gridSize;
    private int shotsRemaining;
    private boolean[][] enemyOccupied;
    private String[][] enemyCellInfo;

    // To save enemies positions
    private static boolean boardGenerated = false;
    private static boolean[][] savedEnemyOccupied;
    private static String[][] savedEnemyCellInfo;
    private static int savedGridSize;
    private static int savedShotsRemaining;
    
    
    public static String[][] getSavedEnemyCellInfo() {
    return savedEnemyCellInfo;
}


    // Ships def
    private class ShipDefinition {
        int size;
        Color color;
        ShipDefinition(int size, Color color) {
            this.size = size;
            this.color = color;
        }
    }
    private List<ShipDefinition> enemyShips = new ArrayList<>();

    private Random random = new Random();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // If already saved, use it.
        if (!boardGenerated) {
            setupEnemyBoard();
            // Saing results
            savedEnemyOccupied = enemyOccupied;
            savedEnemyCellInfo = enemyCellInfo;
            savedGridSize = gridSize;
            savedShotsRemaining = shotsRemaining;
            boardGenerated = true;
        } else {
            gridSize = savedGridSize;
            shotsRemaining = savedShotsRemaining;
            enemyOccupied = savedEnemyOccupied;
            enemyCellInfo = savedEnemyCellInfo;
        }
        
        setupBackToUserButton();
        drawGrid();
        shotsLabel.setText("Player: SkyNet | Shots Remaining: " + shotsRemaining);
    }
    public static void ensureBoardGenerated() {
    if (!boardGenerated) {
        // create temp controller, make setup & save results
        SecondWindowController temp = new SecondWindowController();
        temp.setupEnemyBoard();
        savedEnemyOccupied = temp.enemyOccupied;
        savedEnemyCellInfo = temp.enemyCellInfo;
        savedGridSize  = temp.gridSize;
        savedShotsRemaining  = temp.shotsRemaining;
        boardGenerated = true;
    }
}

    private void setupEnemyBoard() {
        // Look for difficult selected to set game
        this.gridSize = Config.getGridSize();
        this.shotsRemaining = Config.getShots();
        enemyOccupied = new boolean[gridSize][gridSize];
        enemyCellInfo = new String[gridSize][gridSize];
        
        // Initialice shps mirror as player
        enemyShips.add(new ShipDefinition(4, Color.GRAY));
        enemyShips.add(new ShipDefinition(3, Color.BLUE));
        enemyShips.add(new ShipDefinition(3, Color.BLUE));
        enemyShips.add(new ShipDefinition(2, Color.GREEN));
        enemyShips.add(new ShipDefinition(2, Color.GREEN));
        enemyShips.add(new ShipDefinition(2, Color.GREEN));
        enemyShips.add(new ShipDefinition(1, Color.RED));
        enemyShips.add(new ShipDefinition(1, Color.RED));
        enemyShips.add(new ShipDefinition(1, Color.RED));
        enemyShips.add(new ShipDefinition(1, Color.RED));

        // Auto set ships without overlay
        int shipCounter = 0;
        for (ShipDefinition ship : enemyShips) {
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 1000) {
                attempts++;
                boolean horizontal = random.nextBoolean();
                int maxRow = horizontal ? gridSize : gridSize - ship.size;
                int maxCol = horizontal ? gridSize - ship.size : gridSize;
                int row = random.nextInt(maxRow);
                int col = random.nextInt(maxCol);
                if (canPlaceShip(row, col, ship.size, horizontal)) {
                    shipCounter++;
                    for (int i = 0; i < ship.size; i++) {
                        int r = horizontal ? row : row + i;
                        int c = horizontal ? col + i : col;
                        enemyOccupied[r][c] = true;
                        enemyCellInfo[r][c] = "ship:" + shipCounter + ":" + getCssColor(ship.color);
                    }
                    placed = true;
                }
            }
            if (!placed) {
                System.out.println("Error al colocar un barco enemigo de tamaño " + ship.size);
            }
        }

        generateEnemyIslandsAndTraps();
    }

    private boolean canPlaceShip(int row, int col, int size, boolean horizontal) {
        for (int i = 0; i < size; i++) {
            int r = horizontal ? row : row + i;
            int c = horizontal ? col + i : col;
            if (r < 0 || r >= gridSize || c < 0 || c >= gridSize) return false;
            if (enemyOccupied[r][c]) return false;
        }
        return true;
    }

    private void generateEnemyIslandsAndTraps() {
        int islands = Config.getIslandCount();
        int traps = Config.getTrapCount();

        // Set Islands
        for (int i = 0; i < islands; i++) {
            int attempts = 0;
            boolean placed = false;
            while (!placed && attempts < 1000) {
                attempts++;
                int row = random.nextInt(gridSize - 1);
                int col = random.nextInt(gridSize);
                if (enemyCellInfo[row][col] == null && enemyCellInfo[row+1][col] == null) {
                    enemyOccupied[row][col] = true;
                    enemyOccupied[row+1][col] = true;
                    enemyCellInfo[row][col] = "island";
                    enemyCellInfo[row+1][col] = "island";
                    placed = true;
                }
            }
        }

        // Set traps
        for (int i = 0; i < traps; i++) {
            int attempts = 0;
            boolean placed = false;
            while (!placed && attempts < 1000) {
                attempts++;
                int row = random.nextInt(gridSize);
                int col = random.nextInt(gridSize);
                if (enemyCellInfo[row][col] == null) {
                    enemyOccupied[row][col] = true;
                    enemyCellInfo[row][col] = "trap";
                    placed = true;
                }
            }
        }
    }

    private void drawGrid() {
        gridPane.getChildren().clear();
        gridPane.setPrefWidth(gridSize * CELL_SIZE);
        gridPane.setPrefHeight(gridSize * CELL_SIZE);

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-border-color: black;");

                String info = enemyCellInfo[row][col];
                if (info != null) {
                    if (info.startsWith("ship:")) {
                        String[] parts = info.split(":");
                        if (parts.length == 3) {
                            String colorStr = parts[2];
                            cell.setStyle("-fx-background-color: " + colorStr + "; -fx-border-color: black;");
                        } else {
                            cell.setStyle("-fx-background-color: gray; -fx-border-color: black;");
                        }
                    } else if (info.equals("island")) {
                        cell.setStyle("-fx-background-color: yellow; -fx-border-color: black;");
                    } else if (info.equals("trap")) {
                        cell.setStyle("-fx-background-color: black; -fx-border-color: black;");
                    }
                } else {
                    cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                }
                gridPane.add(cell, col, row);
            }
        }
    }

    private void setupBackToUserButton() {
        backToUserButton.setOnAction(e -> {
            try {
                scene.setRoot(loadFXML("PrimaryWindow"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private String getCssColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255));
    }
}

