package ua.kh.excel.comparison.tool.prowider.impl;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import ua.kh.excel.comparison.tool.prowider.CellProvider;
import ua.kh.excel.comparison.tool.prowider.CellProviderFactory;
import ua.kh.excel.comparison.tool.prowider.RowTitleProvider;

import java.util.*;

public class SequentCellProviderFactory implements CellProviderFactory {

    private final RowTitleProvider rowTitleProvider;

    public SequentCellProviderFactory(RowTitleProvider rowTitleProvider) {
        this.rowTitleProvider = rowTitleProvider;
    }

    @Override
    public CellProvider createProvider(Pair<Row, Row> rows) {
        return new SequentCellProvider(rows.getFirst(), rows.getSecond());
    }

    @Override
    public boolean withHeader() {
        return false;
    }

    @Override
    public List<String> getHeader() {
        return Collections.emptyList();
    }

    private class SequentCellProvider implements CellProvider {

        private final Row row1;
        private final Row row2;

        SequentCellProvider(Row row1, Row row2) {
            this.row1 = row1;
            this.row2 = row2;
        }

        @Override
        public Pair<Cell, Cell> getCell(int cellIndex) {
            Cell cell1 = Objects.nonNull(row1) ? row1.getCell(cellIndex) : null;
            Cell cell2 = Objects.nonNull(row2) ? row2.getCell(cellIndex) : null;
            return Pair.create(cell1, cell2);
        }

        @Override
        public Pair<String, String> getRowTitle() {
            return rowTitleProvider.getTitle(row1, row2);
        }

        @Override
        public int size() {
            int size1 = Objects.nonNull(row1) ? row1.getLastCellNum() : 0;
            int size2 = Objects.nonNull(row2) ? row2.getLastCellNum() : 0;
            return Math.max(size1, size2);
        }

        @Override
        public Iterator<Pair<Cell, Cell>> iterator() {
            return new Iterator<>() {
                private final int size = size();
                private int currentIndex = 0;

                @Override
                public boolean hasNext() {
                    return currentIndex <= size;
                }

                @Override
                public Pair<Cell, Cell> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    Cell cell1 = Objects.nonNull(row1) ? row1.getCell(currentIndex) : null;
                    Cell cell2 = Objects.nonNull(row2) ? row2.getCell(currentIndex) : null;
                    currentIndex++;
                    return Pair.create(cell1, cell2);
                }
            };
        }
    }
}
