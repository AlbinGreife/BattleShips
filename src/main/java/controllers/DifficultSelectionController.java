/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
//añadir import javafx.scene.media.AudioClip;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import projectClasses.Config;

/**
 * FXML Controller class
 *
 * @author Albin
 */
public class DifficultSelectionController implements Initializable {

    private final String[] difArray = {"Easy", "Medium", "Hard"};

    @FXML
    private ChoiceBox<String> chooseDif;
    @FXML
    private Label Label1;
    @FXML
    private Button startButton;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        chooseDif.setItems(FXCollections.observableArrayList(difArray));
    }

    @FXML
    public void backMenu() throws IOException {
        scene.setRoot(loadFXML("MapView"));
    }

    @FXML
    private void startGame(ActionEvent event) throws IOException {
        String selectedDifficulty = chooseDif.getValue();

        if (selectedDifficulty != null) {
            Config.setDifficulty(selectedDifficulty);

            Label1.setText("Difficult selected: " + selectedDifficulty);

            System.out.println("Starting game in " + selectedDifficulty + " mode...");
            scene.setRoot(loadFXML("GamePlayWindow"));
        } else {
            Label1.setText("Please select a difficulty level.");
        }
    }
}
