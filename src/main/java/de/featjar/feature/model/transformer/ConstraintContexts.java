package de.featjar.feature.model.transformer;

import de.featjar.feature.model.IFeatureTree;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * A class which can be used as a key in a HashMap which maps feature contexts to
 * constraints.
 *
 * @author Klara Surmeier
 * @author Nermine Mansour
 * @author Malena Horstmann
 */
public class ConstraintContexts {
    private final Set<IFeatureTree> contextFeatures;

    public Set<IFeatureTree> getContextFeatures() {
        return contextFeatures;
    }

    public ConstraintContexts(Set<IFeatureTree> features) {
        this.contextFeatures = new HashSet<>(features);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ConstraintContexts that = (ConstraintContexts) o;

        return this.contextFeatures.equals(that.contextFeatures);
    }

    @Override
    public int hashCode() {

        return Objects.hash(contextFeatures);
    }
}
