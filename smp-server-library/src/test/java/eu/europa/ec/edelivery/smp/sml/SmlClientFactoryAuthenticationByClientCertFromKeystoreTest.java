/*-
 * #START_LICENSE#
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
 * #END_LICENSE#
 */

package eu.europa.ec.edelivery.smp.sml;

import eu.europa.ec.bdmsl.ws.soap.IManageParticipantIdentifierWS;
import eu.europa.ec.bdmsl.ws.soap.IManageServiceMetadataWS;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.services.AbstractServiceTest;
import eu.europa.ec.edelivery.smp.services.ConfigurationService;
import eu.europa.ec.edelivery.smp.services.ui.UIKeystoreService;
import org.apache.cxf.configuration.jsse.TLSClientParameters;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.message.Message;
import org.apache.cxf.transport.http.HTTPConduit;
import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.util.ReflectionTestUtils;

import javax.net.ssl.KeyManager;
import javax.net.ssl.X509KeyManager;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Created by gutowpa on 08/01/2018.
 */
class SmlClientFactoryAuthenticationByClientCertFromKeystoreTest extends AbstractServiceTest {

    private static final String CERTIFICATE_DN_SECOND_DOMAIN = "CN=Second Domain,OU=edelivery,O=digit,C=eu";
    private static final String CERTIFICATE_DN_FIRST_DOMAIN = "CN=SMP Mock Services,OU=DIGIT,O=European Commision,C=BE";

    Path resourceDirectory = Paths.get("src", "test", "resources", "keystores");

    ConfigurationService configurationService = Mockito.mock(ConfigurationService.class);

    @Autowired
    UIKeystoreService keystoreService;

    @Autowired
    private SmlClientFactory smlClientFactory;

    @Autowired
    private SmlConnector testInstance;


    @BeforeEach
    public void before() throws MalformedURLException {

        ReflectionTestUtils.setField(keystoreService, "configurationService", configurationService);
        ReflectionTestUtils.setField(testInstance, "configurationService", configurationService);
        ReflectionTestUtils.setField(testInstance, "keystoreService", keystoreService);

        // set keystore properties
        File keystoreFile = new File(resourceDirectory.toFile(), "smp-keystore_multiple_domains.jks");
        Mockito.doReturn(keystoreFile).when(configurationService).getKeystoreFile();
        Mockito.doReturn(resourceDirectory.toFile()).when(configurationService).getSecurityFolder();
        Mockito.doReturn("test123").when(configurationService).getKeystoreCredentialToken();
        Mockito.doReturn(new URL("https://localhost/edelivery-sml")).when(configurationService).getSMLIntegrationUrl();
        keystoreService.refreshData();

    }

    @Test
    void factoryProducesPreconfiguredCxfClientThatAuthenticatesItselfWithGivenCertAlias() {
        //given
        IManageParticipantIdentifierWS client = smlClientFactory.create();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias("second_domain_alias");
        domain.setSmlClientCertAuth(false);
        // when
        testInstance.configureClient("manageparticipantidentifier", client, domain);

        //then
        assertNotNull(client);
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, Object> requestContext = cxfClient.getRequestContext();
        X509Certificate clientCert = getClientCertFromKeystore(cxfClient);

        // check there is no headers
        Map httpHeaders = (Map) requestContext.get(Message.PROTOCOL_HEADERS);
        assertTrue(httpHeaders == null || httpHeaders.isEmpty());

        assertEquals(CERTIFICATE_DN_SECOND_DOMAIN, clientCert.getSubjectX500Principal().getName());
        assertEquals("https://localhost/edelivery-sml/manageparticipantidentifier", requestContext.get(Message.ENDPOINT_ADDRESS));
    }


    @Test
    void factoryProducesPreconfiguredCxfSMPClientThatAuthenticatesItselfWithGivenCertAlias() {

        //given
        IManageServiceMetadataWS client = smlClientFactory.createSmp();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias("second_domain_alias");
        domain.setSmlClientCertAuth(false);
        // when
        testInstance.configureClient("manageservicemetadata", client, domain);

        //then
        assertNotNull(client);
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, Object> requestContext = cxfClient.getRequestContext();
        X509Certificate clientCert = getClientCertFromKeystore(cxfClient);
        // check there is no headers
        Map httpHeaders = (Map) requestContext.get(Message.PROTOCOL_HEADERS);
        assertTrue(httpHeaders == null || httpHeaders.isEmpty());

        assertEquals(CERTIFICATE_DN_SECOND_DOMAIN, clientCert.getSubjectX500Principal().getName());
        assertEquals("https://localhost/edelivery-sml/manageservicemetadata", requestContext.get(Message.ENDPOINT_ADDRESS));
    }

