/*
 * Copyright (c) 2014 GraphAware
 *
 * This file is part of GraphAware.
 *
 * GraphAware is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 *  without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received a copy of
 * the GNU General Public License along with this program.  If not, see
 * <http://www.gnu.org/licenses/>.
 */
package com.graphaware.module.uuid;

import com.graphaware.common.policy.InclusionPolicies;
import com.graphaware.runtime.config.BaseTxDrivenModuleConfiguration;
import com.graphaware.runtime.policy.InclusionPoliciesFactory;


/**
 * {@link BaseTxDrivenModuleConfiguration} for {@link com.graphaware.module.uuid.UuidModule}.
 */
public class UuidConfiguration extends BaseTxDrivenModuleConfiguration<UuidConfiguration> {

    private static final String DEFAULT_UUID_PROPERTY = "uuid";
    private static final String DEFAULT_UUID_LEGACY_INDEX_NAME = "uuidIndex";

    private String uuidProperty;
    private String uuidLegacyIndexName;

    protected UuidConfiguration(InclusionPolicies inclusionPolicies) {
        super(inclusionPolicies);
    }

    public UuidConfiguration(InclusionPolicies inclusionPolicies, String uuidProperty, String uuidLegacyIndexName) {
        super(inclusionPolicies);
        this.uuidProperty = uuidProperty;
        this.uuidLegacyIndexName = uuidLegacyIndexName;
    }

    /**
     * Create a default configuration with default uuid property = {@link #DEFAULT_UUID_PROPERTY}, labels=all (including nodes with no labels)
     * inclusion strategies = {@link com.graphaware.runtime.policy.InclusionPoliciesFactory#allBusiness()},
     * (nothing is excluded except for framework-internal nodes and relationships)
     * <p/>
     * Change this by calling {@link #withUuidProperty(String)}, with* other inclusion strategies
     * on the object, always using the returned object (this is a fluent interface).
     */
    public static UuidConfiguration defaultConfiguration() {
        return new UuidConfiguration(InclusionPoliciesFactory.allBusiness(), DEFAULT_UUID_PROPERTY, DEFAULT_UUID_LEGACY_INDEX_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected UuidConfiguration newInstance(InclusionPolicies inclusionPolicies) {
        return new UuidConfiguration(inclusionPolicies, getUuidProperty(), getUuidLegacyIndexName());
    }

    public String getUuidProperty() {
        return uuidProperty;
    }

    public String getUuidLegacyIndexName() {
        return uuidLegacyIndexName;
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid property.
     *
     * @param uuidProperty of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidProperty(String uuidProperty) {
        return new UuidConfiguration(getInclusionPolicies(), uuidProperty, getUuidLegacyIndexName());
    }

    /**
     * Create a new instance of this {@link UuidConfiguration} with different uuid legacy index name.
     *
     * @param uuidLegacyIndexName of the new instance.
     * @return new instance.
     */
    public UuidConfiguration withUuidLegacyIndexName(String uuidLegacyIndexName) {
        return new UuidConfiguration(getInclusionPolicies(), getUuidProperty(), uuidLegacyIndexName);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        UuidConfiguration that = (UuidConfiguration) o;

        if (uuidLegacyIndexName != null ? !uuidLegacyIndexName.equals(that.uuidLegacyIndexName) : that.uuidLegacyIndexName != null)
            return false;
        if (uuidProperty != null ? !uuidProperty.equals(that.uuidProperty) : that.uuidProperty != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (uuidProperty != null ? uuidProperty.hashCode() : 0);
        result = 31 * result + (uuidLegacyIndexName != null ? uuidLegacyIndexName.hashCode() : 0);
        return result;
    }
}
