package controllers;

import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;

public class GamePlayWindowController implements Initializable {

    @FXML
    private Label shipText;
    @FXML
    private Rectangle battleship;
    @FXML
    private Rectangle destroyer2;
    @FXML
    private Rectangle destroyer1;
    @FXML
    private Rectangle destroyer3;
    @FXML
    private Rectangle cruiser1;
    @FXML
    private Rectangle submarine4;
    @FXML
    private Rectangle submarine3;
    @FXML
    private Rectangle submarine2;
    @FXML
    private Rectangle submarine1;
    @FXML
    private Rectangle cruiser2;
    @FXML
    private GridPane gridPane;
    @FXML
    private Button backScreen;
    @FXML
    private Button saveGridState;

    private double offsetX, offsetY; // Para el arrastrar los barcos
    private static final int GRID_SIZE = 10; // Tamaño del tablero (10x10)
    private double cellSize; // Tamaño de cada celda (se ajusta dinámicamente)
    @FXML
    private Label shipText1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Ajustar el tamaño del GridPane y los barcos al tamaño de la ventana
        setupGridAndShips();

        // Configurar eventos de arrastre y rotación
        setupDragAndDrop(battleship);
        setupDragAndDrop(destroyer1);
        setupDragAndDrop(destroyer2);
        setupDragAndDrop(destroyer3);
        setupDragAndDrop(cruiser1);
        setupDragAndDrop(cruiser2);
        setupDragAndDrop(submarine1);
        setupDragAndDrop(submarine2);
        setupDragAndDrop(submarine3);
        setupDragAndDrop(submarine4);
    }

    // Ajustar el tamaño del GridPane y los barcos al tamaño de la ventana
    private void setupGridAndShips() {
        // Escuchar cambios en el tamaño del GridPane
        gridPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            cellSize = newVal.doubleValue() / GRID_SIZE;
            resizeShips();
        });

        gridPane.heightProperty().addListener((obs, oldVal, newVal) -> {
            cellSize = newVal.doubleValue() / GRID_SIZE;
            resizeShips();
        });

        // Configurar el tamaño inicial de los barcos
        cellSize = gridPane.getWidth() / GRID_SIZE;
        resizeShips();
    }

    // Redimensionar los barcos según el tamaño de las celdas
    private void resizeShips() {
        setupShipSize(battleship, 4); // Acorazado (4 casillas)
        setupShipSize(cruiser1, 3);   // Crucero 1 (3 casillas)
        setupShipSize(cruiser2, 3);   // Crucero 2 (3 casillas)
        setupShipSize(destroyer1, 2); // Destructor 1 (2 casillas)
        setupShipSize(destroyer2, 2); // Destructor 2 (2 casillas)
        setupShipSize(destroyer3, 2); // Destructor 3 (2 casillas)
        setupShipSize(submarine1, 1); // Submarino 1 (1 casilla)
        setupShipSize(submarine2, 1); // Submarino 2 (1 casilla)
        setupShipSize(submarine3, 1); // Submarino 3 (1 casilla)
        setupShipSize(submarine4, 1); // Submarino 4 (1 casilla)
    }

    // Configurar el tamaño de un barco según su tipo
    private void setupShipSize(Rectangle ship, int size) {
        ship.setWidth(size * cellSize);
        ship.setHeight(cellSize);
    }

    // Configurar eventos de arrastre y rotación para un barco
    private void setupDragAndDrop(Rectangle ship) {
        ship.setOnMousePressed((MouseEvent event) -> {
            offsetX = event.getSceneX() - ship.getTranslateX();
            offsetY = event.getSceneY() - ship.getTranslateY();
        });

        ship.setOnMouseDragged((MouseEvent event) -> {
            ship.setTranslateX(event.getSceneX() - offsetX);
            ship.setTranslateY(event.getSceneY() - offsetY);
        });

        ship.setOnMouseReleased((MouseEvent event) -> {
            // Obtener la posición del ratón respecto al GridPane
            double mouseX = event.getSceneX() - gridPane.localToScene(0, 0).getX();
            double mouseY = event.getSceneY() - gridPane.localToScene(0, 0).getY();

            // Calcular la celda en la que se soltó el barco
            int col = (int) (mouseX / cellSize);
            int row = (int) (mouseY / cellSize);

            // Limitar las posiciones dentro del tablero
            col = Math.max(0, Math.min(col, GRID_SIZE - 1));
            row = Math.max(0, Math.min(row, GRID_SIZE - 1));

            // Verificar si la posición es válida (no se superpone con otros barcos)
            if (isValidPosition(ship, col, row)) {
                // Restablecer la posición del barco dentro del GridPane
                ship.setTranslateX(0);
                ship.setTranslateY(0);
                GridPane.setColumnIndex(ship, col);
                GridPane.setRowIndex(ship, row);
            } else {
                // Si la posición no es válida, regresar el barco a su posición anterior
                ship.setTranslateX(0);
                ship.setTranslateY(0);
            }
        });

        // Rotar el barco 90 grados al hacer doble clic
        ship.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                // Rotar el barco
                double width = ship.getWidth();
                double height = ship.getHeight();
                ship.setWidth(height);
                ship.setHeight(width);

                // Obtener la posición actual del barco
                int col = GridPane.getColumnIndex(ship);
                int row = GridPane.getRowIndex(ship);

                // Verificar si la nueva posición es válida después de la rotación
                if (!isValidPosition(ship, col, row)) {
                    // Si no es válida, revertir la rotación
                    ship.setWidth(width);
                    ship.setHeight(height);
                }
            }
        });
    }

    // Verificar si la posición del barco es válida (no se superpone con otros barcos)
    private boolean isValidPosition(Rectangle ship, int col, int row) {
        int size = (int) (ship.getWidth() / cellSize); // Tamaño del barco

        for (int i = 0; i < size; i++) {
            int checkCol = col + (ship.getWidth() > ship.getHeight() ? i : 0);
            int checkRow = row + (ship.getWidth() > ship.getHeight() ? 0 : i);

            // Verificar si la celda está dentro del tablero
            if (checkCol >= GRID_SIZE || checkRow >= GRID_SIZE) {
                return false;
            }

            // Verificar si la celda está ocupada por otro barco
            for (javafx.scene.Node node : gridPane.getChildren()) {
                if (node instanceof Rectangle && node != ship) {
                    int nodeCol = GridPane.getColumnIndex(node);
                    int nodeRow = GridPane.getRowIndex(node);
                    int nodeSize = (int) (((Rectangle) node).getWidth() / cellSize);

                    for (int j = 0; j < nodeSize; j++) {
                        int nodeCheckCol = nodeCol + (((Rectangle) node).getWidth() > ((Rectangle) node).getHeight() ? j : 0);
                        int nodeCheckRow = nodeRow + (((Rectangle) node).getWidth() > ((Rectangle) node).getHeight() ? 0 : j);

                        if (checkCol == nodeCheckCol && checkRow == nodeCheckRow) {
                            return false; // Superposición detectada
                        }
                    }
                }
            }
        }

        return true; // Posición válida
    }

    private void saveGridState() {
        String[][] gridState = new String[GRID_SIZE][GRID_SIZE];

        // Inicializar todas las celdas como "Agua"
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                gridState[col][row] = "Agua";
            }
        }

        // Recorrer los barcos y marcar sus posiciones en el tablero
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (node instanceof Rectangle) {
                Rectangle ship = (Rectangle) node; // Hacer casting a Rectangle
                int col = GridPane.getColumnIndex(ship);
                int row = GridPane.getRowIndex(ship);
                int size = (int) (ship.getWidth() / cellSize); // Tamaño del barco

                for (int i = 0; i < size; i++) {
                    if (ship.getWidth() > ship.getHeight()) { // Barco horizontal
                        gridState[col + i][row] = "Barco";
                    } else { // Barco vertical
                        gridState[col][row + i] = "Barco";
                    }
                }
            }
        }

        // Imprimir el estado del tablero (puedes guardarlo en un archivo o usarlo en el juego)
        for (int row = 0; row < GRID_SIZE; row++) {
            for (int col = 0; col < GRID_SIZE; col++) {
                System.out.print(gridState[col][row] + " ");
            }
            System.out.println();
        }
    }

    @FXML
    public void backScreenDif() throws IOException {
        scene.setRoot(loadFXML("DifficultSelection"));
    }
}