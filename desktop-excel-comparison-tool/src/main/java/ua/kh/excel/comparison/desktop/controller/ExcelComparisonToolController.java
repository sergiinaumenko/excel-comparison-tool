package ua.kh.excel.comparison.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ua.kh.excel.comparison.desktop.exception.SaveReportException;
import ua.kh.excel.comparison.desktop.task.GenerateReportTask;
import ua.kh.excel.comparison.desktop.task.ReadDocumentTask;
import ua.kh.excel.comparison.desktop.utils.CellChoiceBoxModel;
import ua.kh.excel.comparison.desktop.utils.ExcelDesktopUtils;
import ua.kh.excel.comparison.desktop.utils.JFXUtils;
import ua.kh.excel.comparison.desktop.utils.ValidationUtils;
import ua.kh.excel.service.ExcelService;
import ua.kh.excel.utils.ExcelUtils;

import java.io.File;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class ExcelComparisonToolController {

    private static final List<FileChooser.ExtensionFilter> ACCEPTED_FILES = List.of(
            new FileChooser.ExtensionFilter("All Files", "*.*"),
            new FileChooser.ExtensionFilter("XLSX", "*.xlsx"),
            new FileChooser.ExtensionFilter("XLX", "*.xls"));
    private final FileChooser excelFileChooser;
    @FXML
    private TextField filePathVersion1;
    @FXML
    private TextField filePathVersion2;
    @FXML
    private ToggleGroup isFirstLineHeaderVersion1;
    @FXML
    private ChoiceBox<Sheet> workBookAvailableSheetsVersion1;
    @FXML
    private ChoiceBox<Sheet> workBookAvailableSheetsVersion2;
    @FXML
    private ToggleGroup isFirstLineHeaderVersion2;

    @FXML
    private ChoiceBox<CellChoiceBoxModel> columnNameToCompareV1;

    @FXML
    private ChoiceBox<CellChoiceBoxModel> columnNameToCompareV2;
    private File workbookFileVersion1;

    private File workbookFileVersion2;

    private File reportDirectory;

    @FXML
    private TextField reportDirectoryPath;

    @FXML
    private StackPane rootElement;

    @FXML
    private ProgressIndicator progressIndicator;

    public ExcelComparisonToolController() {
        excelFileChooser = new FileChooser();
        excelFileChooser.getExtensionFilters().addAll(ACCEPTED_FILES);
        excelFileChooser.setTitle("Choose a spreadsheet to compare");
        excelFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    private static void updateAvailableSheets(ChoiceBox<Sheet> sheetChoiceBox, Workbook workbook) {
        List<Sheet> availableSheets = StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(workbook.sheetIterator(), Spliterator.ORDERED), false)
                .filter(ExcelUtils::isSheetVisible)
                .collect(Collectors.toList());
        sheetChoiceBox.setItems(FXCollections.observableList(availableSheets));
        sheetChoiceBox.setConverter(ExcelDesktopUtils.CHOICE_BOX_SHEET_CONVERTER);
        sheetChoiceBox.setDisable(false);
    }

    @FXML
    protected void onchoosefileversion1Click() {
        workbookFileVersion1 = excelFileChooser.showOpenDialog(null);
        if (Objects.isNull(workbookFileVersion1)) {
            return;
        }
        rootElement.setDisable(true);
        ReadDocumentTask task = new ReadDocumentTask(workbookFileVersion1);

        progressIndicator.visibleProperty().bind(task.runningProperty());
        task.setOnFailed(workerStateEvent -> {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Unable to open provided spreadsheet",
                            "alert-danger")
                    .show();
            rootElement.setDisable(false);
        });

        task.setOnSucceeded(workerStateEvent -> {
            filePathVersion1.setText(workbookFileVersion1.getAbsolutePath());
            Workbook workbook = task.getValue();
            updateAvailableSheets(workBookAvailableSheetsVersion1, workbook);
            workBookAvailableSheetsVersion1.setOnAction(event -> sheetV1Selected());
            cleanUpSheetSettings(isFirstLineHeaderVersion1, columnNameToCompareV1);
            rootElement.setDisable(false);
        });
        new Thread(task).start();
    }

    @FXML
    protected void onchoosefileversion2Click() {
        rootElement.setDisable(true);
        workbookFileVersion2 = excelFileChooser.showOpenDialog(null);
        if (Objects.isNull(workbookFileVersion2)) {
            return;
        }
        ReadDocumentTask task = new ReadDocumentTask(workbookFileVersion2);
        progressIndicator.visibleProperty().bind(task.runningProperty());
        task.setOnFailed(workerStateEvent -> {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Unable to open provided spreadsheet",
                            "alert-danger")
                    .show();
            rootElement.setDisable(false);
        });

        task.setOnSucceeded(workerStateEvent -> {
            filePathVersion2.setText(workbookFileVersion2.getAbsolutePath());
            Workbook workbook = task.getValue();
            updateAvailableSheets(workBookAvailableSheetsVersion2, workbook);
            workBookAvailableSheetsVersion2.setOnAction(event -> sheetV2Selected());
            cleanUpSheetSettings(isFirstLineHeaderVersion2, columnNameToCompareV2);
            rootElement.setDisable(false);
        });
        new Thread(task).start();
    }

    private void sheetV1Selected() {
        Sheet selectedItem = workBookAvailableSheetsVersion1.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(selectedItem) && updateSettingsAfterSelectedSheet(selectedItem, columnNameToCompareV1)) {
            isFirstLineHeaderVersion1.getToggles().stream().map(item -> (RadioButton) item)
                    .forEach(item -> item.setDisable(false));
        }
    }

    public void cleanUpSheetSettings(ToggleGroup firstLineHeaderSetting, ChoiceBox<CellChoiceBoxModel> cellChoiceBoxSetting) {
        firstLineHeaderSetting.getToggles().stream().map(item -> (RadioButton) item)
                .forEach(item -> item.setDisable(true));
        cellChoiceBoxSetting.setDisable(true);
        cellChoiceBoxSetting.getItems().clear();
    }

    private boolean updateSettingsAfterSelectedSheet(Sheet selectedSheet, ChoiceBox<CellChoiceBoxModel> cellChoiceBoxSetting) {
        if (selectedSheet.getLastRowNum() < 0) {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Selected sheet does not contain data. Please choose another sheet.",
                            "alert-danger")
                    .show();
            cellChoiceBoxSetting.setDisable(true);
            return false;
        }
        var availableCells = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(
                        selectedSheet.getRow(0).cellIterator(), Spliterator.ORDERED), false)
                .filter(Objects::nonNull)
                .filter(cell -> cell.getCellType() != CellType.BLANK
                        && cell.getCellType() != CellType._NONE)
                .map(ExcelDesktopUtils::convertCell)
                .sorted(Comparator.comparing(CellChoiceBoxModel::getCellIndex))
                .collect(Collectors.toList());
        availableCells.add(0, ExcelDesktopUtils.createDefaultCell());
        cellChoiceBoxSetting.setItems(FXCollections.observableList(availableCells));
        cellChoiceBoxSetting.setConverter(ExcelDesktopUtils.CHOICE_BOX_CELL_CONVERTER);
        cellChoiceBoxSetting.setDisable(false);
        return true;
    }

    private void sheetV2Selected() {
        Sheet selectedItem = workBookAvailableSheetsVersion2.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(selectedItem) && updateSettingsAfterSelectedSheet(selectedItem, columnNameToCompareV2)) {
            isFirstLineHeaderVersion2.getToggles().stream().map(item -> (RadioButton) item)
                    .forEach(item -> item.setDisable(false));
        }
    }

    @FXML
    protected void selectReportDirectory() {
        var directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Choose a directory for report");
        reportDirectory = directoryChooser.showDialog(null);
        if (Objects.nonNull(reportDirectory)) {
            reportDirectoryPath.setText(reportDirectory.getAbsolutePath());
        }
    }

    @FXML
    protected void generateReport() {
        try {
            ValidationUtils.validateExcelComparisonToolSettings(
                    workbookFileVersion1, workbookFileVersion2,
                    workBookAvailableSheetsVersion1, workBookAvailableSheetsVersion2,
                    isFirstLineHeaderVersion1, isFirstLineHeaderVersion2,
                    columnNameToCompareV1, columnNameToCompareV2,
                    reportDirectory
            );

            GenerateReportTask reportTask = new GenerateReportTask(workbookFileVersion1, workbookFileVersion2,
                    workBookAvailableSheetsVersion1, workBookAvailableSheetsVersion2, isFirstLineHeaderVersion1, columnNameToCompareV1, columnNameToCompareV2, reportDirectory);
            rootElement.setDisable(true);
            progressIndicator.visibleProperty().bind(reportTask.runningProperty());

            reportTask.setOnSucceeded(event -> {
                ExcelService.CompareReport report = reportTask.getValue();
                if (report.getNumberOfCellWithDifferences() > 0) {
                    ExcelDesktopUtils.saveReport(reportDirectory, report);
                    JFXUtils.createAlert(Alert.AlertType.CONFIRMATION,
                                    "Report was generated successfully. The report was stored here: "
                                            + reportDirectory.getAbsolutePath(),
                                    "alert-success")
                            .show();
                } else {
                    JFXUtils.createAlert(Alert.AlertType.WARNING,
                                    "Differences between sheets not found",
                                    "alert-warning")
                            .show();
                }
                rootElement.setDisable(false);
            });
            reportTask.setOnFailed(workerStateEvent -> {
                throw new SaveReportException("Unable to create a report.");
            });
            new Thread(reportTask).start();
        } catch (Exception exception) {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Unable to create a report. Root cause: " + exception.getMessage(),
                            "alert-warning")
                    .show();
            rootElement.setDisable(false);
        }
    }

}
