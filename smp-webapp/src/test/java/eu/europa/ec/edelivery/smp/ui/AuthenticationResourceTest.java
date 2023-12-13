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
package eu.europa.ec.edelivery.smp.ui;

import eu.europa.ec.edelivery.smp.auth.SMPAuthenticationService;
import eu.europa.ec.edelivery.smp.auth.SMPAuthorizationService;
import eu.europa.ec.edelivery.smp.data.ui.UserRO;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import eu.europa.ec.edelivery.smp.services.ui.UIUserService;
import eu.europa.ec.edelivery.smp.utils.SMPCookieWriter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import static eu.europa.ec.edelivery.smp.utils.SMPCookieWriter.SESSION_COOKIE_NAME;

public class AuthenticationResourceTest {

    SMPAuthenticationService authenticationService = Mockito.mock(SMPAuthenticationService.class);
    SMPAuthorizationService authorizationService = Mockito.mock(SMPAuthorizationService.class);
    ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);
    SMPCookieWriter smpCookieWriter = Mockito.mock(SMPCookieWriter.class);
    CsrfTokenRepository csrfTokenRepository = Mockito.mock(CsrfTokenRepository.class);
    UIUserService uiUserService = Mockito.mock(UIUserService.class);

    AuthenticationController testInstance = new AuthenticationController(authenticationService,
            authorizationService,
            configurationService,
            smpCookieWriter,
            csrfTokenRepository,
            uiUserService);

    @Test
    public void logout() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        Mockito.doNothing().when(authenticationService).logout(Mockito.any(), Mockito.any());
        testInstance.logout(request, response);

        Mockito.verify(authenticationService, Mockito.times(1)).logout(request, response);
    }

    @Test
    public void authenticateCAS() {

        RedirectView result = testInstance.authenticateCAS();
        Assert.assertNotNull(result);
        Assert.assertEquals("../../../#/", result.getUrl());
    }

    @Test
    public void getUser() {
        UserRO user = new UserRO();
        Mockito.doReturn(user).when(authorizationService).getLoggedUserData();
        UserRO result = testInstance.getUser();
        Assert.assertEquals(user, result);
    }

    @Test
    public void recreatedSessionCookie() {
        String cookieName = SESSION_COOKIE_NAME;
        String cookieValue = "CookieValue";
        boolean sessionCookieSecure = true;
        String sessionCookiePath = "getSessionCookiePath";
        String sessionCookieSameSite = "getSessionCookieSameSite";
        Integer sessionCookieMaxAge = 12;

        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
        HttpSession session = Mockito.mock(HttpSession.class);
        Mockito.doReturn(session).when(request).getSession(Mockito.anyBoolean());
        Mockito.doReturn(cookieValue).when(session).getId();
        Mockito.doReturn(sessionCookieSecure).when(configurationService).getSessionCookieSecure();
        Mockito.doReturn(sessionCookieMaxAge).when(configurationService).getSessionCookieMaxAge();
        Mockito.doReturn(sessionCookiePath).when(configurationService).getSessionCookiePath();
        Mockito.doReturn(sessionCookieSameSite).when(configurationService).getSessionCookieSameSite();


        Mockito.doNothing().when(smpCookieWriter).writeCookieToResponse(cookieName,
                cookieValue, sessionCookieSecure, sessionCookieMaxAge, sessionCookiePath, sessionCookieSameSite, request, response);

        testInstance.recreatedSessionCookie(request, response);

        Mockito.verify(smpCookieWriter, Mockito.times(1)).writeCookieToResponse(cookieName,
                cookieValue, sessionCookieSecure, sessionCookieMaxAge, sessionCookiePath, sessionCookieSameSite, request, response);
    }
}
