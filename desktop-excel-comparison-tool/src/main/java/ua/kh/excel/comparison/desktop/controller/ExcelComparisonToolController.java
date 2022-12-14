package ua.kh.excel.comparison.desktop.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ua.kh.excel.comparison.desktop.utils.CellChoiceBoxModel;
import ua.kh.excel.comparison.desktop.utils.ExcelDesktopUtils;
import ua.kh.excel.comparison.desktop.utils.JFXUtils;
import ua.kh.excel.comparison.desktop.utils.ValidationUtils;
import ua.kh.excel.provider.CellProviderFactory;
import ua.kh.excel.provider.RowProvider;
import ua.kh.excel.provider.impl.FileNameRowTitleProvider;
import ua.kh.excel.provider.impl.HeaderCellProviderFactory;
import ua.kh.excel.provider.impl.KeyColumnRowProvider;
import ua.kh.excel.provider.impl.SequentCellProviderFactory;
import ua.kh.excel.provider.impl.SequentRowProvider;
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
    private final ExcelService excelService;
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

    public ExcelComparisonToolController() {
        this.excelService = new ExcelService();
        excelFileChooser = new FileChooser();
        excelFileChooser.getExtensionFilters().addAll(ACCEPTED_FILES);
        excelFileChooser.setTitle("Choose a spreadsheet to compare");
        excelFileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
    }

    @FXML
    protected void onchoosefileversion1Click() {
        workbookFileVersion1 = excelFileChooser.showOpenDialog(null);
        if (Objects.isNull(workbookFileVersion1)) {
            return;
        }
        if (applyUpdatesForProvidedSpreadsheet(filePathVersion1, workbookFileVersion1, workBookAvailableSheetsVersion1)) {
            workBookAvailableSheetsVersion1.setOnAction(event -> sheetV1Selected());
            cleanUpSheetSettings(isFirstLineHeaderVersion1, columnNameToCompareV1);
        }
    }

    @FXML
    protected void onchoosefileversion2Click() {
        workbookFileVersion2 = excelFileChooser.showOpenDialog(null);
        if (Objects.isNull(workbookFileVersion2)) {
            return;
        }
        if (applyUpdatesForProvidedSpreadsheet(filePathVersion2, workbookFileVersion2, workBookAvailableSheetsVersion2)) {
            workBookAvailableSheetsVersion2.setOnAction(event -> sheetV2Selected());
            cleanUpSheetSettings(isFirstLineHeaderVersion2, columnNameToCompareV2);
        }
    }

    private boolean applyUpdatesForProvidedSpreadsheet(TextField textField, File spreadsheet, ChoiceBox<Sheet> sheetChoiceBox) {
        try {
            textField.setText(spreadsheet.getAbsolutePath());
            Workbook workbook = ExcelUtils.determineExcelWorkBookRepresentation(spreadsheet);
            List<Sheet> availableSheets = StreamSupport.stream(
                            Spliterators.spliteratorUnknownSize(workbook.sheetIterator(), Spliterator.ORDERED), false)
                    .filter(ExcelUtils::isSheetVisible)
                    .collect(Collectors.toList());
            sheetChoiceBox.setItems(FXCollections.observableList(availableSheets));
            sheetChoiceBox.setConverter(ExcelDesktopUtils.CHOICE_BOX_SHEET_CONVERTER);
            sheetChoiceBox.setDisable(false);
            return true;
        } catch (Exception ex) {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Unable to open provided spreadsheet",
                            "alert-danger")
                    .show();
            return false;
        }
    }

    private void sheetV1Selected() {
        Sheet selectedItem = workBookAvailableSheetsVersion1.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(selectedItem)) {
            isFirstLineHeaderVersion1.getToggles().stream().map(item -> (RadioButton) item)
                    .forEach(item -> item.setDisable(false));
            updateSettingsAfterSelectedSheet(selectedItem, columnNameToCompareV1);
        }
    }

    public void cleanUpSheetSettings(ToggleGroup firstLineHeaderSetting, ChoiceBox<CellChoiceBoxModel> cellChoiceBoxSetting) {
        firstLineHeaderSetting.getToggles().stream().map(item -> (RadioButton) item)
                .forEach(item -> item.setDisable(true));
        cellChoiceBoxSetting.setDisable(true);
        cellChoiceBoxSetting.getItems().clear();
    }

    private void updateSettingsAfterSelectedSheet(Sheet selectedSheet, ChoiceBox<CellChoiceBoxModel> cellChoiceBoxSetting) {
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
    }

    private void sheetV2Selected() {
        Sheet selectedItem = workBookAvailableSheetsVersion2.getSelectionModel().getSelectedItem();
        if (Objects.nonNull(selectedItem)) {
            isFirstLineHeaderVersion2.getToggles().stream().map(item -> (RadioButton) item)
                    .forEach(item -> item.setDisable(false));
            updateSettingsAfterSelectedSheet(selectedItem, columnNameToCompareV2);
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
            FileNameRowTitleProvider titleProvider = new FileNameRowTitleProvider(
                    ExcelUtils.getFileNameWitOutExtension(workbookFileVersion1),
                    ExcelUtils.getFileNameWitOutExtension(workbookFileVersion2));

            Sheet selectedSheetVersion1 = workBookAvailableSheetsVersion1.getSelectionModel().getSelectedItem();
            Sheet selectedSheetVersion2 = workBookAvailableSheetsVersion2.getSelectionModel().getSelectedItem();

            CellProviderFactory cellProviderFactory = getCellProviderFactory(titleProvider, selectedSheetVersion1, selectedSheetVersion2);
            RowProvider rowProvider = getCellProvider(selectedSheetVersion1, selectedSheetVersion2, cellProviderFactory);
            ExcelService.CompareReport report = excelService.compareExcelSheets(rowProvider);
            if (report.getNumberOfCellWithDifferences() > 0) {
                ExcelDesktopUtils.saveReport(reportDirectory, report);
                JFXUtils.createAlert(Alert.AlertType.CONFIRMATION,
                                "Report was generated successfully",
                                "alert-success")
                        .show();
            } else {
                JFXUtils.createAlert(Alert.AlertType.WARNING,
                                "Differences between sheets not found",
                                "alert-warning")
                        .show();
            }
        } catch (Exception exception) {
            JFXUtils.createAlert(Alert.AlertType.ERROR,
                            "Unable to create a report. Root cause: " + exception.getMessage(),
                            "alert-warning")
                    .show();
        }
    }

    private RowProvider getCellProvider(Sheet selectedSheetVersion1, Sheet selectedSheetVersion2, CellProviderFactory cellProviderFactory) {
        CellChoiceBoxModel selectedCellItemV1 = columnNameToCompareV1.getSelectionModel().getSelectedItem();
        CellChoiceBoxModel selectedCellItemV2 = columnNameToCompareV2.getSelectionModel().getSelectedItem();
        if (selectedCellItemV1.isDefaultValue()) {
            return new SequentRowProvider(selectedSheetVersion1, selectedSheetVersion2, cellProviderFactory);
        }
        return new KeyColumnRowProvider(
                selectedSheetVersion1, selectedCellItemV1.getCellIndex(),
                selectedSheetVersion2, selectedCellItemV2.getCellIndex(),
                cellProviderFactory);
    }

    private CellProviderFactory getCellProviderFactory(FileNameRowTitleProvider titleProvider, Sheet selectedSheetVersion1, Sheet selectedSheetVersion2) {
        RadioButton isFirstLineHeaderValue = (RadioButton) isFirstLineHeaderVersion1.getSelectedToggle();
        if ("YES".equalsIgnoreCase(isFirstLineHeaderValue.getText())) {
            return new HeaderCellProviderFactory(
                    Pair.create(selectedSheetVersion1.getRow(0), selectedSheetVersion2.getRow(0)),
                    titleProvider);
        }
        return new SequentCellProviderFactory(titleProvider);
    }

}
