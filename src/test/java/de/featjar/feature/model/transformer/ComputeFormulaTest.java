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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.data.Range;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.AtLeast;
import de.featjar.formula.structure.connective.Choose;
import de.featjar.formula.structure.connective.Implies;
import de.featjar.formula.structure.connective.Or;
import de.featjar.formula.structure.connective.Reference;
import de.featjar.formula.structure.predicate.Literal;

class ComputeFormulaTest {
    private IFeatureModel featureModel;
    private IFormula expected;

    @BeforeEach
    public void createFeatureModel() {
        featureModel = new FeatureModel(Identifiers.newCounterIdentifier());
    }

    @Test
    void simpleWithTwoCardinalies() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        IFeatureTree childFeature2Tree = childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        childFeature2Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        expected = new Reference(new And(
//        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2")))),
        		
        		new Implies(new Literal("B_1"), new Literal("root")),
        		new Implies(new Literal("B_2"), new Literal("root")),
        		new Implies(new Literal("B_2"), new Literal("B_1")),
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("B_1"), new Literal("B_2"))))
        		));
    	
        executeSimpleTest();  
    	
    }
    
    @Test
    void withTwoCardinaliesRecursive() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        IFeatureTree childFeature2Tree = childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        childFeature2Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        expected = new Reference(new And(
        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("B_1.A_1"), new Literal("A_1")),
        		new Implies(new Literal("B_2.A_1"), new Literal("A_1")),
        		new Implies(new Literal("B_2.A_1"), new Literal("B_1.A_1")),
        		new Implies(new Literal("A_1"), new AtLeast(0, Arrays.asList(new Literal("B_1.A_1"), new Literal("B_2.A_1")))),
        		
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("B_1.A_2"), new Literal("A_2")),
        		new Implies(new Literal("B_2.A_2"), new Literal("A_2")),
        		new Implies(new Literal("B_2.A_2"), new Literal("B_1.A_2")),
        		new Implies(new Literal("A_2"), new AtLeast(0, Arrays.asList(new Literal("B_1.A_2"), new Literal("B_2.A_2")))),
        		
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2"))))
        		));

        executeRecursiveTest();
    	
    }
    
    @Test
    void simpleWithCardinalityAndChildGroup() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        childFeature1Tree.mutate().toAlternativeGroup();
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        
        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        childFeature1Tree.mutate().addFeatureBelow(childFeature3);
       
        
        expected = new Reference(new And(
//        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2")))),
        		
        		new Implies(new Literal("root"), new Choose(1, Arrays.asList(new Literal("B"), new Literal("C")))),
        		new Implies(new Literal("B"), new Literal("root")),
        		new Implies(new Literal("C"), new Literal("root"))
        		));
    	
        executeSimpleTest();  
    	
    }
    
    @Test
    void withCardinalityAndChildInbetweenRecursive() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        IFeatureTree childFeature1Tree2 = childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        
        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        IFeatureTree childFeature1Tree3 = childFeature1Tree2.mutate().addFeatureBelow(childFeature3);
        childFeature1Tree3.mutate().setFeatureCardinality(Range.of(0, 2));
       
        
        expected = new Reference(new And(
        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("B.A_1"), new Literal("A_1")),
        		new Implies(new Literal("C_1.B.A_1"), new Literal("B.A_1")),
        		new Implies(new Literal("C_2.B.A_1"), new Literal("B.A_1")),
        		new Implies(new Literal("C_2.B.A_1"), new Literal("C_1.B.A_1")),
        		new Implies(new Literal("B.A_1"), new AtLeast(0, Arrays.asList(new Literal("C_1.B.A_1"), new Literal("C_2.B.A_1")))),
        		
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("B.A_2"), new Literal("A_2")),
        		new Implies(new Literal("C_1.B.A_2"), new Literal("B.A_2")),
        		new Implies(new Literal("C_2.B.A_2"), new Literal("B.A_2")),
        		new Implies(new Literal("C_2.B.A_2"), new Literal("C_1.B.A_2")),
        		new Implies(new Literal("B.A_2"), new AtLeast(0, Arrays.asList(new Literal("C_1.B.A_2"), new Literal("C_2.B.A_2")))),
        		
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2"))))
        		

        		));
    	
        executeTest();  
    	
    }
    
    @Test
    void withCardinalityAndChildGroupRecursive() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        childFeature1Tree.mutate().toAlternativeGroup();
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        
        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        childFeature1Tree.mutate().addFeatureBelow(childFeature3);
       
        
        expected = new Reference(new And(
        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("A_1"), new Choose(1, Arrays.asList(new Literal("B.A_1"), new Literal("C.A_1")))),
        		new Implies(new Literal("B.A_1"), new Literal("A_1")),
        		new Implies(new Literal("C.A_1"), new Literal("A_1")),
        		
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("A_2"), new Choose(1, Arrays.asList(new Literal("B.A_2"), new Literal("C.A_2")))),
        		new Implies(new Literal("B.A_2"), new Literal("A_2")),
        		new Implies(new Literal("C.A_2"), new Literal("A_2")),
        		
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2"))))
        		
        		));
    	
        executeRecursiveTest();  	
    }

    @Test
    void withCardinalityAndChildChildGroupRecursive() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        childFeature1Tree.mutate().toAlternativeGroup();
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        
        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        IFeatureTree childFeature1Tree2 = childFeature1Tree.mutate().addFeatureBelow(childFeature3);
        
        childFeature1Tree2.mutate().toOrGroup();
        
        IFeature childFeature4 = featureModel.mutate().addFeature("D");
        childFeature1Tree2.mutate().addFeatureBelow(childFeature4);
        
        IFeature childFeature5 = featureModel.mutate().addFeature("E");
        childFeature1Tree2.mutate().addFeatureBelow(childFeature5);
       
        
        expected = new Reference(new And(
        		new Literal("root"),
        		new Implies(new Literal("A_1"), new Literal("root")),
        		new Implies(new Literal("A_1"), new Choose(1, Arrays.asList(new Literal("B.A_1"), new Literal("C.A_1")))),
        		new Implies(new Literal("B.A_1"), new Literal("A_1")),
        		new Implies(new Literal("C.A_1"), new Literal("A_1")),
        		
        		//sub-subtree
        		new Implies(new Literal("C.A_1"), new Or(Arrays.asList(new Literal("D.C.A_1"), new Literal("E.C.A_1")))),
        		new Implies(new Literal("D.C.A_1"), new Literal("C.A_1")),
        		new Implies(new Literal("E.C.A_1"), new Literal("C.A_1")),
        		
        		new Implies(new Literal("A_2"), new Literal("root")),
        		new Implies(new Literal("A_2"), new Literal("A_1")),
        		new Implies(new Literal("A_2"), new Choose(1, Arrays.asList(new Literal("B.A_2"), new Literal("C.A_2")))),
        		new Implies(new Literal("B.A_2"), new Literal("A_2")),
        		new Implies(new Literal("C.A_2"), new Literal("A_2")),
        		
        		//second sub-subtree
        		new Implies(new Literal("C.A_2"), new Or(Arrays.asList(new Literal("D.C.A_2"), new Literal("E.C.A_2")))),
        		new Implies(new Literal("D.C.A_2"), new Literal("C.A_2")),
        		new Implies(new Literal("E.C.A_2"), new Literal("C.A_2")),
        		
        		new Implies(new Literal("root"), new AtLeast(0, Arrays.asList(new Literal("A_1"), new Literal("A_2"))))
        		
        		// missing
        		));
    	
        executeRecursiveTest();  	
    }

    @Test
    void onlyRoot() {

        // root and nothing else
        featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));

        // root must be selected
        expected = new Reference(new And(
//        		new Literal("root")
        		));

        executeTest();
    }

    @Test
    void oneFeature() {

        // root
        IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
        rootTree.mutate().toAndGroup();

        // TODO: check if setting root feature is missing here or if compute misses adding root feature literal

        // create and add our only child
        IFeature childFeature = featureModel.mutate().addFeature("Test1");
        rootTree.mutate().addFeatureBelow(childFeature);

        // TODO: check order if bug is fixed
        expected = new Reference(new And(
//        		new Literal("root"), 
        		new Implies(new Literal("Test1"), new Literal("root"))
        		));

        executeTest();
    }
    
    @Test
    void withCardinalityGroup() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toCardinalityGroup(Range.of(2, 3));
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("Test1");
        rootTree.mutate().addFeatureBelow(childFeature1);
        
        IFeature childFeature2 = featureModel.mutate().addFeature("Test2");
        rootTree.mutate().addFeatureBelow(childFeature2);
        
        IFeature childFeature3 = featureModel.mutate().addFeature("Test3");
        rootTree.mutate().addFeatureBelow(childFeature3);
    	
        executeTest();  
    }
    
    @Test
    void withOneCardinalityFeature() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature = featureModel.mutate().addFeature("A");
    	IFeatureTree childFeatureTree1 = rootTree.mutate().addFeatureBelow(childFeature);
    	childFeatureTree1.mutate().setFeatureCardinality(Range.of(0, 2));

    	// add normal feature below
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        childFeatureTree1.mutate().addFeatureBelow(childFeature2);
        
        
        executeTest();   	
    }
    
    @Test
    void withTwoCardinalityFeatures() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	// create and set cardinality for the child feature
    	IFeature childFeature1 = featureModel.mutate().addFeature("A");
        IFeatureTree childFeature1Tree = rootTree.mutate().addFeatureBelow(childFeature1);
        childFeature1Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
        IFeature childFeature2 = featureModel.mutate().addFeature("B");
        IFeatureTree childFeature2Tree = childFeature1Tree.mutate().addFeatureBelow(childFeature2);
        childFeature2Tree.mutate().setFeatureCardinality(Range.of(0, 2));
        
     // add normal feature below
        IFeature childFeature3 = featureModel.mutate().addFeature("C");
        childFeature2Tree.mutate().addFeatureBelow(childFeature3);
        
    	
        executeTest();   	
    }

    private void executeTest() {

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula.computeResult().get();

        // assert
        assertEquals(expected, resultFormula);
    }
    
    private void executeSimpleTest() {

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        IFormula resultFormula = computeFormula
        		.set(ComputeFormula.SIMPLE_TRANSLATION, Boolean.TRUE)
        		.computeResult().get();

        // assert
        assertEquals(expected, resultFormula);
    }
    
    private void executeRecursiveTest() {

        ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
        ComputeFormula computeFormula = new ComputeFormula(computeConstant);

        // recursive is default now
//        computeFormula.setRecursive();
        
        IFormula resultFormula = computeFormula.computeResult().get();

        // assert
        assertEquals(expected, resultFormula);
    }
}
