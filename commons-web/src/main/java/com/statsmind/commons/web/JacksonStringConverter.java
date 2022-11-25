package com.statsmind.commons.web;

import com.fasterxml.jackson.databind.util.StdConverter;
import org.apache.commons.lang3.StringUtils;

public class JacksonStringConverter {
    public static class Trim extends StdConverter<String, String> {

        @Override
        public String convert(String s) {
            return s == null ? null : StringUtils.trimToNull(s);
        }
    }

    public static class TrimUpperCase extends StdConverter<String, String> {

        @Override
        public String convert(String s) {
            return s == null ? null : StringUtils.trimToNull(s.toUpperCase());
        }
    }

    public static class TrimLowerCase extends StdConverter<String, String> {

        @Override
        public String convert(String s) {
            return s == null ? null : StringUtils.trimToNull(s.toLowerCase());
        }
    }

    public static class TrimToLine extends StdConverter<String, String> {

        @Override
        public String convert(String s) {
            if (s == null) {
                return null;
            }

            s = s.replaceAll("[\r\n]+", "");
            return StringUtils.trimToNull(s);
        }
    }
}
