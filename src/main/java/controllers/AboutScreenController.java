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
import javafx.scene.control.ScrollBar;

/**
 * FXML Controller class
 */
public class AboutScreenController implements Initializable {

    @FXML
    private ScrollBar colorBar;
    @FXML
    private Button backButtom;
    @FXML
    private Label label1;
    @FXML
    private Label label2;
    @FXML
    private Label label3;
    @FXML
    private Label label4;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    @FXML
    public void backMenu() throws IOException {
        scene.setRoot(loadFXML("MapView"));
    }

}
