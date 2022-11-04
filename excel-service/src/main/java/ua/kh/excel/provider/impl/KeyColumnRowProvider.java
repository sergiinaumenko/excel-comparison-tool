package ua.kh.excel.provider.impl;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import ua.kh.excel.provider.CellProvider;
import ua.kh.excel.provider.CellProviderFactory;
import ua.kh.excel.provider.RowProvider;
import ua.kh.excel.utils.ExcelUtils;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

public class KeyColumnRowProvider implements RowProvider {

    private final Sheet sheet1;
    private final Sheet sheet2;
    private final CellProviderFactory cellProvider;
    private final List<Pair<Integer, Integer>> rowSequence;

    public KeyColumnRowProvider(Sheet sheet1, int keyColumnNumber1,
                                Sheet sheet2, int keyColumnNumber2, CellProviderFactory cellProvider) {
        this.sheet1 = sheet1;
        this.sheet2 = sheet2;
        this.cellProvider = cellProvider;
        this.rowSequence = getRowSequenceByKeyColumns(
                sheet1, keyColumnNumber1, sheet2, keyColumnNumber2);
    }

    @Override
    public List<String> getHeader() {
        return cellProvider.getHeader();
    }

    @Override
    public int size() {
        return rowSequence.size();
    }

    private List<Pair<Integer, Integer>> getRowSequenceByKeyColumns(
            Sheet sheet1, int keyColumnNumber1, Sheet sheet2, int keyColumnNumber2) {
        Map<String, List<Integer>> rowNumberByKey1 =
                groupByKeyColumn(sheet1, keyColumnNumber1, cellProvider.withHeader());
        Map<String, List<Integer>> rowNumberByKey2 =
                groupByKeyColumn(sheet2, keyColumnNumber2, cellProvider.withHeader());
        return ExcelUtils.joinData(rowNumberByKey1, rowNumberByKey2);
    }

    private static Map<String, List<Integer>> groupByKeyColumn(Sheet sheet, int keyColumnNumber,
                                                               boolean skipFirst) {
        return StreamSupport
                .stream(spliteratorUnknownSize(sheet.iterator(), Spliterator.ORDERED), false)
                .skip(skipFirst ? 1 : 0)
                .collect(groupingBy(
                        row -> ExcelUtils.getCellValueAsString(row.getCell(keyColumnNumber)),
                        mapping(Row::getRowNum, toList())
                ));
    }

    @Override
    public Iterator<CellProvider> iterator() {
        return new Iterator<>() {
            private final Iterator<Pair<Integer, Integer>> sequenceIterator =
                    rowSequence.iterator();

            @Override
            public boolean hasNext() {
                return sequenceIterator.hasNext();
            }

            @Override
            public CellProvider next() {
                return cellProvider.createProvider(getRowsByNumber(sequenceIterator.next()));
            }
        };
    }

    private Pair<Row, Row> getRowsByNumber(Pair<Integer, Integer> rowsNumber) {
        Row row1 = rowsNumber.getFirst() != null ? sheet1.getRow(rowsNumber.getFirst()) : null;
        Row row2 = rowsNumber.getSecond() != null ? sheet2.getRow(rowsNumber.getSecond()) : null;
        return Pair.create(row1, row2);
    }
}
