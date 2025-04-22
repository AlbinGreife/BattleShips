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
// añadir import javafx.scene.media.AudioClip;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import projectClasses.Config;

/**
 * FXML Controller class
 *
 * @author Albin
 */
public class DifficultSelectionController implements Initializable {

    private final String[] difArray = {"Easy", "Medium", "Hard", "Hairol"};

    @FXML
    private ChoiceBox<String> chooseDif;
    @FXML
    private Label Label1;
    @FXML
    private Button startButton;
    @FXML
    private TextField playerNameField;

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
        String playerName = playerNameField.getText();

        if (selectedDifficulty != null && playerName != null && !playerName.trim().isEmpty()) {
            Config.setDifficulty(selectedDifficulty);
            Config.setPlayerName(playerName);

            Label1.setText("Difficult selected: " + selectedDifficulty + " | Player: " + playerName);

            System.out.println("Starting game in " + selectedDifficulty + " mode for player " + playerName + "...");
            scene.setRoot(loadFXML("GamePlayWindow"));
        } else {
            Label1.setText("Please select a difficulty level and enter your name.");
        }
    }
}
