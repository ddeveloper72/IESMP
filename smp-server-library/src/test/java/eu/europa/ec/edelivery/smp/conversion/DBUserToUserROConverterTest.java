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
package eu.europa.ec.edelivery.smp.conversion;

import eu.europa.ec.edelivery.smp.data.dao.CredentialDao;
import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.user.DBCertificate;
import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.data.ui.UserRO;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Sebastian-Ion TINCU
 * @since 4.1
 */

@ExtendWith(MockitoExtension.class)
public class DBUserToUserROConverterTest {

    private DBUser source;

    private UserRO target;
    CredentialDao credentialDao = Mockito.mock(CredentialDao.class);
    ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);


    @InjectMocks
    private DBUserToUserROConverter converter = new DBUserToUserROConverter(credentialDao, configurationService);

    @Test
    public void returnsThePasswordAsNotExpiredForCertificateOnlyUsers() {
        givenAnExistingCertificateOnlyUser();

        whenConvertingTheExistingUser();

        thenThePasswordIsNotMarkedAsExpired("The password should have not been marked as expired when the user has no password");
    }

    @Test
    public void returnsThePasswordAsExpiredWhenConvertingAnExistingUserThatHasAPasswordThatHasBeenRecentlyReset() {
        givenAnExistingUserHavingAPasswordThatHasJustBeenReset();
        List<DBCredential> credentialList = source.getUserCredentials();
        Mockito.doReturn(credentialList).when(credentialDao).findUserCredentialForByUserIdTypeAndTarget(Mockito.any(),
                Mockito.any(CredentialType.class),
                Mockito.any(CredentialTargetType.class));

        whenConvertingTheExistingUser();

        thenThePasswordIsMarkedAsExpired("The passwords should be marked as expired when converting users" +
                " having passwords that have been reset by SystemAdministrators");
    }

    @Test
    public void returnsThePasswordAsNotExpiredWhenConvertingAnExistingUserThatHasAPasswordChangedNoLongerThanThreeMonthsAgo() {
        givenAnExistingUserHavingAPasswordThatChangedNoLongerThanThreeMonthsAgo();

        whenConvertingTheExistingUser();

        thenThePasswordIsNotMarkedAsExpired("The passwords should not be marked as expired when converting users having password they have changed in the previous 3 months");
    }

    @Test
    public void returnsThePasswordAsExpiredWhenConvertingAnExistingUserThatHasAPasswordChangedMoreThanThreeMonthsAgo() {
        givenAnExistingUserHavingAPasswordThatChangedMoreThanThreeMonthsAgo();
        List<DBCredential> credentialList = source.getUserCredentials();
        Mockito.doReturn(credentialList).when(credentialDao).findUserCredentialForByUserIdTypeAndTarget(Mockito.any(),
                Mockito.any(CredentialType.class),
                Mockito.any(CredentialTargetType.class));
        whenConvertingTheExistingUser();

        thenThePasswordIsMarkedAsExpired("The passwords should be marked as expired when converting users having password they have changed more than 3 months ago");
    }

    private void givenAnExistingCertificateOnlyUser() {
        givenAnExistingUser(null, null, new DBCertificate());
    }

    private void givenAnExistingUserHavingAPasswordThatHasJustBeenReset() {
        givenAnExistingUser("password", null, null);
    }

    private void givenAnExistingUserHavingAPasswordThatChangedNoLongerThanThreeMonthsAgo() {
        // some month has less than 29 days -therefore -27
        givenAnExistingUser("password", OffsetDateTime.now().minusMonths(2).minusDays(27), null);
    }

    private void givenAnExistingUserHavingAPasswordThatChangedMoreThanThreeMonthsAgo() {
        givenAnExistingUser("password", OffsetDateTime.now().minusMonths(3).minusDays(10), null);
    }

    private void givenAnExistingUser(String password, OffsetDateTime passwordChange, DBCertificate certificate) {
        source = new DBUser();

        Optional<DBCredential> optUserPassCred = source.getUserCredentials().stream().filter(credential -> credential.getCredentialType() == CredentialType.USERNAME_PASSWORD).findFirst();
        Optional<DBCredential> optCertCred = source.getUserCredentials().stream().filter(credential -> credential.getCredentialType() == CredentialType.CERTIFICATE).findFirst();

        if (StringUtils.isNotBlank(password)) {
            DBCredential credential = optUserPassCred.orElse(new DBCredential());
            if (credential.getUser() == null) {
                credential.setUser(source);
                credential.setCredentialType(CredentialType.USERNAME_PASSWORD);
                source.getUserCredentials().add(credential);
            }
            credential.setValue(password);
            credential.setChangedOn(passwordChange);
            credential.setExpireOn(passwordChange != null ? passwordChange.plusMonths(3) : null);
        } else if (optUserPassCred.isPresent()) {
            source.getUserCredentials().remove(optUserPassCred.get());
        }

        if (certificate != null) {
            DBCredential credential = optCertCred.orElse(new DBCredential());
            if (credential.getUser() == null) {
                credential.setUser(source);
                credential.setCredentialType(CredentialType.CERTIFICATE);
                source.getUserCredentials().add(credential);
            }
            credential.setCertificate(certificate);
            credential.setValue(certificate.getCertificateId());
            credential.setChangedOn(passwordChange);
            credential.setExpireOn(certificate.getValidTo());
            credential.setExpireOn(certificate.getValidFrom());
        } else if (optCertCred.isPresent()) {
            source.getUserCredentials().remove(optCertCred.get());
        }


    }

    private void whenConvertingTheExistingUser() {
        target = converter.convert(source);
    }

    private void thenThePasswordIsMarkedAsExpired(String failureDescription) {
        assertThat(target.isPasswordExpired())
                .describedAs(failureDescription)
                .isTrue();
    }

    private void thenThePasswordIsNotMarkedAsExpired(String failureDescription) {
        assertThat(target.isPasswordExpired())
                .describedAs(failureDescription)
                .isFalse();
    }
}
