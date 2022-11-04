package ua.kh.excel.comparison.tool.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.math.NumberUtils;
import ua.kh.excel.comparison.tool.ConsoleHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@UtilityClass
public class CommandLineUtils {

    private static final int MAX_TABLE_SIZE = 10;

    public static String buildCommandLine(ConsoleHelper consoleHelper, int dataSize, String title, int pageNum) {
        int pageSize = (dataSize - 1) / MAX_TABLE_SIZE;
        StringBuilder builder = new StringBuilder();
        builder.append("Enter ")
                .append(title)
                .append(" number from ")
                .append(pageNum * MAX_TABLE_SIZE)
                .append(" to ")
                .append(Math.min(dataSize, MAX_TABLE_SIZE * (pageNum + 1)) - 1)
                .append(" or ");
        if (pageNum < pageSize) {
            builder.append(consoleHelper.underline("N"))
                    .append("ext to see more options, ");
        }
        if (pageNum > 0) {
            builder.append(consoleHelper.underline("P"))
                    .append("rev to see previous options, ");
        }
        builder.append(consoleHelper.underline("C"))
                .append("ancel to exit");
        return builder.toString();
    }

    private static <T> int printConsoleTable(ConsoleHelper helper, List<T> data, Function<T, String> nameExtractor, String name,
                                             int pageNum) {
        ArrayList<Object[]> table = new ArrayList<>();
        table.add(new Object[]{"Number", name});

        List<Object[]> tableData = IntStream
                .range(pageNum * MAX_TABLE_SIZE, Math.min(data.size(), MAX_TABLE_SIZE * (pageNum + 1)))
                .mapToObj(i -> new Object[]{i, nameExtractor.apply(data.get(i))})
                .collect(Collectors.toList());

        table.addAll(tableData);
        return helper.printTable(table.toArray(new Object[0][0]));
    }

    public static <T> Optional<T> selectElement(ConsoleHelper helper, List<T> data, Function<T, String> nameExtractor,
                                                String title, String invitation) {
        int currentPage = 0;
        int printedRowCounter;
        printedRowCounter = printConsoleTable(helper, data, nameExtractor, title, currentPage);
        helper.spacer();
        helper.println(invitation);
        printedRowCounter += 2;
        T element = null;
        do {
            String input = helper.prompt(CommandLineUtils.buildCommandLine(helper, data.size(), title, currentPage), null);
            printedRowCounter++;
            if ("c".equalsIgnoreCase(input) || "cancel".equalsIgnoreCase(input)) {
                helper.removePrintedRows(printedRowCounter);
                return Optional.empty();
            }
            if ("n".equalsIgnoreCase(input) || "next".equalsIgnoreCase(input)) {
                if (currentPage < data.size() / MAX_TABLE_SIZE) {
                    currentPage++;
                    helper.removePrintedRows(printedRowCounter);
                    printedRowCounter = printConsoleTable(helper, data, nameExtractor, title, currentPage);
                } else {
                    helper.printlnWarning(String.format("%s wrong input", input));
                    printedRowCounter++;
                }
                continue;
            }
            if ("p".equalsIgnoreCase(input) || "prev".equalsIgnoreCase(input)) {
                if (currentPage > 0) {
                    currentPage--;
                    helper.removePrintedRows(printedRowCounter);
                    printedRowCounter = printConsoleTable(helper, data, nameExtractor, title, currentPage);
                } else {
                    helper.printlnWarning(String.format("%s wrong input", input));
                    printedRowCounter++;
                }
                continue;
            }
            if (!NumberUtils.isParsable(input)) {
                helper.printlnWarning(String.format("%s wrong number", input));
                printedRowCounter++;
                continue;
            }
            int index = Integer.parseInt(input);
            if (index < currentPage * MAX_TABLE_SIZE ||
                    index > Math.min(currentPage * MAX_TABLE_SIZE + MAX_TABLE_SIZE, data.size()) - 1) {
                helper.printlnWarning(String.format("There is no %s with specified number", title));
                printedRowCounter++;
                continue;
            }
            element = data.get(index);

        } while (Objects.isNull(element));
        helper.removePrintedRows(printedRowCounter);
        return Optional.of(element);
    }

}
