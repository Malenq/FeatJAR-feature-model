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
package de.featjar.feature.model.io;

import de.featjar.base.data.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Helpers for parsing and writing attributes and attribute values.
 *
 * @author Elias Kuiter
 */
public class AttributeIO {
    public static Result<Class<?>> getType(String typeString) {
        switch (typeString.toLowerCase(Locale.ENGLISH)) {
            case "string":
                return Result.of(String.class);
            case "bool":
            case "boolean":
                return Result.of(Boolean.class);
            case "int":
            case "integer":
                return Result.of(Integer.class);
            case "long":
                return Result.of(Long.class);
            case "float":
                return Result.of(Float.class);
            case "double":
                return Result.of(Double.class);
            default:
                return Result.empty();
        }
    }

    public static Result<String> getTypeString(Class<?> type) {
        if (String.class.equals(type)) {
            return Result.of("string");
        } else if (Boolean.class.equals(type)) {
            return Result.of("boolean");
        } else if (Integer.class.equals(type)) {
            return Result.of("integer");
        } else if (Long.class.equals(type)) {
            return Result.of("long");
        } else if (Float.class.equals(type)) {
            return Result.of("float");
        } else if (Double.class.equals(type)) {
            return Result.of("double");
        }
        return Result.empty();
    }

    public static Result<Attribute<?>> parseAttribute(String name, String typeString) {
        return getType(typeString).map(type -> new Attribute<>(name, type));
    }

    public static Result<Attribute<?>> parseAttribute(String namespace, String name, String typeString) {
        return getType(typeString).map(type -> new Attribute<>(namespace, name, type));
    }

    public static Result<Object> parseAttributeValue(Class<?> type, String valueString) {
        if (String.class.equals(type)) {
            return Result.of(valueString);
        } else if (Boolean.class.equals(type)) {
            return Result.of(Boolean.valueOf(valueString));
        } else if (Integer.class.equals(type)) {
            return Result.of(Integer.valueOf(valueString));
        } else if (Long.class.equals(type)) {
            return Result.of(Long.valueOf(valueString));
        } else if (Float.class.equals(type)) {
            return Result.of(Float.valueOf(valueString));
        } else if (Double.class.equals(type)) {
            return Result.of(Double.valueOf(valueString));
        }
        return Result.empty();
    }

    public static Result<Object> parseAttributeValue(String typeString, String valueString) {
        return getType(typeString).flatMap(type -> parseAttributeValue(type, valueString));
    }

    @SuppressWarnings("unchecked")
    public static List<Problem> parseAndSetAttributeValue(
            IAttributable attributable, String namespace, String name, String typeString, String valueString) {
        List<Problem> problems = new ArrayList<>();
        Result<Attribute<?>> attribute = AttributeIO.parseAttribute(namespace, name, typeString);
        Result<?> value = parseAttributeValue(typeString, valueString);
        if (attribute.isEmpty()) {
            problems.add(new Problem("invalid type for attribute " + name, Problem.Severity.WARNING));
        } else if (value.isEmpty()) {
            problems.add(new Problem("invalid value for attribute " + name, Problem.Severity.WARNING));
        } else if (attributable.hasAttributeValue(attribute.get())) {
            problems.add(new Problem("already has value for attribute " + name, Problem.Severity.WARNING));
        } else {
            attributable.mutate().setAttributeValue((Attribute<Object>) attribute.get(), value.get());
        }
        return problems;
    }
}
