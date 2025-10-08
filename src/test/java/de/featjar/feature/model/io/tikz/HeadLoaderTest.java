package de.featjar.feature.model.io.tikz;

import org.junit.jupiter.api.Test;
import java.io.*;

public class HeadLoaderTest {

    @Test
    public void loadHead() {
        StringBuilder stringBuilder = new StringBuilder();
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("head.tex");

        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"))) {
            String line;
            while (((line = bufferedReader.readLine()) != null)) {
                stringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.print(stringBuilder.toString());
    }

}
