package ua.kh.excel.comparison.desktop.utils;

import javafx.scene.control.Alert;
import lombok.experimental.UtilityClass;
import ua.kh.excel.comparison.desktop.DesktopExcelComparisonTool;

@UtilityClass
public class JFXUtils {

    public static Alert createAlert(Alert.AlertType alertType, String contentText, String cssClassStyle) {
        Alert errorAlert = new Alert(alertType);
        errorAlert.setContentText(contentText);
        errorAlert.setTitle("Excel Comparison Tool Notification");
        errorAlert.getDialogPane().getStylesheets().add(
                DesktopExcelComparisonTool.class.getResource("style.css").toExternalForm());
        errorAlert.getDialogPane().getStyleClass().add(cssClassStyle);
        return errorAlert;
    }

}
