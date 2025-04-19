module org.svalero.tvmaze {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.svalero.tvmaze to javafx.fxml;
    exports org.svalero.tvmaze;
    exports org.svalero.tvmaze.app;
    opens org.svalero.tvmaze.app to javafx.fxml;
    exports org.svalero.tvmaze.controller;
    opens org.svalero.tvmaze.controller to javafx.fxml;
}