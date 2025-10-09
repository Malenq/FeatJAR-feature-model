package de.featjar.feature.model.io.tikz;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.*;
import de.featjar.formula.structure.connective.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class FeatureModelDisplayTikzTest {

    private final StringBuilder stringBuilder = new StringBuilder();
    private static IFeatureModel featureModel;

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();

        FeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());

        // First Tree
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("Hello"));
        rootTree.mutate().toAndGroup();

        IFeature feature = featureModel.mutate().addFeature("Feature");
        IFeatureTree firstFeatureTree = rootTree.mutate().addFeatureBelow(feature);
        firstFeatureTree.mutate().toOrGroup();

        IFeature world = featureModel.mutate().addFeature("World");
        rootTree.mutate().addFeatureBelow(world);

        IFeature wonderful = featureModel.addFeature("Wonderful");
        firstFeatureTree.mutate().addFeatureBelow(wonderful);

        IFeature beautiful = featureModel.addFeature("Beautiful");
        firstFeatureTree.mutate().addFeatureBelow(beautiful);

        // Second Tree

        /*IFeatureTree rootTreeSec =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("Hello2"));
        rootTree.mutate().toAndGroup();

        IFeature feature2 = featureModel.mutate().addFeature("Feature2");
        rootTreeSec.mutate().addFeatureBelow(feature2);

        IFeature world2 = featureModel.mutate().addFeature("World2");
        IFeatureTree firstFeatureTree2 = rootTreeSec.mutate().addFeatureBelow(world2);

        IFeature wonderful2 = featureModel.addFeature("Wonderful2");
        firstFeatureTree2.mutate().addFeatureBelow(wonderful2);

        IFeature beautiful2 = featureModel.addFeature("Beautiful2");
        firstFeatureTree2.mutate().addFeatureBelow(beautiful2);

        // Thrid Tree

        IFeatureTree rootTree3 = featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree3.mutate().toAndGroup();

        IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree3.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));

        childFeature1Tree.mutate().toAlternativeGroup();

        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        childFeature1Tree.mutate().addFeatureBelow(childFeature2);

        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        childFeature1Tree.mutate().addFeatureBelow(childFeature3);

         */

        FeatureModelDisplayTikzTest.featureModel = featureModel;
    }

    @Test
    public void perform() {
        new TikzGraphicalFeatureModelFormat(featureModel, false).serialize().ifPresent(s -> {
            FeatJAR.log().info(s);
        });
    }

    /**
     * Test for the "synatx" mechanic
     */

    public void displayTikz() {
        featureModel.getFeatureModel().getRoots().forEach(iFeatureTree -> {
            stringBuilder.append("[").append(iFeatureTree.getFeature().getName().get());
            for (IFeatureTree featureTreeChildren : iFeatureTree.getChildren()) {
                subTreeRecursiv(featureTreeChildren);
            }
        });

        stringBuilder.append("]");
        FeatJAR.log().info(stringBuilder);

        String expectedOutput = "[Hello[Feature[Wonderful][Beautiful]][World]]";

        Assertions.assertEquals(expectedOutput,
                stringBuilder.toString(), "Expected: " + expectedOutput + System.lineSeparator()
                        + "Output: " + stringBuilder);
    }

    private void subTreeRecursiv(IFeatureTree featureTree) {
        stringBuilder.append("[").append(featureTree.getFeature().getName().get());
        for (IFeatureTree featureTreeChildren : featureTree.getChildren()) {
            subTreeRecursiv(featureTreeChildren);
        }
        stringBuilder.append("]");
    }

}
