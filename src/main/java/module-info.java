module com.example.cnetcoffee {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;
    requires java.sql;

    opens com.example.cnetcoffee to javafx.fxml;
    exports com.example.cnetcoffee;
    exports com.example.cnetcoffee.Controller.Admin;
    opens com.example.cnetcoffee.Controller.Admin to javafx.fxml;
    exports com.example.cnetcoffee.utils;
    opens com.example.cnetcoffee.utils to javafx.fxml;
    opens com.example.cnetcoffee.Model to javafx.base;
    opens css;
    exports com.example.cnetcoffee.Controller.User;
    opens com.example.cnetcoffee.Controller.User to javafx.fxml;
    exports com.example.cnetcoffee.dao;
    opens com.example.cnetcoffee.dao to javafx.fxml;
}