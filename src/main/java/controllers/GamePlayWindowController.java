package controllers;

import projectClasses.Config;
import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.input.MouseButton;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class GamePlayWindowController {

    @FXML
    private GridPane gridPane;
    @FXML
    private VBox shipsBox;
    @FXML
    private Button startButton;
    /*@FXML
    public void startBattle() throws IOException {
        scene.setRoot(loadFXML("PrimaryWindow"));
    }*/
    private final int CELL_SIZE = 40;
    private int gridSize;
    private boolean[][] occupiedCells;
    private String[][] cellInfo;
    private List<PlacedShip> placedShips = new ArrayList<>();
    //Ship ID
    private int shipCounter = 0;
    
    private class PlacedShip {
        int row, col, size, id;
        Color color;
        boolean horizontal; // true: horizontal, false: vertical

        PlacedShip(int row, int col, int size, Color color, int id) {
            this.row = row;
            this.col = col;
            this.size = size;
            this.color = color;
            this.id = id;
            this.horizontal = false; // Default
        }
    }
@FXML
public void initialize() {
    this.gridSize = Config.getGridSize();
    gridPane.setPrefWidth(gridSize * CELL_SIZE);
    gridPane.setPrefHeight(gridSize * CELL_SIZE);
    this.occupiedCells = new boolean[gridSize][gridSize];
    this.cellInfo = new String[gridSize][gridSize];
    addGrid();
    addShips();
    startButton.setDisable(true);
    setupStartButton();
}

private void addGrid() {
    gridPane.getChildren().clear();
    for (int row = 0; row < gridSize; row++) {
        for (int col = 0; col < gridSize; col++) {
            Pane pane = new Pane();
            pane.setPrefSize(CELL_SIZE, CELL_SIZE);
            pane.setStyle("-fx-border-color: black; -fx-background-color: lightgray;");
            
            final int finalRow = row;
            final int finalCol = col;
            
            // Initialize drag without deleting ship 
            pane.setOnDragDetected(event -> {
                if (cellInfo[finalRow][finalCol] != null && cellInfo[finalRow][finalCol].startsWith("ship:")) {
                    // Se inicia el drag sin quitar el barco de la cuadrícula.
                    Dragboard db = pane.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString("ship");
                    db.setContent(content);
                    event.consume();
                }
            });pane.setOnDragDetected(event -> {
    if (cellInfo[finalRow][finalCol] != null && cellInfo[finalRow][finalCol].startsWith("ship:")) {
        // locate PlacedShip
        PlacedShip target = null;
        for (PlacedShip ps : placedShips) {
            boolean covers = ps.horizontal
                ? (finalRow == ps.row && finalCol >= ps.col && finalCol < ps.col + ps.size)
                : (finalCol == ps.col && finalRow >= ps.row && finalRow < ps.row + ps.size);
            if (covers) { target = ps; break; }
        }
        if (target != null) {
            // 2) Updt cells & draw
            placedShips.remove(target);
            for (int i = 0; i < target.size; i++) {
                int r = target.horizontal ? target.row : target.row + i;
                int c = target.horizontal ? target.col + i : target.col;
                occupiedCells[r][c] = false;
                cellInfo[r][c] = null;
                Pane cell = getCellAt(r, c);
                if (cell != null) {
                    cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                }
            }
            // Create rectangle(includes orientation)
            Rectangle newShip = createShip(target.size, target.color, target.horizontal);
            shipsBox.getChildren().add(newShip);
            updateStartButton();

            // Start ship's drag
            Dragboard db = newShip.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("ship");
            db.setContent(content);
            event.consume();
        }
    }
});
      
            // Clics: Right clic delete and double left clic rotation.
            pane.setOnMouseClicked(event -> {
                // Delete ship (transfer from grid to shipBox)
                if (event.getButton() == MouseButton.SECONDARY) {
                    if (cellInfo[finalRow][finalCol] != null && cellInfo[finalRow][finalCol].startsWith("ship:")) {
                        PlacedShip target = null;
                        for (PlacedShip ps : placedShips) {
                            if (ps.horizontal) {
                                if (finalRow == ps.row && finalCol >= ps.col && finalCol < ps.col + ps.size) {
                                    target = ps;
                                    break;
                                }
                            } else {
                                if (finalCol == ps.col && finalRow >= ps.row && finalRow < ps.row + ps.size) {
                                    target = ps;
                                    break;
                                }
                            }
                        }
                        if (target != null) {
                            // Delete ship, updt cells
                            placedShips.remove(target);
                            if (target.horizontal) {
                                for (int i = 0; i < target.size; i++) {
                                    int r = target.row, c = target.col + i;
                                    occupiedCells[r][c] = false;
                                    cellInfo[r][c] = null;
                                    Pane cell = getCellAt(r, c);
                                    if (cell != null) {
                                        cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                                    }
                                }
                            } else {
                                for (int i = 0; i < target.size; i++) {
                                    int r = target.row + i, c = target.col;
                                    occupiedCells[r][c] = false;
                                    cellInfo[r][c] = null;
                                    Pane cell = getCellAt(r, c);
                                    if (cell != null) {
                                        cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                                    }
                                }
                            }
                            // Adding ship to shipbox
                            Rectangle newShip = createShips(target.size, target.color);
                            shipsBox.getChildren().add(newShip);
                            updateStartButton();
                        }
                    }
                }
                // Ships rotation
                else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    if (cellInfo[finalRow][finalCol] != null && cellInfo[finalRow][finalCol].startsWith("ship:")) {
                        PlacedShip target = null;
                        for (PlacedShip ps : placedShips) {
                            if (ps.horizontal) {
                                if (finalRow == ps.row && finalCol >= ps.col && finalCol < ps.col + ps.size) {
                                    target = ps;
                                    break;
                                }
                            } else {
                                if (finalCol == ps.col && finalRow >= ps.row && finalRow < ps.row + ps.size) {
                                    target = ps;
                                    break;
                                }
                            }
                        }
                        if (target != null) {
                            rotateShip(target);
                        }
                    }
                }
            });
            
            pane.setOnDragOver(event -> {
                if (event.getGestureSource() != pane && event.getDragboard().hasString()) {
                    if (!occupiedCells[finalRow][finalCol]) {
                        event.acceptTransferModes(TransferMode.MOVE);
                    }
                }
                event.consume();
            });

            pane.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    Rectangle ship = (Rectangle) event.getGestureSource();
                    int shipSize = (int) ship.getProperties().get("shipSize");
                    boolean isHorizontal = Boolean.TRUE.equals(ship.getProperties().get("shipHorizontal"));

                    boolean canPlace = true;
                    if (isHorizontal) {
                        for (int i = 0; i < shipSize; i++) {
                            if (finalCol + i >= gridSize || occupiedCells[finalRow][finalCol + i]) {
                                canPlace = false;
                                break;
                            }
                        }
                    } else {
                        for (int i = 0; i < shipSize; i++) {
                            if (finalRow + i >= gridSize || occupiedCells[finalRow + i][finalCol]) {
                                canPlace = false;
                                break;
                            }
                        }
                    }
                    if (canPlace) {
                        if (isHorizontal) {
                            for (int i = 0; i < shipSize; i++) {
                                occupiedCells[finalRow][finalCol + i] = true;
                                String info = "ship:" + (++shipCounter) + ":" + getCssColor((Color) ship.getFill());
                                cellInfo[finalRow][finalCol + i] = info;
                                Pane cell = getCellAt(finalRow, finalCol + i);
                                if (cell != null) {
                                    cell.setStyle("-fx-background-color: " + getCssColor((Color) ship.getFill()) + "; -fx-border-color: black;");
                                }
                            }
                        } else {
                            for (int i = 0; i < shipSize; i++) {
                                occupiedCells[finalRow + i][finalCol] = true;
                                String info = "ship:" + (++shipCounter) + ":" + getCssColor((Color) ship.getFill());
                                cellInfo[finalRow + i][finalCol] = info;
                                Pane cell = getCellAt(finalRow + i, finalCol);
                                if (cell != null) {
                                    cell.setStyle("-fx-background-color: " + getCssColor((Color) ship.getFill()) + "; -fx-border-color: black;");
                                }
                            }
                        }
                        String[] parts = cellInfo[finalRow][finalCol].split(":");
                        int id = Integer.parseInt(parts[1]);
                        placedShips.add(new PlacedShip(finalRow, finalCol, shipSize, (Color) ship.getFill(), id));
                        shipsBox.getChildren().remove(ship);
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
                updateStartButton();
            });
            gridPane.add(pane, col, row);
        }
    }
}



    private void addShips() {
        shipsBox.getChildren().clear();
        Rectangle[] ships = {
            createShips(4, Color.GRAY),
            createShips(3, Color.BLUE), createShips(3, Color.BLUE),
            createShips(2, Color.GREEN), createShips(2, Color.GREEN), createShips(2, Color.GREEN),
            createShips(1, Color.RED), createShips(1, Color.RED), createShips(1, Color.RED), createShips(1, Color.RED)
        };
        shipsBox.getChildren().addAll(ships);
        updateStartButton();
    }

