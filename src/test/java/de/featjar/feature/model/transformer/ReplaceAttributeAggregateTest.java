package de.featjar.feature.model.transformer;

import static org.junit.jupiter.api.Assertions.assertTrue;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttribute;
import de.featjar.base.tree.Trees;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.predicate.LessThan;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.aggregate.AttributeAverage;
import de.featjar.formula.structure.term.aggregate.AttributeSum;
import de.featjar.formula.structure.term.function.IfThenElse;
import de.featjar.formula.structure.term.function.IntegerAdd;
import de.featjar.formula.structure.term.function.RealAdd;
import de.featjar.formula.structure.term.function.RealDivide;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ReplaceAttributeAggregateTest {

    private static Map<IFormula, Map<IAttribute<?>, Object>> attributes;

    @BeforeAll
    public static void init() {
        FeatJAR.testConfiguration().initialize();

        attributes = new LinkedHashMap<>();
        attributes.put(
                Expressions.literal("cpu"),
                Map.of(
                        new Attribute<>("cost", Long.class),
                        10L,
                        new Attribute<>("required", Boolean.class),
                        true,
                        new Attribute<>("power", Float.class),
                        104.5f));
        attributes.put(
                Expressions.literal("gpu"),
                Map.of(
                        new Attribute<>("cost", Long.class),
                        100L,
                        new Attribute<>("required", Boolean.class),
                        false,
                        new Attribute<>("power", Float.class),
                        200.5f));
        attributes.put(
                Expressions.literal("ram"),
                Map.of(new Attribute<>("cost", Long.class), 20L, new Attribute<>("required", Boolean.class), true));
        attributes.put(
                Expressions.literal("motherboard"),
                Map.of(new Attribute<>("required", Boolean.class), true, new Attribute<>("power", Float.class), 3.5f));
        attributes.put(Expressions.literal("power_supply"), Collections.emptyMap());
        attributes.put(
                new NotEquals(new Variable("refreshrate", Double.class), new Constant(0.0)),
                Map.of(new Attribute<>("required", Boolean.class), true, new Attribute<>("power", Float.class), 1.0f));
    }

    @Test
    public void test1() {
        IFormula test = new LessThan(new AttributeSum("cost"), new Constant(200L, Long.class));
        ReplaceAttributeAggregate replaceAttributeAggregate = new ReplaceAttributeAggregate(attributes, false);
        Trees.traverse(test, replaceAttributeAggregate);

        IFormula comparison = new LessThan(
                new IntegerAdd(
                        new IfThenElse(new Literal("cpu"), new Constant(10L, Long.class), new Constant(0L, Long.class)),
                        new IfThenElse(
                                new Literal("gpu"), new Constant(100L, Long.class), new Constant(0L, Long.class)),
                        new IfThenElse(
                                new Literal("ram"), new Constant(20L, Long.class), new Constant(0L, Long.class))),
                new Constant(200L, Long.class));

        assertTrue(test.equalsTree(comparison));
    }

    @Test
    public void test2() {
        IFormula test = new LessThan(new AttributeSum("cost"), new AttributeAverage("power"));
        ReplaceAttributeAggregate replaceAttributeAggregate = new ReplaceAttributeAggregate(attributes, false);
        Trees.traverse(test, replaceAttributeAggregate);

        IFormula comparison = new LessThan(
                new IntegerAdd(
                        new IfThenElse(new Literal("cpu"), new Constant(10L, Long.class), new Constant(0L, Long.class)),
                        new IfThenElse(
                                new Literal("gpu"), new Constant(100L, Long.class), new Constant(0L, Long.class)),
                        new IfThenElse(
                                new Literal("ram"), new Constant(20L, Long.class), new Constant(0L, Long.class))),
                new RealDivide(
                        new RealAdd(
                                new IfThenElse(
                                        new Literal("cpu"),
                                        new Constant(104.5, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new Literal("gpu"),
                                        new Constant(200.5, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new Literal("motherboard"),
                                        new Constant(3.5, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new NotEquals(new Variable("refreshrate", Double.class), new Constant(0.0)),
                                        new Constant(1.0, Double.class),
                                        new Constant(0.0, Double.class))),
                        new RealAdd(
                                new IfThenElse(
                                        new Literal("cpu"),
                                        new Constant(1.0, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new Literal("gpu"),
                                        new Constant(1.0, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new Literal("motherboard"),
                                        new Constant(1.0, Double.class),
                                        new Constant(0.0, Double.class)),
                                new IfThenElse(
                                        new NotEquals(new Variable("refreshrate", Double.class), new Constant(0.0)),
                                        new Constant(1.0, Double.class),
                                        new Constant(0.0, Double.class)))));

        assertTrue(test.equalsTree(comparison));
    }

    @Test
    public void test3() {
        IFormula test = new And(
                new Implies(
                        new Literal("cables"), new LessThan(new AttributeSum("cost"), new Constant(200L, Long.class))),
                new Literal("case"));
        ReplaceAttributeAggregate replaceAttributeAggregate = new ReplaceAttributeAggregate(attributes, false);
        Trees.traverse(test, replaceAttributeAggregate);

        IFormula comparison = new And(
                new Implies(
                        new Literal("cables"),
                        new LessThan(
                                new IntegerAdd(
                                        new IfThenElse(
                                                new Literal("cpu"),
                                                new Constant(10L, Long.class),
                                                new Constant(0L, Long.class)),
                                        new IfThenElse(
                                                new Literal("gpu"),
                                                new Constant(100L, Long.class),
                                                new Constant(0L, Long.class)),
                                        new IfThenElse(
                                                new Literal("ram"),
                                                new Constant(20L, Long.class),
                                                new Constant(0L, Long.class))),
                                new Constant(200L, Long.class))),
                new Literal("case"));

        assertTrue(test.equalsTree(comparison));
    }
}
