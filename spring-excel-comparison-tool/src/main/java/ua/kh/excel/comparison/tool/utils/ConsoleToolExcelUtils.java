package ua.kh.excel.comparison.tool.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import ua.kh.excel.comparison.tool.ConsoleHelper;
import ua.kh.excel.utils.ExcelUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@UtilityClass
public class ConsoleToolExcelUtils {

    private static boolean validateInputDirectory(ConsoleHelper consoleHelper, File file) {
        if (!file.exists()) {
            consoleHelper.printlnWarning("Specified path is not correct");
            return false;
        }
        if (file.isFile()) {
            consoleHelper.printlnWarning("Specified path is not a directory");
            return false;
        }
        return true;
    }

    public static String getFileNameWitOutExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }

    public static List<File> findExcelFiles(ConsoleHelper consoleHelper, File directory) {
        if (!ConsoleToolExcelUtils.validateInputDirectory(consoleHelper, directory)) {
            return Collections.emptyList();
        }
        File[] files = directory.listFiles(ExcelUtils::isExcelDocument);
        if (Objects.isNull(files) || files.length < 2) {
            consoleHelper.printlnWarning("Specified directory does not contain workbooks");
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(files));
    }

    public static void saveReport(ConsoleHelper consoleHelper, File directory, Workbook report) {
        File file = new File(directory,
                "compare_report" + System.currentTimeMillis() + ExcelUtils.DEFAULT_REPORT_EXTENSION);
        try (FileOutputStream os = new FileOutputStream(file)) {
            report.write(os);
            consoleHelper.printlnInfo("Generated report: " + file.getPath());
        } catch (IOException e) {
            consoleHelper.printlnError(e.getLocalizedMessage());
        }
    }

    public static Optional<File> selectWorkBook(ConsoleHelper helper, List<File> files) {
        List<File> sortedWorkbooks = files.stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        Optional<File> workbook = CommandLineUtils.selectElement(helper, sortedWorkbooks, File::getName, "Workbook",
                "Please select workbook for comparison");
        workbook.ifPresent(w -> helper.printlnInfo(
                "Selected '%s' workbook for comparison", w.getName()));
        return workbook;
    }

    public static Optional<Sheet> selectSheet(ConsoleHelper helper, Iterator<Sheet> iterable) {
        List<Sheet> sortedWorkbooks = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterable, Spliterator.ORDERED), false)
                .filter(ExcelUtils::isSheetVisible)
                .sorted(Comparator.comparing(Sheet::getSheetName))
                .collect(Collectors.toList());
        Optional<Sheet> sheet = CommandLineUtils.selectElement(helper, sortedWorkbooks, Sheet::getSheetName, "Sheet",
                "Please select sheet for comparison");
        sheet.ifPresent(s -> helper.printlnInfo(
                String.format("Selected '%s' sheet for comparison", s.getSheetName())));
        return sheet;
    }

    public static Optional<Integer> selectKeyColumn(ConsoleHelper helper, Sheet sheet) {
        List<Pair<Integer, String>> columns = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(
                        sheet.getRow(0).cellIterator(), Spliterator.ORDERED), false)
                .filter(Objects::nonNull)
                .filter(cell -> cell.getCellType() != CellType.BLANK
                        && cell.getCellType() != CellType._NONE)
                .map(cell -> Pair
                        .create(cell.getColumnIndex(), ExcelUtils.getCellValueAsString(cell)))
                .sorted(Comparator.comparing(Pair::getFirst))
                .collect(Collectors.toList());

        Optional<Pair<Integer, String>> keyColumn = CommandLineUtils.selectElement(helper, columns, Pair::getSecond,
                "Column", "Please select key column from " + sheet.getSheetName() + " sheet");
        keyColumn.ifPresent(c -> helper.printlnInfo("Selected '%s' as key column from %s sheet",
                c.getSecond(), sheet.getSheetName()));
        return keyColumn.map(Pair::getFirst);
    }

    public static Optional<Pair<File, File>> selectWorkbooks(ConsoleHelper helper, File path) {
        List<File> files = ConsoleToolExcelUtils.findExcelFiles(helper, path);
        if (files.isEmpty()) {
            return Optional.empty();
        }
        Optional<File> file1 = ConsoleToolExcelUtils.selectWorkBook(helper, files);
        file1.ifPresent(files::remove);
        Optional<Pair<File, File>> selectedFiles = file1
                .flatMap(w -> ConsoleToolExcelUtils.selectWorkBook(helper, files))
                .map(w -> Pair.create(file1.get(), w));
        helper.spacer();
        return selectedFiles;
    }

    public static Optional<Pair<Sheet, Sheet>> selectSheets(ConsoleHelper helper, Pair<Workbook, Workbook> workbooks) {
        Optional<Sheet> sheet1 = ConsoleToolExcelUtils.selectSheet(helper, workbooks.getFirst().sheetIterator());
        Optional<Pair<Sheet, Sheet>> sheets = sheet1
                .flatMap(s -> ConsoleToolExcelUtils.selectSheet(helper, workbooks.getSecond().sheetIterator()))
                .map(sheet2 -> Pair.create(sheet1.get(), sheet2));
        helper.spacer();
        return sheets;
    }

}
