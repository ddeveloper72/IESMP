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
import eu.europa.ec.edelivery.smp.data.model.DBGroup;
import eu.europa.ec.edelivery.smp.data.model.user.DBGroupMember;
import eu.europa.ec.edelivery.smp.data.model.user.DBUser;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author Joze Rihtarsic
 * @since 5.0
 */
public class GroupMemberDaoTest extends AbstractBaseDao {

    @Autowired
    GroupMemberDao testInstance;

    @Before
    public void init() {
        testUtilsDao.clearData();
        testUtilsDao.createUsers();
        testUtilsDao.createResources();
        testInstance.clearPersistenceContext();
    }

    @Test
    public void testIsUserGroupMember() {
        DBUser user = testUtilsDao.getUser1();
        DBGroup group = testUtilsDao.getGroupD1G1();

        DBGroupMember member = new DBGroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MembershipRoleType.ADMIN);
        testUtilsDao.persistFlushDetach(member);
        // then
        boolean result = testInstance.isUserGroupMember(user, Collections.singletonList(group));

        assertTrue(result);
    }

    @Test
    public void testIsUserGroupMemberFalse() {
        DBUser user = testUtilsDao.getUser1();
        DBGroup group = testUtilsDao.getGroupD1G1();

        // then
        boolean result = testInstance.isUserGroupMember(user, Collections.singletonList(group));

        assertFalse(result);
    }

    @Test
    public void testIsUserGroupMemberWithRole() {
        DBUser user = testUtilsDao.getUser1();
        DBGroup group = testUtilsDao.getGroupD1G1();

        DBGroupMember member = new DBGroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MembershipRoleType.ADMIN);
        testUtilsDao.persistFlushDetach(member);
        // then
        boolean result = testInstance.isUserGroupMemberWithRole(user.getId(), Collections.singletonList(group.getId()), MembershipRoleType.ADMIN);
        assertTrue(result);
        result = testInstance.isUserGroupMemberWithRole(user.getId(), Collections.singletonList(group.getId()), MembershipRoleType.VIEWER);
        assertFalse(result);
    }

    @Test
    public void isUserAnyDomainGroupResourceMember() {
        DBUser user = testUtilsDao.getUser1();
        DBGroup group = testUtilsDao.getGroupD1G1();

        DBGroupMember member = new DBGroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MembershipRoleType.ADMIN);
        testUtilsDao.persistFlushDetach(member);

        boolean result = testInstance.isUserAnyDomainGroupResourceMember(user, testUtilsDao.getD1());
        assertTrue(result);
        result = testInstance.isUserAnyDomainGroupResourceMember(user, testUtilsDao.getD2());
        assertFalse(result);

    }

    @Test
    public void isUserAnyDomainGroupResourceMemberWithRole() {
        DBUser user = testUtilsDao.getUser1();
        DBGroup group = testUtilsDao.getGroupD1G1();

        DBGroupMember member = new DBGroupMember();
        member.setGroup(group);
        member.setUser(user);
        member.setRole(MembershipRoleType.VIEWER);
        testUtilsDao.persistFlushDetach(member);

        boolean result = testInstance.isUserAnyDomainGroupResourceMemberWithRole(user, testUtilsDao.getD1(), MembershipRoleType.VIEWER);
        assertTrue(result);
        result = testInstance.isUserAnyDomainGroupResourceMemberWithRole(user, testUtilsDao.getD1(), MembershipRoleType.ADMIN);
        assertFalse(result);
    }

    @Test
    public void testGetGroupMembersOne() {
        DBGroup group = testUtilsDao.getGroupD1G1();
        DBUser user = testUtilsDao.getUser1();
        addMemberToGroup(user, group, MembershipRoleType.ADMIN);
        // then
        Long resultCount = testInstance.getGroupMemberCount(group.getId(), null);
        List<DBGroupMember> result = testInstance.getGroupMembers(group.getId(), 0, 10, null);
        assertEquals(1, resultCount.intValue());
        assertEquals(1, result.size());
    }

    @Test
    public void testGetDomainMembersOneFilter() {
        DBGroup group = testUtilsDao.getGroupD1G1();
        DBUser user = testUtilsDao.getUser1();
        addMemberToGroup(user, group, MembershipRoleType.ADMIN);
        // then filter no match
        assertFilter("NotExistsAtAll", 0, group);
        assertFilter(user.getUsername(), 1, group);
        assertFilter(user.getFullName(), 1, group);

        assertFilter(StringUtils.upperCase(user.getUsername()), 1, group);
        assertFilter(StringUtils.upperCase(user.getFullName()), 1, group);
        assertFilter(StringUtils.lowerCase(user.getUsername()), 1, group);
        assertFilter(StringUtils.lowerCase(user.getFullName()), 1, group);
        assertFilter("", 1, group);
        assertFilter(null, 1, group);
    }

    private void assertFilter(String filter, int expectedCount, DBGroup group) {
        Long resultCount = testInstance.getGroupMemberCount(group.getId(), filter);
        List<DBGroupMember> result = testInstance.getGroupMembers(group.getId(), 0, 10, filter);
        assertEquals(expectedCount, resultCount.intValue());
        assertEquals(expectedCount, result.size());
    }

    private void addMemberToGroup(DBUser user, DBGroup group, MembershipRoleType role) {
        DBGroupMember groupMember = new DBGroupMember();
        groupMember.setGroup(group);
        groupMember.setUser(user);
        groupMember.setRole(role);
        testInstance.persistFlushDetach(groupMember);
    }
}
