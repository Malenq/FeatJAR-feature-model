package de.featjar.feature.model.transformer;

import de.featjar.base.data.IAttribute;
import de.featjar.base.data.Result;
import de.featjar.base.data.Void;
import de.featjar.base.tree.visitor.ITreeVisitor;
import de.featjar.formula.structure.IExpression;
import de.featjar.formula.structure.IFormula;
import de.featjar.formula.structure.term.aggregate.IAttributeAggregate;
import de.featjar.formula.structure.term.value.Variable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Implements tree visitor {@link ITreeVisitor}.
 * Each {@link IAttributeAggregate} placeholder in a formula will be replaced with the correct formula.
 *
 * @author Lara Merza
 * @author Felix Behme
 * @author Jonas Hanke
 */
public class ReplaceAttributeAggregate implements ITreeVisitor<IFormula, Void> {

    private final Map<Variable, Map<IAttribute<?>, Object>> attributes;
    private boolean hasCardinalityFeatures;

    public ReplaceAttributeAggregate(
            Map<Variable, Map<IAttribute<?>, Object>> attributes, Boolean hasCardinalityFeatures) {
        this.attributes = attributes;
        this.hasCardinalityFeatures = hasCardinalityFeatures;
    }

    @Override
    public TraversalAction lastVisit(List<IFormula> path) {
        final IExpression formula = ITreeVisitor.getCurrentNode(path);

        if (formula instanceof IAttributeAggregate) {

            if (hasCardinalityFeatures) {
                throw new UnsupportedOperationException(
                        "Attribute aggregates and cardinality features can not be translated.");
            }

            final Result<IFormula> parent = ITreeVisitor.getParentNode(path);

            if (parent.isPresent()) {
                ArrayList<Variable> filteredVariables = new ArrayList<>();
                ArrayList<Object> values = new ArrayList<>();
                String attributeFilter = ((IAttributeAggregate) formula).getAttributeFilter();

                attributes.forEach((variable, value) -> {
                    Optional<Map.Entry<IAttribute<?>, Object>> attributeMatch = value.entrySet().stream()
                            .filter(predicate -> predicate.getKey().getName().equals(attributeFilter))
                            .findFirst();

                    if (attributeMatch.isPresent()) {
                        filteredVariables.add(variable);
                        values.add(attributeMatch.get().getValue());
                    }
                });

                Result<IExpression> result = ((IAttributeAggregate) formula).translate(filteredVariables, values);
                if (result.isPresent()) {
                    parent.get().replaceChild(formula, result.get());
                }
            }
        }

        return TraversalAction.CONTINUE;
    }

    @Override
    public Result<Void> getResult() {
        return Result.ofVoid();
    }
}
