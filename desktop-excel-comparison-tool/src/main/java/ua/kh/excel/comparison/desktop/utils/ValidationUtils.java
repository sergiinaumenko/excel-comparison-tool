package ua.kh.excel.comparison.desktop.utils;

import javafx.scene.control.ChoiceBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import lombok.experimental.UtilityClass;
import org.apache.poi.ss.usermodel.Sheet;
import ua.kh.excel.comparison.desktop.exception.ValidationException;

import java.io.File;
import java.util.Objects;

@UtilityClass
public class ValidationUtils {

    public static void validateExcelComparisonToolSettings(File workbookFileVersion1, File workbookFileVersion2,
                                                           ChoiceBox<Sheet> workBookAvailableSheetsVersion1, ChoiceBox<Sheet> workBookAvailableSheetsVersion2,
                                                           ToggleGroup isFirstLineHeaderVersion1, ToggleGroup isFirstLineHeaderVersion2,
                                                           ChoiceBox<CellChoiceBoxModel> columnNameToCompareV1, ChoiceBox<CellChoiceBoxModel> columnNameToCompareV2,
                                                           File reportDirectory) {
        validateNonNullableObject(workbookFileVersion1, "Spreadsheet version 1 is not selected!");
        validateNonNullableObject(workbookFileVersion2, "Spreadsheet version 2 is not selected!");

        validateNonNullableObject(workBookAvailableSheetsVersion1.getSelectionModel(), "Sheet from file version 1 is not selected!");
        validateNonNullableObject(workBookAvailableSheetsVersion1.getSelectionModel().getSelectedItem(), "Sheet from file version 1 is not selected!");

        validateNonNullableObject(workBookAvailableSheetsVersion2.getSelectionModel(), "Sheet from file version 2 is not selected!");
        validateNonNullableObject(workBookAvailableSheetsVersion2.getSelectionModel().getSelectedItem(), "Sheet from file version 2 is not selected!");

        validateNonNullableObject(isFirstLineHeaderVersion1.getSelectedToggle(), "First line header setting for spreadsheet version 1 is not provided!");
        validateNonNullableObject(isFirstLineHeaderVersion2.getSelectedToggle(), "First line header setting for spreadsheet version 2 is not provided!");

        RadioButton isFirstLineHeaderValueV1 = (RadioButton) isFirstLineHeaderVersion1.getSelectedToggle();
        RadioButton isFirstLineHeaderValueV2 = (RadioButton) isFirstLineHeaderVersion2.getSelectedToggle();
        if (!isFirstLineHeaderValueV1.getText().equals(isFirstLineHeaderValueV2.getText())) {
            throw new ValidationException("First line header setting is not the same for two files!");
        }

        validateNonNullableObject(columnNameToCompareV1.getSelectionModel(), "Unique column setting is not provided for spreadsheet version 1!");
        validateNonNullableObject(columnNameToCompareV1.getSelectionModel().getSelectedItem(), "Unique column setting is not provided for spreadsheet version 1!");
        validateNonNullableObject(columnNameToCompareV2.getSelectionModel(), "Unique column setting is not provided for spreadsheet version 2!");
        validateNonNullableObject(columnNameToCompareV2.getSelectionModel().getSelectedItem(), "Unique column setting is not provided for spreadsheet version 2!");

        validateNonNullableObject(reportDirectory, "Report directory setting is not provided!");
    }

    private static void validateNonNullableObject(Object item, String validationMessage) {
        if (Objects.isNull(item)) {
            throw new ValidationException(validationMessage);
        }
    }

}
