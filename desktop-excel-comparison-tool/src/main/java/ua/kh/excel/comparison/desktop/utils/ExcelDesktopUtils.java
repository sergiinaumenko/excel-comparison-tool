package ua.kh.excel.comparison.desktop.utils;

import javafx.util.StringConverter;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Sheet;
import ua.kh.excel.comparison.desktop.exception.SaveReportException;
import ua.kh.excel.service.ExcelService;
import ua.kh.excel.utils.ExcelUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Objects;

@UtilityClass
public class ExcelDesktopUtils {

    public static final StringConverter<Sheet> CHOICE_BOX_SHEET_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(Sheet sheet) {
            if (Objects.isNull(sheet)) {
                return "Select sheet";
            }
            return sheet.getSheetName();
        }

        @Override
        public Sheet fromString(String string) {
            return null;
        }
    };

    public static final StringConverter<CellChoiceBoxModel> CHOICE_BOX_CELL_CONVERTER = new StringConverter<>() {
        @Override
        public String toString(CellChoiceBoxModel cell) {
            if (Objects.isNull(cell)) {
                return "Select unique column name";
            }
            return cell.getCellValue();
        }

        @Override
        public CellChoiceBoxModel fromString(String string) {
            return null;
        }
    };

    public static CellChoiceBoxModel convertCell(Cell cell) {
        return CellChoiceBoxModel.builder()
                .cellIndex(cell.getColumnIndex())
                .cellValue(ExcelUtils.getCellValueAsString(cell))
                .cell(cell)
                .build();
    }

    public static CellChoiceBoxModel createDefaultCell() {
        return CellChoiceBoxModel.builder()
                .cellValue("No unique column")
                .isDefaultValue(true)
                .build();
    }

    public static void saveReport(File reportDirectory, ExcelService.CompareReport report) {
        File file = new File(reportDirectory,
                "compare_report" + System.currentTimeMillis() + ExcelUtils.DEFAULT_REPORT_EXTENSION);
        try (FileOutputStream os = new FileOutputStream(file)) {
            report.getReport().write(os);
        } catch (IOException e) {
            throw new SaveReportException(e);
        }
    }

}
