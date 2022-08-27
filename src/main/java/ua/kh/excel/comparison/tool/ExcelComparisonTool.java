package ua.kh.excel.comparison.tool;

import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.shell.jline.PromptProvider;

@SpringBootApplication
public class ExcelComparisonTool {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication
                .run(ExcelComparisonTool.class, args);
    }

    @Bean
    public PromptProvider promptProvider() {
        return () -> new AttributedStringBuilder()
                .append("excel-comparison-tool:> ", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN))
                .toAttributedString();
    }
}
