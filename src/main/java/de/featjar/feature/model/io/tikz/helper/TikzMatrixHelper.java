package de.featjar.feature.model.io.tikz.helper;

/**
 * This class helps to build a matrix in latex. Choose a header from MatrixStyle and write your nodes, draw or whatever.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class TikzMatrixHelper {

    private static final String NODE = "		\\node {replace}; \\\\" + System.lineSeparator();
    private static final String DRAW = "			\\draw[drawColor] {replace};" + System.lineSeparator();
    private static final String FILL_DRAW = " \\filldraw[drawColor] {replace}; " + System.lineSeparator();
    private static final String FILL = "			\\fill[drawColor] {replace};" + System.lineSeparator();

    private final StringBuilder stringBuilder;
    private final String header;

    public TikzMatrixHelper(TikzMatrixType tikzMatrixType) {
        this.stringBuilder = new StringBuilder();
        this.header = tikzMatrixType.getHeader();
        writeHeader();
    }

    private void writeHeader() {
        stringBuilder.append(header);
    }

    private void writeFooter() {
        stringBuilder.append("	};").append(System.lineSeparator());
    }

    public TikzMatrixHelper writeNode(String value) {
        stringBuilder.append(NODE.replace("{replace}", value));
        return this;
    }

    public TikzMatrixHelper writeDraw(String value) {
        stringBuilder.append(DRAW.replace("{replace}", value));
        return this;
    }

    public TikzMatrixHelper writeFillDraw(String value) {
        stringBuilder.append(FILL_DRAW.replace("{replace}", value));
        return this;
    }

    public TikzMatrixHelper writeFill(String value) {
        stringBuilder.append(FILL.replace("{replace}", value));
        return this;
    }

    public StringBuilder build() {
        writeFooter();
        return stringBuilder;
    }
}
