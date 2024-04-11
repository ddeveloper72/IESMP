/*-
 * #START_LICENSE#
 * smp-server-library
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
package eu.europa.ec.edelivery.smp.services;

import eu.europa.ec.edelivery.smp.data.dao.CredentialDao;
import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import eu.europa.ec.edelivery.smp.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Credential validator service for validating and alerting usernames password expirations, access token expirations and
 * certificate expirations.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
@Service
public class CredentialValidatorService {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(CredentialValidatorService.class);

    private final CredentialsAlertService alertService;
    private final ConfigurationService configurationService;
    private final CredentialDao credentialDao;

    public CredentialValidatorService(ConfigurationService configurationService,
                                      CredentialsAlertService alertService,
                                      CredentialDao credentialDao) {
        this.configurationService = configurationService;
        this.alertService = alertService;
        this.credentialDao = credentialDao;
    }

    /**
     * Method validates username, access tokens, and certificates.
     * If the credentials are about to expire or are expired, it generates alerts.
     */
    public void validateCredentials() {
        if (skipCredentialValidation()) {
            LOG.debug("Skip Credentials validation");
            return;
        }
        validateCredentialsForBeforeExpireUsernames();
        validateCredentialsForExpiredUsernames();

        validateCredentialsForBeforeExpireAccessToken();
        validateCredentialsForExpiredAccessToken();

        validateCredentialsForBeforeExpireCertificate();
        validateCredentialsForExpiredCertificate();
    }

    protected void validateCredentialsForBeforeExpireUsernames() {

        Boolean alertBeforeExpire = configurationService.getAlertBeforeExpirePasswordEnabled();
        if (alertBeforeExpire == null || !alertBeforeExpire) {
            LOG.debug("Before expire user password validation is disabled");
            return;
        }
        List<DBCredential> dbUserBeforeExpireList = credentialDao.getBeforePasswordExpireUsersForAlerts(
                configurationService.getAlertBeforeExpirePasswordPeriod(),
                configurationService.getAlertBeforeExpirePasswordInterval(),
                configurationService.getAlertCredentialsBatchSize());

        dbUserBeforeExpireList.forEach(alertService::alertBeforeCredentialExpire);
    }

    protected void validateCredentialsForExpiredUsernames() {
        Boolean alertExpired = configurationService.getAlertExpiredPasswordEnabled();
        if (alertExpired == null || !alertExpired) {
            LOG.debug("Expire user password validation is disabled");
            return;
        }
        List<DBCredential> dbUserExpiredList = credentialDao.getPasswordExpiredUsersForAlerts(
                configurationService.getAlertExpiredPasswordPeriod(),
                configurationService.getAlertExpiredPasswordInterval(),
                configurationService.getAlertCredentialsBatchSize());
        dbUserExpiredList.forEach(alertService::alertCredentialExpired);
    }

    protected void validateCredentialsForBeforeExpireAccessToken() {

        Boolean alertBeforeExpire = configurationService.getAlertBeforeExpireAccessTokenEnabled();
        if (alertBeforeExpire == null || !alertBeforeExpire) {
            LOG.debug("Before expire user AccessToken validation is disabled");
            return;
        }
        List<DBCredential> dbUserBeforeExpireList = credentialDao.getBeforeAccessTokenExpireUsersForAlerts(
                configurationService.getAlertBeforeExpireAccessTokenPeriod(),
                configurationService.getAlertBeforeExpireAccessTokenInterval(),
                configurationService.getAlertCredentialsBatchSize());
        dbUserBeforeExpireList.forEach(alertService::alertBeforeCredentialExpire);
    }

    protected void validateCredentialsForExpiredAccessToken() {
        Boolean alertExpired = configurationService.getAlertExpiredAccessTokenEnabled();
        if (alertExpired == null || !alertExpired) {
            LOG.debug("Expire user AccessToken validation is disabled");
            return;
        }
        List<DBCredential> dbUserExpiredList = credentialDao.getAccessTokenExpiredUsersForAlerts(
                configurationService.getAlertExpiredAccessTokenPeriod(),
                configurationService.getAlertExpiredAccessTokenInterval(),
                configurationService.getAlertCredentialsBatchSize());
        dbUserExpiredList.forEach(alertService::alertCredentialExpired);
    }


    protected void validateCredentialsForBeforeExpireCertificate() {

        Boolean alertBeforeExpire = configurationService.getAlertBeforeExpireCertificateEnabled();
        if (alertBeforeExpire == null || !alertBeforeExpire) {
            LOG.debug("Before expire user Certificate validation is disabled");
            return;
        }
        List<DBCredential> dbUserBeforeExpireList = credentialDao.getBeforeCertificateExpireUsersForAlerts(
                configurationService.getAlertBeforeExpireCertificatePeriod(),
                configurationService.getAlertBeforeExpireCertificateInterval(),
                configurationService.getAlertCredentialsBatchSize());
        dbUserBeforeExpireList.forEach(alertService::alertBeforeCredentialExpire);
    }

    protected void validateCredentialsForExpiredCertificate() {
        Boolean alertExpired = configurationService.getAlertExpiredCertificateEnabled();
        if (alertExpired == null || !alertExpired) {
            LOG.debug("Expire user Certificate validation is disabled");
            return;
        }
        List<DBCredential> dbUserExpiredList = credentialDao.getCertificateExpiredUsersForAlerts(
                configurationService.getAlertExpiredCertificatePeriod(),
                configurationService.getAlertExpiredCertificateInterval(),
                configurationService.getAlertCredentialsBatchSize());
        dbUserExpiredList.forEach(alertService::alertCredentialExpired);
    }

    /**
     * Method returns true if credential validation should be skipped. The validation is skipped if SMP runs in cluster
     * and node server name is not "target" credential validation server.
     *
     * @return true if credential validation must be skipped
     */
    protected boolean skipCredentialValidation() {
        if (!configurationService.isClusterEnabled()) {
            LOG.debug("The server is not running in cluster mode. The Credential validation is not skipped!");
            return false;
        }
        String serverHost = HttpUtils.getServerAddress();
        String targetValidationServerHost = configurationService.getTargetServerForCredentialValidation();
        if (StringUtils.equalsIgnoreCase(serverHost, targetValidationServerHost)) {
            LOG.debug("The server host [{}] is targeted Credential validation server! The Credential validation is not skipped!", targetValidationServerHost);
            return false;
        }

        LOG.debug("The server host [{}] is not targeted Credential validation server [{}]. Skip credential validation!",
                serverHost, targetValidationServerHost);
        return true;
    }
}
