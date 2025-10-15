package de.featjar.feature.model.io.tikz.format;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.feature.model.io.tikz.helper.MatrixHelper;
import de.featjar.feature.model.io.tikz.helper.MatrixType;
import de.featjar.feature.model.io.tikz.helper.PrintVisitor;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.LaTexSymbols;

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

    public TikzMainFormat(IFeatureModel featureModel ,IFeatureTree featureTree, StringBuilder stringBuilder) {
        this.featureModel = featureModel;
        this.featureTree = featureTree;
        this.stringBuilder = stringBuilder;
    }

    /**
     * Build the complete tree of the FeatureModel.
     */
    public void printForest() {
        stringBuilder.append("\\begin{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\tfeatureDiagram").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\t");

        PrintVisitor printVisitor = new PrintVisitor();
        Trees.traverse(featureTree, printVisitor);
        stringBuilder.append(printVisitor.getResult().get());

        postProcessing();
        stringBuilder.append("\t").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        if (!featureTree.getFeature().isHidden()) {
            printLegend();
        }
        printConstraints();
        stringBuilder.append("\\end{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }

    /**
     * Processes a String to make special symbols LaTeX compatible.
     */
    private void postProcessing() {
        stringBuilder.replace(0, stringBuilder.length(), stringBuilder.toString().replace("_", "\\_"));
    }

    private void printLegend() {
        MatrixHelper matrixHelper = new MatrixHelper(MatrixType.LEGEND);

        if (stringBuilder.indexOf(",abstract") != -1 && stringBuilder.indexOf(",concrete") != -1) {
            matrixHelper.writeNode("[abstract,label=right:Abstract Feature] {}");
            matrixHelper.writeNode("[concrete,label=right:Concrete Feature] {}");
        } else if (stringBuilder.indexOf(",abstract") != -1) {
            matrixHelper.writeNode("[abstract,label=right:Feature] {}");
        } else if (stringBuilder.indexOf(",concrete") != -1) {
            matrixHelper.writeNode("[concrete,label=right:Feature] {}");
        }

        if (stringBuilder.indexOf(",mandatory") != -1) {
            matrixHelper.writeNode("[mandatory,label=right:Mandatory] {}");
        }

        if (stringBuilder.indexOf(",optional") != -1) {
            matrixHelper.writeNode("[optional,label=right:Optional] {}");
        }

        if (stringBuilder.indexOf(",or") != -1) {
            matrixHelper
                    .writeFillDraw("(0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0)")
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeFill("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[label=right:Or Group] {}");
        }

        if (stringBuilder.indexOf(",alternative") != -1) {
            matrixHelper
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeDraw("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[alternative,label=right:Alternative Group] {}");
        }

        stringBuilder.append(matrixHelper.build());
    }

    private void printConstraints() {
        ExpressionSerializer expressionSerializer = new ExpressionSerializer();
        expressionSerializer.setEnquoteAlways(true);
        expressionSerializer.setSymbols(LaTexSymbols.INSTANCE);

        stringBuilder.append("	\\matrix [below=1mm of current bounding box] {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        for (IConstraint constraint : featureModel.getConstraints()) {
            String text = constraint.getFormula().traverse(expressionSerializer).get();
            text = text.replaceAll("\"([\\w\" ]+)\"", " \\\\text\\{$1\\} "); // wrap all words in \text{} // replace with $2
            text = text.replaceAll("\\s+", " "); // remove unnecessary whitespace characters
            stringBuilder.append("	\\node {\\(").append(text).append("\\)}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            expressionSerializer.reset();
        }
        stringBuilder.append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }
}