package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import java.util.List;
import java.util.Optional;

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

        new AttributeHelper(feature, stringBuilder)
                .addFilterValue("name")
                .setFilterType(AttributeHelper.FilterType.DISPLAY)
                .build();
        insertNodeHead(feature);
        handleGroups(feature);
        return TraversalAction.CONTINUE;
    }

    private void handleGroups(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElse(null);
        if (featureTree == null) {
            return;
        }

        featureTree.getChildren().forEach(featureStream -> {
           FeatJAR.log().info(feature.getName().get() + " - " + featureStream.getFeature().getName().get());
        });

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

    private void insertNodeHead(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElse(null);
        FeatureTree.Group featureTreeParentGroup = feature.getFeatureTree().get().getParentGroup().orElse(null);

        if (feature.isAbstract()) {
            stringBuilder.append(",abstract");
        }

        if (feature.isConcrete()) {
            stringBuilder.append(",concrete");
        }

        if (isNotRootFeature(feature) && featureTreeParentGroup != null && featureTreeParentGroup.isAnd()) {
            if (featureTree.getFeatureCardinalityLowerBound() == 0 &&
                    featureTree.getFeatureCardinalityUpperBound() == 1) {
                stringBuilder.append(",optional");
            } else if(featureTree.getFeatureCardinalityLowerBound() == 1 &&
                    featureTree.getFeatureCardinalityUpperBound() == 1) {
                stringBuilder.append(",mandatory");
            } else {
                stringBuilder.append(String.format(",featurecardinality={%d}{%d}",
                        feature.getFeatureTree().get().getFeatureCardinalityLowerBound(),
                        feature.getFeatureTree().get().getFeatureCardinalityUpperBound()));
            }
        }

        if (isNotRootFeature(feature)) {
            int previousChildrenCount = 0;
            for(int i = 0; i < featureTree.getChildrenGroups().size(); i++) {
                if(featureTree.getChildrenGroup(i).isPresent()) {
                    FeatureTree.Group group = featureTree.getChildrenGroup(i).get();

                    if(group.isOr()) {
                        stringBuilder.append(String.format(",or={%d}{%d}", previousChildrenCount + 1,
                                featureTree.getChildren(i).size()));
                    } else if(group.isAlternative()) {
                        stringBuilder.append(String.format(",alternative={%d}{%d}", previousChildrenCount + 1,
                                featureTree.getChildren(i).size()));
                    } else if(group.isCardinalityGroup()) {
                        stringBuilder.append(String.format(",groupcardinality={%d}{%d}", previousChildrenCount + 1,
                                featureTree.getChildren(i).size()));
                    }

                    previousChildrenCount += featureTree.getChildren(i).size();
                }
            }
        }
    }

    private boolean isNotRootFeature(IFeature feature) {
        return !feature.getFeatureModel().getRootFeatures().contains(feature);
    }
}