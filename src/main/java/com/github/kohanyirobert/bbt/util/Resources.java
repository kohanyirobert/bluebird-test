package com.github.kohanyirobert.bbt.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// A classpath-on lévő fájlok beolvasását egyszerűsíti.
public final class Resources {

    public static List<String> getResourceAsLines(String name) {
        return getResourceAsLines(Resources.class, name);
    }

    public static List<String> getResourceAsLines(Class<?> clazz, String name) {
        List<String> lines = new ArrayList<>();
        try (InputStream is = clazz.getResourceAsStream(name);
                Scanner scanner = new Scanner(is)) {
            while (scanner.hasNext()) {
                lines.add(scanner.nextLine());
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        return lines;
    }

    public static String getResourceAsString(String name) {
        return getResourceAsString(Resources.class, name);
    }

    public static String getResourceAsString(Class<?> clazz, String name) {
        try (InputStream is = clazz.getResourceAsStream(name);
                Scanner scanner = new Scanner(is);) {
            return scanner.useDelimiter("^").next();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Resources() {
    }
}
