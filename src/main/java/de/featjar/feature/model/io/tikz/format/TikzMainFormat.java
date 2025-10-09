package de.featjar.feature.model.io.tikz.format;

import de.featjar.feature.model.IFeatureTree;
import de.featjar.feature.model.io.tikz.TikzGraphicalFeatureModelFormat;

public class TikzMainFormat implements IGraphicalFormat{

    private final boolean[] LEGEND = new boolean[7];

    private final IFeatureTree featureTree;
    private final StringBuilder stringBuilder;

    public TikzMainFormat(IFeatureTree featureTree, StringBuilder stringBuilder) {
        this.featureTree = featureTree;
        this.stringBuilder = stringBuilder;
    }

    @Override
    public void write() {
        printForest();
    }

    /*private void insertNodeHead(String featureName) {
        stringBuilder.append("[").append(featureName);
        IFeature feature = featureModel.getFeature(featureName).orElse(null);

        if (feature == null) {
            problemList.add(new Problem("The feature " + featureName + " is null"));
            return;
        }

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
    }
     */

    /**
     * A Feature is allowed to have a tree. This method checks the children and add them to the StringBuilder
     * in LateX (.tex) style.
     *
     * @param featureTree (the part tree of the feature)
     */
    private void printTree(IFeatureTree featureTree) {
        stringBuilder.append("[").append(featureTree.getFeature().getName().get());
        for (IFeatureTree featureTreeChildren : featureTree.getChildren()) {
            printTree(featureTreeChildren);
        }
        stringBuilder.append("]");
    }

    /**
     * Build the complete tree of the FeatureModel.
     */
    private void printForest() {
        stringBuilder.append("\\begin{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\tfeatureDiagram").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("\t");
        stringBuilder.append("[").append(featureTree.getFeature().getName().get());
        for (IFeatureTree featureTreeChildren : featureTree.getChildren()) {
            printTree(featureTreeChildren);
        }
        stringBuilder.append("]");
        postProcessing();
        stringBuilder.append("\t").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        /*if (!object.isLegendHidden()) {
            printLegend(str, object);
        }*/
        //printConstraints(str, object);
        stringBuilder.append("\\end{forest}").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
    }

    /**
     * Processes a String to make special symbols LaTeX compatible.
     */
    private void postProcessing() {
        stringBuilder.replace(0, stringBuilder.length(), stringBuilder.toString().replace("_", "\\_"));
    }

   /* private void printLegend() {
        boolean check = false;
        final StringBuilder sb = new StringBuilder();
        if (LEGEND[0] && LEGEND[1]) {
            check = true;
            sb.append("		\\node [abstract,label=right:Abstract Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[0] = false;
            sb.append("		\\node [concrete,label=right:Concrete Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[1] = false;
        }
        if (LEGEND[0]) {
            check = true;
            sb.append("		\\node [abstract,label=right:Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[0] = false;
        }
        if (LEGEND[1]) {
            check = true;
            sb.append("		\\node [concrete,label=right:Feature] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[1] = false;
        }
        if (LEGEND[2]) {
            check = true;
            sb.append("		\\node [mandatory,label=right:Mandatory] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[2] = false;
        }
        if (LEGEND[3]) {
            check = true;
            sb.append("		\\node [optional,label=right:Optional] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[3] = false;
        }
        if (LEGEND[4]) {
            check = true;
            // myString.append(" \\filldraw[drawColor] (0.45,0.15) ++ (225:0.3) arc[start angle=315,end angle=225,radius=0.2]; " + lnSep
            // + " \\node [or,label=right:Or] {}; \\\\" + lnSep);
            sb.append("			\\filldraw[drawColor] (0.1,0) - +(-0,-0.2) - +(0.2,-0.2)- +(0.1,0);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\fill[drawColor] (0,-0.2) arc (240:300:0.2);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [or,label=right:Or Group] {}; \\\\");
            LEGEND[4] = false;
        }
        if (LEGEND[5]) {
            check = true;
            // myString.append(" \\draw[drawColor] (0.45,0.15) ++ (225:0.3) arc[start angle=315,end angle=225,radius=0.2] -- cycle; " + lnSep
            // + " \\node [alternative,label=right:Alternative] {}; \\\\" + lnSep);
            sb.append("			\\draw[drawColor] (0.1,0) -- +(-0.2, -0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0.1,0) -- +(0.2,-0.4);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("			\\draw[drawColor] (0,-0.2) arc (240:300:0.2);").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [alternative,label=right:Alternative Group] {}; \\\\");
            LEGEND[5] = false;
        }
        if (LEGEND[6]) {
            check = true;
            sb.append("		\\node [hiddenNodes,label=center:1,label=right:Collapsed Nodes] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            LEGEND[6] = false;
        }
/*Color
        final ColorScheme colorScheme = FeatureColorManager.getCurrentColorScheme(graphicalFeatureModel.getFeatureModelManager().getSnapshot());
        int colorIndex = 1;

        for (final FeatureColor currentColor : new HashSet<>(colorScheme.getColors().values())) {
            if (currentColor != FeatureColor.NO_COLOR) {
                String meaning = currentColor.getMeaning();
                if (meaning.isEmpty()) {
                    meaning = "Custom Color " + String.format("%02d", colorIndex);
                    colorIndex++;
                }
                sb.append("		\\node [" + featureColorToTikzStyle(currentColor) + ",label=right:" + meaning + "] {}; \\\\" + lnSep);
            }
        }



        if (check) {
            stringBuilder.append("	\\matrix [anchor=north west] at (current bounding box.north east) {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [placeholder] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("	\\matrix [draw=drawColor,anchor=north west] at (current bounding box.north east) {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR).append("		\\node [label=center:\\underline{Legend:}] {}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            stringBuilder.append(sb);
            stringBuilder.append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
            check = false;
        } else {
            for (int i = 0; i < LEGEND.length; ++i) {
                LEGEND[i] = false;
            }
            check = false;
        }
    }
    */

    /*private void printConstraints() {
        stringBuilder.append("	\\matrix [below=1mm of current bounding box] {").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        for (IConstraint constraint : featureModel.getConstraints()) {
            String text = constraint.getFormula().print();
            text = text.replaceAll("\"([\\w\" ]+)\"", " \\\\text\\{$1\\} "); // wrap all words in \text{} // replace with $2
            text = text.replaceAll("\\s+", " "); // remove unnecessary whitespace characters
            stringBuilder.append("	\\node {\\(").append(text).append("\\)}; \\\\").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        }
        stringBuilder.append("	};").append(TikzGraphicalFeatureModelFormat.LINE_SEPERATOR);
        /*for (final IGraphicalConstraint constraint : graphicalFeatureModel.getConstraints()) {
            String text = constraint.getObject().getNode().toString(NodeWriter.latexSymbols, true);
            text = text.replaceAll("\"([\\w\" ]+)\"", " \\\\text\\{$1\\} "); // wrap all words in \text{} // replace with $2
            text = text.replaceAll("\\s+", " "); // remove unnecessary whitespace characters
            str.append("	\\node {\\(" + text + "\\)}; \\\\" + lnSep);
        }
        str.append("	};" + lnSep);
    }
     */

}
