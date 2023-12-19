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
package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.model.doc.DBResource;
import eu.europa.ec.edelivery.smp.data.model.user.DBResourceMember;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import eu.europa.ec.edelivery.smp.testutil.TestConstants;
import org.junit.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.*;


/**
 * Purpose of class is to test all resource methods with database.
 *
 * @author Joze Rihtarsic
 * @since 4.1
 */
public class ResourceDaoMembershipIntegrationTest extends AbstractResourceDaoTest {

    @Test
    @Transactional
    public void persistNewResourceWithMember() {
        DBResource resource = createResourceWithMembers(TestConstants.USERNAME_1);

        Optional<DBResource> res = testInstance.findServiceGroup(resource.getIdentifierValue(), resource.getIdentifierScheme());
        assertTrue(res.isPresent());
        assertNotSame(resource, res.get());
        assertEquals(resource, res.get());
        assertEquals(1, res.get().getMembers().size());
        assertEquals(TestConstants.USERNAME_1, res.get().getMembers().get(0).getUser().getUsername());
    }

    @Test
    @Transactional
    public void addTwoMembersToServiceGroup() {
        DBResource resource = createResourceWithMembers(TestConstants.USERNAME_1, TestConstants.USERNAME_3);

        Optional<DBResource> res = testInstance.findServiceGroup(resource.getIdentifierValue(), resource.getIdentifierScheme());
        assertTrue(res.isPresent());
        assertEquals(2, res.get().getMembers().size());
    }

    @Test
    @Transactional
    public void removeMemberFromResource() {
        DBResource resource = createResourceWithMembers(TestConstants.USERNAME_1, TestConstants.USERNAME_3);
        assertEquals(2, resource.getMembers().size());

        DBResourceMember resourceMember = resource.getMembers().get(0);

        // when
        boolean result = resourceMemberDao.removeById(resourceMember.getId());

        assertTrue(result);

    }

    public DBResource createResourceWithMembers(String... usernames) {
        DBResource resource = createAndSaveNewResource();
        assertTrue(resource.getMembers().isEmpty());
        for (String username : usernames) {
            Optional<DBUser> user = userDao.findUserByUsername(username);
            DBResourceMember member = testUtilsDao.createResourceMembership(MembershipRoleType.ADMIN, user.get(), resource);
            resource.getMembers().add(member);
        }

        testInstance.clearPersistenceContext();
        return resource;
    }

}
