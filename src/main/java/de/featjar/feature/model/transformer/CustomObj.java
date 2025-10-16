package de.featjar.feature.model.transformer;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import de.featjar.feature.model.IFeatureTree;


/**
 * 
 */
public class CustomObj {
	private final Set<IFeatureTree> contextFeatures;

    public Set<IFeatureTree> getContextFeatures() {
		return contextFeatures;
	}

	public CustomObj(Set<IFeatureTree> features) {
        this.contextFeatures = new HashSet<>(features); 
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomObj that = (CustomObj) o;
        // The magic is here: Set.equals() is order-independent!
        return this.contextFeatures.equals(that.contextFeatures);
    }

    @Override
    public int hashCode() {
        // Set.hashCode() is also order-independent.
        return Objects.hash(contextFeatures);
    }
}


//import de.featjar.feature.model.IConstraint;
//import de.featjar.feature.model.IFeatureTree;
//
//public class CustomObj {
//	List<IFeatureTree> contexts;
//	List<IConstraint> constraints;
//	
//	public CustomObj(List<IFeatureTree> contexts, List<IConstraint> constraints) {
//		this.contexts = contexts;
//		this.constraints = constraints;
//	}
//
//	public List<IConstraint> getConstraints() {
//		return constraints;
//	}
//
//	public void setConstraints(List<IConstraint> constraints) {
//		this.constraints = constraints;
//	}
//
//	public List<IFeatureTree> getContexts() {
//		return contexts;
//	}
//
//	public void setContexts(List<IFeatureTree> contexts) {
//		this.contexts = contexts;
//	}
//
//}


