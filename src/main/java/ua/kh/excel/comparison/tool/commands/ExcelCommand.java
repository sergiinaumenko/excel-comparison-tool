package ua.kh.excel.comparison.tool.commands;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import ua.kh.excel.comparison.tool.ConsoleHelper;
import ua.kh.excel.comparison.tool.ConsoleHelper.LoadingBur;
import ua.kh.excel.comparison.tool.prowider.CellProviderFactory;
import ua.kh.excel.comparison.tool.prowider.RowProvider;
import ua.kh.excel.comparison.tool.prowider.RowTitleProvider;
import ua.kh.excel.comparison.tool.prowider.impl.*;
import ua.kh.excel.comparison.tool.service.ExcelService;
import ua.kh.excel.comparison.tool.utils.ExcelUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

@ShellComponent
public class ExcelCommand {

    private static final String YES = "Y";
    private static final int MAX_TABLE_SIZE = 10;
    private static final float SECOND_SCALE = 1000F;

    private final ConsoleHelper helper;
    private final ExcelService excelService;

    public ExcelCommand(@Autowired ConsoleHelper helper, @Autowired ExcelService excelService) {
        this.helper = helper;
        this.excelService = excelService;
    }

    @ShellMethod(value = "Compare two Excel documents", key = "compare")
    public void compare(@ShellOption(value = {"-p", "--path"}, defaultValue = ".") File path) {
        Optional<Pair<File, File>> files = selectWorkbooks(path);
        Optional<Pair<Workbook, Workbook>> loadedWorkbooks = files.flatMap(this::loadWorkbooks);
        Optional<Pair<Sheet, Sheet>> selectedSheets = loadedWorkbooks.flatMap(this::selectSheets);
        if (selectedSheets.isEmpty()) {
            return;
        }
        Pair<Sheet, Sheet> sheets = selectedSheets.get();
        FileNameRowTitleProvider titleProvider = new FileNameRowTitleProvider(
                getFileNameWitOutExtension(files.get().getFirst()),
                getFileNameWitOutExtension(files.get().getSecond()));
        CellProviderFactory cellProvider = selectCellProvider(sheets, titleProvider);
        RowProvider rowProvider = selectRowProvider(sheets, cellProvider);

        long started = System.currentTimeMillis();
        LoadingBur comparingBar = helper.getLoadingBar("Comparing workbooks");
        comparingBar.display();
        ExcelService.CompareReport report = excelService.compareExcelSheets(rowProvider);
        comparingBar.stop();

        if (report.getNumberOfCellWithDifferences() > 0) {
            saveReport(files.get().getFirst().getParentFile(), report.getReport());
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

    private Optional<Pair<File, File>> selectWorkbooks(File path) {
        List<File> files = findExcelFiles(path);
        if (files.isEmpty()) {
            return Optional.empty();
        }
        Optional<File> file1 = selectWorkBook(files);
        file1.ifPresent(files::remove);
        Optional<Pair<File, File>> selectedFiles = file1
                .flatMap(w -> selectWorkBook(files))
                .map(w -> Pair.create(file1.get(), w));
        helper.spacer();
        return selectedFiles;
    }

    private Optional<Pair<Sheet, Sheet>> selectSheets(Pair<Workbook, Workbook> workbooks) {
        Optional<Sheet> sheet1 = selectSheet(workbooks.getFirst().sheetIterator());
        Optional<Pair<Sheet, Sheet>> sheets = sheet1
                .flatMap(s -> selectSheet(workbooks.getSecond().sheetIterator()))
                .map(sheet2 -> Pair.create(sheet1.get(), sheet2));
        helper.spacer();
        return sheets;
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
            Optional<Integer> keyColumn1 = selectKeyColumn(sheets.getFirst());
            Optional<Integer> keyColumn2 = selectKeyColumn(sheets.getSecond());
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

    private Optional<Integer> selectKeyColumn(Sheet sheet) {
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

        Optional<Pair<Integer, String>> keyColumn = selectElement(columns, Pair::getSecond,
                "Column", "Please select key column from " + sheet.getSheetName() + " sheet");
        keyColumn.ifPresent(c -> helper.printlnInfo("Selected '%s' as key column from %s sheet",
                c.getSecond(), sheet.getSheetName()));
        return keyColumn.map(Pair::getFirst);
    }

    private void saveReport(File directory, Workbook report) {
        File file = new File(directory,
                "compare_report" + System.currentTimeMillis() + ExcelUtils.EXCEL_EXTENSION);
        try (FileOutputStream os = new FileOutputStream(file)) {
            report.write(os);
            helper.printlnInfo("Generated report: " + file.getPath());
        } catch (IOException e) {
            helper.printlnError(e.getLocalizedMessage());
        }
    }

    private List<File> findExcelFiles(File directory) {
        if (!validateInputDirectory(directory)) {
            return Collections.emptyList();
        }
        File[] files = directory.listFiles(ExcelUtils::isExcelDocument);
        if (Objects.isNull(files) || files.length < 2) {
            helper.printlnWarning("Specified directory does not contain workbooks");
            return Collections.emptyList();
        }
        return new ArrayList<>(Arrays.asList(files));
    }

    private Optional<File> selectWorkBook(List<File> files) {
        List<File> sortedWorkbooks = files.stream()
                .sorted(Comparator.comparing(File::getName))
                .collect(Collectors.toList());
        Optional<File> workbook = selectElement(sortedWorkbooks, File::getName, "Workbook",
                "Please select workbook for comparison");
        workbook.ifPresent(w -> helper.printlnInfo(
                "Selected '%s' workbook for comparison", w.getName()));
        return workbook;
    }

    private Optional<Sheet> selectSheet(Iterator<Sheet> iterable) {
        List<Sheet> sortedWorkbooks = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(iterable, Spliterator.ORDERED), false)
                .filter(ExcelUtils::isSheetVisible)
                .sorted(Comparator.comparing(Sheet::getSheetName))
                .collect(Collectors.toList());
        Optional<Sheet> sheet = selectElement(sortedWorkbooks, Sheet::getSheetName, "Sheet",
                "Please select sheet for comparison");
        sheet.ifPresent(s -> helper.printlnInfo(
                String.format("Selected '%s' sheet for comparison", s.getSheetName())));
        return sheet;
    }

    private <T> Optional<T> selectElement(List<T> data, Function<T, String> nameExtractor,
                                          String title, String invitation) {
        int currentPage = 0;
        int printedRowCounter;
        printedRowCounter = printTable(data, nameExtractor, title, currentPage);
        helper.spacer();
        helper.println(invitation);
        printedRowCounter += 2;
        T element = null;
        do {
            String input = helper.prompt(buildCommandLine(data.size(), title, currentPage), null);
            printedRowCounter++;
            if ("c".equalsIgnoreCase(input) || "cancel".equalsIgnoreCase(input)) {
                helper.removePrintedRows(printedRowCounter);
                return Optional.empty();
            }
            if ("n".equalsIgnoreCase(input) || "next".equalsIgnoreCase(input)) {
                if (currentPage < data.size() / MAX_TABLE_SIZE) {
                    currentPage++;
                    helper.removePrintedRows(printedRowCounter);
                    printedRowCounter = printTable(data, nameExtractor, title, currentPage);
                } else {
                    helper.printlnWarning(String.format("%s wrong input", input));
                    printedRowCounter++;
                }
                continue;
            }
            if ("p".equalsIgnoreCase(input) || "prev".equalsIgnoreCase(input)) {
                if (currentPage > 0) {
                    currentPage--;
                    helper.removePrintedRows(printedRowCounter);
                    printedRowCounter = printTable(data, nameExtractor, title, currentPage);
                } else {
                    helper.printlnWarning(String.format("%s wrong input", input));
                    printedRowCounter++;
                }
                continue;
            }
            if (!NumberUtils.isParsable(input)) {
                helper.printlnWarning(String.format("%s wrong number", input));
                printedRowCounter++;
                continue;
            }
            int index = Integer.parseInt(input);
            if (index < currentPage * MAX_TABLE_SIZE ||
                    index > Math.min(currentPage * MAX_TABLE_SIZE + MAX_TABLE_SIZE, data.size()) - 1) {
                helper.printlnWarning(String.format("There is no %s with specified number", title));
                printedRowCounter++;
                continue;
            }
            element = data.get(index);

        } while (Objects.isNull(element));
        helper.removePrintedRows(printedRowCounter);
        return Optional.of(element);
    }

    private String buildCommandLine(int dataSize, String title, int pageNum) {
        int pageSize = (dataSize - 1) / MAX_TABLE_SIZE;
        StringBuilder builder = new StringBuilder();
        builder.append("Enter ")
                .append(title)
                .append(" number from ")
                .append(pageNum * MAX_TABLE_SIZE)
                .append(" to ")
                .append(Math.min(dataSize, MAX_TABLE_SIZE * (pageNum + 1)) - 1)
                .append(" or ");
        if (pageNum < pageSize) {
            builder.append(helper.underline("N"))
                    .append("ext to see more options, ");
        }
        if (pageNum > 0) {
            builder.append(helper.underline("P"))
                    .append("rev to see previous options, ");
        }
        builder.append(helper.underline("C"))
                .append("ancel to exit");
        return builder.toString();
    }

    private <T> int printTable(List<T> data, Function<T, String> nameExtractor, String name,
                               int pageNum) {
        ArrayList<Object[]> table = new ArrayList<>();
        table.add(new Object[]{"Number", name});

        List<Object[]> tableData = IntStream
                .range(pageNum * MAX_TABLE_SIZE, Math.min(data.size(), MAX_TABLE_SIZE * (pageNum + 1)))
                .mapToObj(i -> new Object[]{i, nameExtractor.apply(data.get(i))})
                .collect(Collectors.toList());

        table.addAll(tableData);
        return helper.printTable(table.toArray(new Object[0][0]));
    }

    private boolean validateInputDirectory(File file) {
        if (!file.exists()) {
            helper.printlnWarning("Specified path is not correct");
            return false;
        }
        if (file.isFile()) {
            helper.printlnWarning("Specified path is not a directory");
            return false;
        }
        return true;
    }

    private static String getFileNameWitOutExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf('.'));
    }
}
