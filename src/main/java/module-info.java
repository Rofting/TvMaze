module org.svalero.tvmaze {
    requires javafx.controls;
    requires javafx.fxml;
    requires retrofit2;
    requires retrofit2.converter.gson;
    requires retrofit2.adapter.rxjava3;
    requires io.reactivex.rxjava3;
    requires gson;
    requires static lombok;


    opens org.svalero.tvmaze to javafx.fxml;

}