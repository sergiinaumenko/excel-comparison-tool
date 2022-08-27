package ua.kh.excel.comparison.tool.prowider.impl;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Sheet;
import ua.kh.excel.comparison.tool.prowider.CellProvider;
import ua.kh.excel.comparison.tool.prowider.CellProviderFactory;
import ua.kh.excel.comparison.tool.prowider.RowProvider;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

public class SequentRowProvider implements RowProvider {

    private final Sheet sheet1;
    private final Sheet sheet2;
    private final CellProviderFactory cellProviderFactory;

    public SequentRowProvider(Sheet sheet1, Sheet sheet2,
                              CellProviderFactory cellProviderFactory) {
        this.sheet1 = sheet1;
        this.sheet2 = sheet2;
        this.cellProviderFactory = cellProviderFactory;
    }

    @Override
    public List<String> getHeader() {
        return cellProviderFactory.getHeader();
    }

    @Override
    public int size() {
        return Math.max(sheet1.getLastRowNum(), sheet2.getLastRowNum());
    }

    @Override
    public Iterator<CellProvider> iterator() {
        return new Iterator<>() {
            private int currentIndex = cellProviderFactory.withHeader() ? 1 : 0;
            private final int size = size();

            @Override
            public boolean hasNext() {
                return currentIndex <= size;
            }

            @Override
            public CellProvider next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                CellProvider provider = cellProviderFactory.createProvider(
                        Pair.create(sheet1.getRow(currentIndex), sheet2.getRow(currentIndex)));
                currentIndex++;
                return provider;
            }
        };
    }
}
