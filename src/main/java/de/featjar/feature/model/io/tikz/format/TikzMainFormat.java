package de.featjar.feature.model.io.tikz.format;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.feature.model.io.tikz.helper.MatrixHelper;
import de.featjar.feature.model.io.tikz.helper.MatrixType;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.LaTexSymbols;
import de.featjar.formula.structure.IFormula;

/**
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class TikzMainFormat implements IGraphicalFormat{

    private final boolean[] LEGEND = new boolean[7];
    private boolean check = false;

    private final IFeatureModel featureModel;
    private final IFeatureTree featureTree;
    private final StringBuilder stringBuilder;

    public TikzMainFormat(IFeatureModel featureModel ,IFeatureTree featureTree, StringBuilder stringBuilder) {
        this.featureModel = featureModel;
        this.featureTree = featureTree;
        this.stringBuilder = stringBuilder;
    }

    @Override
    public void write() {
        printForest();
    }

    private void insertNodeHead(IFeature feature) {
        if (feature.isAbstract()) {
            stringBuilder.append(",abstract");
            LEGEND[0] = true;
            check = true;
        }
        if (feature.isConcrete()) {
            stringBuilder.append(",concrete");
            LEGEND[1] = true;
            check = true;
        }

        if (!isRootFeature(feature) && feature.getFeatureTree().isPresent()
                && feature.getFeatureTree().get().getParentGroup().isEmpty()) {
            if (feature.getFeatureTree().get().isMandatory()) {
                stringBuilder.append(",mandatory");
                LEGEND[2] = true;
                check = true;
            } else {
                stringBuilder.append(",optional");
                LEGEND[3] = true;
                check = true;
            }
        }

        if (!isRootFeature(feature)) {
            if (feature.getFeatureTree().get().getParentGroup().get().isOr()) {
                stringBuilder.append(",or");
                LEGEND[4] = true;
                check = true;
            }
        }
        if (!isRootFeature(feature)) {
            if (feature.getFeatureTree().get().getParentGroup().get().isAlternative()) {
                stringBuilder.append(",alternative");
                LEGEND[5] = true;
                check = true;
            }
        }
    }

    private boolean isRootFeature(IFeature feature) {
        return featureModel.getRootFeatures().contains(feature);
    }

    /**
     * A Feature is allowed to have a tree. This method checks the children and add them to the StringBuilder
     * in LateX (.tex) style.
     *
     * @param featureTree (the part tree of the feature)
     */
    private void printTree(IFeatureTree featureTree) {
        IFeature feature = featureTree.getFeature();
        //insertNodeHead(feature.getName().get());
        stringBuilder.append("[").append(feature.getName().get());
        insertNodeHead(feature);
        for (IFeatureTree featureTreeChildren : featureTree.getChildren()) {
            printTree(featureTreeChildren);
        }
        stringBuilder.append("]");
    }

    /**
     * Build the complete tree of the FeatureModel.
     */
    private void printForest() {
        IFeature feature = featureTree.getFeature();

        stringBuilder.append("\\begin{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\tfeatureDiagram").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\t");
        stringBuilder.append("[").append(feature.getName().get());
        insertNodeHead(feature);
        for (IFeatureTree featureTreeChildren : featureTree.getChildren()) {
            printTree(featureTreeChildren);
        }
        stringBuilder.append("]");
        postProcessing();
        stringBuilder.append("\t").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        if (!featureTree.getFeature().isHidden()) {
            printLegend();
        }
        //printConstraints(str, object);
        stringBuilder.append("\\end{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }

    /**
     * Processes a String to make special symbols LaTeX compatible.
     */
    private void postProcessing() {
        stringBuilder.replace(0, stringBuilder.length(), stringBuilder.toString().replace("_", "\\_"));
    }

    private void printLegend() {
        if (!check) {
            return;
        }

        MatrixHelper matrixHelper = getMatrixHelper();

        if (LEGEND[4]) {
            matrixHelper
                    .writeFillDraw("filldraw[drawColor] (0.45,0.15) ++ (225:0.3) arc[start angle=315,end angle=225,radius=0.2]")
                    .writeNode("[or,label=right:Or] {}")
                    .writeFillDraw("(0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0)")
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeFill("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[or,label=right:Or Group] {}");
        }

        if (LEGEND[5]) {
            matrixHelper
                    .writeDraw("(0.45,0.15) ++ (225:0.3) arc[start angle=315,end angle=225,radius=0.2] -- cycle")
                    .writeNode("[alternative,label=right:Alternative] {}")
                    .writeDraw("(0.1,0) -- +(-0.2, -0.4)")
                    .writeDraw("(0.1,0) -- +(0.2,-0.4)")
                    .writeDraw("(0,-0.2) arc (240:300:0.2)")
                    .writeNode("[alternative,label=right:Alternative Group] {}");
        }

        stringBuilder.append(matrixHelper.build());
    }

    private MatrixHelper getMatrixHelper() {
        MatrixHelper matrixHelper = new MatrixHelper(MatrixType.LEGEND);
        boolean abstractConcreteExists = false;

        if (LEGEND[0] && LEGEND[1]) {
            abstractConcreteExists = true;
            matrixHelper.writeNode("[abstract,label=right:Abstract Feature] {}");
            matrixHelper.writeNode("[concrete,label=right:Concrete Feature] {}");
        }

        if (LEGEND[0] && !abstractConcreteExists) {
            matrixHelper.writeNode("[abstract,label=right:Feature] {}");
        }

        if (LEGEND[1] && !abstractConcreteExists) {
            matrixHelper.writeNode("[concrete,label=right:Feature] {}");
        }

        if (LEGEND[2]) {
            matrixHelper.writeNode("[mandatory,label=right:Mandatory] {}");
        }

        if (LEGEND[3]) {
            matrixHelper.writeNode("[optional,label=right:Optional] {}");
        }
        return matrixHelper;
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
