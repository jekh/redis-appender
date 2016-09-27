package org.jekh.appenders.mdc;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdcUtil {
    public static final Pattern MDC_PATTERN = Pattern.compile("@\\{([^}:]+)(?::-([^}]+))?}");
    public static final int MDC_KEY_NAME_GROUP = 1;
    public static final int DEFAULT_VALUE_GROUP = 2;

    public static String resolveSubstitutions(String literalValue, Map<String, String> mdc) {
        if (mdc.isEmpty() || !literalValue.contains("@{")) {
            return literalValue;
        }

        // allocating "raw" String * 2 for the StringBuffer, on the assumption that substituted strings will often be larger
        // than the "raw" String, and that it's better to allocate some extra memory up front than to resize the buffer.
        // these assumptions may not be true, and this microoptimization may be ill-advised.
        StringBuffer sb = new StringBuffer(literalValue.length() * 2);

        Matcher matcher = MDC_PATTERN.matcher(literalValue);
        while (matcher.find()) {
            String variableName = matcher.group(MDC_KEY_NAME_GROUP);
            String substitution = mdc.get(variableName);
            if (substitution == null) {
                String defaultValue = matcher.group(DEFAULT_VALUE_GROUP);
                if (defaultValue == null) {
                    substitution = "@{" + variableName + '}';
                } else {
                    substitution = defaultValue;
                }
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(substitution));
        }

        matcher.appendTail(sb);

        return sb.toString();

    }
}
