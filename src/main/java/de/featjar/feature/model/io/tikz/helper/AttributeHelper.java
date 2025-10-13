package de.featjar.feature.model.io.tikz.helper;

import de.featjar.base.FeatJAR;
import de.featjar.base.data.IAttribute;
import de.featjar.feature.model.IFeature;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AttributeHelper {

    private final static String NAME = "\\underline{{replace}}";
    private final static String SHORTSTACK = "[{\\shortstack{{replace}\\\\" + System.lineSeparator();
    private final static String SCRIPTSIZE = "\\scriptsize {replace}\\\\" + System.lineSeparator();
    private final static String SCRIPTSIZE_END = "\\scriptsize {replace} " + System.lineSeparator();
    private final static String END = "}}";

    private final StringBuilder stringBuilder;

    private List<String> filter;

    private int size = 0;
    private int runs = 0;

    private int frontLength = 0;
    private int backLength = 0;

    public AttributeHelper(StringBuilder stringBuilder) {
        this.stringBuilder = stringBuilder;
        this.filter = new ArrayList<>();

        makeListUpperCase();
    }

    public AttributeHelper(StringBuilder stringBuilder, List<String> filter) {
        this.stringBuilder = stringBuilder;
        this.filter = filter;

        makeListUpperCase();
    }

    public void writeAttributes(IFeature feature) {
        StringBuilder stringBuilderInternal = new StringBuilder();
        feature.getAttributes().ifPresent(iAttributeObjectMap -> {
            size = iAttributeObjectMap.size();
            iAttributeObjectMap.forEach((iAttribute, object) -> writeAttributes(iAttribute, object, stringBuilderInternal));
        });
        String featureName = feature.getName().orElse("");
        if (size == 0) {
            this.stringBuilder.append("[").append(replace(NAME, featureName));
        } else {
            stringBuilder.append(replace(SHORTSTACK, replace(NAME, featureName)));
            stringBuilder.append(stringBuilderInternal);
        }
    }

    private void writeAttributes(IAttribute<?> attribute, Object object, StringBuilder stringBuilder) {
        if (filter.contains(attribute.getName().toUpperCase())) {
            size-=1;
            return;
        }
        String format = attribute.getName() + " (" + object.getClass().getSimpleName() + ") = " + object;
        runs++;
        if (isEnd()) {
            stringBuilder.append(replace(SCRIPTSIZE_END, format));
            stringBuilder.append(END);
        } else {
            stringBuilder.append(replace(SCRIPTSIZE, format));
        }
    }

    private boolean isEnd() {
        return runs == size;
    }

    private String replace(String key, String value) {
        return key.replace("{replace}", value);
    }

    private void makeListUpperCase() {
        List<String> upperFilter = new ArrayList<>();
        filter.forEach(value -> {
            upperFilter.add(value.toUpperCase());
        });
        filter = upperFilter;
    }

}
