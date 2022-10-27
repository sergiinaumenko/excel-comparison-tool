package ua.kh.excel.comparison.tool.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ua.kh.excel.comparison.tool.prowider.CellProvider;
import ua.kh.excel.comparison.tool.prowider.RowProvider;
import ua.kh.excel.comparison.tool.utils.ExcelUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ExcelService {

    public CompareReport compareExcelSheets(RowProvider rowProvider) {
        XSSFWorkbook reportWorkbook = new XSSFWorkbook();
        XSSFSheet reportSheet = reportWorkbook.createSheet("Report");
        CellStyle cellStyle = createMarkedCellStyle(reportWorkbook);
        List<String> header = rowProvider.getHeader();
        int rowsCounter = 0;
        int cellCounter = 0;
        if (!header.isEmpty()) {
            addHeader(reportSheet, header, createHeaderCellStyle(reportWorkbook));
        }
        for (CellProvider cellProvider : rowProvider) {
            Set<Integer> cellIndexes = compareRows(cellProvider);
            if (!cellIndexes.isEmpty()) {
                addNewRowsToReport(reportSheet, cellProvider, cellIndexes, cellStyle);
                rowsCounter++;
                cellCounter = cellCounter + cellIndexes.size();
            }
        }
        return CompareReport.builder()
                .report(reportWorkbook)
                .numberOfRowsWithDifferences(rowsCounter)
                .numberOfCellWithDifferences(cellCounter)
                .build();
    }

    private void addHeader(XSSFSheet reportSheet, List<String> header, CellStyle headerStyle) {
        XSSFRow headerRow = reportSheet.createRow(0);
        reportSheet.createFreezePane(0, 1);
        XSSFCell cell = headerRow.createCell(0);
        cell.setCellStyle(headerStyle);
        for (int i = 0; i < header.size(); i++) {
            cell = headerRow.createCell(i + 1);
            cell.setCellValue(header.get(i));
            cell.setCellStyle(headerStyle);
        }
    }

    private void addNewRowsToReport(XSSFSheet reportSheet, CellProvider cellProvider,
                                    Set<Integer> cellIndexes, CellStyle cellStyle) {
        int lastRowNumber = reportSheet.getLastRowNum() + 1;
        XSSFRow row1 = reportSheet.createRow(lastRowNumber);
        XSSFRow row2 = reportSheet.createRow(lastRowNumber + 1);
        reportSheet.createRow(lastRowNumber + 2); //spacer
        Pair<String, String> rowTitle = cellProvider.getRowTitle();
        int cellIndex = 0;
        row1.createCell(cellIndex).setCellValue(rowTitle.getFirst());
        row2.createCell(cellIndex).setCellValue(rowTitle.getSecond());
        cellIndex++;
        for (Pair<Cell, Cell> cells : cellProvider) {
            String value1 = ExcelUtils.getCellValueAsString(cells.getFirst());
            boolean isCellValueDifferent = cellIndexes.contains(cellIndex - 1);
            if (StringUtils.isNotBlank(value1) || isCellValueDifferent) {
                XSSFCell cell1 = row1.createCell(cellIndex);
                cell1.setCellValue(value1);
                if (isCellValueDifferent) {
                    cell1.setCellStyle(cellStyle);
                }
            }
            String value2 = ExcelUtils.getCellValueAsString(cells.getSecond());
            if (StringUtils.isNotBlank(value2) || isCellValueDifferent) {
                XSSFCell cell2 = row2.createCell(cellIndex);
                cell2.setCellValue(value2);
                if (isCellValueDifferent) {
                    cell2.setCellStyle(cellStyle);
                }
            }
            cellIndex++;
        }
    }

    private CellStyle createMarkedCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(IndexedColors.LIGHT_ORANGE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createHeaderCellStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setTopBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setLeftBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setRightBorderColor(IndexedColors.BLACK.getIndex());
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        return headerStyle;
    }

    private Set<Integer> compareRows(CellProvider cellProvider) {
        return IntStream.range(0, cellProvider.size())
                .filter(i -> isCellValueNotEqual(cellProvider.getCell(i)))
                .boxed()
                .collect(Collectors.toSet());
    }

    private boolean isCellValueNotEqual(Pair<Cell, Cell> cells) {
        return !Objects.equals(
                ExcelUtils.getCellValueAsString(cells.getFirst()),
                ExcelUtils.getCellValueAsString(cells.getSecond()));
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CompareReport {

        private final Workbook report;
        private final int numberOfRowsWithDifferences;
        private final int numberOfCellWithDifferences;

    }
}
