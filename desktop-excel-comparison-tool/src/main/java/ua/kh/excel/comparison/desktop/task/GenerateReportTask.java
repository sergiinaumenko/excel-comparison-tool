package ua.kh.excel.comparison.desktop.task;

import javafx.concurrent.Task;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import ua.kh.excel.comparison.desktop.utils.CellChoiceBoxModel;
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

public class GenerateReportTask extends Task<ExcelService.CompareReport> {

    private final static int DEFAULT_WAIT_TIME = 100;

    private final ExcelService excelService;

    private final File workbookFileVersion1;
    private final File workbookFileVersion2;
    private final ChoiceBox<Sheet> workBookAvailableSheetsVersion1;
    private final ChoiceBox<Sheet> workBookAvailableSheetsVersion2;

    private final ToggleGroup isFirstLineHeaderVersion1;

    private final ChoiceBox<CellChoiceBoxModel> columnNameToCompareV1;
    private final ChoiceBox<CellChoiceBoxModel> columnNameToCompareV2;

    private final File reportDirectory;

    public GenerateReportTask(File workbookFileVersion1, File workbookFileVersion2,
                              ChoiceBox<Sheet> workBookAvailableSheetsVersion1, ChoiceBox<Sheet> workBookAvailableSheetsVersion2,
                              ToggleGroup isFirstLineHeaderVersion1,
                              ChoiceBox<CellChoiceBoxModel> columnNameToCompareV1, ChoiceBox<CellChoiceBoxModel> columnNameToCompareV2,
                              File reportDirectory) {
        this.excelService = new ExcelService();
        this.workbookFileVersion1 = workbookFileVersion1;
        this.workbookFileVersion2 = workbookFileVersion2;
        this.workBookAvailableSheetsVersion1 = workBookAvailableSheetsVersion1;
        this.workBookAvailableSheetsVersion2 = workBookAvailableSheetsVersion2;
        this.isFirstLineHeaderVersion1 = isFirstLineHeaderVersion1;
        this.columnNameToCompareV1 = columnNameToCompareV1;
        this.columnNameToCompareV2 = columnNameToCompareV2;
        this.reportDirectory = reportDirectory;
    }

    @Override
    protected ExcelService.CompareReport call() throws Exception {
        Thread.sleep(DEFAULT_WAIT_TIME);
        FileNameRowTitleProvider titleProvider = new FileNameRowTitleProvider(
                ExcelUtils.getFileNameWitOutExtension(workbookFileVersion1),
                ExcelUtils.getFileNameWitOutExtension(workbookFileVersion2));

        Sheet selectedSheetVersion1 = workBookAvailableSheetsVersion1.getSelectionModel().getSelectedItem();
        Sheet selectedSheetVersion2 = workBookAvailableSheetsVersion2.getSelectionModel().getSelectedItem();

        CellProviderFactory cellProviderFactory = getCellProviderFactory(titleProvider, selectedSheetVersion1, selectedSheetVersion2);
        RowProvider rowProvider = getCellProvider(selectedSheetVersion1, selectedSheetVersion2, cellProviderFactory);
        return excelService.compareExcelSheets(rowProvider);
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
