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

    /**
     * Initializes the controller class.
     */
    String[] difArray = {
         "Easy","Medium","Hard"};
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
   public void backMenu() throws IOException{
        scene.setRoot(loadFXML("MapView"));
   }

    @FXML
    private void startGame(ActionEvent event) throws IOException {
        String selectedDifficult = chooseDif.getValue();

    if (selectedDifficult != null) {
        Config.setSelectedDifficult(selectedDifficult);

        Label1.setText("Difficult selected: " + selectedDifficult);

        // cambiar a pantalla de juego
        System.out.println("Starting game in " + selectedDifficult + " mode...");
        scene.setRoot(loadFXML("GamePlayWindow"));
    } else {
        Label1.setText("Please select a difficult level.");
    }
    }
   

    
}
