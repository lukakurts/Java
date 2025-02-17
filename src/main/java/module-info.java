module org.example.finalproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens finalproject to javafx.fxml;
    exports finalproject;
}