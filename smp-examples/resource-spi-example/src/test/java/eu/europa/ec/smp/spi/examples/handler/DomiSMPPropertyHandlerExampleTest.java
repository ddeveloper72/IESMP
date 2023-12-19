/*-
 * #START_LICENSE#
 * resource-spi-example
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
package eu.europa.ec.smp.spi.examples.handler;

import eu.europa.ec.smp.spi.api.model.ResourceIdentifier;
import eu.europa.ec.smp.spi.exceptions.ResourceException;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertThrows;

class DomiSMPPropertyHandlerExampleTest extends AbstractHandlerTest {

    @Override
    AbstractHandler getTestInstance() {
        return new DomiSMPPropertyHandlerExample(mockSmpDataApi, mockSmpIdentifierServiceApi, mockSignatureApi);
    }

    @Test
    void testGenerateResource() throws ResourceException {

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test-test-test");

        generateResourceAction(resourceIdentifier);
    }

    @Test
    void validateResourceOK() throws ResourceException {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test-test-test");
        // validate
        validateResourceAction("/examples/properties_ok.properties", resourceIdentifier);
    }


    @Test
    void validateResourceInvalidIdentifier() {
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("urn:poland:ncpb:utestt", "test-test-test");
        // validate
        ResourceException result = assertThrows(ResourceException.class,
                () -> validateResourceAction("/examples/properties_ok.properties", resourceIdentifier));
        MatcherAssert.assertThat(result.getMessage(), org.hamcrest.Matchers.containsString("Property: [domismp.extension.example.identifier] does not match value for the resource"));
    }

    @Test
    void validateResourceInvalidScheme() {

        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test1-test-test");
        // validate
        ResourceException result = assertThrows(ResourceException.class,
                () -> validateResourceAction("/examples/properties_ok.properties", resourceIdentifier));
        MatcherAssert.assertThat(result.getMessage(), org.hamcrest.Matchers.containsString("Property: [domismp.extension.example.identifier] does not match value for the resource"));
    }

    @Test
    void readResourceOK() throws ResourceException {
        String resourceName = "/examples/properties_ok.properties";
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test-test-test");

        readResourceAction(resourceName, resourceIdentifier);
    }

    @Test
    void storeResourceOK() throws ResourceException {
        String resourceName = "/examples/properties_ok.properties";
        ResourceIdentifier resourceIdentifier = new ResourceIdentifier("test-identifier", "test-test-test");

        storeResourceAction(resourceName, resourceIdentifier);
    }

}
