package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;

import java.util.Arrays;
import java.util.List;

/**
 * This class travers a given {@link IFeatureTree} and generates the Tikz representation of the tree.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class PrintVisitor implements ITreeVisitor<IFeatureTree, String> {

    private final StringBuilder stringBuilder;

    public PrintVisitor() {
        this.stringBuilder = new StringBuilder();
    }

    @Override
    public TraversalAction firstVisit(List<IFeatureTree> path) {
        IFeature feature = ITreeVisitor.getCurrentNode(path).getFeature();

        //new AttributeHelper(stringBuilder/*, Arrays.asList("abstract", "name")*/)
        //        .writeAttributes(feature);
        stringBuilder.append("[");
        insertAttributes(feature);
        insertNodeHead(feature);
        return TraversalAction.CONTINUE;
    }

    @Override
    public TraversalAction lastVisit(List<IFeatureTree> path) {
        stringBuilder.append("]");
        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<String> getResult() {
        return Result.of(stringBuilder.toString());
    }

    private void insertAttributes(IFeature feature) {
        if(feature.getAttributes().isPresent() && !feature.getAttributes().get().isEmpty()) {
            stringBuilder.append(String.format("\\multicolumn{2}{c}{%s} \\\\\\hline",
                    feature.getName().orElse("")));

            feature.getAttributes().get().forEach((attribute, value) -> {
                if(!attribute.getName().equals("name")) {
                    stringBuilder.append(String.format("\\small\\texttt{%s (%s)} &\\small\\texttt{= %s} \\\\",
                            attribute.getName(), attribute.getType().getSimpleName(),
                            value.toString()));
                }
            });

            stringBuilder.append(",align=ll");
        } else {
            stringBuilder.append(feature.getName().orElse(""));
        }
    }

    private void insertNodeHead(IFeature feature) {
        if (feature.isAbstract()) {
            stringBuilder.append(",abstract");
        }

        if (feature.isConcrete()) {
            stringBuilder.append(",concrete");
        }

        if(feature.getFeatureTree().isPresent()) {
            if (feature.getFeatureTree().get().getFeatureCardinalityUpperBound() > 1) {
                stringBuilder.append(String.format(",featurecardinality={%d}{%d}",
                        feature.getFeatureTree().get().getFeatureCardinalityLowerBound(),
                        feature.getFeatureTree().get().getFeatureCardinalityUpperBound()));
            }

            if (feature.getFeatureTree().get().isMandatory()) {
                stringBuilder.append(",mandatory");
            } else {
                stringBuilder.append(",optional");
            }
        }

        if (isNotRootFeature(feature) && feature.getFeatureTree().isPresent()) {
            if (feature.getFeatureTree().get().getParentGroup().isPresent() &&
                    feature.getFeatureTree().get().getParentGroup().get().isOr()) {
                stringBuilder.append(",or");
            }
        }
        if (isNotRootFeature(feature) && feature.getFeatureTree().isPresent()) {
            if (feature.getFeatureTree().get().getParentGroup().isPresent() &&
                    feature.getFeatureTree().get().getParentGroup().get().isAlternative()) {
                stringBuilder.append(",alternative");
            }
        }
    }

    private boolean isNotRootFeature(IFeature feature) {
        return !feature.getFeatureModel().getRootFeatures().contains(feature);
    }
}