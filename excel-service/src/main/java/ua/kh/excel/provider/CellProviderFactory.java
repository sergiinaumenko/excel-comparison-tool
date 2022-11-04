package ua.kh.excel.provider;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Row;

import java.util.List;

public interface CellProviderFactory {

    CellProvider createProvider(Pair<Row, Row> rows);

    boolean withHeader();

    List<String> getHeader();
}
