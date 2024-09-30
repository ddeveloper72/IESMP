/*-
 * #START_LICENSE#
 * smp-spi
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
package eu.europa.ec.smp.spi.api;


import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;

import java.util.List;

/**
 * Class contains useful utils for retrieving data from the DomiSMP
 */
public interface SmpDataServiceApi {

    /**
     * Return subresource identifiers with subresource definition and resource
     *
     * @param identifier of the resource
     * @param subresourceDefinitionIdentifier identifier of the subresource
     * @return list of subresource identifiers
     */
    List<ResourceIdentifier> getSubResourceIdentifiers(ResourceIdentifier identifier, String subresourceDefinitionIdentifier);


    /**
     * The request returns requestor URL with only root context
     *
     * @return
     */
    String getResourceUrl();

    String getURIPathSegmentForSubresource(String name);

}
