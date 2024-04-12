/*-
 * #START_LICENSE#
 * smp-webapp
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

package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.DBUserDeleteValidation;
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
import java.util.List;
import java.util.Optional;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;
import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY;
import static eu.europa.ec.edelivery.smp.exceptions.ErrorCode.INVALID_USER_NO_IDENTIFIERS;

/**
 * @author gutowpa
 * @since 3.0
 */
@Repository
public class UserDao extends BaseDao<DBUser> {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(UserDao.class);


    /**
     * Persists the user to the database. Before that test if user has identifiers. Usernames are saved to database in lower caps
     *
     * @param user
     */
    @Override
    @Transactional
    public void persistFlushDetach(DBUser user) {
        // update username to lower caps
        if (StringUtils.isBlank(user.getUsername())) {
            throw new SMPRuntimeException(INVALID_USER_NO_IDENTIFIERS);
        }
        user.setUsername(user.getUsername().toLowerCase());
        super.persistFlushDetach(user);
    }

    /**
     * Searches for a user entity by its primary key and returns it if found. Returns an empty {@code Optional} if missing.
     *
     * @param userId The primary key of the user entity to find
     * @return an optional user entity
     */
    public Optional<DBUser> findUser(Long userId) {
        DBUser dbUser = memEManager.find(DBUser.class, userId);
        return Optional.ofNullable(dbUser);
    }

    /**
     * Finds a user by identifier. User identifier is username or certificateId. First it tries to find user by username
     * and than by certificate id. If user does not exists Optional with isPresent - false is returned.
     *
     * @param identifier
     * @return resturns Optional DBUser for identifier
     */
    public Optional<DBUser> findUserByIdentifier(String identifier) {
        Optional<DBUser> usr = findUserByUsername(identifier);
        if (!usr.isPresent()) {
            LOG.info("Service group owner [{}] not found by username. Try with the access token!", identifier);
            usr = findUserByAuthenticationToken(identifier);
        }
        if (!usr.isPresent()) { // try to retrieve by identifier
            LOG.info("Service group owner  [{}] not found by username. Try with certificate id!", identifier);
            usr = findUserByCertificateId(identifier);
        }
        return usr;
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
        // authentication token is case-sensitive and is used only for REST_API
        return findUserByCredentialNameTargetName(false,
                tokeIdentifier,
                CredentialType.ACCESS_TOKEN,
                CredentialTargetType.REST_API);
    }


    /**
     * Method finds user by certificateId. If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param certificateId
     * @return returns Optional DBUser for certificateID
     */
    public Optional<DBUser> findUserByCertificateId(String certificateId) {
        // check if blank
        if (StringUtils.isBlank(certificateId)) {
            return Optional.empty();
        }
        return findUserByCertificateId(true, certificateId);
    }

    /**
     * Method finds user by certificateId. If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param certificateId
     * @param caseInsensitive
     * @return returns Optional DBUser for certificateID
     */
    public Optional<DBUser> findUserByCertificateId(boolean caseInsensitive, String certificateId) {
        if (StringUtils.isBlank(certificateId)) {
            return Optional.empty();
        }
        // Certificate identifier is used only for REST_API
        return findUserByCredentialNameTargetName(caseInsensitive, certificateId,
                CredentialType.CERTIFICATE,
                CredentialTargetType.REST_API);
    }


    /**
     * Method finds user by user credentials for credential name, type and target. If user identity token not exist
     * Optional  with isPresent - false is returned.
     *
     * @param credentialName       the name of the credential
     * @param credentialType       the type of the credential
     * @param credentialTargetType the target of the credential
     * @return returns Optional DBUser for username
     */
    public Optional<DBUser> findUserByCredentialNameTargetName(boolean caseInsensitive,
                                                               String credentialName,
                                                               CredentialType credentialType,
                                                               CredentialTargetType credentialTargetType) {
        // check if blank
        if (StringUtils.isBlank(credentialName)) {
            return Optional.empty();
        }
        try {
            String queryName = caseInsensitive ? QUERY_USER_BY_CI_CREDENTIAL_NAME_TYPE_TARGET : QUERY_USER_BY_CREDENTIAL_NAME_TYPE_TARGET;
            TypedQuery<DBUser> query = memEManager.createNamedQuery(queryName, DBUser.class);
            query.setParameter(PARAM_CREDENTIAL_NAME, StringUtils.trim(credentialName));
            query.setParameter(PARAM_CREDENTIAL_TYPE, credentialType);
            query.setParameter(PARAM_CREDENTIAL_TARGET, credentialTargetType);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, credentialName);
        }
    }

    /**
     * Method finds user by username.If user does not exist
     * Optional  with isPresent - false is returned.
     *
     * @param username
     * @return returns Optional DBUser for username
     */
    public Optional<DBUser> findUserByUsername(String username) {
        // check if blank
        if (StringUtils.isBlank(username)) {
            return Optional.empty();
        }
        try {
            TypedQuery<DBUser> query = memEManager.createNamedQuery(QUERY_USER_BY_CI_USERNAME, DBUser.class);
            query.setParameter(PARAM_USER_USERNAME, StringUtils.trim(username));
            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        } catch (NonUniqueResultException e) {
            throw new SMPRuntimeException(ILLEGAL_STATE_USERNAME_MULTIPLE_ENTRY, username);
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

    public List<DBUser> getFilteredUserList(int iPage, int iPageSize, String filter) {
        boolean hasFilter = StringUtils.isNotBlank(filter);
        TypedQuery<DBUser> query = memEManager.createNamedQuery(hasFilter ?
                QUERY_QUERY_USERS_FILTER : QUERY_USERS, DBUser.class);

        if (iPageSize > -1 && iPage > -1) {
            query.setFirstResult(iPage * iPageSize);
        }
        if (iPageSize > 0) {
            query.setMaxResults(iPageSize);
        }
        if (hasFilter) {
            query.setParameter(PARAM_USER_FILTER, StringUtils.wrapIfMissing(StringUtils.trim(filter), "%"));
        }
        return query.getResultList();
    }

    public Long getFilteredUserListCount(String filter) {
        boolean hasFilter = StringUtils.isNotBlank(filter);
        TypedQuery<Long> query = memEManager.createNamedQuery(hasFilter ? QUERY_USER_FILTER_COUNT : QUERY_USER_COUNT, Long.class);
        if (hasFilter) {
            query.setParameter(PARAM_USER_FILTER, StringUtils.wrapIfMissing(StringUtils.trim(filter), "%"));
        }
        return query.getSingleResult();
    }
}
