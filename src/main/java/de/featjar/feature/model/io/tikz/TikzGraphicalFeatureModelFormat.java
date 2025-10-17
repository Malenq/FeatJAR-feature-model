package de.featjar.feature.model.io.tikz;

import de.featjar.base.data.Problem;
import de.featjar.base.data.Result;
import de.featjar.base.io.format.IFormat;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.format.TikzHeadFormat;
import de.featjar.feature.model.io.tikz.format.TikzMainFormat;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is moved from FeatureIDE to FeatJAR. The former class was written by Simon Wenk and Yang Liu.
 * We did some changes, moved in different classes, and we rewrote some code and added new functions.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class TikzGraphicalFeatureModelFormat implements IFormat<IFeatureModel> {

    public static String LINE_SEPERATOR  = System.lineSeparator();

    @Override
    public Result<String> serialize(IFeatureModel featureModel) {
        StringBuilder stringBuilder = new StringBuilder();
        List<Problem> problemList = new ArrayList<>();

        stringBuilder.append("\\documentclass[border=5pt]{standalone}").append(LINE_SEPERATOR);
        TikzHeadFormat.header(stringBuilder, problemList, false);

        stringBuilder
                .append("\\begin{document}").append(LINE_SEPERATOR)
                .append("	%---The Feature Diagram-----------------------------------------------------").append(LINE_SEPERATOR);
        for (IFeatureTree featureTree : featureModel.getRoots()) {
            new TikzMainFormat(featureModel, featureTree, stringBuilder).printForest();
        }
        stringBuilder
                .append(LINE_SEPERATOR)
                .append("\t%---------------------------------------------------------------------------").append(LINE_SEPERATOR)
                .append("\\end{document}");

        return Result.of(stringBuilder.toString(), problemList);
    }

    @Override
    public boolean supportsWrite() {
        return true;
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