package ua.kh.excel.comparison.tool.config;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.shell.jline.PromptProvider;
import ua.kh.excel.comparison.tool.ConsoleHelper;
import ua.kh.excel.service.ExcelService;

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

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedStringBuilder()
                .append("excel-comparison-tool:> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .toAttributedString();
    }

}
