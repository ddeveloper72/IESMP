/*-
 * #%L
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
 * #L%
 */
package eu.europa.ec.edelivery.smp.data.dao;

import eu.europa.ec.edelivery.smp.data.enums.MembershipRoleType;
import eu.europa.ec.edelivery.smp.data.model.DBDomain;
import eu.europa.ec.edelivery.smp.data.model.DBGroup;
import eu.europa.ec.edelivery.smp.testutil.TestConstants;
import eu.europa.ec.edelivery.smp.testutil.TestDBUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.PersistenceException;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class GroupDaoTest extends AbstractBaseDao {

    @Autowired
    GroupDao testInstance;

    @Before
    public void prepareDatabase() {
        // setup initial data!
        testUtilsDao.clearData();
        testUtilsDao.createGroupMemberships();
        testUtilsDao.createResourceMemberships();
        testInstance.clearPersistenceContext();
    }


    @Test
    public void persistTest() {
        DBDomain domain = testUtilsDao.getD1();
        int initSize = testInstance.getAllGroupsForDomain(domain).size();

        DBGroup group = TestDBUtils.createDBGroup("NewGroup");
        group.setDomain(domain);
        // execute
        testInstance.persistFlushDetach(group);

        // test
        List<DBGroup> res = testInstance.getAllGroupsForDomain(domain);
        assertEquals(initSize+1, res.size());
    }

    @Test
    public void persistDuplicate() {
        // set
        DBDomain domain = testUtilsDao.getD1();

        DBGroup group2 = TestDBUtils.createDBGroup(TestConstants.TEST_GROUP_B);
        group2.setDomain(domain);

        // execute
        PersistenceException result = assertThrows(PersistenceException.class, () -> testInstance.persistFlushDetach(group2));
        assertEquals("org.hibernate.exception.ConstraintViolationException: could not execute statement", result.getMessage());
    }

    @Test
    public void getDomainByCodeExists() {
        // set
        DBDomain domain = testUtilsDao.getD1();

        // test
        Optional<DBGroup> res = testInstance.getGroupByNameAndDomain(TestConstants.TEST_GROUP_B, domain);
        assertTrue(res.isPresent());
        assertEquals(TestConstants.TEST_GROUP_B, res.get().getGroupName());
    }

    @Test
    public void getDomainByCodeNotExists() {
        // set
        DBDomain domain = testUtilsDao.getD1();
        // test
        Optional<DBGroup> res = testInstance.getGroupByNameAndDomain("WrongGroup", domain);
        assertFalse(res.isPresent());
    }

    @Test
    public void removeByDomainCodeExists() {
        // set
        DBDomain domain = testUtilsDao.getD1();

        Optional<DBGroup> optDmn = testInstance.getGroupByNameAndDomain(TestConstants.TEST_GROUP_B, domain);
        assertTrue(optDmn.isPresent());

        // test
        boolean res = testInstance.removeByNameAndDomain(TestConstants.TEST_GROUP_B, domain);
        assertTrue(res);
        Optional<DBGroup> optDmn1 = testInstance.getGroupByNameAndDomain(TestConstants.TEST_GROUP_B, domain);
        assertFalse(optDmn1.isPresent());
    }

    @Test
    public void getGroupsByDomainUserIdAndGroupRolesExists() {

        List<DBGroup> groups = testInstance.getGroupsByDomainUserIdAndGroupRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.ADMIN);

        assertEquals(1, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndGroupRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser2().getId(),
                MembershipRoleType.ADMIN);

        assertEquals(0, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndGroupRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.VIEWER);

        assertEquals(0, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndGroupRoles(
                testUtilsDao.getD2().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.VIEWER);

        assertEquals(1, groups.size());
    }

    @Test
    public void getGroupsByDomainUserIdAndResourceRoles() {

        List<DBGroup> groups = testInstance.getGroupsByDomainUserIdAndResourceRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.ADMIN);

        assertEquals(1, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndResourceRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser2().getId(),
                MembershipRoleType.ADMIN);

        assertEquals(0, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndResourceRoles(
                testUtilsDao.getD1().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.VIEWER);

        assertEquals(0, groups.size());

        groups = testInstance.getGroupsByDomainUserIdAndResourceRoles(
                testUtilsDao.getD2().getId(),
                testUtilsDao.getUser1().getId(),
                MembershipRoleType.VIEWER);

        assertEquals(1, groups.size());
    }
}
