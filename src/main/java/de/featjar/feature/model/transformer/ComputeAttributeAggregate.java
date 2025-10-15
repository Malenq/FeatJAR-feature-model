package de.featjar.feature.model.transformer;

import de.featjar.base.computation.AComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IFormula;

import java.util.List;

public class ComputeAttributeAggregate extends AComputation<IFormula> {

    /*protected static final Dependency<IAttributeAggregate> ATTRIBUTE_AGGREGATE = Dependency.newDependency(IAttributeAggregate.class);
    protected static final Dependency<List<Variable>> VARIABLES = Dependency.newDependency(List.class);

    public ComputeAttributeAggregate(IComputation<IAttributeAggregate> attributeAggregate,
                                     IComputation<List<Variable>> variables,
                                     IComputation<List<Object>> values) {
        super(attributeAggregate, variables, values);
    }

    protected ComputeAttributeAggregate(ComputeFormula other) {
        super(other);
    }*/

    @Override
    public Result<IFormula> compute(List<Object> dependencyList, Progress progress) {
        return Result.empty();
    }
}
