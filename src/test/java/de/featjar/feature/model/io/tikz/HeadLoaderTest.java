package de.featjar.feature.model.io.tikz;

import de.featjar.feature.model.io.tikz.format.TikzHeadFormat;
import org.junit.jupiter.api.Test;

import java.util.Collections;

/**
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class HeadLoaderTest {

    @Test
    public void loadHead() {
        StringBuilder stringBuilder = new StringBuilder();

        TikzHeadFormat.header(stringBuilder, Collections.emptyList(), false);

        System.out.print(stringBuilder);
    }

}
