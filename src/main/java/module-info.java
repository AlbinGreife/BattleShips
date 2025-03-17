module com.mycompany.battleships {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.battleships to javafx.fxml;
    opens controllers to javafx.fxml;
    exports com.mycompany.battleships;
    opens fxml to javafx.fxml;
}
