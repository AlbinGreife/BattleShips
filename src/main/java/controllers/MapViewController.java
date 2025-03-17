/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package controllers;

import com.mycompany.battleships.App;
import static com.mycompany.battleships.App.loadFXML;
import static com.mycompany.battleships.App.scene;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * FXML Controller class
 *
 * @author Albin
 */
public class MapViewController implements Initializable {

    @FXML
    private Button playbuttom;
    @FXML
    private Label label1;
    @FXML
    private Button aboutButtom;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
   @FXML
   public void switchPage() throws IOException{
        scene.setRoot(loadFXML("DifficultSelection"));
   }
      @FXML
   public void toAboutScreen() throws IOException{
        scene.setRoot(loadFXML("AboutScreen"));
   }
}
