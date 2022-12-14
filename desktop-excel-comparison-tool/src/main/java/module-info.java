module ua.kh.excel.comparison.desktop {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;

    requires org.controlsfx.controls;
    requires ua.kh.excel;
    requires org.apache.poi.poi;
    requires commons.math3;

    opens ua.kh.excel.comparison.desktop to javafx.fxml;
    opens ua.kh.excel.comparison.desktop.controller to javafx.fxml;
    opens ua.kh.excel.comparison.desktop.utils to javafx.fxml;
    opens ua.kh.excel.comparison.desktop.exception to javafx.fxml;

    exports ua.kh.excel.comparison.desktop;
    exports ua.kh.excel.comparison.desktop.controller;
    exports ua.kh.excel.comparison.desktop.utils;
    exports ua.kh.excel.comparison.desktop.exception;
}