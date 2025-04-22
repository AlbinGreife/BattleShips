package controllers;

import projectClasses.Game;
import projectClasses.Config;
import projectClasses.WildCards;
import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.TextInputDialog;

public class PrimaryWindowController implements Initializable {

    @FXML private GridPane gridPaneUser;
    @FXML private GridPane gridPaneTarget;
    @FXML private Label shotsLabel;
    @FXML private Button enemyButton;
    @FXML private Button showPositionsButton;
    @FXML private TextField rowField;
    @FXML private TextField colField;
    @FXML private Button fireButton;
    @FXML private Button wildCardsButton;
    @FXML private Label statusLabel;

    private final int CELL_SIZE = 40;
    private int gridSize;
    private boolean positionsShown = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        Game.startGame();
        setupGame();
        setupEnemyButton();
        setupShowPositionsButton();
        boolean isHairol = Config.getGridSize() == 22;
        wildCardsButton.setDisable(!isHairol);
        setupFireButton();
        setupWildCardsButton();
    }
    
    private void setupGame() {
        this.gridSize = Config.getGridSize();
        gridPaneUser.setPrefSize(gridSize * CELL_SIZE, gridSize * CELL_SIZE);
        updateUserGrid();
        gridPaneTarget.setPrefSize(gridSize * CELL_SIZE, gridSize * CELL_SIZE);
        drawEmptyTarget();
        shotsLabel.setText("Player: " + Config.getPlayerName() + " | Shots Remaining: " + Game.getPlayerShots());
    }

    private void drawEmptyTarget() {
        gridPaneTarget.getChildren().clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                gridPaneTarget.add(cell, col, row);
            }
        }
    }

    private String getStyleForInfo(String info, boolean isHit) {
        if (isHit) {            
            if ("trap".equals(info)) {
                return "-fx-background-color: black; -fx-border-color: black;";
           }

            if (info != null && info.startsWith("ship:")) {
                return "-fx-background-color: #8B0000; -fx-border-color: black;"; // Dark red for hits
            } else {
                return "-fx-background-color: lightblue; -fx-border-color: black;";
            }
        } else {
            if (info != null && info.startsWith("ship:")) {
                String color = info.split(":")[2];
                return "-fx-background-color: " + color + "; -fx-border-color: black;";
            } else if ("island".equals(info)) {
                return "-fx-background-color: yellow; -fx-border-color: black;";
            } else if ("trap".equals(info)) {
                return "-fx-background-color: black; -fx-border-color: black;";
            } else {
                return "-fx-background-color: lightgray; -fx-border-color: black;";
            }
        }
    }

    private void setupFireButton() {
        fireButton.setOnAction(e -> {
            try {
                int row = Integer.parseInt(rowField.getText());
                int col = Integer.parseInt(colField.getText());
                Game.playerTurn(row, col);
                if (!positionsShown) {
                    updateTargetGrid();
                } else {
                    showEnemyPositions();
                }
                updateUserGrid(); // Reflect Skynet's hits
                shotsLabel.setText("Player: " + Config.getPlayerName() + " | Shots Remaining: " + Game.getPlayerShots());
            } catch (NumberFormatException ex) {
                System.out.println("Invalid coordinates.");
            }
            List<String> msgs = Game.getNotifications();
            statusLabel.setText(String.join("   ", msgs));

           if (Game.isGameOver()) {
            try {
                scene.setRoot(loadFXML("EndGame"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        });
    }

    private void setupShowPositionsButton() {
        showPositionsButton.setOnAction(e -> {
            positionsShown = !positionsShown;
            if (positionsShown) {
                showEnemyPositions();
                showPositionsButton.setText("Hide Positions");
            } else {
                updateTargetGrid();
                showPositionsButton.setText("Show Positions");
            }
        });
    }

    private void setupEnemyButton() {
        enemyButton.setOnAction(e -> {
            try {
                scene.setRoot(loadFXML("SecondWindow"));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void updateTargetGrid() {
        boolean[][] hits = Game.getTargetHits();
        String[][] enemyBoard = controllers.SecondWindowController.getSavedEnemyCellInfo();
        gridPaneTarget.getChildren().clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                if (hits[row][col]) {
                    cell.setStyle(getStyleForInfo(enemyBoard[row][col], true));
                } else {
                    cell.setStyle("-fx-background-color: lightgray; -fx-border-color: black;");
                }
                gridPaneTarget.add(cell, col, row);
            }
        }
    }

    private void showEnemyPositions() {
        boolean[][] hits = Game.getTargetHits();
        String[][] enemyBoard = controllers.SecondWindowController.getSavedEnemyCellInfo();
        gridPaneTarget.getChildren().clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                if (hits[row][col]) {
                    cell.setStyle(getStyleForInfo(enemyBoard[row][col], true));
                } else {
                    cell.setStyle(getStyleForInfo(enemyBoard[row][col], false));
                }
                gridPaneTarget.add(cell, col, row);
            }
        }
    }

    private void updateUserGrid() {
        boolean[][] userHits = Game.getUserHits();
        String[][] infoGrid = Config.getCellInfo();
        gridPaneUser.getChildren().clear();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                if (userHits[row][col]) {
                    cell.setStyle(getStyleForInfo(infoGrid[row][col], true));
                } else {
                    cell.setStyle(getStyleForInfo(infoGrid[row][col], false));
                }
                gridPaneUser.add(cell, col, row);
            }
        }
    }

    private void setupWildCardsButton() {
        wildCardsButton.setOnAction(e -> {
            List<String> options = List.of("Tiro doble", "Tiro triple", "Escudo");
            ChoiceDialog<String> dlg = new ChoiceDialog<>(options.get(0), options);
            dlg.setTitle("Selecciona comodín");
            dlg.setHeaderText("Elige un comodín para este turno");
            Optional<String> chosen = dlg.showAndWait();
            if (chosen.isPresent()) {
                String sel = chosen.get();
                boolean ok = false;
                switch (sel) {
                    case "Tiro doble":
                        ok = WildCards.activateDoubleShot();
                        break;
                    case "Tiro triple":
                        ok = WildCards.activateTripleShot();
                        break;
                    case "Escudo":
                        TextInputDialog rd = new TextInputDialog();
                        rd.setHeaderText("Fila del escudo (0-" + (gridSize-1) + ")");
                        Optional<String> or = rd.showAndWait();
                        if (or.isPresent()) {
                            TextInputDialog cd = new TextInputDialog();
                            cd.setHeaderText("Columna del escudo (0-" + (gridSize-1) + ")");
                            Optional<String> oc = cd.showAndWait();
                            if (oc.isPresent()) {
                                int sr = Integer.parseInt(or.get());
                                int sc = Integer.parseInt(oc.get());
                                ok = WildCards.activateShield(sr, sc);
                            }
                        }
                        break;
                }
                statusLabel.setText(ok ? sel + " activado" : sel + " no disponible aún");
            }
        });
    }
}