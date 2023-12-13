/*-
 * #%L
 * oasis-cppa3-spi
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
 * #L%
 */
package eu.europa.ec.smp.spi.def;

import eu.europa.ec.smp.spi.handler.OasisCppa3CppHandler;
import eu.europa.ec.smp.spi.resource.ResourceDefinitionSpi;
import eu.europa.ec.smp.spi.resource.ResourceHandlerSpi;
import eu.europa.ec.smp.spi.resource.SubresourceDefinitionSpi;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;


/**
 * The Oasis CPPA cpp document
 *
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Component
public class OasisCppaCppDocument implements ResourceDefinitionSpi {


    OasisCppa3CppHandler serviceGroup10Handler;

    public OasisCppaCppDocument(OasisCppa3CppHandler serviceGroup10Handler) {
        this.serviceGroup10Handler = serviceGroup10Handler;
    }

    @Override
    public String identifier() {
        return "edelivery-oasis-cppa-3.0-cpp";
    }

    @Override
    public String defaultUrlSegment() {
        return "cpp";
    }

    @Override
    public String name() {
        return "Oasis CPPA3 CPP document";
    }

    @Override
    public String description() {
        return "Oasis CPPA-CPP document";
    }

    @Override
    public String mimeType() {
        return "text/xml";
    }

    @Override
    public List<SubresourceDefinitionSpi> getSubresourceSpiList() {
        return Collections.emptyList();
    }

    @Override
    public ResourceHandlerSpi getResourceHandler() {
        return serviceGroup10Handler;
    }

    @Override
    public String toString() {
        return "OasisCppaCppDocument {" +
                "identifier=" + identifier() +
                "defaultUrlSegment=" + defaultUrlSegment() +
                "name=" + name() +
                "mimeType=" + mimeType() +
                '}';
    }
}
