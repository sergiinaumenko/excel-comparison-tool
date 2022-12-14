package ua.kh.excel.comparison.desktop;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class DesktopExcelComparisonTool extends Application {

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader fxmlLoader = new FXMLLoader(DesktopExcelComparisonTool.class
                .getResource("excel-comparison-tool-desktop.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        scene.getStylesheets().add(DesktopExcelComparisonTool.class.getResource("style.css").toExternalForm());
        stage.setTitle("Excel Comparison Tool");
        stage.setResizable(false);
        stage.getIcons().add(new Image(getClass().getResourceAsStream("tool_icon.jpg")));
        stage.setScene(scene);
        stage.show();
    }
}
