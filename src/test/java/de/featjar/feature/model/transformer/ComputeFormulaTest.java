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

import de.featjar.base.computation.ComputeConstant;
import de.featjar.base.data.Range;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
        expected = new Reference(new And(new Literal("root")));

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
        expected = new Reference(new And(new Literal("root"), new Implies(new Literal("Test1"), new Literal("root"))));

        executeTest();
    }
    
    @Test
    void withCardinality() {
    	IFeatureTree rootTree =
                featureModel.mutate().addFeatureTreeRoot(featureModel.mutate().addFeature("root"));
    	rootTree.mutate().toAndGroup();
    	
    	IFeature childFeature = featureModel.mutate().addFeature("Test1");
    	
    	// set cardinality for the child feature
        rootTree.mutate().addFeatureBelow(childFeature).mutate().setFeatureCardinality(Range.of(0, 2));
    	
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
