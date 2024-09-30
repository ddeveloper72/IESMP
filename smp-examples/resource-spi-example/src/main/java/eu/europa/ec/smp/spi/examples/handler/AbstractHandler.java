/*-
 * #START_LICENSE#
 * resource-spi-example
 * %%
 * Copyright (C) 2017 - 2024 European Commission | eDelivery | DomiSMP
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
package eu.europa.ec.smp.spi.examples.handler;

import eu.europa.ec.smp.spi.api.model.RequestData;
import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import eu.europa.ec.smp.spi.resource.ResourceHandlerSpi;
import org.apache.commons.lang3.StringUtils;

/**
 * The abstract class with common methods for implementation of the  ResourceHandlerSpi.
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
public abstract class AbstractHandler implements ResourceHandlerSpi {

    public ResourceIdentifier getResourceIdentifier(RequestData resourceData) throws ResourceException {
        if (resourceData == null || resourceData.getResourceIdentifier() == null || StringUtils.isEmpty(resourceData.getResourceIdentifier().getValue())) {
            throw new ResourceException(ResourceException.ErrorCode.INVALID_PARAMETERS, "Missing resource identifier for the resource CPP ");
        }
        return resourceData.getResourceIdentifier();
    }
}
