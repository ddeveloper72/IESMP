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
package eu.europa.ec.edelivery.smp.ui.internal;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.ec.edelivery.smp.data.ui.CertificateRO;
import eu.europa.ec.edelivery.smp.data.ui.UserRO;
import eu.europa.ec.edelivery.smp.services.ui.UITruststoreService;
import eu.europa.ec.edelivery.smp.test.SmpTestWebAppConfig;
import eu.europa.ec.edelivery.smp.test.testutils.X509CertificateTestUtils;
import eu.europa.ec.edelivery.smp.ui.AbstractControllerTest;
import eu.europa.ec.edelivery.smp.ui.external.UserResourceIT;
import org.apache.commons.io.IOUtils;
import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.List;

import static eu.europa.ec.edelivery.smp.test.testutils.MockMvcUtils.*;
import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.CONTEXT_PATH_INTERNAL_TRUSTSTORE;
import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.CONTEXT_PATH_PUBLIC_TRUSTSTORE;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {SmpTestWebAppConfig.class, UITruststoreService.class})
public class TruststoreAdminControllerIT extends AbstractControllerTest {
    private static final String PATH_INTERNAL = CONTEXT_PATH_INTERNAL_TRUSTSTORE;
    private static final String PATH_PUBLIC = CONTEXT_PATH_PUBLIC_TRUSTSTORE;

    @Autowired
    private WebApplicationContext webAppContext;

    @Autowired
    private UITruststoreService uiTruststoreService;

    private MockMvc mvc;

    @BeforeEach
    public void setup() throws IOException {

        X509CertificateTestUtils.reloadKeystores();
        uiTruststoreService.refreshData();
        mvc = initializeMockMvc(webAppContext);
    }


    @Test
    public void validateInvalidCertificate() throws Exception {
        byte[] buff = "Not a certificate :) ".getBytes();

        // login
        MockHttpSession session = loginWithSystemAdmin(mvc);
        // when update data
        UserRO userRO = getLoggedUserData(mvc, session);

        // given when
        mvc.perform(post(PATH_PUBLIC + "/" + userRO.getUserId() + "/validate-certificate")
                        .session(session)
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isOk())
                .andExpect(content().string(CoreMatchers.containsString("Can not read the certificate!")));
    }

    @Test
    public void validateCertificateSystemAdmin() throws Exception {
        byte[] buff = IOUtils.toByteArray(UserResourceIT.class.getResourceAsStream("/SMPtest.crt"));
        // login
        MockHttpSession session = loginWithSystemAdmin(mvc);
        // when update data
        UserRO userRO = getLoggedUserData(mvc, session);
        // given when
        MvcResult result = mvc.perform(post(PATH_PUBLIC + "/" + userRO.getUserId() + "/validate-certificate")
                        .session(session)
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isOk()).andReturn();

        //then
        ObjectMapper mapper = getObjectMapper();

        CertificateRO res = mapper.readValue(result.getResponse().getContentAsString(), CertificateRO.class);

        assertNotNull(res);
        assertEquals("CN=Intermediate CA,O=DIGIT,C=BE", res.getIssuer());
        assertEquals("1.2.840.113549.1.9.1=#160c736d7040746573742e636f6d,CN=SMP test,O=DIGIT,C=BE", res.getSubject());
        assertEquals("3", res.getSerialNumber());
        assertEquals("CN=SMP test,O=DIGIT,C=BE:0000000000000003", res.getCertificateId());
        assertEquals("sno=3&subject=1.2.840.113549.1.9.1%3D%23160c736d7040746573742e636f6d%2CCN%3DSMP+test%2CO%3DDIGIT%2CC%3DBE&validfrom=May+22+18%3A59%3A00+2018+GMT&validto=May+22+18%3A56%3A00+2019+GMT&issuer=CN%3DIntermediate+CA%2CO%3DDIGIT%2CC%3DBE", res.getClientCertHeader());
    }

