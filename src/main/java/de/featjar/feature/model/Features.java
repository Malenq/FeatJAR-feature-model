package de.featjar.feature.model;

import de.featjar.base.FeatJAR;
import de.featjar.formula.structure.Expressions;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.predicate.NotEquals;
import de.featjar.formula.structure.term.value.Constant;
import de.featjar.formula.structure.term.value.Variable;

public class Features {

    public static IFormula createFeatureFormel(IFeature feature) {
        return createFeatureFormel(feature, feature.getName().orElse(""));
    }

    public static IFormula createFeatureFormel(IFeature feature, String featureName) {
        if (feature.getType().equals(Boolean.class)) {
            return Expressions.literal(featureName);
        } else if (feature.getType().equals(Integer.class)) {
            return new NotEquals(new Variable(featureName, feature.getType()), new Constant(0));
        } else if(feature.getType().equals(Float.class)) {
            return new NotEquals(new Variable(featureName, feature.getType()), new Constant(0.0f));
        } else {
            FeatJAR.log().warning("Could not handle type "+ feature.getType());
            return null;
        }
    }
}
