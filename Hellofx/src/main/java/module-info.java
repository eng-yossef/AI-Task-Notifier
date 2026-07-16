module com.example.hellofx {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires java.net.http;
    requires jakarta.mail;
    requires javafx.web;
    requires java.desktop;
    requires okhttp3;
    requires com.fasterxml.jackson.databind;
    requires itextpdf;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;

    opens com.example.hellofx to javafx.fxml;
    opens com.example.hellofx.utils to javafx.fxml;
    exports com.example.hellofx;
    exports com.example.hellofx.utils;
    exports com.example.hellofx.components;
    opens com.example.hellofx.components to javafx.fxml;
}