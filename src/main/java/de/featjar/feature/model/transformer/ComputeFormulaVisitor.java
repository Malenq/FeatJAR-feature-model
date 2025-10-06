package de.featjar.feature.model.transformer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

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

public class ComputeFormulaVisitor implements ITreeVisitor<IFeatureTree, Void> {
	
	protected ArrayList<IFormula> constraints = new ArrayList<>();
	protected HashSet<Variable> variables = new HashSet<>();
	protected Stack<CardinalityObject> cardinalityStack = new Stack<>();

	public ComputeFormulaVisitor(ArrayList<IFormula> constraints, HashSet<Variable> variables) {

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
        } 
        else if(node.getFeatureCardinalityUpperBound() > 1) {
        	handleCardinalityFeature(featureLiteral, node);
        }
        else{
            handleParent(featureLiteral, node);
        }

        handleGroups(featureLiteral, node);
        
        
        return ITreeVisitor.super.firstVisit(path);
	}
	
	@Override
	public TraversalAction lastVisit(List<IFeatureTree> path) {
		// TODO Auto-generated method stub
		return ITreeVisitor.super.lastVisit(path);
	}
	
    private void handleParent(Literal featureLiteral, IFeatureTree node) {
        constraints.add(new Implies(
                featureLiteral,
                Expressions.literal(
                        node.getParent().get().getFeature().getName().orElse(""))));
    }

    private void handleRoot(Literal featureLiteral, IFeatureTree node) {
        if (node.isMandatory()) {
            constraints.add(featureLiteral);
        }
    }
    
    private void handleCardinalityFeature(Literal featureLiteral, IFeatureTree node) {
    	
    	// step 0: check stack
    	if (cardinalityStack.empty()) {
    		int lowerBound = node.getFeatureCardinalityLowerBound();
        	int upperBound = node.getFeatureCardinalityUpperBound();
        	
        	ArrayList<IFormula> featureList = new ArrayList<IFormula>();
        	
        	// step 2: add literals and implication to parent
        	String literalName = "";
        	for (int i = 1; i<= upperBound; i++) {
        		
        		literalName = node.getFeature().getName().get() + "_" + i;
        		featureLiteral = new Literal(literalName);
        		handleParent(featureLiteral, node);
        		
        		if (i > 1) {
        			// step 3: add to implication chain
        			IFormula previousLiteral = featureList.get(featureList.size()-1);
        			constraints.add(new Implies(featureLiteral, previousLiteral));
        		}
        		
        		featureList.add(featureLiteral);
        	}
        	
        	// step 4: add cardinality constraint
        	// TODO: feature literal must be parent!
        	if (lowerBound > 0) {
                if (upperBound != Range.OPEN) {
                    constraints.add(new Implies(
                    		featureLiteral, new Between(lowerBound, upperBound, featureList)));
                } else {
                    constraints.add(new Implies(featureLiteral, new AtMost(upperBound, featureList)));
                }
            } else {
                if (upperBound != Range.OPEN) {
                    constraints.add(new Implies(featureLiteral, new AtLeast(lowerBound, featureList)));
                }
            }
    	} else {
    		int lowerBound = node.getFeatureCardinalityLowerBound();
        	int upperBound = node.getFeatureCardinalityUpperBound();
    		
    		CardinalityObject parentCardObject = cardinalityStack.pop();
        	int parentUpperBound = parentCardObject.getNumber();
        	
        	String literalName = "";
        	for (int i = 1; i <= parentUpperBound; i++) {
        		
        		ArrayList<IFormula> featureList = new ArrayList<IFormula>();
        		for (int j = 1; j <= upperBound; j++) {
            		
            		literalName = node.getFeature().getName().get() + "_" + i + "_" + j;
            		featureLiteral = new Literal(literalName);
            		handleParent(featureLiteral, node);
            		
            		if (j > 1) {
            			// step 3: add to implication chain
            			IFormula previousLiteral = featureList.get(featureList.size()-1);
            			constraints.add(new Implies(featureLiteral, previousLiteral));
            		}
            		
            		featureList.add(featureLiteral);
            	}
        		
        		// step 4: add cardinality constraint
            	if (lowerBound > 0) {
                    if (upperBound != Range.OPEN) {
                        constraints.add(new Implies(
                        		featureLiteral, new Between(lowerBound, upperBound, featureList)));
                    } else {
                        constraints.add(new Implies(featureLiteral, new AtMost(upperBound, featureList)));
                    }
                } else {
                    if (upperBound != Range.OPEN) {
                        constraints.add(new Implies(featureLiteral, new AtLeast(lowerBound, featureList)));
                    }
                }
        		
        	}
        	
    	}
    	
    
    	// step 1: add cardinality feature to stack:
    	cardinalityStack.add(new CardinalityObject(node.getFeature().getName().get(), node.getFeatureCardinalityUpperBound()));
    	

    }

    private void handleGroups(Literal featureLiteral, IFeatureTree node) {
        List<Group> childrenGroups = node.getChildrenGroups();
        int groupCount = childrenGroups.size();
        ArrayList<List<IFormula>> groupLiterals = new ArrayList<>(groupCount);
        for (int i = 0; i < groupCount; i++) {
            groupLiterals.add(null);
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
