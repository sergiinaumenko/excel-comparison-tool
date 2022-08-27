package ua.kh.excel.comparison.tool.config;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import ua.kh.excel.comparison.tool.ConsoleHelper;
import ua.kh.excel.comparison.tool.service.ExcelService;

@Configuration
public class ExcelComparisonToolConfig {

    @Bean
    public ConsoleHelper consoleHelper(@Lazy Terminal terminal, @Lazy LineReader lineReader) {
        return new ConsoleHelper(terminal, lineReader);
    }

    @Bean
    public ExcelService excelService() {
        return new ExcelService();
    }
}
