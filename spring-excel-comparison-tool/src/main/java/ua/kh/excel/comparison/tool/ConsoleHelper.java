package ua.kh.excel.comparison.tool;

import org.apache.commons.lang3.StringUtils;
import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.shell.table.ArrayTableModel;
import org.springframework.shell.table.BorderStyle;
import org.springframework.shell.table.TableBuilder;
import org.springframework.shell.table.TableModel;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ConsoleHelper {

    private static final int TERMINAL_WIDTH = 120;
    private static final String CUU = "\u001B[A";
    private static final String DL = "\u001B[1M";

    private final Terminal terminal;
    private final LineReader lineReader;

    public ConsoleHelper(Terminal terminal, LineReader lineReader) {
        this.lineReader = lineReader;
        this.terminal = terminal;
    }

    public void printlnWarning(String warningMessage) {
        this.terminal.writer().println(addColor(warningMessage, AttributedStyle.YELLOW));
        this.terminal.writer().flush();
    }

    public void printWarning(String warningMessage) {
        this.terminal.writer().print(addColor(warningMessage, AttributedStyle.YELLOW));
        this.terminal.writer().flush();
    }


    public void printlnInfo(String infoMessage) {
        this.terminal.writer().println(addColor(infoMessage, AttributedStyle.CYAN));
        this.terminal.writer().flush();
    }

    public void printlnInfo(String infoMessage, Object... args) {
        this.terminal.writer()
                .println(addColor(String.format(infoMessage, args), AttributedStyle.CYAN));
        this.terminal.writer().flush();
    }

    public void printInfo(String infoMessage) {
        this.terminal.writer().print(addColor(infoMessage, AttributedStyle.CYAN));
        this.terminal.writer().flush();
    }

    public void printlnError(String errorMessage) {
        this.terminal.writer().println(addColor(errorMessage, AttributedStyle.RED));
        this.terminal.writer().flush();
    }

    private String addColor(String message, int color) {
        return new AttributedStringBuilder()
                .append(message, AttributedStyle.DEFAULT.foreground(color))
                .toAnsi();
    }

    public void println(String message) {
        this.terminal.writer().println(message);
        this.terminal.writer().flush();
    }

    public void print(String message) {
        this.terminal.writer().print(message);
        this.terminal.writer().flush();
    }

    public void spacer() {
        this.terminal.writer().println("");
        this.terminal.writer().flush();
    }

    public int printTable(Object[][] data) {
        TableModel model = new ArrayTableModel(data);
        TableBuilder tableBuilder = new TableBuilder(model);
        tableBuilder.addFullBorder(BorderStyle.oldschool);
        String render = tableBuilder.build().render(TERMINAL_WIDTH);
        print(render);
        return (int) render.lines().count();
    }


    public String prompt(String prompt, String defaultValue) {
        String answer = lineReader.readLine(prompt + ": ");
        if (StringUtils.isBlank(answer)) {
            return defaultValue;
        }
        return StringUtils.trim(answer);
    }

    public String underline(String text) {
        return new AttributedStringBuilder()
                .append(text, AttributedStyle.DEFAULT.underline())
                .toAnsi();
    }

    public LoadingBur getLoadingBar(String title) {
        return new LoadingBur(this, title);
    }

    public void removePrintedRows(int i) {
        for (int j = 0; j < i; j++) {
            this.print(CUU + "\r" + DL);
        }
    }


    public static class LoadingBur {

        private static final long TIME_OUT = 200L;
        private static final List<String> SYMBOLS = List.of(" \\", " |", " /", " -");
        private final ConsoleHelper helper;
        private final String title;
        private final Thread barThread;

        private final AtomicBoolean loading = new AtomicBoolean(true);

        public LoadingBur(ConsoleHelper helper, String title) {
            this.helper = helper;
            this.title = title;
            this.barThread = createBarThread();
        }

        public void stop() {
            loading.set(false);
            try {
                barThread.join();
            } catch (InterruptedException ignore) {
            }
        }

        public void display() {
            this.barThread.start();
        }

        private Thread createBarThread() {
            Runnable task = () -> {
                try {
                    while (loading.get()) {
                        for (String symbol : SYMBOLS) {
                            helper.printInfo(title);
                            helper.printlnError(symbol);
                            Thread.sleep(TIME_OUT);
                            helper.print(CUU + "\r" + DL);
                        }
                    }
                } catch (InterruptedException ignore) {
                }
            };
            return new Thread(task);
        }
    }

}
