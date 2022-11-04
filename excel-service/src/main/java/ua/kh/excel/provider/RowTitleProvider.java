package ua.kh.excel.provider;

import org.apache.commons.math3.util.Pair;
import org.apache.poi.ss.usermodel.Row;

public interface RowTitleProvider {

    Pair<String, String> getTitle(Row row1, Row row2);

}
