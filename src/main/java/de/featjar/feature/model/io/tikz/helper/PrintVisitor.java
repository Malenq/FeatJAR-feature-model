package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import java.util.ArrayList;
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

    private final TikzAttributeHelper.FilterType filterType;
    private final List<String> filterValues;

    public PrintVisitor() {
        this.stringBuilder = new StringBuilder();
        this.filterType = TikzAttributeHelper.FilterType.WITH_OUT; // default
        this.filterValues = new ArrayList<>();
    }

    public PrintVisitor(TikzAttributeHelper.FilterType filterType, List<String> filterValues) {
        this.stringBuilder = new StringBuilder();
        this.filterType = filterType;
        this.filterValues = filterValues;
    }

    @Override
    public TraversalAction firstVisit(List<IFeatureTree> path) {
        IFeature feature = ITreeVisitor.getCurrentNode(path).getFeature();

        new TikzAttributeHelper(feature, stringBuilder)
                .addFilterValue(filterValues)
                .setFilterType(filterType)
                .build();
        insertFeatureType(feature);
        insertFeatureCardinality(feature);
        insertGroupCardinality(feature);

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

    private void insertFeatureType(IFeature feature) {
        if (feature.isAbstract()) {
            stringBuilder.append(",abstract");
        }

        if (feature.isConcrete()) {
            stringBuilder.append(",concrete");
        }
    }

    private void insertFeatureCardinality(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElse(null);
        FeatureTree.Group featureTreeParentGroup = feature.getFeatureTree().get().getParentGroup().orElse(null);

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
    }

    private void insertGroupCardinality(IFeature feature) {
        IFeatureTree featureTree = feature.getFeatureTree().orElse(null);

        if (isNotRootFeature(feature)) {
            int previousChildrenCount = 1;
            for(int i = 0; i < featureTree.getChildrenGroups().size(); i++) {
                if(featureTree.getChildrenGroup(i).isPresent()) {
                    FeatureTree.Group group = featureTree.getChildrenGroup(i).get();

                    int childrenCount = featureTree.getChildren(i).size();
                    if(group.isOr()) {
                        stringBuilder.append(String.format(",or={%d}{%d}{%d}", previousChildrenCount, previousChildrenCount + childrenCount - 1,
                                (2 * previousChildrenCount + childrenCount - 1) / 2));
                    } else if(group.isAlternative()) {
                        stringBuilder.append(String.format(",alternative={%d}{%d}{%d}", previousChildrenCount, previousChildrenCount + childrenCount - 1,
                                (2 * previousChildrenCount + childrenCount - 1) / 2));
                    } else if(group.isCardinalityGroup()) {
                        stringBuilder.append(String.format(",groupcardinality={%d}{%d}{%d}{%d}{%d}", previousChildrenCount, previousChildrenCount + childrenCount - 1,
                                (2 * previousChildrenCount + childrenCount - 1) / 2, group.getLowerBound(), group.getUpperBound()));
                    }

                    previousChildrenCount += childrenCount;
                }
            }
        }
    }

    private boolean isNotRootFeature(IFeature feature) {
        return !feature.getFeatureModel().getRootFeatures().contains(feature);
    }
}