package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.data.IAttribute;
import de.featjar.feature.model.IFeature;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class allows you to display the attributes of a feature. You can also filter specific names to display
 * in the tikz file.
 *
 * @author Felix Behme
 * @author Lara Merza
 * @author Jonas Hanke
 */
public class TikzAttributeHelper {

    private final String MULTICOLUMN = "\\multicolumn{2}{c}{{0}} \\\\\\hline" + System.lineSeparator();
    private final String VALUE = "\\small\\texttt{{0} ({1})} &\\small\\texttt{= {2}} \\\\" + System.lineSeparator();

    private final IFeature feature;
    private final StringBuilder stringBuilder;

    private List<String> filter;
    private FilterType filterType = FilterType.WITH_OUT; // FALLBACK

    public TikzAttributeHelper(IFeature feature, StringBuilder stringBuilder) {
        this.feature = feature;
        this.stringBuilder = stringBuilder;
        this.filter = new ArrayList<>();
    }

    public TikzAttributeHelper(IFeature feature, StringBuilder stringBuilder, List<String> filter) {
        this.feature = feature;
        this.stringBuilder = stringBuilder;
        this.filter = filter;
    }

    /**
     * Set a filter type to customize your output for the attributes
     * @param filterType (DISPLAY or WITH_OUT)
     *                   DISPLAY: Shows every value in the filter
     *                   WITH_OUT: Shows every value that isn't in the filter
     * @return this class
     */
    public TikzAttributeHelper setFilterType(FilterType filterType) {
        this.filterType = filterType;
        return this;
    }

    /**
     *
     * @param values (the keys, words or whatever that will be filtered in the running process.
     * @return this
     */
    public TikzAttributeHelper addFilterValue(List<String> values) {
        filter.addAll(values);
        return this;
    }

    private void writeAttributes(IFeature feature) {
        StringBuilder stringBuilderInternal = new StringBuilder();
        String featureName = feature.getName().orElse("");

        Map<IAttribute<?>, Object> iAttributeObjectMap = feature.getAttributes().orElse(null);
        // check: if the attribute map is empty or null
        if (iAttributeObjectMap == null || iAttributeObjectMap.isEmpty() || countAttributeToDisplay(iAttributeObjectMap) == 0) {
            stringBuilder.append("[").append(featureName); // add the name without multicolumn
            return;
        }
        stringBuilder.append("[").append(replace(MULTICOLUMN, featureName));

        iAttributeObjectMap.forEach((iAttribute, object) -> {
            writeAttributes(iAttribute, object, stringBuilderInternal);
        });

        stringBuilder.append(stringBuilderInternal);
        stringBuilder.append(",align=ll");
    }

    private void writeAttributes(IAttribute<?> attribute, Object object, StringBuilder stringBuilder) {
        // filter with type the current boolean value for the running process
        if (filterWithType(attribute.getName().toUpperCase())) {
            return; // ignore attribute
        }
        stringBuilder.append(replace(VALUE, attribute.getName(), attribute.getType().getSimpleName(), object));
        replace(VALUE, 12, 2);
    }

    /**
     * Count all items to display in the feature for more functions and more beauty
     *
     * @param values (Map of the feature with his attributes)
     * @return count if items to diplay
     */
    private int countAttributeToDisplay(Map<IAttribute<?>, Object> values) {
        int size = values.size();
        for (IAttribute<?> attribute : values.keySet()) {
            if (filterWithType(attribute.getName().toUpperCase())) {
                size-=1;
            }
        }
        return size;
    }

    private String replace(String key, Object... values) {
        String result = key;
        for (short i = 0; i < values.length; i++) {
            // Synatx: Hello my name is {0} and I am from {1} -> Hello my name is FeatJar, and I am from Germany
            result = result.replace("{" + i + "}", values[i].toString());
        }
        return result;
    }

    private void makeListUpperCase() {
        List<String> upperFilter = new ArrayList<>();
        filter.forEach(value -> {
            // allows filtering with contains and no streams.
            upperFilter.add(value.toUpperCase());
        });
        filter = upperFilter;
    }

    private boolean filterWithType(String key) {
        if (filter.isEmpty()) {
            return false;
        }
        if (filterType == FilterType.DISPLAY) {
            return !filter.contains(key);
        }
        if (filterType == FilterType.WITH_OUT) {
            return filter.contains(key);
        }

        return false;
    }

    /**
     * Paste everything together in the string builder.
     */
    public void build() {
        makeListUpperCase();
        writeAttributes(feature);
    }

    public enum FilterType {
        DISPLAY,
        WITH_OUT;
    }

}
