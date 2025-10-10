package de.featjar.feature.model.io.tikz.helper;

/**
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class MatrixHelper {

    private final static String NODE = "		\\node {replace}; \\\\" + System.lineSeparator();
    private final static String DRAW = "			\\draw[drawColor] {replace};" + System.lineSeparator();
    private final static String FILL_DRAW = " \\filldraw[drawColor] {replace}; " + System.lineSeparator();
    private final static String FILL = "			\\fill[drawColor] {replace};" + System.lineSeparator();

    private final StringBuilder stringBuilder;
    private final String header ;

    public MatrixHelper(MatrixType matrixType) {
        this.stringBuilder = new StringBuilder();
        this.header = matrixType.getHeader();
        writeHeader();
    }

    private void writeHeader() {
        stringBuilder.append(header);
    }

    private void writeFooter() {
        stringBuilder.append("	};")
                .append(System.lineSeparator());
    }

    public MatrixHelper writeNode(String value) {
        stringBuilder.append(NODE.replace("{replace}", value));
        return this;
    }

    public MatrixHelper writeDraw(String value) {
        stringBuilder.append(DRAW.replace("{replace}", value));
        return this;
    }

    public MatrixHelper writeFillDraw(String value) {
        stringBuilder.append(FILL_DRAW.replace("{replace}", value));
        return this;
    }

    public MatrixHelper writeFill(String value) {
        stringBuilder.append(FILL.replace("{replace}", value));
        return this;
    }

    public StringBuilder build() {
        writeFooter();
        return stringBuilder;
    }

}
