package ua.kh.excel.comparison.desktop.exception;

public class ValidationException extends RuntimeException {

    public ValidationException() {
        super();
    }

    public ValidationException(Exception ex) {
        super(ex);
    }

    public ValidationException(String message) {
        super(message);
    }

}
