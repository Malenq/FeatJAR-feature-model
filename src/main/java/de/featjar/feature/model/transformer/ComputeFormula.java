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
import java.util.List;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Dependency;
import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.base.tree.Trees;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.term.value.Variable;

/**
 * Transforms a feature model into a boolean formula.
 *
 * @author Sebastian Krieter
 */
public class ComputeFormula extends AComputation<IFormula> {
	protected static final Dependency<IFeatureModel> FEATURE_MODEL = Dependency.newDependency(IFeatureModel.class);
	protected boolean doSimpleCardinalityTranslation = false;

    public ComputeFormula(IComputation<IFeatureModel> formula) {
        super(formula);
    }

    protected ComputeFormula(ComputeFormula other) {
        super(other);
    }
    
    public void SetSimple() {
    	doSimpleCardinalityTranslation = true;
    }
    

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        IFeatureModel featureModel = FEATURE_MODEL.get(dependencyList);
        ArrayList<IFormula> constraints = new ArrayList<>();
        HashSet<Variable> variables = new HashSet<>();
        
        IFeatureTree iFeatureTree = featureModel.getRoots().get(0);
        
        if(doSimpleCardinalityTranslation) {
        	Trees.traverse(iFeatureTree, new ComputeSimpleFormulaVisitor(constraints, variables));        	
        }
        else {
        	Trees.traverse(iFeatureTree, new ComputeFormulaVisitor(constraints, variables));
        }
        
//        featureModel.getFeatureTreeStream().forEach(node -> {
//            // TODO use better error value
//            IFeature feature = node.getFeature();
//            String featureName = feature.getName().orElse("");
//            Variable variable = new Variable(featureName, feature.getType());
//            variables.add(variable);
//
//            // TODO take featureRanges into Account
//            Result<IFeatureTree> potentialParentTree = node.getParent();
//            Literal featureLiteral = Expressions.literal(featureName);
//            if (potentialParentTree.isEmpty()) {
//                handleRoot(constraints, featureLiteral, node);
//            } 
//            else {
//                handleParent(constraints, featureLiteral, node);
//            }
//
//            if(node.getFeatureCardinalityUpperBound() > 1) {
//            	handleCardinalityFeature(constraints, featureLiteral, node);
//            }
//            handleGroups(constraints, featureLiteral, node);
//        });
        Reference reference = new Reference(new And(constraints));
        reference.setFreeVariables(variables);
        return Result.of(reference);
    }

}
