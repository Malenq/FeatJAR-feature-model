package de.featjar.feature.model.io.tikz.format;

public class TikzBodyFormat implements IGraphicalFormat{

    private final StringBuilder stringBuilder;
    private final String fileName;

    public TikzBodyFormat(String fileName, StringBuilder stringBuilder) {
        this.fileName = fileName;
        this.stringBuilder = (stringBuilder == null) ? new StringBuilder() : stringBuilder;
    }

    @Override
    public void write() {
        stringBuilder.append("\\documentclass[border=5pt]{standalone}")
                .append(System.lineSeparator());
        stringBuilder.append("\\input{head.tex}")
                .append(System.lineSeparator()); // Include head
        stringBuilder.append("\\begin{document}")
                .append(System.lineSeparator())
                .append("	");
        stringBuilder.append("\\sffamily")
                .append(System.lineSeparator());
        stringBuilder.append("	\\input{")
                .append(fileName)
                .append("}")
                .append(System.lineSeparator()); // Include main
        stringBuilder.append("\\end{document}");
    }

    @Override
    public boolean supportWirte() {
        return true;
    }

    @Override
    public boolean supportRead() {
        return false;
    }

    @Override
    public String getSuffix() {
        return ".tex";
    }

    @Override
    public String getName() {
        return "LaTeX-Document with TikZ";
    }

    @Override
    public String getId() {
        return "";
    }
}
