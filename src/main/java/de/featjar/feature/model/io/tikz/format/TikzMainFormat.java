package de.featjar.feature.model.io.tikz.format;

import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;
import de.featjar.formula.io.textual.ExpressionSerializer;
import de.featjar.formula.io.textual.LaTexSymbols;
import de.featjar.formula.structure.IFormula;

public class TikzMainFormat implements IGraphicalFormat{

    private final boolean[] LEGEND = new boolean[7];

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
        }
        if (feature.isConcrete()) {
            stringBuilder.append(",concrete");
            LEGEND[1] = true;
        }

        if (!isRootFeature(feature) && feature.getFeatureTree().isPresent()
                && feature.getFeatureTree().get().getParentGroup().isEmpty()) {
            if (feature.getFeatureTree().get().isMandatory()) {
                stringBuilder.append(",mandatory");
                LEGEND[2] = true;
            } else {
                stringBuilder.append(",optional");
                LEGEND[3] = true;
            }
        }

        if (!isRootFeature(feature)) {
            if (feature.getFeatureTree().get().getParentGroup().get().isOr()) {
                stringBuilder.append(",or");
                LEGEND[4] = true;
            }
        }
        if (!isRootFeature(feature)) {
            if (feature.getFeatureTree().get().getParentGroup().get().isAlternative()) {
                stringBuilder.append(",alternative");
                LEGEND[5] = true;
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
        if (!featureTree.getFeature().isHidden()) { // todo: fix error
            printLegend();                          // todo
        }                                           // todo
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

            stringBuilder.append("		\\node [abstract,label=right:Abstract Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("		\\node [concrete,label=right:Concrete Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("		\\node [abstract,label=right:Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("		\\node [concrete,label=right:Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("		\\node [mandatory,label=right:Mandatory] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("		\\node [optional,label=right:Optional] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("			\\filldraw[drawColor] (0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\fill[drawColor] (0,-0.2) arc (240:300:0.2);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [or,label=right:Or Group] {}; \\\\");

            stringBuilder.append("			\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0,-0.2) arc (240:300:0.2);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [alternative,label=right:Alternative Group] {}; \\\\");

            stringBuilder.append("		\\node [hiddenNodes,label=center:1,label=right:Collapsed Nodes] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);

            stringBuilder.append("	\\matrix [anchor=north west] at (current bounding box.north east) {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [placeholder] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("	\\matrix [draw=drawColor,anchor=north west] at (current bounding box.north east) {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [label=center:\\underline{Legend:}] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            stringBuilder.append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);


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
