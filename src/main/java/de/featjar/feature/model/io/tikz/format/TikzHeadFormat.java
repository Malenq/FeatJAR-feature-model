package de.featjar.feature.model.io.tikz.format;

import de.featjar.base.data.Problem;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TikzHeadFormat {

    public static void header(StringBuilder stringBuilder, List<Problem> problemList, boolean hasVerticalLayout) {
        String replacement = String.format( //
                "			parent anchor = %s," + System.lineSeparator() //
                        + "			child anchor = %s," + System.lineSeparator() //
                        + "%s" //
                        + "			l sep = 2em," + System.lineSeparator() //
                        + "			s sep = 1em," //
                        + "%s", //
                hasVerticalLayout ? "east" : "south", //
                hasVerticalLayout ? "west" : "north", //
                hasVerticalLayout ? "			grow' = east," + System.lineSeparator() : "", //
                hasVerticalLayout ? "			tier/.pgfmath=level()," : "");

        InputStream inputStream = TikzHeadFormat.class.getClassLoader().getResourceAsStream("head.tex");

        if (inputStream == null) {
            problemList.add(new Problem("InputStream in header is null / not found"));
            return;
        }

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while (((line = bufferedReader.readLine()) != null)) {
                if (line.contains("{replaceWithVerticalSetting}")) {
                    line = replacement;
                }
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
