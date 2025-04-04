package controllers;

import projectClasses.Config;
import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
    @FXML
    public void startBattle() throws IOException {
        scene.setRoot(loadFXML("PrimaryWindow"));
    }
    private final int CELL_SIZE = 40;
    private int gridSize;
    private boolean[][] occupiedCells;
    // cellInfo saves "ship:<color>" for ships, "island" for islands, "trap" for traps.
    private String[][] cellInfo;
    // Ships list and replacing
    private List<PlacedShip> placedShips = new ArrayList<>();

    // Class represents a ship into the cell.
    private class PlacedShip {
        int row, col, size;
        Color color;
        boolean horizontal; // true: horizontal, false: vertical.

        PlacedShip(int row, int col, int size, Color color) {
            this.row = row;
            this.col = col;
            this.size = size;
            this.color = color;
            this.horizontal = true; // Default ini position.
        }
    }

    public void initialize()  {
        this.gridSize = Config.getGridSize();
        this.occupiedCells = new boolean[gridSize][gridSize];
        this.cellInfo = new String[gridSize][gridSize];
        createGrid();
        addShips();
        setupStartButton();
    }

    private void createGrid() {
        gridPane.getChildren().clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane pane = new Pane();
                pane.setPrefSize(CELL_SIZE, CELL_SIZE);
                pane.setStyle("-fx-border-color: black; -fx-background-color: lightgray;");
                final int finalRow = row;
                final int finalCol = col;

                // Allow dragging an placed ship.
                pane.setOnDragDetected(event -> {
                    // If the cell has a ship, restart colocation.
                    if (cellInfo[finalRow][finalCol] != null && cellInfo[finalRow][finalCol].startsWith("ship:")) {
                        PlacedShip target = null;
                        // Look for the ship that encompasses that cell.
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
                            // Remove ship from the list and clean it.
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
                            //Create draggable node for ships and add it to shipbox.
                            Rectangle newShip = createShip(target.size, target.color);
                            shipsBox.getChildren().add(newShip);
                            event.consume();
                        }
                    }
                });

                // Allow a cell to recieve a ship.
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
                        int shipSize = (int) (ship.getWidth() / CELL_SIZE);
                        // Check horizontal position without overlap.
                        boolean canPlace = true;
                        for (int i = 0; i < shipSize; i++) {
                            if (finalCol + i >= gridSize || occupiedCells[finalRow][finalCol + i]) {
                                canPlace = false;
                                break;
                            }
                        }
                        if (canPlace) {
                            for (int i = 0; i < shipSize; i++) {
                                occupiedCells[finalRow][finalCol + i] = true;
                                cellInfo[finalRow][finalCol + i] = "ship:" + getCssColor((Color) ship.getFill());
                                Pane cell = getCellAt(finalRow, finalCol + i);
                                if (cell != null) {
                                    cell.setStyle("-fx-background-color: " + getCssColor((Color) ship.getFill()) + "; -fx-border-color: black;");
                                }
                            }
                            placedShips.add(new PlacedShip(finalRow, finalCol, shipSize, (Color) ship.getFill()));
                            shipsBox.getChildren().remove(ship);
                            success = true;
                        }
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                // Ship rotation(double click).
                pane.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2) {
                        Integer r = GridPane.getRowIndex(pane);
                        Integer c = GridPane.getColumnIndex(pane);
                        if (r == null || c == null) return;
                        if (cellInfo[r][c] != null && cellInfo[r][c].startsWith("ship:")) {
                            PlacedShip target = null;
                            for (PlacedShip ps : placedShips) {
                                if (ps.horizontal) {
                                    if (r == ps.row && c >= ps.col && c < ps.col + ps.size) {
                                        target = ps;
                                        break;
                                    }
                                } else {
                                    if (c == ps.col && r >= ps.row && r < ps.row + ps.size) {
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

                gridPane.add(pane, col, row);
            }
        }
    }

    private void addShips() {
        shipsBox.getChildren().clear();
        Rectangle[] ships = {
            createShip(4, Color.GRAY),
            createShip(3, Color.BLUE), createShip(3, Color.BLUE),
            createShip(2, Color.GREEN), createShip(2, Color.GREEN), createShip(2, Color.GREEN),
            createShip(1, Color.RED), createShip(1, Color.RED), createShip(1, Color.RED), createShip(1, Color.RED)
        };
        shipsBox.getChildren().addAll(ships);
    }

    private Rectangle createShip(int size, Color color) {
        Rectangle ship = new Rectangle(CELL_SIZE * size, CELL_SIZE);
        ship.setFill(color);
        ship.setStroke(Color.BLACK);
        ship.setOnDragDetected(event -> {
            Dragboard db = ship.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString("ship");
            db.setContent(content);
            event.consume();
        });
        return ship;
    }

    // Once pressed checks ovelaps between ships
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
        Config.setOccupiedCells(occupiedCells);
        Config.setCellInfo(cellInfo);
        placeIslandsTraps();
        try {
            scene.setRoot(loadFXML("PrimaryWindow"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    });
}

    // cheking no overlap(ships).
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

    private void placeIslandsTraps() {
        // Set islands (show in yellow)
        List<int[]> islands = Config.getIslandPositions();
        for (int[] pos : islands) {
            Pane cell = getCellAt(pos[0], pos[1]);
            if (cell != null) {
                cell.setStyle("-fx-background-color: yellow;");
                cellInfo[pos[0]][pos[1]] = "island";
            }
        }
        // Set traps (shows in black)
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
                (int)(color.getRed() * 255),
                (int)(color.getGreen() * 255),
                (int)(color.getBlue() * 255));
    }

    // Ships rotation
    private void rotateShip(PlacedShip ps) {
        boolean newOrientation = !ps.horizontal;
        int[][] newCells = new int[ps.size][2];
        if (newOrientation) { // De horizontal a vertical.
            if (ps.row + ps.size > gridSize) return;
            for (int i = 0; i < ps.size; i++) {
                newCells[i][0] = ps.row + i;
                newCells[i][1] = ps.col;
            }
        } else { // Vertical to horizontal
            if (ps.col + ps.size > gridSize) return;
            for (int i = 0; i < ps.size; i++) {
                newCells[i][0] = ps.row;
                newCells[i][1] = ps.col + i;
            }
        }
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
            if (simOccupied[r][c]) {
                return; // cancel rotation(colision detected).
            }
        }
        ps.horizontal = newOrientation;
        // Rebuild global state->update window.
        boolean[][] newOccupied = new boolean[gridSize][gridSize];
        String[][] newCellInfo = new String[gridSize][gridSize];
        for (PlacedShip ship : placedShips) {
            if (ship.horizontal) {
                for (int i = 0; i < ship.size; i++) {
                    int r = ship.row, c = ship.col + i;
                    newOccupied[r][c] = true;
                    newCellInfo[r][c] = "ship:" + getCssColor(ship.color);
                }
            } else {
                for (int i = 0; i < ship.size; i++) {
                    int r = ship.row + i, c = ship.col;
                    newOccupied[r][c] = true;
                    newCellInfo[r][c] = "ship:" + getCssColor(ship.color);
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
                            cell.setStyle("-fx-background-color: " + cellInfo[row][col].substring(5) + "; -fx-border-color: black;");
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
