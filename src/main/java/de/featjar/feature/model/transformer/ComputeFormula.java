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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Computations;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Attribute;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.FeatureTree.Group;
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
import de.featjar.formula.structure.predicate.Literal;
import de.featjar.formula.structure.term.value.Variable;

/**
 * Transforms a feature model into a boolean formula.
 *
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
	protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);
	protected static final Dependency<Boolean> SIMPLE_TRANSLATION = Dependency.newDependency(Boolean.class);

	static Attribute<String> literalNameAttribute = new Attribute<>("literalName", String.class);

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

		IFeatureTree iFeatureTree = featureModel.getRoots().get(0);

		if (SIMPLE_TRANSLATION.get(dependencyList)) {
			Trees.traverse(iFeatureTree, new ComputeSimpleFormulaVisitor(constraints, variables));
		} else {
			traverseFeatureModel(featureModel, constraints, variables);
		}

		Reference reference = new Reference(new And(constraints));
		reference.setFreeVariables(variables);
		return Result.of(reference);
	}

	private void traverseFeatureModel(IFeatureModel featureModel, ArrayList<IFormula> constraints,
			HashSet<Variable> variables) {

		for (IFeatureTree root : featureModel.getRoots()) {
			
			Literal rootLiteral = new Literal(root.getFeature().getName().orElse(""));
			if(root.isMandatory()) {				
				constraints.add(rootLiteral);
			}
			handleGroups(rootLiteral, root, constraints);
			
			addChildConstraints(root, constraints);
		}
	}

	private void addChildConstraints(IFeatureTree node, ArrayList<IFormula> constraints) {

		Literal parentLiteral = new Literal(getLiteralName(node));

		for (IFeatureTree child : node.getChildren()) {

			if (isCardinalityFeature(child)) {

				int upperBound = child.getFeatureCardinalityUpperBound();
				int lowerBound = child.getFeatureCardinalityLowerBound();

				LinkedList<Literal> constraintGroupLiterals = new LinkedList<Literal>();

				for (int i = 1; i <= upperBound; i++) {

					String literalName = getLiteralName(child) + "_" + i;
					if (cardinalityFeatureAbove(child)) {
						literalName += "." + getLiteralName(node);
					}

					// clone only tree for traversal, not its children
					IFeatureTree cardinalityClone = child.cloneTree();
					cardinalityClone.mutate().setAttributeValue(literalNameAttribute, literalName);

					Literal currentLiteral = new Literal(literalName);

					// add all the constraints
					// imply parent
					constraints.add(new Implies(currentLiteral, parentLiteral));
					// implication chain part
					if (i > 1) {
						Literal previousLiteral = constraintGroupLiterals.getLast();
						constraints.add(new Implies(currentLiteral, previousLiteral));
					}
					// group constraints
					handleGroups(currentLiteral, cardinalityClone, constraints);

					constraintGroupLiterals.add(currentLiteral);

					addChildConstraints(cardinalityClone, constraints);
				}
				// check if 0 and do not add implication
				if(lowerBound != 0)
					constraints.add(new Implies(parentLiteral, new AtLeast(lowerBound, constraintGroupLiterals)));

				return;
			} else {

				String literalName = getLiteralName(child);
				if (cardinalityFeatureAbove(child)) {
					literalName += "." + getLiteralName(node);
				}

				Literal childFeatureLiteral = new Literal(literalName);
				child.mutate().setAttributeValue(literalNameAttribute, literalName);

				// add constraints
				// always add parent implications (child implies parent)
				constraints.add(new Implies(childFeatureLiteral, parentLiteral));

				// handle group
				handleGroups(childFeatureLiteral, child, constraints);

				addChildConstraints(child, constraints);
			}
		}
	}

	private String getLiteralName(IFeatureTree node) {
		String literalName = "";
		if (node.getAttributeValue(literalNameAttribute).isEmpty()) {
			literalName = node.getFeature().getName().orElse("");
		} else {
			literalName = node.getAttributeValue(literalNameAttribute).orElse("");
		}
		return literalName;
	}

	private boolean cardinalityFeatureAbove(IFeatureTree child) {

		if (!child.getParent().isPresent())
			return false;

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

	private void handleGroups(Literal featureLiteral, IFeatureTree node, ArrayList<IFormula> constraints) {
		List<Group> childrenGroups = node.getChildrenGroups();
		int groupCount = childrenGroups.size();
		ArrayList<List<IFormula>> groupLiterals = new ArrayList<>(groupCount);
		for (int i = 0; i < groupCount; i++) {
			groupLiterals.add(null);
		}
		List<? extends IFeatureTree> children = node.getChildren();
		for (IFeatureTree childNode : children) {

			String childLiteralName = getLiteralName(childNode);
			if (childNode.getAttributeValue(literalNameAttribute).isEmpty() && cardinalityFeatureAbove(childNode))
				childLiteralName += "." + getLiteralName(node);

			Literal childLiteral = new Literal(childLiteralName);

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
							constraints.add(new Implies(featureLiteral,
									new Between(lowerBound, upperBound, groupLiterals.get(i))));
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
