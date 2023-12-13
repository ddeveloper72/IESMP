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
package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.data.ui.CredentialRO;
import eu.europa.ec.edelivery.smp.utils.SessionSecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;


/**
 * @author Sebastian-Ion TINCU
 */
@Component
public class CredentialROToDBCredentialConverter implements Converter<CredentialRO, DBCredential> {

    @Override
    public DBCredential convert(CredentialRO source) {

        DBCredential target = new DBCredential();
        if (StringUtils.isNotBlank(source.getCredentialId())) {
            target.setId(SessionSecurityUtils.decryptEntityId(source.getCredentialId()));
        }
        target.setName(source.getName());
        target.setCredentialType(source.getCredentialType());
        target.setActive(source.isActive());
        target.setDescription(source.getDescription());
        target.setSequentialLoginFailureCount(source.getSequentialLoginFailureCount());
        target.setLastFailedLoginAttempt(source.getLastFailedLoginAttempt());
        target.setActiveFrom(source.getActiveFrom());
        target.setExpireOn(source.getExpireOn());
        target.setChangedOn(source.getUpdatedOn());
        target.setSequentialLoginFailureCount(source.getSequentialLoginFailureCount());
        return target;
    }

}
