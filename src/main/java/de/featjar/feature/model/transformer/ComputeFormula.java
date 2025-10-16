/*
 * Copyright (C) 2025 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-feature-model.
 *
 * feature-model is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * feature-model is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with feature-model. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-feature-model> for further information.
 */
package de.featjar.feature.model.transformer;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.Features;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.term.value.Variable;
import java.util.*;

/**
 * Transforms a feature model into a boolean formula.
 *
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
    protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);
    protected static final Dependency<Boolean> SIMPLE_TRANSLATION = Dependency.newDependency(Boolean.class);

    static Attribute<String> literalNameAttribute = new Attribute<>("literalName", String.class);
    private Boolean hasCardinalityFeatures = Boolean.FALSE;

    public ComputeFormula(IComputation<IFeatureModel> formula) {
        super(formula, Computations.of(Boolean.FALSE));
    }

    protected ComputeFormula(ComputeFormula other) {
        super(other);
    }

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        ArrayList<IFormula> constraints = new ArrayList<>();
        HashSet<Variable> variables = new HashSet<>();
        Map<IFormula, Map<IAttribute<?>, Object>> attributes = new LinkedHashMap<>();

        if (SIMPLE_TRANSLATION.get(dependencyList)) {
            IFeatureTree iFeatureTree = featureModel.getRoots().get(0);

            ComputeSimpleFormulaVisitor simpleVisitor =
                    new ComputeSimpleFormulaVisitor(constraints, variables, attributes);
            Trees.traverse(iFeatureTree, simpleVisitor);

            hasCardinalityFeatures = simpleVisitor.getHasCardinalityFeature();
        } else {
            traverseFeatureModel(featureModel, constraints, variables, attributes);
        }

        ReplaceAttributeAggregate replaceAttributeAggregate =
                new ReplaceAttributeAggregate(attributes, hasCardinalityFeatures);
        featureModel.getConstraints().forEach(constraint -> {
            Trees.traverse(constraint.getFormula(), replaceAttributeAggregate);

            constraints.add(constraint.getFormula());
        });

        Reference reference = new Reference(new And(constraints));
        reference.setFreeVariables(variables);
        return Result.of(reference);
    }

    private void traverseFeatureModel(
            IFeatureModel featureModel,
            ArrayList<IFormula> constraints,
            HashSet<Variable> variables,
            Map<IFormula, Map<IAttribute<?>, Object>> attributes) {

        for (IFeatureTree root : featureModel.getRoots()) {

            // collect the attributes of root
            Variable variable = new Variable(
                    root.getFeature().getName().get(), root.getFeature().getType());
            variables.add(variable);
            if (root.getFeature().getAttributes().isPresent()) {
                attributes.put(Features.createFeatureFormel(root.getFeature()), root.getFeature().getAttributes().get());
            }

            IFormula rootFormula = Features.createFeatureFormel(root.getFeature());
            if (root.isMandatory()) {
                constraints.add(rootFormula);
            }
            handleGroups(rootFormula, root, constraints);

            addChildConstraints(root, constraints, variables, attributes);
        }
    }

    private void addChildConstraints(
            IFeatureTree node,
            ArrayList<IFormula> constraints,
            HashSet<Variable> variables,
            Map<IFormula, Map<IAttribute<?>, Object>> attributes) {

        // collect the attributes of all features
        // TODO: check if the variables need to be duplicated?
        Variable variable = new Variable(
                node.getFeature().getName().get(), node.getFeature().getType());
        variables.add(variable);
        if (node.getFeature().getAttributes().isPresent()) {
            attributes.put(Features.createFeatureFormel(node.getFeature()), node.getFeature().getAttributes().get());
        }

        IFormula parentFormula = Features.createFeatureFormel(node.getFeature(), getFormulaName(node));

        for (IFeatureTree child : node.getChildren()) {

            if (isCardinalityFeature(child)) {
                hasCardinalityFeatures = Boolean.TRUE;

                int upperBound = child.getFeatureCardinalityUpperBound();
                int lowerBound = child.getFeatureCardinalityLowerBound();

                LinkedList<IFormula> constraintGroupFormulas = new LinkedList<>();

                for (int i = 1; i <= upperBound; i++) {

                    String formulaName = getFormulaName(child) + "_" + i;
                    if (cardinalityFeatureAbove(child)) {
                        formulaName += "." + getFormulaName(node);
                    }

                    // clone only tree for traversal, not its children
                    IFeatureTree cardinalityClone = child.cloneTree();
                    cardinalityClone.mutate().setAttributeValue(literalNameAttribute, formulaName);

                    IFormula currentFormula = Features.createFeatureFormel(child.getFeature(), formulaName);

                    // add all the constraints
                    // imply parent
                    constraints.add(new Implies(currentFormula, parentFormula));
                    // implication chain part
                    if (i > 1) {
                        IFormula previousFormula = constraintGroupFormulas.getLast();
                        constraints.add(new Implies(currentFormula, previousFormula));
                    }
                    // group constraints
                    handleGroups(currentFormula, cardinalityClone, constraints);

                    constraintGroupFormulas.add(currentFormula);

                    addChildConstraints(cardinalityClone, constraints, variables, attributes);
                }
                // check if 0 and do not add implication
                if (lowerBound != 0)
                    constraints.add(new Implies(parentFormula, new AtLeast(lowerBound, constraintGroupFormulas)));

                return;
            } else {

                String formulaName = getFormulaName(child);
                if (cardinalityFeatureAbove(child)) {
                    formulaName += "." + getFormulaName(node);
                }

                IFormula childFeatureFormula = Features.createFeatureFormel(child.getFeature(), formulaName);
                child.mutate().setAttributeValue(literalNameAttribute, formulaName);

                // add constraints
                // always add parent implications (child implies parent)
                constraints.add(new Implies(childFeatureFormula, parentFormula));

                // handle group
                handleGroups(childFeatureFormula, child, constraints);

                addChildConstraints(child, constraints, variables, attributes);
            }
        }
    }

    private String getFormulaName(IFeatureTree node) {
        String literalName = "";
        if (node.getAttributeValue(literalNameAttribute).isEmpty()) {
            literalName = node.getFeature().getName().orElse("");
        } else {
            literalName = node.getAttributeValue(literalNameAttribute).orElse("");
        }
        return literalName;
    }

    private boolean cardinalityFeatureAbove(IFeatureTree child) {

        if (!child.getParent().isPresent()) return false;

        if (isCardinalityFeature(child.getParent().get())) {
            return true;
        } else {
            return cardinalityFeatureAbove(child.getParent().get());
        }
    }

    private boolean isCardinalityFeature(IFeatureTree node) {

        if (node.getFeatureCardinalityUpperBound() > 1) {
            return true;
        }
        return false;
    }

   // private void handleGroups(ArrayList<IFormula> constraints, IFormula featureLiteral, IFeatureTree node) {
    private void handleGroups(IFormula featureFormula, IFeatureTree node, ArrayList<IFormula> constraints) {
        List<Group> childrenGroups = node.getChildrenGroups();
        int groupCount = childrenGroups.size();
        ArrayList<List<IFormula>> groupFormulas = new ArrayList<>(groupCount);

        for (int i = 0; i < groupCount; i++) {
            groupFormulas.add(null);
        }

        List<? extends IFeatureTree> children = node.getChildren();
        for (IFeatureTree childNode : children) {

            String childFormulaName = getFormulaName(childNode);
            if (childNode.getAttributeValue(literalNameAttribute).isEmpty() && cardinalityFeatureAbove(childNode))
                childFormulaName += "." + getFormulaName(node);

            IFormula childFormula = Features.createFeatureFormel(childNode.getFeature(), childFormulaName);

            if (childNode.isMandatory()) {
                constraints.add(new Implies(featureFormula, childFormula));
            }

            int groupID = childNode.getParentGroupID();
            List<IFormula> list = groupFormulas.get(groupID);
            if (list == null) {
                groupFormulas.set(groupID, list = new ArrayList<>());
            }
            list.add(childFormula);
        }
        for (int i = 0; i < groupCount; i++) {
            Group group = childrenGroups.get(i);
            if (group != null) {
                if (group.isOr()) {
                    constraints.add(new Implies(featureFormula, new Or(groupFormulas.get(i))));
                } else if (group.isAlternative()) {
                    constraints.add(new Implies(featureFormula, new Choose(1, groupFormulas.get(i))));
                } else {
                    int lowerBound = group.getLowerBound();
                    int upperBound = group.getUpperBound();
                    if (lowerBound > 0) {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(
                                    featureFormula, new Between(lowerBound, upperBound, groupFormulas.get(i))));
                        } else {
                            constraints.add(new Implies(featureFormula, new AtMost(upperBound, groupFormulas.get(i))));
                        }
                    } else {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(featureFormula, new AtLeast(lowerBound, groupFormulas.get(i))));
                        }
                    }
                }
            }
        }
    }
}
