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
package eu.europa.ec.edelivery.smp.utils;

import eu.europa.ec.edelivery.security.utils.SecurityUtils;
import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationToken;
import eu.europa.ec.edelivery.smp.auth.SMPCertificateAuthentication;
import eu.europa.ec.edelivery.smp.auth.SMPUserDetails;
import eu.europa.ec.edelivery.smp.auth.UILoginAuthenticationToken;
import eu.europa.ec.edelivery.smp.logging.SMPLogger;
import eu.europa.ec.edelivery.smp.logging.SMPLoggerFactory;
import org.springframework.security.cas.authentication.CasAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class provides common session security tools to enhance UI security.
 * Session is enabled only for UI. To increase security SMP encrypt sensitive data. The
 * class provides tools for encrypting, decrypting data for the session.
 *
 * @author Joze Rihtarsic
 * @since 4.2
 */
public class SessionSecurityUtils {
    private static final SMPLogger LOG = SMPLoggerFactory.getLogger(SessionSecurityUtils.class);


    protected SessionSecurityUtils() {
    }

    /**
     * Current supported SMP authentication tokens.
     */
    protected static final List<Class> sessionAuthenticationClasses = Arrays.asList(
            UILoginAuthenticationToken.class,
            CasAuthenticationToken.class,
            SMPAuthenticationToken.class,
            SMPCertificateAuthentication.class);

    /**
     * SMP uses entity ids type long. Because the keys are sequence keys, SMP encrypts ids for the User.
     *
     * @param id
     * @return
     */
    public static String encryptedEntityId(Long id) {
        if (id == null) {
            return null;
        }
        SecurityUtils.Secret secret = getAuthenticationSecret();
        String idValue = id.toString();
        if (secret == null) {
            return idValue;
        }
        String valWithSeed = idValue + '#' + Calendar.getInstance().getTimeInMillis();
        return SecurityUtils.encryptURLSafe(secret, valWithSeed);
    }


    public static Long decryptEntityId(String id) {
        if (id == null) {
            return null;
        }
        SecurityUtils.Secret secret = getAuthenticationSecret();
        if (secret == null) {
            // try to convert to long value
            return Long.valueOf(id);
        }
        String decVal = SecurityUtils.decryptUrlSafe(secret, id);
        int indexOfSeparator = decVal.indexOf('#');
        String value = indexOfSeparator > -1 ? decVal.substring(0, indexOfSeparator) : decVal;
        return Long.valueOf(value);
    }

    public static Authentication getSessionAuthentication() {
        if (SecurityContextHolder.getContext() == null) {
            LOG.warn("No Security context!");
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            LOG.warn("No active Authentication!");
            return null;
        }

        if (getSessionAuthenticationClasses().contains(authentication.getClass())) {
            return authentication;
        }

        LOG.warn("Authentication class [{}] is not session enabled class types: [{}]!", authentication.getClass(),
                getSessionAuthenticationClasses().stream().map(Class::getName).collect(Collectors.toList()));
        return null;
    }

    public static SMPUserDetails getSessionUserDetails() {

        Authentication authentication = getSessionAuthentication();
        if (authentication == null) {
            LOG.warn("No active SMP session Authentication!");
            return null;
        }

        if (authentication instanceof UILoginAuthenticationToken) {
            LOG.debug("Return user details from SMPAuthenticationToken");
            return ((UILoginAuthenticationToken) authentication).getUserDetails();
        }

        if (authentication instanceof SMPAuthenticationToken) {
            LOG.debug("Return user details from SMPAuthenticationToken");
            return ((SMPAuthenticationToken) authentication).getUserDetails();
        }

        if (authentication instanceof CasAuthenticationToken) {
            LOG.debug("Return session secret from CasAuthenticationToken");
            return (SMPUserDetails) ((CasAuthenticationToken) authentication).getUserDetails();
        }

        LOG.warn("Authentication class [{}] is not session enabled class types: [{}]!", authentication.getClass(),
                getSessionAuthenticationClasses().stream().map(Class::getName).collect(Collectors.toList()));
        return null;
    }

    public static Long getSessionUserId() {
        SMPUserDetails smpUserDetails = getSessionUserDetails();
        if (smpUserDetails == null) {
            LOG.warn("No SMPUserDetails object!");
            return null;
        }
        if (smpUserDetails.getUser() == null) {
            LOG.warn("No User object in SMPUserDetails!");
            return null;
        }
        return smpUserDetails.getUser().getId();
    }

    public static SecurityUtils.Secret getAuthenticationSecret() {
        SMPUserDetails smpUserDetails = getSessionUserDetails();
        if (smpUserDetails == null) {
            LOG.warn("No SMPUserDetails object!");
            return null;
        }
        return smpUserDetails.getSessionSecret();
    }

    /**
     * Method returns authentication name  for logging
     *
     * @return authentication name
     */
    public static String getAuthenticationName() {
        if (SecurityContextHolder.getContext() == null) {
            LOG.debug("No Security context!");
            return null;
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            LOG.debug("No active Authentication!");
            return null;
        }
        return authentication.getName();
    }

    public static List<Class> getSessionAuthenticationClasses() {
        return sessionAuthenticationClasses;
    }
}
