package de.featjar.feature.model.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * Test class with presentation example of ComputeFormula.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 */
public class ComputeFormulaPresentationExample {
    private IFeatureModel featureModel;
    private IFormula expected;

    @BeforeAll
    public static void init() {
        FeatJAR.defaultConfiguration().initialize();
    }

    @AfterAll
    public static void deinit() {
        FeatJAR.deinitialize();
    }

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
    }

    @Test
    void presentationExample() {
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("IceCreamOrder"));
        rootTree.mutate().makeMandatory();
        rootTree.mutate().toAndGroup();

        // create and set cardinality for the child feature
        IFeature scoop = featureModel.mutate().addFeature("Scoop");
        IFeatureTree scoopTree = rootTree.mutate().addFeatureBelow(scoop);
        scoopTree.mutate().setFeatureCardinality(Range.of(0, 2));
        scoopTree.mutate().toAlternativeGroup();

        // add normal feature below
        IFeature topping = featureModel.mutate().addFeature("Topping");
        rootTree.mutate().addFeatureBelow(topping);

        IFeature chocolate = featureModel.mutate().addFeature("Chocolate");
        scoopTree.mutate().addFeatureBelow(chocolate);

        IFeature strawberry = featureModel.mutate().addFeature("Strawberry");
        scoopTree.mutate().addFeatureBelow(strawberry);

        // cross-tree constraints
        featureModel.mutate().addConstraint(new Implies(new Literal("Topping"), new Literal("Chocolate")));

        expected = new Reference(new And(
                new Literal("IceCreamOrder"),
                new Implies(new Literal("Scoop_1"), new Literal("IceCreamOrder")),
                new Implies(new Literal("Scoop_2"), new Literal("IceCreamOrder")),
                new Implies(new Literal("Scoop_2"), new Literal("Scoop_1")),
                new Implies(new Literal("Chocolate.Scoop_1"), new Literal("Scoop_1")),
                new Implies(new Literal("Strawberry.Scoop_1"), new Literal("Scoop_1")),
                new Implies(
                        new Literal("Scoop_1"),
                        new Choose(1, new Literal("Chocolate.Scoop_1"), new Literal("Strawberry.Scoop_1"))),
                new Implies(new Literal("Chocolate.Scoop_2"), new Literal("Scoop_2")),
                new Implies(new Literal("Strawberry.Scoop_2"), new Literal("Scoop_2")),
                new Implies(
                        new Literal("Scoop_2"),
                        new Choose(1, new Literal("Chocolate.Scoop_2"), new Literal("Strawberry.Scoop_2"))),
                new Implies(new Literal("Topping"), new Literal("IceCreamOrder")),
                // these two might have an or for both chocolate scoops
                new Implies(
                        new Literal("Scoop_1"), new Implies(new Literal("Topping"), new Literal("Chocolate.Scoop_1"))),
                new Implies(
                        new Literal("Scoop_2"), new Implies(new Literal("Topping"), new Literal("Chocolate.Scoop_2"))),
                new Implies(
                        new Literal("Topping"),
                        new Or(new Literal("Chocolate.Scoop_1"), new Literal("Chocolate.Scoop_2")))));

        executeTest();
    }

    @Test
    void simplePresentationExample() {
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("IceCreamOrder"));
        rootTree.mutate().makeMandatory();
        rootTree.mutate().toAndGroup();

        // create and set cardinality for the child feature
        IFeature scoop = featureModel.mutate().addFeature("Scoop");
        IFeatureTree scoopTree = rootTree.mutate().addFeatureBelow(scoop);
        scoopTree.mutate().setFeatureCardinality(Range.of(0, 2));
        scoopTree.mutate().toAlternativeGroup();

        // add normal feature below
        IFeature topping = featureModel.mutate().addFeature("Topping");
        rootTree.mutate().addFeatureBelow(topping);

        IFeature chocolate = featureModel.mutate().addFeature("Chocolate");
        scoopTree.mutate().addFeatureBelow(chocolate);

        IFeature strawberry = featureModel.mutate().addFeature("Strawberry");
        scoopTree.mutate().addFeatureBelow(strawberry);

        // cross-tree constraints
        featureModel.mutate().addConstraint(new Implies(new Literal("Topping"), new Literal("Chocolate")));

        expected = new Reference(new And(
                new Literal("IceCreamOrder"),
                new Implies(new Literal("Scoop_1"), new Literal("IceCreamOrder")),
                new Implies(new Literal("Scoop_2"), new Literal("IceCreamOrder")),
                new Implies(new Literal("Scoop_2"), new Literal("Scoop_1")),
                new Implies(new Literal("Chocolate"), new Or(new Literal("Scoop_1"), new Literal("Scoop_2"))),
                new Implies(new Literal("Strawberry"), new Or(new Literal("Scoop_1"), new Literal("Scoop_2"))),
                new Implies(
                        new Or(new Literal("Scoop_1"), new Literal("Scoop_2")),
                        new Choose(1, new Literal("Chocolate"), new Literal("Strawberry"))),
                new Implies(new Literal("Topping"), new Literal("IceCreamOrder")),
                new Implies(new Literal("Topping"), new Literal("Chocolate"))));

        executeSimpleTest();
    }

    private void executeTest() {

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula.computeResult().orElseThrow();

        // not the same amount of constraints in both formulas
        assertEquals(
                expected.getFirstChild().get().getChildrenCount(),
                resultFormula.getFirstChild().get().getChildrenCount());

        TreePrinter visitor = new TreePrinter();
        visitor.setFilter(n -> !(n instanceof Variable));

        FeatJAR.log().message("********************************************************************");
        FeatJAR.log().message(Trees.traverse(resultFormula, visitor));

        for (IExpression expr : expected.getFirstChild().get().getChildren()) {
            try {
                resultFormula.getFirstChild().get().removeChild(expr);
            } catch (Exception e) {
                fail(e);
            }
        }

        // assert
        assertEquals(resultFormula.getFirstChild().get().getChildrenCount(), 0);
    }

    private void executeSimpleTest() {

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula
                .set(ComputeFormula.SIMPLE_TRANSLATION, Boolean.TRUE)
                .computeResult()
                .orElseThrow();

        // not the same amount of constraints in both formulas
        assertEquals(
                expected.getFirstChild().get().getChildrenCount(),
                resultFormula.getFirstChild().get().getChildrenCount());

        TreePrinter visitor = new TreePrinter();
        visitor.setFilter(n -> !(n instanceof Variable));

        FeatJAR.log().message("********************************************************************");
        FeatJAR.log().message(Trees.traverse(resultFormula, visitor));

        for (IExpression expr : expected.getFirstChild().get().getChildren()) {
            try {
                resultFormula.getFirstChild().get().removeChild(expr);
            } catch (Exception e) {
                fail(e);
            }
        }

        // assert
        assertEquals(resultFormula.getFirstChild().get().getChildrenCount(), 0);
    }

    private void executeExpectedException() {
        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        assertThrows(
                UnsupportedOperationException.class,
                () -> computeFormula.computeResult().orElseThrow());
    }

    private void executeSimpleExpectedException() {
        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        assertThrows(UnsupportedOperationException.class, () -> computeFormula
                .set(ComputeFormula.SIMPLE_TRANSLATION, Boolean.TRUE)
                .computeResult()
                .orElseThrow());
    }
}
