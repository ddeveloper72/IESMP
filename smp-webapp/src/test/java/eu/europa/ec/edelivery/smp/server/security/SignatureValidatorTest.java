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

package eu.europa.ec.edelivery.smp.server.security;


import eu.europa.ec.edelivery.security.PreAuthenticatedCertificatePrincipal;
import eu.europa.ec.edelivery.smp.test.SmpTestWebAppConfig;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.WebApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.Calendar;
import java.util.Date;

import static java.lang.String.format;
import static java.net.URLEncoder.encode;
import static org.springframework.http.MediaType.APPLICATION_XML_VALUE;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@WebAppConfiguration
@ContextConfiguration(classes = {SmpTestWebAppConfig.class})
@Sql(scripts = {
        "classpath:/cleanup-database.sql",
        "classpath:/webapp_integration_test_data.sql"},
        executionPhase = BEFORE_TEST_METHOD)
public class SignatureValidatorTest {

    protected Path resourceDirectory = Paths.get("src", "test", "resources", "keystores");
    protected Path targetDirectory = Paths.get("target", "keystores");

    private static final String C14N_METHOD = CanonicalizationMethod.INCLUSIVE;
    private static final String PARSER_DISALLOW_DTD_PARSING_FEATURE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final RequestPostProcessor ADMIN_CREDENTIALS = httpBasic("pat_smp_admin", "123456");

    @Autowired
    private WebApplicationContext webAppContext;

    private MockMvc mvc;

    @BeforeEach
    public void setup() throws IOException {
        FileUtils.deleteDirectory(targetDirectory.toFile());
        FileUtils.copyDirectory(resourceDirectory.toFile(), targetDirectory.toFile());

        mvc = MockMvcBuilders.webAppContextSetup(webAppContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        initServletContext();

    }

    private void initServletContext() {
        MockServletContext sc = new MockServletContext("");
        ServletContextListener listener = new ContextLoaderListener(webAppContext);
        ServletContextEvent event = new ServletContextEvent(sc);
        listener.contextInitialized(event);
    }

    @Test
    void validateSignature() throws Throwable {
        String serviceGroupId = "ehealth-actorid-qns::urn:australia:ncpb";
        Principal principal = generateMockValidPrincipal();

        String filePathToLoad = "/input/ServiceMetadata.xml";
        String signedByCustomizedSignatureFilePath = "/expected_output/PUT_ServiceMetadata_request.xml";
        String defaultSignatureFilePath = "/expected_output/GET_SignedServiceMetadata_response.xml";

        commonTest(serviceGroupId, principal, filePathToLoad, signedByCustomizedSignatureFilePath, defaultSignatureFilePath);
    }

    @Test
    void validateLinearizedSignature() throws Throwable {
        String serviceGroupId = "ehealth-actorid-qns::urn:brazil:ncpb";
        Principal principal = generateMockValidPrincipal();
        String filePathToLoad = "/input/ServiceMetadata_linarized.xml";
        String signedByCustomizedSignatureFilePath = "/expected_output/PUT_ServiceMetadata_request_linarized.xml";
        String defaultSignatureFilePath = "/expected_output/GET_SignedServiceMetadata_response_linarized.xml";

        commonTest(serviceGroupId, principal, filePathToLoad, signedByCustomizedSignatureFilePath, defaultSignatureFilePath);
    }

    private Principal generateMockValidPrincipal() {
        PreAuthenticatedCertificatePrincipal principal = new PreAuthenticatedCertificatePrincipal("C=BE, O=European Commission, OU=CEF_eDelivery.europa.eu, OU=eHealth, CN=EHEALTH_SMP_TEST_BRAZIL", "C=DE, O=T-Systems International GmbH, OU=T-Systems Trust Center, ST=Nordrhein Westfalen/postalCode=57250, L=Netphen/street=Untere Industriestr. 20, CN=Shared Business CA 4", "48:b6:81:ee:8e:0d:cc:08");
        Date date = Calendar.getInstance().getTime();
        principal.setNotAfter(DateUtils.addDays(date, 2));
        principal.setNotBefore(DateUtils.addDays(date, -1));
        return principal;
    }

    private void commonTest(String serviceGroupId, Principal principal, String filePathToLoad, String signedByCustomizedSignatureFilePath, String defaultSignatureFilePath) throws Throwable {
        //given
        String documentTypeId = encode("ehealth-resid-qns::urn::epsos##services:extended:epsos::107", "UTF-8");
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(principal, "N/A");
        authentication.setDetails(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Sign w/ Customized Signature
        Document docPutRequest = SignatureUtil.loadDocument(filePathToLoad);
        Element serviceInfExtension = SignatureUtil.findExtensionInServiceInformation(docPutRequest);
        SignatureUtil.sign("", serviceInfExtension, C14N_METHOD);
        String signedByCustomizedSignature = SignatureUtil.marshall(docPutRequest);
        URI uri = new URI(format("/%s/services/%s", serviceGroupId, documentTypeId));

        //When
        //Save ServiceMetadata
        mvc.perform(put(uri).header("Domain", "domain")
                        .with(ADMIN_CREDENTIALS)
                        .contentType(APPLICATION_XML_VALUE)
                        .content(signedByCustomizedSignature))
                .andExpect(status().is2xxSuccessful());

        //Retrieve saved ServiceMetadata
        String responseStr = mvc.perform(get(uri))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Document response = parse(responseStr);

        //store Customized Signature for validation
        Element smNode = SignatureUtil.findFirstElementByName(response, "ServiceMetadata");
        Document docUnwrapped = SignatureUtil.buildDocWithGivenRoot(smNode);
        Element adminSignature = SignatureUtil.findServiceInfoSig(docUnwrapped);

        //Then
        //Check signed document
        //Admin signature validation
        SignatureUtil.validateSignature(adminSignature);
        //Default signature validation
        Element smpSigPointer = SignatureUtil.findSignatureByParentNode(response.getDocumentElement());
        SignatureUtil.validateSignature(smpSigPointer);
    }

    public static Document parse(String serviceMetadataXml) throws SAXException, IOException, ParserConfigurationException {
        InputStream inputStream = new ByteArrayInputStream(serviceMetadataXml.getBytes());
        return getDocumentBuilder().parse(inputStream);
    }

    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        dbf.setValidating(true);
        dbf.setFeature(PARSER_DISALLOW_DTD_PARSING_FEATURE, true);
        return dbf.newDocumentBuilder();
    }
}
