package ua.kh.excel.comparison.desktop.task;

import javafx.concurrent.Task;
import org.apache.poi.ss.usermodel.Workbook;
import ua.kh.excel.utils.ExcelUtils;

import java.io.File;

public class ReadDocumentTask extends Task<Workbook> {

    private final static int DEFAULT_WAIT_TIME = 100;

    private final File spreadsheet;

    public ReadDocumentTask(File spreadsheet) {
        this.spreadsheet = spreadsheet;
    }

    @Override
    protected Workbook call() throws Exception {
        Thread.sleep(DEFAULT_WAIT_TIME);
        return ExcelUtils.determineExcelWorkBookRepresentation(spreadsheet);
    }

}
