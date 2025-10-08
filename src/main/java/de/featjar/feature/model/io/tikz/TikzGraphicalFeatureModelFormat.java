package de.featjar.feature.model.io.tikz;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.base.io.format.ParseException;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.io.tikz.format.TikzHeadFormat;
import de.featjar.feature.model.io.tikz.format.TikzMainFormat;

import java.util.ArrayList;
import java.util.List;

public class TikzGraphicalFeatureModelFormat implements IFormat<IFeatureModel> {

    public static String LINE_SEPERATOR  = System.lineSeparator();

    private final boolean[] legend = new boolean[7];

    private final StringBuilder stringBuilder;
    private final IFeatureModel featureModel;
    private final List<Problem> problemList;

    public TikzGraphicalFeatureModelFormat(IFeatureModel featureModel) {
        this.stringBuilder = new StringBuilder();
        this.featureModel = featureModel;
        this.problemList = new ArrayList<>();
    }

    public Result<String> serialize() {
        stringBuilder.append("\\documentclass[border=5pt]{standalone}");
        stringBuilder.append(LINE_SEPERATOR);
        TikzHeadFormat.header(stringBuilder);
        stringBuilder.append("\\begin{document}" + LINE_SEPERATOR + "	%---The Feature Diagram-----------------------------------------------------" + LINE_SEPERATOR);
        new TikzMainFormat(featureModel, stringBuilder, problemList).printForest();
        stringBuilder.append(LINE_SEPERATOR);
        stringBuilder.append("\t%---------------------------------------------------------------------------" + LINE_SEPERATOR + "\\end{document}");
        return Result.of(stringBuilder.toString());
    }

    public IFeatureModel getFeatureModel() {
        return featureModel;
    }

    public StringBuilder getStringBuilder() {
        return stringBuilder;
    }

    @Override
    public String getFileExtension() {
        return ".tex";
    }

    @Override
    public String getName() {
        return "LaTeX-Document with TikZ";
    }
}
