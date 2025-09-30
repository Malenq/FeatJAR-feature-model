package de.featjar.feature.model.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.data.identifier.Identifiers;
import de.featjar.feature.model.FeatureModel;
import de.featjar.feature.model.IFeature;
import de.featjar.feature.model.IFeatureModel;
import de.featjar.feature.model.IFeatureTree;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.connective.And;
import de.featjar.formula.structure.connective.Implies;
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
	void onlyRoot() {
		
		// root and nothing else
		featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
		
		// root must be selected
		expected = new Reference(new And(
					new Literal("root")
				));
		
		executeTest();
	}
	
	@Test
	void oneFeature () {
		
		// root
		IFeatureTree rootTree = 
				featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
		rootTree.mutate().toAndGroup();
		
		// TODO: check if setting root feature is missing here or if compute misses adding root feature literal

		// create and add our only child
		IFeature childFeature = featureModel.mutate().addFeature("Test1");
		rootTree.mutate().addFeatureBelow(childFeature);
		
		// TODO: check order if bug is fixed
        expected = new Reference( new And (
    				new Literal ("root"),
        			new Implies( new Literal("Test1") , new Literal ("root") )
        		));
        
        executeTest();
	}

	private void executeTest() {
		
		ComputeConstant<IFeatureModel> computeConstant = new ComputeConstant<IFeatureModel>(featureModel);
		ComputeFormula computeFormula = new ComputeFormula(computeConstant);
		
		IFormula resultFormula = computeFormula.computeResult().get();
		
		// assert
		assertEquals(expected, resultFormula);
	}
}
