package de.featjar.feature.model.io.tikz;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.*;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.connective.*;
import de.featjar.formula.structure.term.value.Variable;
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

        IFeature featureRootS = featureModel.mutate().addFeature("Hello");
        featureRootS.mutate().setAbstract();
        // First Tree
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureRootS);
        rootTree.mutate().toOrGroup();

        IFeature feature = featureModel.mutate().addFeature("Feature");
        IFeatureTree firstFeatureTree = rootTree.mutate().addFeatureBelow(feature);
        firstFeatureTree.mutate().makeMandatory();

        IFeature world = featureModel.mutate().addFeature("World");
        rootTree.mutate().addFeatureBelow(world);

        IFeature wonderful = featureModel.addFeature("Wonderful");
        firstFeatureTree.mutate().addFeatureBelow(wonderful);

        IFeature beautiful = featureModel.addFeature("Beautiful");
        firstFeatureTree.mutate().addFeatureBelow(beautiful);
        featureModel.addConstraint(new And(Expressions.literal("A"), Expressions.literal("B")));
        featureModel.addConstraint(new Implies(Expressions.literal("C"), new ForAll(new Variable("A"), new BiImplies(Expressions.literal("A"), Expressions.literal("C")))));


        FeatureModelDisplayTikzTest.featureModel = featureModel;
    }

    @Test
    public void perform() {
        new TikzGraphicalFeatureModelFormat(featureModel, false).serialize(featureModel).ifPresent(s -> {
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
