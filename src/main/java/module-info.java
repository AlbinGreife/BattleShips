module com.mycompany.battleships {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.mycompany.battleships to javafx.fxml;
    exports com.mycompany.battleships;
}
