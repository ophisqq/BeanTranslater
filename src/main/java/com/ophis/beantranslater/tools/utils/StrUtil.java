package com.ophis.beantranslater.tools.utils;

public class StrUtil {

    public static boolean isBlank(String str) {
        return str==null||str.trim().equals("");
    }

    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
