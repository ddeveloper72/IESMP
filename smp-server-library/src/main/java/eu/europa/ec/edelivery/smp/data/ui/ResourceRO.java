/*-
 * #START_LICENSE#
 * smp-server-library
 * %%
 * Copyright (C) 2017 - 2023 European Commission | eDelivery | DomiSMP
 * %%
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by the European Commission - subsequent
 * versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 * 
 * [PROJECT_HOME]\license\eupl-1.2\license.txt or https://joinup.ec.europa.eu/collection/eupl/eupl-text-eupl-12
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is
 * distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 * #END_LICENSE#
 */
package eu.europa.ec.edelivery.smp.data.ui;


import eu.europa.ec.edelivery.smp.data.enums.VisibilityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */

public class ResourceRO extends BaseRO {

    private static final long serialVersionUID = 9008583888835630028L;

    private String resourceId;

    private String resourceTypeIdentifier;

    private String identifierValue;

    private String identifierScheme;

    private boolean smlRegistered = false;

    private VisibilityType visibility = VisibilityType.PUBLIC;

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceTypeIdentifier() {
        return resourceTypeIdentifier;
    }

    public void setResourceTypeIdentifier(String resourceTypeIdentifier) {
        this.resourceTypeIdentifier = resourceTypeIdentifier;
    }

    public String getIdentifierValue() {
        return identifierValue;
    }

    public void setIdentifierValue(String identifierValue) {
        this.identifierValue = identifierValue;
    }

    public String getIdentifierScheme() {
        return identifierScheme;
    }

    public void setIdentifierScheme(String identifierScheme) {
        this.identifierScheme = identifierScheme;
    }

    public boolean isSmlRegistered() {
        return smlRegistered;
    }

    public void setSmlRegistered(boolean smlRegistered) {
        this.smlRegistered = smlRegistered;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }

    @Override
    public String toString() {
        return "ResourceRO{" +
                "resourceId='" + resourceId + '\'' +
                ", identifierValue='" + identifierValue + '\'' +
                ", identifierScheme='" + identifierScheme + '\'' +
                ", smlRegistered=" + smlRegistered +
                ", visibility=" + visibility +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ResourceRO that = (ResourceRO) o;

        return new EqualsBuilder().append(smlRegistered, that.smlRegistered).append(resourceId, that.resourceId).append(identifierValue, that.identifierValue).append(identifierScheme, that.identifierScheme).append(visibility, that.visibility).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(resourceId).append(identifierValue).append(identifierScheme).toHashCode();
    }
}
