/*-
 * #%L
 * smp-webapp
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

package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.DBUserDeleteValidation;
import eu.europa.ec.edelivery.smp.data.model.user.DBCredential;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.exceptions.SMPRuntimeException;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;
import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.ILLEGAL_STATE_CERT_ID_MULTIPLE_ENTRY;
import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
@Repository
public class CredentialDao extends BaseDao<DBCredential> {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(CredentialDao.class);
    private static final String QUERY_PARAM_ALERT_CREDENTIAL_START_ALERT_SEND_DATE = "start_alert_send_date";
    private static final String QUERY_PARAM_ALERT_CREDENTIAL_END_DATE = "endAlertDate";
    private static final String QUERY_PARAM_ALERT_CREDENTIAL_EXPIRE_TEST_DATE = "expire_test_date";
    private static final String QUERY_PARAM_ALERT_CREDENTIAL_LAST_ALERT_DATE = "lastSendAlertDate";


    /**
     * Persists the user to the database. Before that test if user has identifiers. Usernames are saved to database in lower caps
     *
     * @param user
     */
    @Override
    @Transactional
    public void persistFlushDetach(DBCredential user) {
        super.persistFlushDetach(user);
    }

    /**
     * Searches for a user entity by its primary key and returns it if found. Returns an empty {@code Optional} if missing.
     *
     * @param credentialId The primary key of the user entity to find
     * @return an optional user entity
     */
    public Optional<DBCredential> findCredential(Long credentialId) {
        DBCredential dbUser = memEManager.find(DBCredential.class, credentialId);
        return Optional.ofNullable(dbUser);
    }

    /**
     * Method finds user by username.If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param username
     * @return returns Optional DBUser for username
     */
    public Optional<DBCredential> findUsernamePasswordCredentialForUsernameAndUI(String username) {
        // check if blank
        if (StringUtils.isBlank(username)) {
            return Optional.empty();
        }
        try {
            TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIALS_BY_CI_USERNAME_CREDENTIAL_TYPE_TARGET, DBCredential.class);
            query.setParameter(PARAM_USER_USERNAME, StringUtils.trim(username));
            query.setParameter(PARAM_CREDENTIAL_TYPE, CredentialType.USERNAME_PASSWORD);
            query.setParameter(PARAM_CREDENTIAL_TARGET, CredentialTargetType.UI);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, username);
        }
    }

    /**
     * Method finds username/password credential for user id.If user does not exist
     * an empty Optional is returned. If there are more than one credential the SMPRuntimeException is thrown
     *
     * @param userId
     * @return returns Optional DBUser for username
     * @throws SMPRuntimeException if more than one username/password credential is found!
     */
    public Optional<DBCredential> findUsernamePasswordCredentialForUserIdAndUI(Long userId) {
        // check if blank
        if (userId == null) {
            return Optional.empty();
        }
        List<DBCredential> list = findUserCredentialForByUserIdTypeAndTarget(userId,
                CredentialType.USERNAME_PASSWORD,
                CredentialTargetType.UI);

        if (list.isEmpty()) {
            return Optional.empty();
        } else if (list.size() > 1) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, userId);
        }
        return Optional.of(list.get(0));
    }

    /**
     * Method finds user by username.If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param accessToken
     * @return returns Optional DBUser for username
     */
    public Optional<DBCredential> findAccessTokenCredentialForAPI(String accessToken) {
        // check if blank
        if (StringUtils.isBlank(accessToken)) {
            return Optional.empty();
        }
        try {
            TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIAL_BY_CREDENTIAL_NAME_TYPE_TARGET, DBCredential.class);
            query.setParameter(PARAM_CREDENTIAL_NAME, StringUtils.trimToEmpty(accessToken));
            query.setParameter(PARAM_CREDENTIAL_TYPE, CredentialType.ACCESS_TOKEN);
            query.setParameter(PARAM_CREDENTIAL_TARGET, CredentialTargetType.REST_API);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, accessToken);
        }
    }

    public List<DBCredential> findUserCredentialForByUserIdTypeAndTarget(Long userId,
                                                                         CredentialType credentialType,
                                                                         CredentialTargetType credentialTargetType) {

        TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIALS_BY_USERID_CREDENTIAL_TYPE_TARGET, DBCredential.class);
        query.setParameter(PARAM_USER_ID, userId);
        query.setParameter(PARAM_CREDENTIAL_TYPE, credentialType);
        query.setParameter(PARAM_CREDENTIAL_TARGET, credentialTargetType);
        return query.getResultList();
    }

    public List<DBCredential> findAll() {
        // check if blank
        TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIAL_ALL, DBCredential.class);
        return query.getResultList();
    }

    /**
     * Method finds user by user authentication token identifier. If user identity token not exist
     * Optional  with isPresent - false is returned.
     *
     * @param tokeIdentifier
     * @return returns Optional DBUser for username
     */
    public Optional<DBUser> findUserByAuthenticationToken(String tokeIdentifier) {
        // check if blank
        if (StringUtils.isBlank(tokeIdentifier)) {
            return Optional.empty();
        }
        try {
            TypedQuery<DBUser> query = memEManager.createNamedQuery("DBUser.getUserByPatId", DBUser.class);
            query.setParameter("patId", tokeIdentifier.trim());
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, tokeIdentifier);
        }
    }

    /**
     * Get users with credentials which are about to expire, and they were not yet notified in alertInterval period
     * @param credentialType - the credential type to send alert
     * @param beforeStartDays - days before password is expired and the alerting starts
     * @param alertInterval - how many days must past since last alert before we can send next alert
     * @param maxAlertsInBatch - max number of alerts we can process in on batch
     * @return
     */
    public List<DBCredential> getCredentialsBeforeExpireForAlerts(CredentialType credentialType, int beforeStartDays, int alertInterval, int maxAlertsInBatch) {

        OffsetDateTime expireTestDate = OffsetDateTime.now();
        OffsetDateTime startAlertSendDate = expireTestDate.plusDays(beforeStartDays);
        OffsetDateTime lastSendAlertDate = expireTestDate.minusDays(alertInterval);

        TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIAL_BEFORE_EXPIRE, DBCredential.class);

        query.setParameter(PARAM_CREDENTIAL_TYPE, credentialType );
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_START_ALERT_SEND_DATE, startAlertSendDate);
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_EXPIRE_TEST_DATE, expireTestDate);
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_LAST_ALERT_DATE, lastSendAlertDate);
        query.setMaxResults(maxAlertsInBatch);
        return query.getResultList();
    }
    /**
     * Get users with passwords which are about to expire, and they were not yet notified in alertInterval period
     * @param credentialType - the credential type to send alert
     * @param alertPeriodDays - days before password is expired and the alerting starts
     * @param alertInterval - how many days must past since last alert before we can send next alert
     * @param maxAlertsInBatch - max number of alerts we can process in on batch
     * @return
     */
    public List<DBCredential> getUsersWithExpiredCredentialsForAlerts(CredentialType credentialType, int alertPeriodDays, int alertInterval, int maxAlertsInBatch) {
        OffsetDateTime expireDate = OffsetDateTime.now();
        // the alert period must be less than expire day
        OffsetDateTime startDateTime = expireDate.minusDays(alertPeriodDays);
        OffsetDateTime lastSendAlertDate = expireDate.minusDays(alertInterval);

        TypedQuery<DBCredential> query = memEManager.createNamedQuery(QUERY_CREDENTIAL_EXPIRED, DBCredential.class);
        query.setParameter(PARAM_CREDENTIAL_TYPE, credentialType );
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_END_DATE, startDateTime);
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_EXPIRE_TEST_DATE, expireDate);
        query.setParameter(QUERY_PARAM_ALERT_CREDENTIAL_LAST_ALERT_DATE, lastSendAlertDate);
        query.setMaxResults(maxAlertsInBatch);
        return query.getResultList();
    }

    public List<DBCredential> getBeforePasswordExpireUsersForAlerts(int beforeStartDays, int alertInterval, int maxAlertsInBatch) {
        return getCredentialsBeforeExpireForAlerts(CredentialType.USERNAME_PASSWORD, beforeStartDays, alertInterval, maxAlertsInBatch);
    }

    public List<DBCredential> getPasswordExpiredUsersForAlerts(int alertPeriodDays, int alertInterval, int maxAlertsInBatch) {
        return getUsersWithExpiredCredentialsForAlerts(CredentialType.USERNAME_PASSWORD, alertPeriodDays, alertInterval, maxAlertsInBatch);
    }

    public List<DBCredential> getBeforeAccessTokenExpireUsersForAlerts(int beforeStartDays, int alertInterval, int maxAlertsInBatch) {
        return getCredentialsBeforeExpireForAlerts(CredentialType.ACCESS_TOKEN, beforeStartDays, alertInterval, maxAlertsInBatch);
    }

    public List<DBCredential> getAccessTokenExpiredUsersForAlerts(int alertPeriodDays, int alertInterval, int maxAlertsInBatch) {
        return getUsersWithExpiredCredentialsForAlerts(CredentialType.ACCESS_TOKEN, alertPeriodDays, alertInterval, maxAlertsInBatch);
    }

    public List<DBCredential> getBeforeCertificateExpireUsersForAlerts(int beforeStartDays, int alertInterval, int maxAlertsInBatch) {
        return getCredentialsBeforeExpireForAlerts(CredentialType.CERTIFICATE, beforeStartDays, alertInterval, maxAlertsInBatch);
    }

    public List<DBCredential> getCertificateExpiredUsersForAlerts(int alertPeriodDays, int alertInterval, int maxAlertsInBatch) {
        return getUsersWithExpiredCredentialsForAlerts(CredentialType.CERTIFICATE, alertPeriodDays, alertInterval, maxAlertsInBatch);
    }

    /**
     * Method finds user by certificateId. If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param certificateId
     * @return returns Optional DBUser for certificateID
     */
    public Optional<DBCredential> findUserByCertificateId(String certificateId) {
        return findUserByCertificateId(certificateId, true);
    }

    /**
     * Method finds user by certificateId. If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param certificateId
     * @param caseInsensitive
     * @return returns Optional DBUser for certificateID
     */
    public Optional<DBCredential> findUserByCertificateId(String certificateId, boolean caseInsensitive) {
        try {
            String namedQuery = caseInsensitive ? QUERY_CREDENTIAL_BY_CI_CERTIFICATE_ID : QUERY_CREDENTIAL_BY_CERTIFICATE_ID;
            TypedQuery<DBCredential> query = memEManager.createNamedQuery(namedQuery, DBCredential.class);
            query.setParameter(PARAM_CERTIFICATE_IDENTIFIER, certificateId);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_CERT_ID_MULTIPLE_ENTRY, certificateId);
        }
    }

    /**
     * Validation report for users which owns service group
     *
     * @param userIds
     * @return
     */
    public List<DBUserDeleteValidation> validateUsersForDelete(List<Long> userIds) {
        TypedQuery<DBUserDeleteValidation> query = memEManager.createNamedQuery("DBUserDeleteValidation.validateUsersForOwnership",
                DBUserDeleteValidation.class);
        query.setParameter("idList", userIds);
        return query.getResultList();
    }

    @Transactional
    public void updateAlertSentForUserCredentials(DBCredential credential,  OffsetDateTime dateTime) {
        // attach to jpa session of not already
        DBCredential managedCredential = find(credential.getId());
        managedCredential.setExpireAlertOn(dateTime);
    }



}
