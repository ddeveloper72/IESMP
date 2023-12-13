/*-
 * #%L
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
 * #L%
 */
package eu.europa.ec.edelivery.smp.sml;

import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.identifiers.Identifier;



public class SmlConnectorTestConstants {

    protected static final Identifier PARTICIPANT_ID =  new Identifier("sample:value", "sample:scheme");
    protected static final DBDomain DEFAULT_DOMAIN;

    static {
        DEFAULT_DOMAIN = new DBDomain();
        DEFAULT_DOMAIN.setDomainCode("default_domain_id");
        DEFAULT_DOMAIN.setSmlSmpId("SAMPLE-SMP-ID");
    }

    protected static final String ERROR_UNEXPECTED_MESSAGE = "[ERR-106] Something unexpected happened";
    protected static final String ERROR_SMP_NOT_EXISTS = "[ERR-100] The SMP '" + DEFAULT_DOMAIN.getSmlSmpId() + "' doesn't exist";
    protected static final String ERROR_SMP_ALREADY_EXISTS = "[ERR-106] The SMP '" + DEFAULT_DOMAIN.getSmlSmpId() + "' already exists";
    protected static final String ERROR_PI_ALREADY_EXISTS = "[ERR-106] The participant identifier 'sample:value' with scheme: 'sample:scheme' already exist";
    protected static final String ERROR_PI_NO_EXISTS = "[ERR-110] At least one of the participants doesn't exist in the list";

}
