package de.featjar.feature.model.io.tikz.helper;

/**
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public enum TikzMatrixType {
    LEGEND("	\\matrix [anchor=north west] at (current bounding box.north east) {" + System.lineSeparator()
            + "		\\node [placeholder] {}; \\\\" + System.lineSeparator()
            + "	};" + System.lineSeparator()
            + "	\\matrix [draw=drawColor,anchor=north west] at (current bounding box.north east) {"
            + System.lineSeparator()
            + "		\\node [label=center:\\underline{Legend:}] {}; \\\\" + System.lineSeparator()),
    CONSTRAINS(""),
    ATTRIBUTES("");

    final String header;

    TikzMatrixType(String header) {
        this.header = header;
    }

    public String getHeader() {
        return header;
    }
}
