package de.featjar.feature.model.io.tikz.format;

public interface IGraphicalFormat {

    void write();

    boolean supportWirte();

    boolean supportRead();

    String getSuffix();

    String getName();

    String getId();

}
