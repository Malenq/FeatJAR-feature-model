package de.featjar.feature.model.io.tikz.format;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.feature.model.io.tikz.helper.PrintVisitor;
import de.featjar.feature.model.io.tikz.helper.TikzAttributeHelper;
import de.featjar.feature.model.io.tikz.helper.TikzMatrixHelper;
import de.featjar.feature.model.io.tikz.helper.TikzMatrixType;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.LaTexSymbols;
import java.util.List;

/**
 * This class generates the Tikz representation of a {@link IFeatureModel} including all constraints ({@link IConstraint}).
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class TikzMainFormat {

    private final IFeatureModel featureModel;
    private final IFeatureTree featureTree;
    private final StringBuilder stringBuilder;
    private final TikzAttributeHelper.FilterType filterType;
    private final List<String> filterValues;

    public TikzMainFormat(
            IFeatureModel featureModel,
            IFeatureTree featureTree,
            StringBuilder stringBuilder,
            TikzAttributeHelper.FilterType filterType,
            List<String> filterValues) {
        this.featureModel = featureModel;
        this.featureTree = featureTree;
        this.stringBuilder = stringBuilder;
        this.filterType = filterType;
        this.filterValues = filterValues;
    }

    /**
     * Build the complete tree of the FeatureModel.
     */
    public void printForest() {
        stringBuilder
                .append("\\begin{forest}")
                .append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR)
                .append("\tfeatureDiagram")
                .append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR)
                .append("\t");

        PrintVisitor printVisitor = new PrintVisitor(filterType, filterValues);
        Trees.traverse(featureTree, printVisitor);
        stringBuilder.append(printVisitor.getResult().get());

        postProcessing();
        stringBuilder.append("\t").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        if (!featureTree.getFeature().isHidden()) {
            printLegend();
        }
        if (!featureModel.getConstraints().isEmpty()) {
            printConstraints();
        }
        stringBuilder.append("\\end{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }

    /**
     * Processes a String to make special symbols LaTeX compatible.
     */
    private void postProcessing() {
        stringBuilder.replace(
                0, stringBuilder.length(), stringBuilder.toString().replace("_", "\\_"));
    }

    private void printLegend() {
        TikzMatrixHelper tikzMatrixHelper = new TikzMatrixHelper(TikzMatrixType.LEGEND);

        if (stringBuilder.indexOf(",abstract") != -1 && stringBuilder.indexOf(",concrete") != -1) {
            tikzMatrixHelper.writeNode("[abstract,label=right:Abstract Feature] {}");
            tikzMatrixHelper.writeNode("[concrete,label=right:Concrete Feature] {}");
        } else if (stringBuilder.indexOf(",abstract") != -1) {
            tikzMatrixHelper.writeNode("[abstract,label=right:Feature] {}");
        } else if (stringBuilder.indexOf(",concrete") != -1) {
            tikzMatrixHelper.writeNode("[concrete,label=right:Feature] {}");
        }

        if (stringBuilder.indexOf(",mandatory") != -1) {
            tikzMatrixHelper.writeNode("[mandatory,label=right:Mandatory] {}");
        }

        if (stringBuilder.indexOf(",optional") != -1) {
            tikzMatrixHelper.writeNode("[optional,label=right:Optional] {}");
        }

        if (stringBuilder.indexOf(",or") != -1) {
            tikzMatrixHelper
                    .writeFillDraw("(0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0)")
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeFill("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[label=right:Or Group] {}");
        }

        if (stringBuilder.indexOf(",alternative") != -1) {
            tikzMatrixHelper
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeDraw("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[label=right:Alternative Group] {}");
        }

        stringBuilder.append(tikzMatrixHelper.build());
    }

    private void printConstraints() {
        ExpressionSerializer expressionSerializer = new ExpressionSerializer();
        expressionSerializer.setEnquoteAlways(true);
        expressionSerializer.setSymbols(LaTexSymbols.INSTANCE);

        stringBuilder
                .append("	\\matrix [below=1mm of current bounding box] {")
                .append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        for (IConstraint constraint : featureModel.getConstraints()) {
            String text = constraint.getFormula().traverse(expressionSerializer).get();
            text = text.replaceAll(
                    "\"([\\w\" ]+)\"", " \\\\text\\{$1\\} "); // wrap all words in \text{} // replace with $2
            text = text.replaceAll("\\s+", " "); // remove unnecessary whitespace characters
            stringBuilder
                    .append("	\\node {\\(")
                    .append(text)
                    .append("\\)}; \\\\")
                    .append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            expressionSerializer.reset();
        }
        stringBuilder.append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }
}
