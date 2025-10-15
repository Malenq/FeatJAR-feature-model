package de.featjar.feature.model.transformer;

import java.util.List;

import de.featjar.feature.model.IConstraint;
import de.featjar.feature.model.IFeatureTree;

public class CustomObj {
	List<IFeatureTree> contexts;
	List<IConstraint> constraints;
	
	public CustomObj(List<IFeatureTree> contexts, List<IConstraint> constraints) {
		this.contexts = contexts;
		this.constraints = constraints;
	}

	public List<IConstraint> getConstraints() {
		return constraints;
	}

	public void setConstraints(List<IConstraint> constraints) {
		this.constraints = constraints;
	}

	public List<IFeatureTree> getContexts() {
		return contexts;
	}

	public void setContexts(List<IFeatureTree> contexts) {
		this.contexts = contexts;
	}

}
