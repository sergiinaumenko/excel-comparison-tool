package ua.kh.excel.provider.impl;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Row;
import ua.kh.excel.provider.RowTitleProvider;

import java.util.Objects;

public class FileNameRowTitleProvider implements RowTitleProvider {

    private final String fileName1;
    private final String fileName2;

    public FileNameRowTitleProvider(String fileName1, String fileName2) {
        this.fileName1 = fileName1;
        this.fileName2 = fileName2;
    }

    @Override
    public Pair<String, String> getTitle(Row row1, Row row2) {
        String title1 = Objects.nonNull(row1) ? fileName1 + ":" + row1.getRowNum() : null;
        String title2 = Objects.nonNull(row2) ? fileName2 + ":" + row2.getRowNum() : null;
        return Pair.create(title1, title2);
    }
}
