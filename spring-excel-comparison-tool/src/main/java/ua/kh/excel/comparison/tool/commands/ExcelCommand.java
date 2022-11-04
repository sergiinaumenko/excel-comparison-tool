package ua.kh.excel.comparison.tool.commands;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ua.kh.excel.comparison.tool.ConsoleHelper;
import ua.kh.excel.comparison.tool.ConsoleHelper.LoadingBur;
import ua.kh.excel.comparison.tool.utils.ConsoleToolExcelUtils;
import ua.kh.excel.provider.CellProviderFactory;
import ua.kh.excel.provider.RowProvider;
import ua.kh.excel.provider.RowTitleProvider;
import ua.kh.excel.provider.impl.FileNameRowTitleProvider;
import ua.kh.excel.provider.impl.HeaderCellProviderFactory;
import ua.kh.excel.provider.impl.KeyColumnRowProvider;
import ua.kh.excel.provider.impl.SequentCellProviderFactory;
import ua.kh.excel.provider.impl.SequentRowProvider;
import ua.kh.excel.service.ExcelService;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@ShellComponent
public class ExcelCommand {

    private static final String YES = "Y";

    private static final float SECOND_SCALE = 1000F;

    private final ConsoleHelper helper;
    private final ExcelService excelService;

    public ExcelCommand(@Autowired ConsoleHelper helper, @Autowired ExcelService excelService) {
        this.helper = helper;
        this.excelService = excelService;
    }

    @ShellMethod(value = "Compare two Excel documents", key = "compare")
    public void compare(@ShellOption(value = {"-p", "--path"}, defaultValue = ".") File path) {
        Optional<Pair<File, File>> files = ConsoleToolExcelUtils.selectWorkbooks(helper, path);
        Optional<Pair<Workbook, Workbook>> loadedWorkbooks = files.flatMap(this::loadWorkbooks);
        Optional<Pair<Sheet, Sheet>> selectedSheets = loadedWorkbooks
                .flatMap(sheet -> ConsoleToolExcelUtils.selectSheets(helper, sheet));
        if (selectedSheets.isEmpty()) {
            return;
        }
        Pair<Sheet, Sheet> sheets = selectedSheets.get();
        FileNameRowTitleProvider titleProvider = new FileNameRowTitleProvider(
                ConsoleToolExcelUtils.getFileNameWitOutExtension(files.get().getFirst()),
                ConsoleToolExcelUtils.getFileNameWitOutExtension(files.get().getSecond()));
        CellProviderFactory cellProvider = selectCellProvider(sheets, titleProvider);
        RowProvider rowProvider = selectRowProvider(sheets, cellProvider);

        long started = System.currentTimeMillis();
        LoadingBur comparingBar = helper.getLoadingBar("Comparing workbooks");
        comparingBar.display();
        ExcelService.CompareReport report = excelService.compareExcelSheets(rowProvider);
        comparingBar.stop();

        if (report.getNumberOfCellWithDifferences() > 0) {
            ConsoleToolExcelUtils.saveReport(helper, files.get().getFirst().getParentFile(), report.getReport());
            long finished = System.currentTimeMillis();
            helper.printlnInfo("Found differences in %d Rows and in %d Cells",
                    report.getNumberOfRowsWithDifferences(), report.getNumberOfCellWithDifferences());
            helper.printlnInfo("Total time: %.2f s", (finished - started) / SECOND_SCALE);
            helper.spacer();
            return;
        }
        helper.printlnInfo("Differences between sheets not found");
    }

    private Optional<Pair<Workbook, Workbook>> loadWorkbooks(Pair<File, File> files) {
        LoadingBur loadBar = helper.getLoadingBar("Loading workbooks");
        loadBar.display();
        // TODO: Need create a utility method for providing correct WorkBook implementation to support different types
        try (Workbook firstWorkbook = new XSSFWorkbook(files.getFirst());
             Workbook secondWorkbook = new XSSFWorkbook(files.getSecond())) {
            return Optional.of(Pair.create(firstWorkbook, secondWorkbook));
        } catch (InvalidFormatException | IOException e) {
            loadBar.stop();
            helper.printlnError(e.getLocalizedMessage());
        } finally {
            loadBar.stop();
        }
        return Optional.empty();
    }

    private CellProviderFactory selectCellProvider(Pair<Sheet, Sheet> sheets,
                                                   RowTitleProvider titleProvider) {
        String result = helper
                .prompt("Would you like using the first line as a header? Y, N", "N");
        helper.removePrintedRows(1);
        if (YES.equalsIgnoreCase(result)) {
            helper.printlnInfo("First line will be used as a header");
            helper.spacer();
            return new HeaderCellProviderFactory(
                    Pair.create(sheets.getFirst().getRow(0), sheets.getSecond().getRow(0)),
                    titleProvider);
        }
        return new SequentCellProviderFactory(titleProvider);
    }

    private RowProvider selectRowProvider(Pair<Sheet, Sheet> sheets,
                                          CellProviderFactory cellProvider) {
        String result = helper.prompt("Would you like compare sheets using key column? Y, N", "N");
        helper.removePrintedRows(1);
        if (YES.equalsIgnoreCase(result)) {
            Optional<Integer> keyColumn1 = ConsoleToolExcelUtils.selectKeyColumn(helper, sheets.getFirst());
            Optional<Integer> keyColumn2 = ConsoleToolExcelUtils.selectKeyColumn(helper, sheets.getSecond());
            helper.spacer();
            if (keyColumn1.isPresent() && keyColumn2.isPresent()) {
                return new KeyColumnRowProvider(
                        sheets.getFirst(), keyColumn1.get(),
                        sheets.getSecond(), keyColumn2.get(),
                        cellProvider);
            }
        }
        return new SequentRowProvider(sheets.getFirst(), sheets.getSecond(), cellProvider);
    }

}
