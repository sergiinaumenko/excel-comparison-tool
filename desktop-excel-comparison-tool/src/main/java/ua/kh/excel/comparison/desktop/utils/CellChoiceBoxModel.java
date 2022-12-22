package ua.kh.excel.comparison.desktop.utils;

import lombok.Builder;
import lombok.Data;
import org.apache.poi.ss.usermodel.Cell;

@Data
@Builder
public class CellChoiceBoxModel {

    private Integer cellIndex;

    private String cellValue;

    private Cell cell;

    private boolean isDefaultValue;

}
