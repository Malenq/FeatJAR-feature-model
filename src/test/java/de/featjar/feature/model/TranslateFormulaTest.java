package de.featjar.feature.model;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.Computations;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.transformer.ComputeFormula;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */

public class TranslateFormulaTest {

    @BeforeAll
    public static void insert() {
        FeatJAR.testConfiguration().initialize();
    }

    @Test
    public void testInteger() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Integer.class);

        IFormula result = Computations.of(featureModel)
                .map(ComputeFormula::new)
                .compute();
        IFormula formula = buildFormula(Integer.class, 0);
        Assertions.assertEquals(result.print(), formula.print());
        FeatJAR.log().info("Integer Test expected value: " + formula.print());
        FeatJAR.log().info("Integer Test result output: " + result.print());
    }

    @Test
    public void testBoolean() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Boolean.class);

        IFormula result = Computations.of(featureModel)
                .map(ComputeFormula::new)
                .compute();
        IFormula formula = buildBooleanForumla();
        Assertions.assertEquals(result.print(), formula.print());
        FeatJAR.log().info("Boolean Test expected value: " + formula.print());
        FeatJAR.log().info("Boolean Test result output: " + result.print());
    }

    @Test
    public void testFloat() {
        IFeatureModel featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
        addValues(featureModel, Float.class);

        IFormula result = Computations.of(featureModel)
                .map(ComputeFormula::new)
                .compute();
        IFormula formula = buildFormula(Float.class, (float) 0.0);
        Assertions.assertEquals(result.print(), formula.print());
        FeatJAR.log().info("Float Test expected value: " + formula.print());
        FeatJAR.log().info("Float Test result output: " + result.print());
    }

    private void addValues(IFeatureModel featureModel, Class<?> type) {
        IFeature root = featureModel.mutate().addFeature("root");
        IFeatureTree rootTree = featureModel.mutate().addFeatureTreeRoot(root);
        rootTree.getRoot().mutate().toAndGroup();
        for (short i = 0; i < 5; i++) {
            IFeature feature = featureModel.mutate().addFeature(i + "feature");
            feature.mutate().setName("feature" + i);
            feature.mutate().setType(type);
            rootTree.mutate().addFeatureBelow(feature);

            FeatJAR.log().info("Added Feature " + feature.getName().get() + " with type " + feature.getType());
        }
    }

    private IFormula buildFormula(Class<?> type, Object expectedValue) {
        return new Reference(new And(
                Expressions.implies(new NotEquals(new Variable("feature0", type),
                        new Constant(expectedValue, type)), new Literal("root")),
                Expressions.implies(new NotEquals(new Variable("feature1", Boolean.class),
                        new Constant(expectedValue, type)), new Literal("root")),
                Expressions.implies(new NotEquals(new Variable("feature2", Boolean.class),
                        new Constant(expectedValue, type)), new Literal("root")),
                Expressions.implies(new NotEquals(new Variable("feature3", Boolean.class),
                        new Constant(expectedValue, type)), new Literal("root")),
                Expressions.implies(new NotEquals(new Variable("feature4", Boolean.class),
                        new Constant(expectedValue, type)), new Literal("root"))
        ));
    }

    private IFormula buildBooleanForumla() {
        return new Reference(new And(
                Expressions.implies(Expressions.literal("feature0"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature1"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature2"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature3"), Expressions.literal("root")),
                Expressions.implies(Expressions.literal("feature4"), Expressions.literal("root"))
        ));
    }
}