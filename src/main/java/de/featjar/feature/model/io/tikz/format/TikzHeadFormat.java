package de.featjar.feature.model.io.tikz.format;

import java.io.*;
import java.util.Objects;

public class TikzHeadFormat {

    public static void header(StringBuilder stringBuilder) {
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(TikzHeadFormat.class
                                .getClassLoader()
                                .getResourceAsStream("head.tlx"))))) {

            bufferedReader.lines()
                    .forEach(line -> stringBuilder.append(line).append("\n"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
