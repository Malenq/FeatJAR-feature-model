package de.featjar.feature.model.transformer;

import de.featjar.base.FeatJAR;
import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.base.tree.Trees;
import de.featjar.base.tree.visitor.TreePrinter;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.aggregate.AttributeSum;
import de.featjar.formula.structure.term.value.Variable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NumericFeatureAttributeAggregateExample {
    private IFeatureModel featureModel;

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
    public void numericFeatures() {
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("IceCreamOrder"));
        rootTree.mutate().makeMandatory();
        rootTree.mutate().toAndGroup();

        // create and set cardinality for the child feature
        IFeature scoop = featureModel.mutate().addFeature("Scoop");
        scoop.mutate().setType(Integer.class);
        IFeatureTree scoopTree = rootTree.mutate().addFeatureBelow(scoop);
        scoopTree.mutate().setFeatureCardinality(Range.of(1, 1));

        IFeature topping = featureModel.mutate().addFeature("Topping");
        rootTree.mutate().addFeatureBelow(topping);

        featureModel.mutate().addConstraint(Expressions.greaterThan(new Variable("Scoop", Integer.class), Expressions.constant(0)));

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula
                .set(ComputeFormula.SIMPLE_TRANSLATION, Boolean.TRUE)
                .computeResult()
                .orElseThrow();

        TreePrinter visitor = new TreePrinter();

        FeatJAR.log().message("********************************************************************");
        FeatJAR.log().message(Trees.traverse(resultFormula, visitor));
    }

    @Test
    public void attributeAggregate() {
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("IceCreamOrder"));
        rootTree.mutate().makeMandatory();
        rootTree.mutate().toAndGroup();

        // create and set cardinality for the child feature
        IFeature scoop = featureModel.mutate().addFeature("Scoop");
        scoop.mutate().setAttributeValue(new Attribute<>("cost", Double.class), 1.0);
        IFeatureTree scoopTree = rootTree.mutate().addFeatureBelow(scoop);
        scoopTree.mutate().setFeatureCardinality(Range.of(1, 1));

        IFeature topping1 = featureModel.mutate().addFeature("Topping1");
        topping1.mutate().setAttributeValue(new Attribute<>("cost", Double.class), 0.5);
        rootTree.mutate().addFeatureBelow(topping1);

        IFeature topping2 = featureModel.mutate().addFeature("Topping2");
        topping2.mutate().setAttributeValue(new Attribute<>("cost", Double.class), 0.5);
        rootTree.mutate().addFeatureBelow(topping2);

        IFeature topping3 = featureModel.mutate().addFeature("Topping3");
        topping3.mutate().setAttributeValue(new Attribute<>("cost", Double.class), 0.5);
        rootTree.mutate().addFeatureBelow(topping3);

        featureModel.mutate().addConstraint(Expressions.lessEqual(new AttributeSum("cost"), Expressions.constant(3.0)));

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula
                .set(ComputeFormula.SIMPLE_TRANSLATION, Boolean.TRUE)
                .computeResult()
                .orElseThrow();

        TreePrinter visitor = new TreePrinter();
        visitor.setFilter(n -> !(n instanceof Variable));

        FeatJAR.log().message("********************************************************************");
        FeatJAR.log().message(Trees.traverse(resultFormula, visitor));
    }
}