private Rectangle createShips(int size, Color color) {
    double currentCellSize = gridPane.getWidth() / gridSize;
    // If grid not showed yet, get default value
    if (currentCellSize <= 0) {
        currentCellSize = CELL_SIZE;
    }
    Rectangle ship = new Rectangle(CELL_SIZE, CELL_SIZE * size);
    //ship.getStyleClass().add(getShipStyleClass(size, color));
    ship.setFill(color);//comentar después para imagenes de barcos
    ship.setStroke(Color.BLACK);
    // Load size in case of redimention
    ship.getProperties().put("shipSize", size);
    ship.setOnDragDetected(event -> {
        Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("ship");
        db.setContent(content);
        event.consume();
    });
    return ship;
}
/*private String getShipStyleClass(int size, Color color) {
    // Asigna clase CSS según tamaño y color (ajusta según tus imágenes)
    if (size == 1 && color.equals(Color.RED)) {
        return "ship-submarine";
    } else if (size == 2 && color.equals(Color.GREEN)) {
        return "ship-cruiser";
    } else if (size == 3 && color.equals(Color.BLUE)) {
        return "ship-destroyer";
    } else if (size == 4 && color.equals(Color.GRAY)) {
        return "ship-battleship";
    }
    return ""; // Clase por defecto si no coincide
}*/
private Rectangle createShip(int size, Color color, boolean horizontal) {
    double w = horizontal ? CELL_SIZE * size : CELL_SIZE;
    double h = horizontal ? CELL_SIZE : CELL_SIZE * size;
    Rectangle ship = new Rectangle(w, h);
    ship.setFill(color);//comentar despues para imagen de barcos
    //ship.getStyleClass().add(getShipStyleClass(size, color));
    ship.setStroke(Color.BLACK);
    // Safe size and orientation
    ship.getProperties().put("shipSize", size);
    ship.getProperties().put("shipHorizontal", horizontal);
    ship.setOnDragDetected(event -> {
        Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
        ClipboardContent content = new ClipboardContent();
        content.putString("ship");
        db.setContent(content);
        event.consume();
    });
    return ship;
}

    // Start button on/off
    private void updateStartButton() {
        startButton.setDisable(!shipsBox.getChildren().isEmpty());
    }

    // Verify overlay
    private void setupStartButton() {
        startButton.setOnAction(event -> {
            if (!verifyNoOverlap()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error de Colocación");
                alert.setHeaderText("Superposición de Barcos");
                alert.setContentText("Existen barcos superpuestos. Por favor, recolóquelos hasta que no se solapen.");
                alert.showAndWait();
                return;
            }          
            Config.setCellInfo(cellInfo);
            Config.setOccupiedCells(occupiedCells);
            addIslandsandTraps();
            try {
                scene.setRoot(loadFXML("PrimaryWindow"));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    // verify overlay .
    private boolean verifyNoOverlap() {
        int[][] counts = new int[gridSize][gridSize];
        for (PlacedShip ship : placedShips) {
            if (ship.horizontal) {
                for (int i = 0; i < ship.size; i++) {
                    counts[ship.row][ship.col + i]++;
                }
            } else {
                for (int i = 0; i < ship.size; i++) {
                    counts[ship.row + i][ship.col]++;
                }
            }
        }
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (counts[r][c] > 1) {
                    return false;
                }
            }
        }
        return true;
    }

    private void addIslandsandTraps() {
        // set Islands
        List<int[]> islands = Config.getIslandPositions();
        for (int[] pos : islands) {
            Pane cell = getCellAt(pos[0], pos[1]);
            if (cell != null) {
                cell.setStyle("-fx-background-color: yellow;");
                cellInfo[pos[0]][pos[1]] = "island";
            }
        }
        // Set traps
        List<int[]> traps = Config.getTrapPositions();
        for (int[] pos : traps) {
            Pane cell = getCellAt(pos[0], pos[1]);
            if (cell != null) {
                cell.setStyle("-fx-background-color: black;");
                cellInfo[pos[0]][pos[1]] = "trap";
            }
        }
    }

    private Pane getCellAt(int row, int col) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            Integer r = GridPane.getRowIndex(node);
            Integer c = GridPane.getColumnIndex(node);
            if (r != null && c != null && r == row && c == col) {
                return (Pane) node;
            }
        }
        return null;
    }

    private String getCssColor(Color color) {
        return String.format("#%02X%02X%02X",
                (int)(color.getRed()*255),
                (int)(color.getGreen()*255),
                (int)(color.getBlue()*255));
    }

  private void rotateShip(PlacedShip ps) {
    boolean newOrientation = !ps.horizontal;
    // Calculate new cells to fit ship
    int[][] newCells = new int[ps.size][2];
    if (newOrientation) { 
        // To Horizontal & verify limits
        if (ps.col + ps.size > gridSize) return;
        for (int i = 0; i < ps.size; i++) {
            newCells[i][0] = ps.row;        // Same row
            newCells[i][1] = ps.col + i;    // Consecutive columns
        }
    } else {
        // Set vertical & verify limits
        if (ps.row + ps.size > gridSize) return;
        for (int i = 0; i < ps.size; i++) {
            newCells[i][0] = ps.row + i;    // Consecutive row
            newCells[i][1] = ps.col;        // Same columns
        }
    }

    // Emulate ships cells ocupation
    boolean[][] simOccupied = new boolean[gridSize][gridSize];
    for (PlacedShip other : placedShips) {
        if (other == ps) continue;
        if (other.horizontal) {
            for (int i = 0; i < other.size; i++) {
                simOccupied[other.row][other.col + i] = true;
            }
        } else {
            for (int i = 0; i < other.size; i++) {
                simOccupied[other.row + i][other.col] = true;
            }
        }
    }
    for (int i = 0; i < ps.size; i++) {
        int r = newCells[i][0], c = newCells[i][1];
        if (simOccupied[r][c]) return;  // Overlay with ship
    }

    // All veriffied, uptd orientation & cellInfo/occupiedCells
    ps.horizontal = newOrientation;
    boolean[][] newOccupied = new boolean[gridSize][gridSize];
    String[][] newCellInfo = new String[gridSize][gridSize];

    for (PlacedShip ship : placedShips) {
        if (ship.horizontal) {
            for (int i = 0; i < ship.size; i++) {
                int r = ship.row, c = ship.col + i;
                newOccupied[r][c] = true;
                newCellInfo[r][c] = "ship:" + ship.id + ":" + getCssColor(ship.color);
            }
        } else {
            for (int i = 0; i < ship.size; i++) {
                int r = ship.row + i, c = ship.col;
                newOccupied[r][c] = true;
                newCellInfo[r][c] = "ship:" + ship.id + ":" + getCssColor(ship.color);
            }
        }
    }

    occupiedCells = newOccupied;
    cellInfo = newCellInfo;
    refreshGrid();
}


    private void refreshGrid() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = getCellAt(row, col);
                if (cell != null) {
                    if (cellInfo[row][col] != null) {
                        if (cellInfo[row][col].startsWith("ship:")) {
                            cell.setStyle("-fx-background-color: " + cellInfo[row][col].split(":")[2] + "; -fx-border-color: black;");
                        } else if (cellInfo[row][col].equals("island")) {
                            cell.setStyle("-fx-background-color: yellow; -fx-border-color: black;");
                        } else if (cellInfo[row][col].equals("trap")) {
                            cell.setStyle("-fx-background-color: black; -fx-border-color: black;");
                        }
                    } else {
                        cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                    }
                }
            }
        }
    }
}
