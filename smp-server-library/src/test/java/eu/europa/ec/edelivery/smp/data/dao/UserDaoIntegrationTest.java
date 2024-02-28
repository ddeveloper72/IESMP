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

import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.testutil.TestConstants;
import eu.europa.ec.edelivery.smp.testutil.TestDBUtils;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.INVALID_USER_NO_IDENTIFIERS;
import static org.junit.jupiter.api.Assertions.*;


/**
 * Purpose of class is to test all resource methods with database.
 *
 * @author Joze Rihtarsic
 * @since 4.1
 */
public class UserDaoIntegrationTest extends AbstractBaseDao {

    @Autowired
    UserDao testInstance;

    @Autowired
    CredentialDao credentialDao;

    @Autowired
    ResourceDao serviceGroupDao;

    @Test
    public void persistUserWithoutIdentifier() {
        // set
        DBUser u = new DBUser();
        SMPRuntimeException result = assertThrows(SMPRuntimeException.class, () -> testInstance.persistFlushDetach(u));

        MatcherAssert.assertThat(result.getMessage(), CoreMatchers.containsString(INVALID_USER_NO_IDENTIFIERS.getMessage()));
    }

    @Test
    public void persistUserWithUsername() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByUsername(TestConstants.USERNAME_1);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
        assertEquals(u.getUsername(), ou.get().getUsername());
    }

    @Test
    public void persistUserWithoutCredentials() {

        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);
        assertTrue(u.getUserCredentials().isEmpty());

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByUsername(TestConstants.USERNAME_1);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getUsername(), ou.get().getUsername());
        assertTrue(u.getUserCredentials().isEmpty());
    }

    @Test
    @Transactional
    public void persistUserWithUsernamePasswordCredential() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_2);
        DBCredential credential = TestDBUtils.createDBCredential(TestConstants.USERNAME_2);

        // execute
        testInstance.persistFlushDetach(u);
        credential.setUser(u);
        credentialDao.persistFlushDetach(credential);

        //test
        Optional<DBUser> ou = testInstance.findUserByUsername(TestConstants.USERNAME_2);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
        assertEquals(1, ou.get().getUserCredentials().size());
        assertEquals(credential.getValue(), ou.get().getUserCredentials().get(0).getValue());
        assertEquals(credential.getName(), ou.get().getUserCredentials().get(0).getName());
        assertEquals(credential.getCredentialTarget(), ou.get().getUserCredentials().get(0).getCredentialTarget());
        assertEquals(credential.getCredentialType(), ou.get().getUserCredentials().get(0).getCredentialType());

    }

    @Test
    @Transactional
    public void persistUserWithCertificate() {
        // set
        DBUser u = TestDBUtils.createDBUserByCertificate(TestConstants.USER_CERT_1);
        DBCredential credential = u.getUserCredentials().get(0);

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByCertificateId(TestConstants.USER_CERT_1);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
        assertEquals(1, ou.get().getUserCredentials().size());
        assertEquals(credential.getValue(), ou.get().getUserCredentials().get(0).getValue());
        assertEquals(credential.getName(), ou.get().getUserCredentials().get(0).getName());
        assertEquals(credential.getCredentialTarget(), ou.get().getUserCredentials().get(0).getCredentialTarget());
        assertEquals(credential.getCredentialType(), ou.get().getUserCredentials().get(0).getCredentialType());

        assertEquals(credential.getCertificate().getCertificateId(), ou.get().getUserCredentials().get(0).getCertificate().getCertificateId());
        assertEquals(credential.getCertificate().getValidFrom().toInstant(),
                ou.get().getUserCredentials().get(0).getCertificate().getValidFrom().toInstant());

        assertEquals(credential.getCertificate().getValidTo().toInstant(),
                ou.get().getUserCredentials().get(0).getCertificate().getValidTo().toInstant());
    }

    @Test
    public void findCertUserByIdentifier() {
        // set
        DBUser u = TestDBUtils.createDBUserByCertificate(TestConstants.USER_CERT_1);

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByIdentifier(TestConstants.USER_CERT_1);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
    }

    @Test
    @Transactional
    public void findUsernameUserByIdentifier() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);
        DBCredential credential = TestDBUtils.createDBCredentialForUserAccessToken(u, null, null, null);
        credential.setName(TestConstants.USERNAME_TOKEN_1);
        u.getUserCredentials().add(credential);
        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByIdentifier(TestConstants.USERNAME_TOKEN_1);
        assertNotSame(u, ou.get());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
    }

    @Test
    public void deleteUserWithCertificate() {
        // given
        DBUser u = TestDBUtils.createDBUserByCertificate(TestConstants.USER_CERT_1);
        DBCredential credential = u.getUserCredentials().get(0);

        testInstance.persistFlushDetach(u);
        assertNotNull(credential.getName());

        // when then
        testInstance.removeById(u.getId());
        //test
        Optional<DBUser> ou = testInstance.findUserByIdentifier(credential.getName());
        assertFalse(ou.isPresent());

    }

    @Test
    public void findBlankUsernameUser() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByIdentifier(null);
        assertFalse(ou.isPresent());

        ou = testInstance.findUserByIdentifier("");
        assertFalse(ou.isPresent());

        ou = testInstance.findUserByIdentifier(" ");
        assertFalse(ou.isPresent());
    }

    @Test
    public void findNotExistsUsernameUser() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1);

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByIdentifier(TestConstants.USERNAME_2);
        assertFalse(ou.isPresent());
    }

    @Test
    public void findCaseInsensitiveUsernameUser() {
        // set
        DBUser u = TestDBUtils.createDBUserByUsername(TestConstants.USERNAME_1.toLowerCase());

        // execute
        testInstance.persistFlushDetach(u);

        //test
        Optional<DBUser> ou = testInstance.findUserByUsername(TestConstants.USERNAME_1.toUpperCase());
        assertTrue(ou.isPresent());
        assertEquals(u, ou.get());
        assertEquals(u.getEmailAddress(), ou.get().getEmailAddress());
    }
}
