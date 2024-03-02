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
package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.user.DBCertificate;
import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.testutil.TestConstants;
import eu.europa.ec.edelivery.smp.testutil.TestDBUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.lowerCase;
import static org.apache.commons.lang3.StringUtils.upperCase;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
class CredentialDaoIntegrationTest extends AbstractBaseDao {

    @Autowired
    UserDao userDao;

    @Autowired
    CredentialDao testInstance;

    @Test
    void findUsernameCredentialForUsername() {
        DBCredential credential = TestDBUtils.createDBCredential(TestConstants.USERNAME_1, "TEST", CredentialType.USERNAME_PASSWORD, CredentialTargetType.UI);
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);
        // execute
        userDao.persistFlushDetach(u);
        credential.setUser(u);
        testInstance.persistFlushDetach(credential);

        //test
        Optional<DBCredential> ou = testInstance.findUsernamePasswordCredentialForUsernameAndUI(TestConstants.USERNAME_1);
        assertNotSame(credential, ou.get());
        assertEquals(credential, ou.get());
        assertEquals(credential.getCredentialType(), ou.get().getCredentialType());
        assertEquals(credential.getCredentialTarget(), ou.get().getCredentialTarget());
        assertEquals(credential.getName(), ou.get().getName());
        assertEquals(credential.getValue(), ou.get().getValue());

        assertEquals(u.getEmailAddress(), ou.get().getUser().getEmailAddress());
        assertEquals(u.getUsername(), ou.get().getUser().getUsername());
    }

    @Test
    void findUsernameCredentialForUsernameCaseInsensitive() {
        String username = lowerCase(TestConstants.USERNAME_1);
        DBCredential credential = TestDBUtils.createDBCredential(username, "TEST", CredentialType.USERNAME_PASSWORD, CredentialTargetType.UI);
        DBUser u = TestDBUtils.createDBUserByUsername(username);
        // execute
        userDao.persistFlushDetach(u);
        credential.setUser(u);
        testInstance.persistFlushDetach(credential);

        //test
        Optional<DBCredential> ou = testInstance.findUsernamePasswordCredentialForUsernameAndUI(upperCase(TestConstants.USERNAME_1));
        assertNotSame(credential, ou.get());
        assertEquals(credential, ou.get());
        assertEquals(credential.getCredentialType(), ou.get().getCredentialType());
        assertEquals(credential.getCredentialTarget(), ou.get().getCredentialTarget());
        assertEquals(credential.getName(), ou.get().getName());
        assertEquals(credential.getValue(), ou.get().getValue());

        assertEquals(u.getEmailAddress(), ou.get().getUser().getEmailAddress());
        assertEquals(u.getUsername(), ou.get().getUser().getUsername());
    }

    @Test
    void findCertificateCredential() {
        String username = lowerCase(TestConstants.USERNAME_1);
        DBCredential credential = TestDBUtils.createDBCredential(username, "TEST", CredentialType.CERTIFICATE, CredentialTargetType.REST_API);
        DBCertificate certificate = TestDBUtils.createDBCertificate();
        credential.setCertificate(certificate);
        DBUser u = TestDBUtils.createDBUserByUsername(username);
        // execute
        userDao.persistFlushDetach(u);
        credential.setUser(u);
        testInstance.persistFlushDetach(credential);

        //test
        Optional<DBCredential> ou = testInstance.findCredential(credential.getId());
        assertNotSame(credential, ou.get());
        assertEquals(credential, ou.get());
        assertEquals(credential.getCredentialType(), ou.get().getCredentialType());
        assertEquals(credential.getCredentialTarget(), ou.get().getCredentialTarget());
        assertEquals(credential.getName(), ou.get().getName());
        assertEquals(credential.getValue(), ou.get().getValue());
        assertNotNull(credential.getCertificate());

    }


}
