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
package de.featjar.feature.model;

import de.featjar.base.data.Attribute;
import de.featjar.base.data.Sets;
import de.featjar.base.data.identifier.IIdentifiable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Defines useful {@link Attribute attributes} for {@link FeatureModel feature models},
 * {@link Feature features}, and {@link Constraint constraints}.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public class Attributes {

    private static final LinkedHashMap<Attribute<?>, Attribute<?>> attributeSet = new LinkedHashMap<>();

    public static final String NAMESPACE = Attributes.class.getCanonicalName();

    public static final Attribute<String> NAME = get(NAMESPACE, "name", String.class)
            .setDefaultValueFunction(identifiable ->
                    "@" + ((IIdentifiable) identifiable).getIdentifier().toString())
            .setValidator(
                    (element, name) -> // TODO: can also be name of feature model or constraint, but this validates only
                            // feature name uniqueness
                            ((AFeatureModelElement) element)
                                    .getFeatureModel()
                                    .getFeature((String) name)
                                    .isEmpty());

    public static final Attribute<String> DESCRIPTION = get(NAMESPACE, "description", String.class);

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static final Attribute<LinkedHashSet<String>> TAGS = getRaw(NAMESPACE, "tags", LinkedHashSet.class)
            .setDefaultValueFunction(attributable -> Sets.<String>empty())
            .setCopyValueFunction(set -> new LinkedHashSet((Collection) set));

    public static final Attribute<Boolean> HIDDEN =
            get(NAMESPACE, "hidden", Boolean.class).setDefaultValue(false);

    public static final Attribute<Boolean> ABSTRACT =
            get(NAMESPACE, "abstract", Boolean.class).setDefaultValue(false);

    public static Set<Attribute<?>> getAllAttributes() {
        return Collections.unmodifiableSet(attributeSet.keySet());
    }

    public static <T> Attribute<T> get(String name, Class<T> type) {
        return get(NAMESPACE, name, type);
    }

    @SuppressWarnings("unchecked")
    public static <T> Attribute<T> get(String namespace, String name, Class<T> type) {
        return getRaw(namespace, name, type);
    }

    @SuppressWarnings("rawtypes")
    public static Attribute getRaw(String namespace, String name, Class<?> type) {
        Attribute attribute = new Attribute<>(namespace, name, type);
        Attribute cachedAttribute = attributeSet.get(attribute);
        if (cachedAttribute == null) {
            attributeSet.put(attribute, attribute);
            return attribute;
        } else {
            if (type != cachedAttribute.getType()) {
                throw new IllegalArgumentException(String.format(
                        "Cannot create attribute for type %s. Attribute already defined for type %s.",
                        type.toString(), cachedAttribute.getType()));
            }
            return cachedAttribute;
        }
    }
}
