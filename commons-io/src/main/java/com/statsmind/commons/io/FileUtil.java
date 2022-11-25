package com.statsmind.commons.io;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;

public class FileUtil {
    public static void touch(String file) throws IOException {
        touch(new File(file));
    }

    public static void touch(File file) throws IOException {
        FileUtils.touch(file);
    }

    public static String getFileExtension(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
        if (StringUtils.isBlank(extension) || extension.length() > 16) {
            extension = "";
        }
        return extension;
    }

    public static String combine(String... paths) {
        String combined = StringUtils.join(paths, "/")
            .replaceAll("\\/[\\.\\/]*\\/", "/");
        return combined;
    }
//
//    public static void main(String[] args) {
//        String a = combine("/hello/", "./pp", "cccccc");
//        System.out.print(a);
//    }
}
