package controllers;

import projectClasses.Game;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import java.net.URL;
import java.util.ResourceBundle;

public class EndGameController implements Initializable {

    @FXML private Label winnerLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        String w = Game.getWinner();
        if (w == null) w = "Nadie";
        winnerLabel.setText("¡" + w + " ha ganado! Felicidades.");
    }

    @FXML
    private void onExit() {
        System.exit(0);
    }
}
