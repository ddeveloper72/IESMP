package eu.europa.ec.edelivery.smp.ui.edit;

import eu.europa.ec.edelivery.smp.data.ui.*;
import eu.europa.ec.edelivery.smp.data.ui.exceptions.ErrorResponseRO;
import eu.europa.ec.edelivery.smp.exceptions.ErrorBusinessCode;
import eu.europa.ec.edelivery.smp.services.ui.UIDocumentService;
import eu.europa.ec.edelivery.smp.test.testutils.TestROUtils;
import eu.europa.ec.edelivery.smp.ui.AbstractControllerTest;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MvcResult;

import java.io.IOException;
import java.util.List;

import static eu.europa.ec.edelivery.smp.test.testutils.MockMvcUtils.*;
import static eu.europa.ec.edelivery.smp.ui.ResourceConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DocumentEditControllerIT extends AbstractControllerTest {
    private static final String PATH = CONTEXT_PATH_EDIT_DOCUMENT;

    @Autowired
    protected UIDocumentService documentService;

    @BeforeEach
    public void setup() throws IOException {
        super.setup();
    }

    @Test
    public void testGetDocumentForNewResource() throws Exception {
        // given when
        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);
        List<DomainRO> domainsForUser = geUserDomainsForRole(mvc, session, userRO, null);
        assertEquals(1, domainsForUser.size());
        DomainRO domainRO = domainsForUser.get(0);
        List<GroupRO> groupsForUser = geUserGroups(mvc, session, userRO, domainRO, null);
        assertFalse(groupsForUser.isEmpty()); // set the webapp_integration_test_data.sql file
        GroupRO groupRO = groupsForUser.get(0);
        // add new resource
        ResourceRO resourceRO = addResourceToGroup(session, domainRO, groupRO, userRO);

        // when
        MvcResult result = mvc.perform(get(PATH + '/' + SUB_CONTEXT_PATH_EDIT_DOCUMENT_GET,
                        userRO.getUserId(), resourceRO.getResourceId())
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().isOk()).andReturn();
        // then
        DocumentRo documentRo = getObjectFromResponse(result, DocumentRo.class);
        assertNotNull(documentRo);
        assertTrue(documentRo.getAllVersions().isEmpty()); // was just created without document
    }

    @Test
    public void testGetDocumentForResource() throws Exception {
        // given when
        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);
        List<DomainRO> domainsForUser = geUserDomainsForRole(mvc, session, userRO, null);
        assertEquals(1, domainsForUser.size());
        DomainRO domainRO = domainsForUser.get(0);
        List<GroupRO> groupsForUser = geUserGroups(mvc, session, userRO, domainRO, null);
        assertFalse(groupsForUser.isEmpty()); // set the webapp_integration_test_data.sql file
        GroupRO groupRO = groupsForUser.get(0);

        List<ResourceRO> resources = getEditResourcesForGroup(session, userRO, domainRO, groupRO, RESOURCE_001_IDENTIFIER_VALUE, null);
        assertEquals(1, resources.size());
        ResourceRO resourceRO = resources.get(0);

        // when
        MvcResult result = mvc.perform(get(PATH + '/' + SUB_CONTEXT_PATH_EDIT_DOCUMENT_GET,
                        userRO.getUserId(), resourceRO.getResourceId())
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().isOk()).andReturn();
        // then
        DocumentRo documentRo = getObjectFromResponse(result, DocumentRo.class);
        assertNotNull(documentRo);
        assertFalse(documentRo.getAllVersions().isEmpty()); // was just created without document
        assertNotNull(documentRo.getPayload());
    }

    @Test
    public void testValidateDocumentOk() throws Exception {
        // given when
        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);
        List<DomainRO> domainsForUser = geUserDomainsForRole(mvc, session, userRO, null);
        assertEquals(1, domainsForUser.size());
        DomainRO domainRO = domainsForUser.get(0);
        List<GroupRO> groupsForUser = geUserGroups(mvc, session, userRO, domainRO, null);
        assertFalse(groupsForUser.isEmpty()); // set the webapp_integration_test_data.sql file
        GroupRO groupRO = groupsForUser.get(0);

        List<ResourceRO> resources = getEditResourcesForGroup(session, userRO, domainRO, groupRO, RESOURCE_001_IDENTIFIER_VALUE, null);
        assertEquals(1, resources.size());
        ResourceRO resourceRO = resources.get(0);

        // document to validate
        DocumentRo documentRo = new DocumentRo();
        documentRo.setPayload(TestROUtils.createSMP10ServiceGroupPayload(resourceRO.getIdentifierValue(), resourceRO.getIdentifierScheme()));

        // when
        mvc.perform(post(PATH + '/' + SUB_CONTEXT_PATH_EDIT_DOCUMENT_VALIDATE,
                        userRO.getUserId(), resourceRO.getResourceId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getObjectMapper().writeValueAsBytes(documentRo))
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().isOk()).andReturn();
    }

    @Test
    public void testValidateDocumentInvalid() throws Exception {
        // given when
        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);
        List<DomainRO> domainsForUser = geUserDomainsForRole(mvc, session, userRO, null);
        assertEquals(1, domainsForUser.size());
        DomainRO domainRO = domainsForUser.get(0);
        List<GroupRO> groupsForUser = geUserGroups(mvc, session, userRO, domainRO, null);
        assertFalse(groupsForUser.isEmpty()); // set the webapp_integration_test_data.sql file
        GroupRO groupRO = groupsForUser.get(0);

        List<ResourceRO> resources = getEditResourcesForGroup(session, userRO, domainRO, groupRO, RESOURCE_001_IDENTIFIER_VALUE, null);
        assertEquals(1, resources.size());
        ResourceRO resourceRO = resources.get(0);

        // document to validate
        DocumentRo documentRo = new DocumentRo();
        documentRo.setPayload("invalid payload");

        // when
        MvcResult result = mvc.perform(post(PATH + '/' + SUB_CONTEXT_PATH_EDIT_DOCUMENT_VALIDATE,
                        userRO.getUserId(), resourceRO.getResourceId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(getObjectMapper().writeValueAsBytes(documentRo))
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().is4xxClientError()).andReturn();

        ErrorResponseRO errorRO = getObjectFromResponse(result, ErrorResponseRO.class);
        assertNotNull(errorRO);
        assertEquals(ErrorBusinessCode.TECHNICAL.name(), errorRO.getBusinessCode());
        MatcherAssert.assertThat(errorRO.getErrorDescription(), Matchers.containsString("Invalid request [ResourceValidation]"));
    }

    @Test
    public void testGenerateDocument() throws Exception {
        // given when
        MockHttpSession session = loginWithSystemAdmin(mvc);
        UserRO userRO = getLoggedUserData(mvc, session);
        List<DomainRO> domainsForUser = geUserDomainsForRole(mvc, session, userRO, null);
        assertEquals(1, domainsForUser.size());
        DomainRO domainRO = domainsForUser.get(0);
        List<GroupRO> groupsForUser = geUserGroups(mvc, session, userRO, domainRO, null);
        assertFalse(groupsForUser.isEmpty()); // set the webapp_integration_test_data.sql file
        GroupRO groupRO = groupsForUser.get(0);

        List<ResourceRO> resources = getEditResourcesForGroup(session, userRO, domainRO, groupRO, RESOURCE_001_IDENTIFIER_VALUE, null);
        assertEquals(1, resources.size());
        ResourceRO resourceRO = resources.get(0);

        // when
        MvcResult response = mvc.perform(post(PATH + '/' + SUB_CONTEXT_PATH_EDIT_DOCUMENT_GENERATE,
                        userRO.getUserId(), resourceRO.getResourceId())
                        .session(session)
                        .with(csrf())
                )
                .andExpect(status().isOk()).andReturn();

        DocumentRo result = getObjectFromResponse(response, DocumentRo.class);
        assertNotNull(result);
        assertNotNull(result.getPayload());
    }
}
