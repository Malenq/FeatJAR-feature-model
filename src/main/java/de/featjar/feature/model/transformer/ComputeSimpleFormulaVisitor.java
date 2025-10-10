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

import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.AtMost;
import de.featjar.formula.structure.connective.Between;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * This visitor implements a simple translation of IFeatureModel to boolean
 * formula. In this implementation, a cardinality feature can not be a parent.
 * The next non-cardinality feature will be the parent instead within the
 * boolean representation.
 */
public class ComputeSimpleFormulaVisitor implements ITreeVisitor<IFeatureTree, Void> {

    protected ArrayList<IFormula> constraints = new ArrayList<>();
    protected HashSet<Variable> variables = new HashSet<>();

    public ComputeSimpleFormulaVisitor(ArrayList<IFormula> constraints, HashSet<Variable> variables) {

        this.constraints = constraints;
        this.variables = variables;
    }

    @Override
    public TraversalAction firstVisit(List<IFeatureTree> path) {
        IFeatureTree node = ITreeVisitor.getCurrentNode(path);

        // TODO use better error value
        IFeature feature = node.getFeature();
        String featureName = feature.getName().orElse("");
        // TODO: do not add variable if its a cardinality var. Add duplicates instead
        Variable variable = new Variable(featureName, feature.getType());
        variables.add(variable);

        // TODO take featureRanges into Account
        Result<IFeatureTree> potentialParentTree = node.getParent();
        Literal featureLiteral = Expressions.literal(featureName);
        if (potentialParentTree.isEmpty()) {
            handleRoot(featureLiteral, node);
        } else if (node.getFeatureCardinalityUpperBound() > 1) {
            handleCardinalityFeature(featureLiteral, node);
        } else {
            handleParent(featureLiteral, node);
        }

        handleGroups(featureLiteral, node);

        return ITreeVisitor.super.firstVisit(path);
    }

    private void handleParent(Literal featureLiteral, IFeatureTree node) {
        // cardinal features must not be a parent
        Literal parentLiteral = getNextNonCardinalityParent(node);
        constraints.add(new Implies(featureLiteral, parentLiteral));
    }

    private void handleRoot(Literal featureLiteral, IFeatureTree node) {
        if (node.isMandatory()) {
            constraints.add(featureLiteral);
        }
    }

    private void handleCardinalityFeature(Literal featureLiteral, IFeatureTree node) {

        int lowerBound = node.getFeatureCardinalityLowerBound();
        int upperBound = node.getFeatureCardinalityUpperBound();

        ArrayList<IFormula> originalFeatureConstraints = getConstraints(node);
        constraints.removeAll(originalFeatureConstraints);

        ArrayList<IFormula> featureConstraintList = new ArrayList<IFormula>();

        // add literals and implication to parent
        String literalName = "";
        Literal parentLiteral = getNextNonCardinalityParent(node);
        for (int i = 1; i <= upperBound; i++) {

            literalName = node.getFeature().getName().get() + "_" + i;
            featureLiteral = new Literal(literalName);
            handleParent(featureLiteral, node);

            if (i > 1) {
                // add to implication chain
                IFormula previousLiteral = featureConstraintList.get(featureConstraintList.size() - 1);
                constraints.add(new Implies(featureLiteral, previousLiteral));
            }

            featureConstraintList.add(featureLiteral);
        }

        // replace original feature with or of newly created cardinality feature pseudo literals
        IFormula replacement = new Or(featureConstraintList);
        for (IFormula constr : originalFeatureConstraints) {
            constr.replaceChild(new Literal(node.getFeature().getName().get()), replacement);
            constraints.add(constr);
        }

        // add cardinality constraint
        // check if 0 and do not add implication
        if (lowerBound != 0)
            constraints.add(new Implies(parentLiteral, new AtLeast(lowerBound, featureConstraintList)));
    }

    private ArrayList<IFormula> getConstraints(IFeatureTree node) {

        ArrayList<IFormula> matchingConstraints = new ArrayList<IFormula>();
        for (IFormula constraint : constraints) {

            Literal nodeLiteral = new Literal(node.getFeature().getName().get());

            if (constraint.hasChild(nodeLiteral) || constraint.equals(nodeLiteral)) {
                matchingConstraints.add(constraint);
            }
        }
        return matchingConstraints;
    }

    private Literal getNextNonCardinalityParent(IFeatureTree node) {

        // if it is possible that root can be as well a cardinality feature - there must
        // be an alternative
        node = node.getParent().get();

        if (node.getFeatureCardinalityUpperBound() > 1) {
            return getNextNonCardinalityParent(node);
        }

        return Expressions.literal(node.getFeature().getName().orElse(""));
    }

    private void handleGroups(Literal featureLiteral, IFeatureTree node) {
        List<Group> childrenGroups = node.getChildrenGroups();
        int groupCount = childrenGroups.size();
        ArrayList<List<IFormula>> groupLiterals = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) {
            groupLiterals.add(null);
        }

        // if node is cardinality feature, set feature literal to parent with no
        // cardinality
        if (node.getFeatureCardinalityUpperBound() > 1) {
            featureLiteral = getNextNonCardinalityParent(node);
        }

        List<? extends IFeatureTree> children = node.getChildren();
        for (IFeatureTree childNode : children) {
            Literal childLiteral =
                    Expressions.literal(childNode.getFeature().getName().orElse(""));

            if (childNode.isMandatory()) {
                constraints.add(new Implies(featureLiteral, childLiteral));
            }

            int groupID = childNode.getParentGroupID();
            List<IFormula> list = groupLiterals.get(groupID);
            if (list == null) {
                groupLiterals.set(groupID, list = new ArrayList<>());
            }
            list.add(childLiteral);
        }

        for (int i = 0; i < groupCount; i++) {
            Group group = childrenGroups.get(i);
            if (group != null) {
                if (group.isOr()) {
                    constraints.add(new Implies(featureLiteral, new Or(groupLiterals.get(i))));
                } else if (group.isAlternative()) {
                    constraints.add(new Implies(featureLiteral, new Choose(1, groupLiterals.get(i))));
                } else {
                    int lowerBound = group.getLowerBound();
                    int upperBound = group.getUpperBound();
                    if (lowerBound > 0) {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(
                                    featureLiteral, new Between(lowerBound, upperBound, groupLiterals.get(i))));
                        } else {
                            constraints.add(new Implies(featureLiteral, new AtMost(upperBound, groupLiterals.get(i))));
                        }
                    } else {
                        if (upperBound != Range.OPEN) {
                            constraints.add(new Implies(featureLiteral, new AtLeast(lowerBound, groupLiterals.get(i))));
                        }
                    }
                }
            }
        }
    }
}