    @Test
    public void validateCertificateIdWithEmailSerialNumberInSubjectCertIdTest() throws Exception {
        // login
        MockHttpSession session = loginWithSystemAdmin(mvc);
        // when update data
        UserRO userRO = getLoggedUserData(mvc, session);

        String subject = "CN=common name,emailAddress=CEF-EDELIVERY-SUPPORT@ec.europa.eu,serialNumber=1,O=org,ST=My town,postalCode=2151, L=GreatTown,street=My Street. 20, C=BE";
        String serialNumber = "1234321";
        X509Certificate certificate = X509CertificateTestUtils.createX509CertificateForTest(serialNumber, subject);
        byte[] buff = certificate.getEncoded();
        // given when
        MvcResult result = mvc.perform(post(PATH_PUBLIC + "/" + userRO.getUserId() + "/validate-certificate")
                        .session(session)
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isOk()).andReturn();

        //them
        ObjectMapper mapper = getObjectMapper();
        CertificateRO res = mapper.readValue(result.getResponse().getContentAsString(), CertificateRO.class);

        assertEquals("CN=common name,O=org,C=BE:0000000001234321", res.getCertificateId());
    }

    @Test
    public void uploadCertificateInvalidUser() throws Exception {
        byte[] buff = IOUtils.toByteArray(UserResourceIT.class.getResourceAsStream("/SMPtest.crt"));
        // id and logged user not match
        // given when
        mvc.perform(post(PATH_PUBLIC + "/34556655/validate-certificate")
                        .with(getHttpBasicSMPAdminCredentials())
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isUnauthorized()).andReturn();
    }

    @Test
    public void getCertificateList() throws Exception {
        // given when
        // login
        MockHttpSession session = loginWithSystemAdmin(mvc);
        // when update data
        UserRO userRO = getLoggedUserData(mvc, session);

        int countStart = uiTruststoreService.getCertificateROEntriesList().size();
        MvcResult result = mvc.perform(get(PATH_INTERNAL + "/" + userRO.getUserId())
                        .session(session)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        //then
        ObjectMapper mapper = getObjectMapper();
        List<CertificateRO> listCerts = mapper.readValue(result.getResponse().getContentAsString(), new TypeReference<List<CertificateRO>>() {
        });

        assertNotNull(listCerts);
        assertEquals(countStart, listCerts.size());
        listCerts.forEach(sgMap -> {
            CertificateRO cert = mapper.convertValue(sgMap, CertificateRO.class);
            assertNotNull(cert.getAlias());
            assertNotNull(cert.getCertificateId());
            assertNotNull(cert.getClientCertHeader());
            assertNull(cert.getEncodedValue()); // submit only metadata
        });
    }

    @Test
    @Ignore("Fails on CITNET bamboo")
    public void deleteCertificateSystemAdmin() throws Exception {

        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);

        byte[] buff = IOUtils.toByteArray(UserResourceIT.class.getResourceAsStream("/SMPtest.crt"));

        int countStart = uiTruststoreService.getNormalizedTrustedList().size();
        MvcResult prepRes = mvc.perform(post(PATH_INTERNAL + "/" + userRO.getUserId() + "/upload-certificate")
                        .session(session)
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isOk()).andReturn();

        // given when
        ObjectMapper mapper = getObjectMapper();
        CertificateRO res = mapper.readValue(prepRes.getResponse().getContentAsString(), CertificateRO.class);
        assertNotNull(res);
        uiTruststoreService.refreshData();
        assertEquals(countStart + 1, uiTruststoreService.getNormalizedTrustedList().size());

        // then
        MvcResult result = mvc.perform(delete(PATH_INTERNAL + "/" + userRO.getUserId() + "/delete/" + res.getAlias())
                        .session(session)
                        .with(csrf())
                        .content(buff))
                .andExpect(status().isOk()).andReturn();
        uiTruststoreService.refreshData();
        assertEquals(countStart, uiTruststoreService.getNormalizedTrustedList().size());
    }

}
