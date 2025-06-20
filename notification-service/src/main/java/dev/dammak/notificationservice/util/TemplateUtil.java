package dev.dammak.notificationservice.util;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class TemplateUtil {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{([^}]+)\\}\\}");

    public String processTemplate(String template, Map<String, Object> variables) {
        if (template == null || variables == null) {
            return template;
        }

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(template);

        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            Object value = variables.get(variable);

            if (value != null) {
                result = result.replace("{{" + variable + "}}", value.toString());
            }
        }

        return result;
    }
}
