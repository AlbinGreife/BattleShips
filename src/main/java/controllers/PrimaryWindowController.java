/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import projectClasses.Config;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * FXML Controller class
 *
 * @author Albin
 */


public class PrimaryWindowController implements Initializable {

    @FXML
    private GridPane gridPane;
    @FXML
    private Label shotsLabel;

    private final int CELL_SIZE = 40;
    private int gridSize;
    private int shotsRemaining;
    private boolean[][] occupiedCells;
    private String[][] cellInfo;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupGame();
    }

    private void setupGame() {
        this.gridSize = Config.getGridSize();
        this.shotsRemaining = Config.getShots();
        this.occupiedCells = Config.getOccupiedCells();
        this.cellInfo = Config.getCellInfo();

        gridPane.getChildren().clear();
        shotsLabel.setText("Shots Remaining: " + shotsRemaining);

        drawGrid();
    }

    private void drawGrid() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Pane cell = new Pane();
                cell.setPrefSize(CELL_SIZE, CELL_SIZE);
                cell.setStyle("-fx-border-color: black;");

                String info = cellInfo[row][col];
                if (info != null) {
                    if (info.startsWith("ship:")) {
                        // Get string value then converts into CSS
                        String colorStr = info.substring(5);
                        cell.setStyle("-fx-background-color: " + mapColor(colorStr) + "; -fx-border-color: black;");
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

    // String to color into an css format
    private String mapColor(String colorStr) {
        try {
            if (colorStr.startsWith("0x")) {
                colorStr = colorStr.substring(2);
            }
            String hex = "#" + colorStr.substring(0, 6);
            return hex;
        } catch(Exception e) {
            return "gray";
        }
    }
}
