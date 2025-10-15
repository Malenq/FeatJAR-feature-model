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
import de.featjar.feature.model.FeatureTree;
import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeature;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;

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
        
        Map<IFeature, List<IFeatureTree>> featureToCardinalityNames = new HashMap<>(); 
        Map<IFeatureTree, Map<IFeature, List<IFeatureTree>>> featureToChildren = new HashMap<>();
        
        // <<<<<<< HEAD

        IFeatureTree iFeatureTree = featureModel.getRoots().get(0);

        Collection<IConstraint> crossTreeConstr = featureModel.getConstraints();
        for (IConstraint constr : crossTreeConstr) {
        	constraints.add(constr.getFormula());
        }
        
        Map<IFeatureTree, List<IConstraint>> localConstraints = findLocalConstraints(crossTreeConstr);
        
        if (SIMPLE_TRANSLATION.get(dependencyList)) {
            Trees.traverse(iFeatureTree, new ComputeSimpleFormulaVisitor(constraints, variables));
        } else {
            traverseFeatureModel(featureModel, constraints, variables, featureToCardinalityNames, featureToChildren);
            List<IConstraint> transformedConstraints = transformLocalConstraints(localConstraints, featureToCardinalityNames, featureToChildren);
        }
        // =======
        //        Map<Variable, Map<IAttribute<?>, Object>> attributes = new LinkedHashMap<>();
        //
        //        featureModel.getFeatureTreeStream().forEach(node -> {
        //            // TODO use better error value
        //            IFeature feature = node.getFeature();
        //            String featureName = feature.getName().orElse("");
        //            Variable variable = new Variable(featureName, feature.getType());
        //            variables.add(variable);
        //
        //            if(node.getAttributes().isPresent()) {
        //                attributes.put(variable, node.getAttributes().get());
        //            }
        //
        //            // TODO take featureRanges into Account
        //            Result<IFeatureTree> potentialParentTree = node.getParent();
        //            Literal featureLiteral = Expressions.literal(featureName);
        //            if (potentialParentTree.isEmpty()) {
        //                handleRoot(constraints, featureLiteral, node);
        //            } else {
        //                handleParent(constraints, featureLiteral, node);
        //            }
        //            handleGroups(constraints, featureLiteral, node);
        //        });
        //
        //        ReplaceAttributeAggregate replaceAttributeAggregate = new ReplaceAttributeAggregate(attributes);
        //        featureModel.getConstraints().forEach(constraint -> {
        //            Trees.traverse(constraint.getFormula(), replaceAttributeAggregate);
        //            constraints.add(constraint.getFormula());
        //        });
        // >>>>>>> main_malenq

        Reference reference = new Reference(new And(constraints));
        reference.setFreeVariables(variables);
        return Result.of(reference);
    }

    private void traverseFeatureModel(
            IFeatureModel featureModel, ArrayList<IFormula> constraints, HashSet<Variable> variables, Map<IFeature, List<IFeatureTree>> featureToCardinalityNames, 
            	Map<IFeatureTree, Map<IFeature, List<IFeatureTree>>> featureToChildren ) {

        for (IFeatureTree root : featureModel.getRoots()) {

            Literal rootLiteral = new Literal(root.getFeature().getName().orElse(""));
            if (root.isMandatory()) {
                constraints.add(rootLiteral);
            }
            handleGroups(rootLiteral, root, constraints);

            addChildConstraints(root, constraints, featureToCardinalityNames, featureToChildren);
        }
    }

    private void addChildConstraints(IFeatureTree node, ArrayList<IFormula> constraints, Map<IFeature, List<IFeatureTree>> featureToCardinalityNames,
    		Map<IFeatureTree, Map<IFeature, List<IFeatureTree>>> featureToChildren ) {

        Literal parentLiteral = new Literal(getLiteralName(node));
        
        
       // create a new entry for the node in featureToChildren in order to store its children
//       if (!featureToChildren.containsKey(node)) {
//    	   featureToChildren.put(node, new HashMap<IFeature, IFeatureTree>());
//       }
        featureToChildren.computeIfAbsent(node, k -> new HashMap<>());


        for (IFeatureTree child : node.getChildren()) {
        	
            if (isCardinalityFeature(child)) {
            	
            	int upperBound = child.getFeatureCardinalityUpperBound();
                int lowerBound = child.getFeatureCardinalityLowerBound();
                
                List<IFeatureTree> children = (List<IFeatureTree>) node.getChildren();

                LinkedList<Literal> constraintGroupLiterals = new LinkedList<Literal>();
                
                
                // add an entry for the current cardinality feature to featureToCardinalityNames
//                if (!featureToCardinalityNames.containsKey(child.getFeature())) {
//             	   featureToCardinalityNames.put(child.getFeature(), new ArrayList<IFeatureTree>());
//                }
                List<IFeatureTree> cardinalityInstances = featureToCardinalityNames.computeIfAbsent(
                		child.getFeature(), k -> new ArrayList<>());
                

                for (int i = 1; i <= upperBound; i++) {

                    String literalName = getLiteralName(child) + "_" + i;
                    if (cardinalityFeatureAbove(child)) {
                        literalName += "." + getLiteralName(node);
                    }
                    
                    // clone only tree for traversal, not its children
                    IFeatureTree cardinalityClone = child.cloneTree();
                    cardinalityClone.mutate().setAttributeValue(literalNameAttribute, literalName);

                    Literal currentLiteral = new Literal(literalName);
                    
                    
                    // add the children to their parent in featureToChildren
//                    if (featureToChildren.containsKey(node)) {
//                    	featureToChildren.get(node).put(child.getFeature(), cardinalityClone);
//                    }
                    Map<IFeature, List<IFeatureTree>> childrenOfNode = featureToChildren.computeIfAbsent(
                    		node, k -> new HashMap<>());
                    List<IFeatureTree> childInstances = childrenOfNode.computeIfAbsent(
                    		child.getFeature(), k -> new ArrayList<>());
                    childInstances.add(cardinalityClone);
                    
                    // and map them to the feature name without cardinality
                    cardinalityInstances.add(cardinalityClone);
                    
                    
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

                    addChildConstraints(cardinalityClone, constraints, featureToCardinalityNames, featureToChildren);
                    
                }
       
                // check if 0 and do not add implication
                if (lowerBound != 0)
                    constraints.add(new Implies(parentLiteral, new AtLeast(lowerBound, constraintGroupLiterals)));

            } else {

                String literalName = getLiteralName(child);
                if (cardinalityFeatureAbove(child)) {
                    literalName += "." + getLiteralName(node);
                }

                Literal childFeatureLiteral = new Literal(literalName);
                child.mutate().setAttributeValue(literalNameAttribute, literalName);
                
                
                // add the children to their parent in featureToChildren
//                if (featureToChildren.containsKey(node)) {
//                	featureToChildren.get(node).put(child.getFeature(), child);
//                }
                Map<IFeature, List<IFeatureTree>> childrenOfNode = featureToChildren.computeIfAbsent(
                		node, k -> new HashMap<>());
                List<IFeatureTree> childInstances = childrenOfNode.computeIfAbsent(
                		child.getFeature(), k -> new ArrayList<>());
                childInstances.add(child); 
                
                
                // add constraints
                // always add parent implications (child implies parent)
                constraints.add(new Implies(childFeatureLiteral, parentLiteral));

                // handle group
                handleGroups(childFeatureLiteral, child, constraints);

                addChildConstraints(child, constraints, featureToCardinalityNames, featureToChildren);
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
    
    private IFeatureTree getNextCardinalityParent(IFeatureTree node) {

        if (!node.getParent().isPresent()) return null;

        if (isCardinalityFeature(node.getParent().get())) {
            return node.getParent().get();
        } else {
            return getNextCardinalityParent(node.getParent().get());
        }
    }
    
    private ArrayList<IFormula> getConstraints(IFeatureTree node, ArrayList<IFormula> constraints) {
        ArrayList<IFormula> matchingConstraints = new ArrayList<IFormula>();
        for (IFormula constraint : constraints) {

            Literal nodeLiteral = new Literal(node.getFeature().getName().get());

            if (constraint.hasChild(nodeLiteral) || constraint.equals(nodeLiteral)) {
                matchingConstraints.add(constraint);
            }
        }
        return matchingConstraints;
    }
    
    private IFeatureTree getCommonCardinalityParent(IConstraint constraint) {
    	
    	// identify the contained features 
    	LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();
    		
    	// find the first ancestor with cardinality for each feature 
    	Iterator<IFeature> iterator = features.iterator();
    	
    	IFeatureTree referenceParent = getNextCardinalityParent(iterator.next().getFeatureTree().get());
    	
    	while (iterator.hasNext()) {
            IFeatureTree currentCardinalityParent = getNextCardinalityParent(iterator.next().getFeatureTree().get());

            // no common cardinality parent if two features do not have the same parent
            if (!Objects.equals(referenceParent, currentCardinalityParent)) {
                return null;
            }
        }
    		
    	return referenceParent;
    }
    
    private Map<IFeatureTree, List<IConstraint>> findLocalConstraints(Collection<IConstraint> constraints) {
    	Map<IFeatureTree, List<IConstraint>> localConstraints = new HashMap<>();
    	
    	for (IConstraint constraint : constraints) {
    		IFeatureTree commonCardinalityParent = getCommonCardinalityParent(constraint);
    		if (commonCardinalityParent != null) {
    			if (!localConstraints.containsKey(commonCardinalityParent)) {
                    localConstraints.put(commonCardinalityParent, new ArrayList<>());
                }
               
                localConstraints.get(commonCardinalityParent).add(constraint);
    		}
    	}
    	
    	return localConstraints;
    }
    
    private List<IFeatureTree> findContextualFeatureNames(IFeatureTree contextFeature, IFeature targetFeature, 
    		Map<IFeatureTree, Map<IFeature, List<IFeatureTree>>> featureToChildren) {
    	
    	List<IFeatureTree> contextualFeatureNames = new ArrayList<>();
    	
    	Queue<IFeatureTree> nodesToVisit = new LinkedList<>();
        nodesToVisit.add(contextFeature);
        
        while (!nodesToVisit.isEmpty()) {
            IFeatureTree currentNode = nodesToVisit.poll();

            Map<IFeature, List<IFeatureTree>> childrenMap = featureToChildren.get(currentNode);
            if (childrenMap == null) {
                continue;
            }

            for (Map.Entry<IFeature, List<IFeatureTree>> entry : childrenMap.entrySet()) {
                IFeature originalChildFeature = entry.getKey();
                List<IFeatureTree> childInstances = entry.getValue();

                for (IFeatureTree childInstance : childInstances) {
          
                    if (originalChildFeature.equals(targetFeature)) {
                        contextualFeatureNames.add(childInstance);
                    }
                 
                    nodesToVisit.add(childInstance);
                }
            }
        }

        return contextualFeatureNames;
    
    }
    
    private IFormula createOrFromFeatureTrees(List<IFeatureTree> featureTrees) {
    	List<Literal> featureLiterals = new ArrayList<>();
    	
    	for (IFeatureTree featureTree : featureTrees) {
    		String literalName = featureTree.getAttributeValue(literalNameAttribute).orElse("");;
    		Literal featureLiteral = new Literal(literalName);
    		featureLiterals.add(featureLiteral);
    	}
    	
    	return new Or(featureLiterals);
    }
    
    private List<IConstraint> transformLocalConstraints(Map<IFeatureTree, List<IConstraint>> localConstraints, 
    		Map<IFeature, List<IFeatureTree>> featureToCardinalityNames, Map<IFeatureTree, Map<IFeature, List<IFeatureTree>>> featureToChildren) {
    	List<IConstraint> finalConstraints = new ArrayList<>();
    	
    	for (Map.Entry<IFeatureTree, List<IConstraint>> entry : localConstraints.entrySet()) {
    		IFeatureTree contextOriginalFeature = entry.getKey();
    		List<IConstraint> constraints = entry.getValue();
    		
    		List<IFeatureTree> contextFeatureNames = featureToCardinalityNames.get(contextOriginalFeature.getFeature());
    		
    	    
    		for (IConstraint constraint : constraints) {
    			for (IFeatureTree contextFeatureName : contextFeatureNames) {
    				IConstraint modifiedConstraint = constraint.clone();
    				LinkedHashSet<IFeature> features = constraint.getReferencedFeatures();
    			
    				for (IFeature feature : features) {
    					List<IFeatureTree> contextualFeatureNames = findContextualFeatureNames(contextFeatureName, feature, featureToChildren);
    					IFormula replacement = createOrFromFeatureTrees(contextualFeatureNames);
    					modifiedConstraint.getFormula().replaceChild(new Literal(feature.getName().get()), replacement);
    				}
    				
    				IFormula formula = modifiedConstraint.getFormula();
    				IFormula contextFormula = new Implies(new Literal(contextFeatureName.getAttributeValue(literalNameAttribute).orElse("")), formula);
    				modifiedConstraint.mutate().setFormula(contextFormula);
    				
    				finalConstraints.add(modifiedConstraint);
    			}
    		}
       }
    	
      return finalConstraints;
   }
    
}


