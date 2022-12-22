package ua.kh.excel.comparison.desktop.exception;

public class SaveReportException extends RuntimeException {

    public SaveReportException() {
        super();
    }

    public SaveReportException(Exception ex) {
        super(ex);
    }

    public SaveReportException(String message) {
        super(message);
    }

}
