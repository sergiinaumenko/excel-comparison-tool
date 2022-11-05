package ua.kh.excel.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.FileMagic;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaError;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.NumberToTextConverter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.apache.poi.ss.usermodel.CellType.FORMULA;

@UtilityClass
public class ExcelUtils {

    private static final List<String> EXCEL_EXTENSION = List.of(".xlsx", ".xls");

    public static final String DEFAULT_REPORT_EXTENSION = ".xlsx";

    private static final DataFormatter DATA_FORMATTER = new DataFormatter();

    public Workbook determineExcelWorkBookRepresentation(File file) {
        try (FileInputStream stream = new FileInputStream(file)) {
            InputStream is = FileMagic.prepareToCheckMagic(stream);
            if (FileMagic.valueOf(is) == FileMagic.OLE2) {
                return new HSSFWorkbook(is);
            }
            return new XSSFWorkbook(is);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Unfortunately this type of file is not supported");
        }
    }

    public static String getCellValueAsString(Cell cell) {
        if (Objects.isNull(cell)) {
            return "";
        }
        CellType cellType = cell.getCellType();
        if (cellType.equals(FORMULA)) {
            cellType = cell.getCachedFormulaResultType();
        }
        switch (cellType) {
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case ERROR:
                return FormulaError.forInt(cell.getErrorCellValue()).getString();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return DATA_FORMATTER.formatCellValue(cell);
                } else {
                    return NumberToTextConverter.toText(cell.getNumericCellValue());
                }
            case STRING:
                return StringUtils.trimToEmpty(
                        cell.getRichStringCellValue().getString().replaceAll(" ", " "));
            case BLANK:
            case FORMULA:
            case _NONE:
            default:
                return "";
        }
    }

    public static List<Pair<Integer, Integer>> joinData(Map<String, List<Integer>> data1,
                                                        Map<String, List<Integer>> data2) {
        List<Pair<Integer, Integer>> sequence = new ArrayList<>(data1.size());
        for (Entry<String, List<Integer>> entry : data1.entrySet()) {
            List<Integer> dataNumber1 = entry.getValue();
            List<Integer> dataNumber2 = data2
                    .getOrDefault(entry.getKey(), Collections.emptyList());
            for (int i = 0, size = Math.max(dataNumber1.size(), dataNumber2.size());
                 i < size; i++) {
                Integer index1 = (i < dataNumber1.size()) ? dataNumber1.get(i) : null;
                Integer index2 = (i < dataNumber2.size()) ? dataNumber2.get(i) : null;
                sequence.add(new Pair<>(index1, index2));
            }
            data2.remove(entry.getKey());
        }
        List<Pair<Integer, Integer>> orphanRows = data2.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(rowIndex -> new Pair<>((Integer) null, rowIndex))
                .collect(Collectors.toList());
        sequence.addAll(orphanRows);

        sequence.sort(Comparator
                .comparing(p -> Objects.nonNull(p.getFirst()) ? p.getFirst() : p.getSecond()));
        return sequence;
    }

    public static boolean isSheetVisible(Sheet sheet) {
        Workbook workbook = sheet.getWorkbook();
        return !workbook.isSheetHidden(workbook.getSheetIndex(sheet.getSheetName()));
    }

    public static boolean isExcelDocument(File file) {
        String name = file.getName();
        int index = name.lastIndexOf('.');
        if (index > 0) {
            return EXCEL_EXTENSION.contains(name.substring(index).toLowerCase());
        }
        return false;
    }
}
