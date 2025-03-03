module org.example.finalproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires javafx.media;
    requires java.net.http;
    requires org.apache.httpcomponents.core5.httpcore5;
    requires se.michaelthelin.spotify;
    requires nv.i18n;
    requires com.google.gson;
    requires java.sql;


    opens finalproject to javafx.fxml;
    exports finalproject;
    exports project;
    opens project to javafx.fxml;
}