    @Test
    void factoryProducesClientWithAnotherCertFromKeystore() {
        //given
        IManageParticipantIdentifierWS client = smlClientFactory.create();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias("single_domain_key");
        domain.setSmlClientCertAuth(false);
        // when
        testInstance.configureClient("changedEndpoint", client, domain);

        //then
        assertNotNull(client);
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, Object> requestContext = cxfClient.getRequestContext();
        X509Certificate clientCert = getClientCertFromKeystore(cxfClient);

        assertEquals(CERTIFICATE_DN_FIRST_DOMAIN, clientCert.getSubjectX500Principal().getName());
        assertEquals("https://localhost/edelivery-sml/changedEndpoint", requestContext.get(Message.ENDPOINT_ADDRESS));
    }

    @Test
    void factoryProducesSMPClientWithAnotherCertFromKeystore() {

        //given
        IManageServiceMetadataWS client = smlClientFactory.createSmp();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias("single_domain_key");
        domain.setSmlClientCertAuth(false);
        // when
        testInstance.configureClient("changedEndpoint", client, domain);

        //then
        assertNotNull(client);
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, Object> requestContext = cxfClient.getRequestContext();
        X509Certificate clientCert = getClientCertFromKeystore(cxfClient);

        assertEquals(CERTIFICATE_DN_FIRST_DOMAIN, clientCert.getSubjectX500Principal().getName());
        assertEquals("https://localhost/edelivery-sml/changedEndpoint", requestContext.get(Message.ENDPOINT_ADDRESS));
    }

    @Test
    void factoryProducesClientNoDefinedAlias() {
        //given
        IManageParticipantIdentifierWS client = smlClientFactory.create();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias(null);
        domain.setSmlClientCertAuth(false);

        IllegalStateException result = assertThrows(IllegalStateException.class,
                () -> testInstance.configureClient("changedEndpoint", client, domain));

        MatcherAssert.assertThat(result.getMessage(), CoreMatchers.containsString("Invalid integration configuration. Missing Client cert configuration!"));
    }

    @Test
    void factoryProducesSMPClientNoDefinedAlias() {

        //given
        IManageServiceMetadataWS client = smlClientFactory.createSmp();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias(null);
        domain.setSmlClientCertAuth(false);

        IllegalStateException result = assertThrows(IllegalStateException.class,
                () -> testInstance.configureClient("changedEndpoint", client, domain));

        MatcherAssert.assertThat(result.getMessage(), CoreMatchers.containsString("Invalid integration configuration. Missing Client cert configuration!"));
    }

    @Test
    void factoryProducesClientNoDefinedAliasOneKeyInKeystore() {
        //given
        File keystoreFile = new File(resourceDirectory.toFile(), "service_integration_signatures_single_domain.jks");
        Mockito.doReturn(keystoreFile).when(configurationService).getKeystoreFile();
        Mockito.doReturn(resourceDirectory.toFile()).when(configurationService).getSecurityFolder();
        Mockito.doReturn("test123").when(configurationService).getKeystoreCredentialToken();
        ReflectionTestUtils.setField(keystoreService, "configurationService", configurationService);
        keystoreService.refreshData();


        IManageParticipantIdentifierWS client = smlClientFactory.create();
        DBDomain domain = new DBDomain();
        domain.setSmlClientKeyAlias(null);
        domain.setSmlClientCertAuth(false);

        // when
        testInstance.configureClient("changedEndpoint", client, domain);

        // then
        Client cxfClient = ClientProxy.getClient(client);
        Map<String, Object> requestContext = cxfClient.getRequestContext();
        X509Certificate clientCert = getClientCertFromKeystore(cxfClient);

        assertEquals(CERTIFICATE_DN_FIRST_DOMAIN, clientCert.getSubjectX500Principal().getName());

    }


    private static X509Certificate getClientCertFromKeystore(Client cxfClient) {
        HTTPConduit httpConduit = (HTTPConduit) cxfClient.getConduit();
        TLSClientParameters tlsParams = httpConduit.getTlsClientParameters();
        String alias = tlsParams.getCertAlias();
        KeyManager keyManager = tlsParams.getKeyManagers()[0];
        assertTrue(keyManager instanceof X509KeyManager);
        PrivateKey key = ((X509KeyManager) keyManager).getPrivateKey(alias);
        assertNotNull(key);
        return ((X509KeyManager) keyManager).getCertificateChain(alias)[0];
    }
}
