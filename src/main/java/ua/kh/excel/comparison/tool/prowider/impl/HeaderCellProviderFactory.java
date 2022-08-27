package ua.kh.excel.comparison.tool.prowider.impl;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import ua.kh.excel.comparison.tool.prowider.CellProvider;
import ua.kh.excel.comparison.tool.prowider.CellProviderFactory;
import ua.kh.excel.comparison.tool.prowider.RowTitleProvider;
import ua.kh.excel.comparison.tool.utils.ExcelUtils;

import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.*;

public class HeaderCellProviderFactory implements CellProviderFactory {

    private final List<Pair<Integer, Integer>> cellSequence;
    private final Pair<Row, Row> header;
    private final RowTitleProvider rowTitleProvider;

    public HeaderCellProviderFactory(Pair<Row, Row> header, RowTitleProvider rowTitleProvider) {
        this.header = header;
        this.rowTitleProvider = rowTitleProvider;
        this.cellSequence = getCellSequence(header);
    }

    private List<Pair<Integer, Integer>> getCellSequence(Pair<Row, Row> header) {
        Map<String, List<Integer>> cellNumberByHeader1 = groupByHeader(header.getFirst());
        Map<String, List<Integer>> cellNumberByHeader2 = groupByHeader(header.getSecond());
        return ExcelUtils.joinData(cellNumberByHeader1, cellNumberByHeader2);
    }

    private Map<String, List<Integer>> groupByHeader(Row header) {
        return StreamSupport
                .stream(spliteratorUnknownSize(header.cellIterator(), Spliterator.ORDERED), false)
                .collect(groupingBy(ExcelUtils::getCellValueAsString,
                        mapping(Cell::getColumnIndex, toList())));
    }

    @Override
    public CellProvider createProvider(Pair<Row, Row> rows) {
        return new HeaderCellProvider(rows.getFirst(), rows.getSecond());
    }

    @Override
    public boolean withHeader() {
        return true;
    }

    @Override
    public List<String> getHeader() {
        return cellSequence.stream()
                .map(this::getColumnHeader)
                .collect(toList());
    }

    private String getColumnHeader(Pair<Integer, Integer> cellIndex) {
        if (Objects.nonNull(cellIndex.getFirst())) {
            return ExcelUtils.getCellValueAsString(header.getFirst().getCell(cellIndex.getFirst()));
        }
        if (Objects.nonNull(cellIndex.getSecond())) {
            return ExcelUtils
                    .getCellValueAsString(header.getSecond().getCell(cellIndex.getSecond()));
        }
        return "";
    }


    private class HeaderCellProvider implements CellProvider {

        private final Row row1;
        private final Row row2;

        HeaderCellProvider(Row row1, Row row2) {
            this.row1 = row1;
            this.row2 = row2;
        }

        @Override
        public Pair<Cell, Cell> getCell(int cellNum) {
            Pair<Integer, Integer> pair = cellSequence.get(cellNum);
            return Pair.create(getCell(row1, pair.getFirst()), getCell(row2, pair.getSecond()));
        }

        private Cell getCell(Row row, Integer index) {
            if (Objects.nonNull(index) && Objects.nonNull(row)) {
                return row.getCell(index);
            }
            return null;
        }

        @Override
        public Pair<String, String> getRowTitle() {
            return rowTitleProvider.getTitle(row1, row2);
        }

        @Override
        public int size() {
            return cellSequence.size();
        }

        @Override
        public Iterator<Pair<Cell, Cell>> iterator() {
            return new Iterator<>() {
                private Iterator<Pair<Integer, Integer>> iterator = cellSequence.iterator();

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Pair<Cell, Cell> next() {
                    Pair<Integer, Integer> pair = iterator.next();
                    return Pair
                            .create(getCell(row1, pair.getFirst()), getCell(row2, pair.getSecond()));
                }
            };
        }
    }
}
