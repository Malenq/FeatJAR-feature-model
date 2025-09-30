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

import de.featjar.base.data.IAttributable;
import de.featjar.base.data.Range;
import de.featjar.base.data.Result;
import de.featjar.base.tree.structure.ARootedTree;
import de.featjar.base.tree.structure.IRootedTree;
import de.featjar.feature.model.FeatureTree.Group;
import de.featjar.feature.model.mixins.IHasFeatureTree;
import java.util.List;
import java.util.Optional;

/**
 * An ordered {@link ARootedTree} labeled with {@link Feature features}.
 * Implements some concepts from feature-oriented domain analysis, such as
 * mandatory/optional features and groups.
 *
 * @author Elias Kuiter
 * @author Sebastian Krieter
 */
public interface IFeatureTree extends IRootedTree<IFeatureTree>, IAttributable, IHasFeatureTree {

    IFeature getFeature();

    /**
     * {@return the groups of this feature's children.}
     * This list may contain {@code null}.
     */
    List<Group> getChildrenGroups();

    /**
     * {@return the group of this feature's children with the given id.}
     * @param groupID the groupID
     */
    Optional<Group> getChildrenGroup(int groupID);

    /**
     * {@return all children within the group with the given id.}
     * @param groupID the groupID
     */
    List<IFeatureTree> getChildren(int groupID);

    /**
     * {@return the group of this feature. (The group of this feature's parent, in which this feature is contained.)}
     * @see #getParentGroupID()
     */
    Optional<Group> getParentGroup();

    /**
     * {@return the id of the group of this feature.}
     * @see #getParentGroup()
     */
    int getParentGroupID();

    int getFeatureCardinalityLowerBound();

    int getFeatureCardinalityUpperBound();

    default boolean isMandatory() {
        return getFeatureCardinalityLowerBound() > 0;
    }

    default boolean isOptional() {
        return getFeatureCardinalityLowerBound() <= 0;
    }

    default IMutableFeatureTree mutate() {
        return (IMutableFeatureTree) this;
    }

    static interface IMutableFeatureTree extends IFeatureTree, IMutatableAttributable {

        default IFeatureTree addFeatureBelow(IFeature newFeature) {
            return addFeatureBelow(newFeature, getChildrenCount(), 0);
        }

        default IFeatureTree addFeatureBelow(IFeature newFeature, int index) {
            return addFeatureBelow(newFeature, index, 0);
        }

        default IFeatureTree addFeatureBelow(IFeature newFeature, int index, int groupID) {
            FeatureTree newTree = new FeatureTree(newFeature);
            addChild(index, newTree);
            newTree.setParentGroupID(groupID);
            return newTree;
        }

        default IFeatureTree addFeatureAbove(IFeature newFeature) {
            FeatureTree newTree = new FeatureTree(newFeature);
            Result<IFeatureTree> parent = getParent();
            if (parent.isPresent()) {
                parent.get().replaceChild(this, newTree);
            }
            newTree.addChild(this);
            setParentGroupID(0);
            return newTree;
        }

        default void removeFromTree() { // TODO what about the containing constraints?
            Result<IFeatureTree> parent = getParent();
            if (parent.isPresent()) {
                int childIndex = parent.get().getChildIndex(this).orElseThrow();
                parent.get().removeChild(this);
                // TODO improve group handling, probably needs slicing
                for (Group group : getChildrenGroups()) {
                    parent.get().mutate().addCardinalityGroup(group.getLowerBound(), group.getUpperBound());
                }
                for (IFeatureTree child : getChildren()) {
                    parent.get().mutate().addChild(childIndex++, child);
                }
            }
        }

        void setParentGroupID(int groupID);

        void setFeatureCardinality(Range featureRange);

        void makeMandatory();

        void makeOptional();

        int addCardinalityGroup(int lowerBound, int upperBound);

        default int addCardinalityGroup(Range groupRange) {
            return addCardinalityGroup(groupRange.getLowerBound(), groupRange.getUpperBound());
        }

        default int addAndGroup() {
            return addCardinalityGroup(0, Range.OPEN);
        }

        default int addAlternativeGroup() {
            return addCardinalityGroup(1, 1);
        }

        default int addOrGroup() {
            return addCardinalityGroup(1, Range.OPEN);
        }

        /**
         * Changes the cardinality of the children group with the given id.
         *
         * @param groupID          the id of the group to change
         * @param lowerBound       the new lower bound
         * @param upperBound       the new upper bound
         */
        void toCardinalityGroup(int groupID, int lowerBound, int upperBound);

        /**
         * Changes the cardinality of the children group with the given id.
         *
         * @param groupID          the id of the group to change
         * @param groupCardinality the new cardinality
         */
        default void toCardinalityGroup(int groupID, Range groupCardinality) {
            toCardinalityGroup(groupID, groupCardinality.getLowerBound(), groupCardinality.getUpperBound());
        }

        /**
         * Change children group to and group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 0, Range.OPEN)}.
         *
         * @param groupID the id of the group to change
         */
        default void toAndGroup(int groupID) {
            toCardinalityGroup(groupID, 0, Range.OPEN);
        }

        /**
         * Change children group to or group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 1, Range.OPEN)}.
         *
         * @param groupID the id of the group to change
         */
        default void toOrGroup(int groupID) {
            toCardinalityGroup(groupID, 1, Range.OPEN);
        }

        /**
         * Change children group to alternative group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(groupID, 1, 1)}.
         *
         * @param groupID the id of the group to change
         */
        default void toAlternativeGroup(int groupID) {
            toCardinalityGroup(groupID, 1, 1);
        }

        /**
         * Changes the cardinality of the first children group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, groupCardinality)}.
         *
         * @param groupCardinality the new cardinality
         */
        default void toCardinalityGroup(Range groupCardinality) {
            toCardinalityGroup(0, groupCardinality);
        }

        /**
         * Change first children group to cardinality group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, lowerBound, upperBound)}.
         *
         * @param lowerBound       the new lower bound
         * @param upperBound       the new upper bound
         */
        default void toCardinalityGroup(int lowerBound, int upperBound) {
            toCardinalityGroup(0, lowerBound, upperBound);
        }

        /**
         * Change first children group to and group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 0, Range.OPEN)}.
         *
         */
        default void toAndGroup() {
            toAndGroup(0);
        }

        /**
         * Change first children group to alternative group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 1, Range.OPEN)}.
         */
        default void toAlternativeGroup() {
            toAlternativeGroup(0);
        }

        /**
         * Change first children group to or group.
         * Equivalent to calling {@link #toCardinalityGroup(int, int, int) toCardinalityGroup(0, 1, 1)}.
         */
        default void toOrGroup() {
            toOrGroup(0);
        }
    }
}
