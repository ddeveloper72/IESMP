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
package eu.europa.ec.edelivery.smp.data.model.user;

import eu.europa.ec.edelivery.smp.data.dao.QueryNames;
import eu.europa.ec.edelivery.smp.data.dao.utils.ColumnDescription;
import eu.europa.ec.edelivery.smp.data.enums.ApplicationRoleType;
import eu.europa.ec.edelivery.smp.data.model.BaseEntity;
import eu.europa.ec.edelivery.smp.data.model.CommonColumnsLengths;
import eu.europa.ec.edelivery.smp.data.model.DBUserDeleteValidationMapping;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;

@Entity
@Audited
@Table(name = "SMP_USER")
@org.hibernate.annotations.Table(appliesTo = "SMP_USER", comment = "SMP can handle multiple domains. This table contains domain specific data")
@NamedQuery(name = QueryNames.QUERY_USER_BY_CI_USERNAME, query = "SELECT u FROM DBUser u WHERE upper(u.username) = upper(:username)")
@NamedQuery(name = QueryNames.QUERY_USER_BY_CREDENTIAL_NAME_TYPE_TARGET, query = "SELECT u FROM DBCredential c JOIN c.user u " +
        " WHERE c.name = :credential_name" +
        " AND c.credentialType = :credential_type " +
        " AND c.credentialTarget = :credential_target")
@NamedQuery(name = QueryNames.QUERY_USER_BY_CI_CREDENTIAL_NAME_TYPE_TARGET, query = "SELECT u FROM DBCredential c JOIN c.user u " +
        " WHERE upper(c.name) = upper(:credential_name) " +
        " AND c.credentialType = :credential_type " +
        " AND c.credentialTarget = :credential_target")


@NamedQuery(name = QUERY_USER_COUNT, query = "SELECT count(c) FROM DBUser c")
@NamedQuery(name = QUERY_USERS, query = "SELECT c FROM DBUser c  order by c.username")
@NamedQuery(name = QUERY_USER_FILTER_COUNT, query = "SELECT count(c) FROM DBUser c " +
        " WHERE (lower(c.username) like lower(:user_filter) OR  lower(c.fullName) like lower(:user_filter))")
@NamedQuery(name = QUERY_QUERY_USERS_FILTER, query = "SELECT c FROM DBUser c " +
        " WHERE (lower(c.username) like lower(:user_filter) OR  lower(c.fullName) like lower(:user_filter))  order by c.username")
@NamedNativeQuery(name = "DBUserDeleteValidation.validateUsersForOwnership",
        resultSetMapping = "DBUserDeleteValidationMapping",
        query = "SELECT S.ID as ID, S.USERNAME as USERNAME, " +
                "    C.CERTIFICATE_ID as certificateId, COUNT(S.ID) as  ownedCount  FROM " +
                " SMP_USER S LEFT JOIN SMP_CERTIFICATE C ON (S.ID=C.ID) " +
                " INNER JOIN SMP_RESOURCE_MEMBER SG ON (S.ID = SG.FK_USER_ID) " +
                " WHERE S.ID IN (:idList)" +
                " GROUP BY S.ID, S.USERNAME, C.CERTIFICATE_ID")
@SqlResultSetMapping(name = "DBUserDeleteValidationMapping", classes = {
        @ConstructorResult(targetClass = DBUserDeleteValidationMapping.class,
                columns = {@ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "username", type = String.class),
                        @ColumnResult(name = "certificateId", type = String.class),
                        @ColumnResult(name = "ownedCount", type = Integer.class)})
})

public class DBUser extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SMP_USER_SEQ")
    @GenericGenerator(name = "SMP_USER_SEQ", strategy = "native")
    @Column(name = "ID")
    @ColumnDescription(comment = "Unique user id")
    Long id;
    // username
    @Column(name = "USERNAME", length = CommonColumnsLengths.MAX_USERNAME_LENGTH, unique = true, nullable = false)
    @ColumnDescription(comment = "Unique username identifier. The Username must not be null")
    private String username;
    @Column(name = "ACTIVE", nullable = false)
    @ColumnDescription(comment = "Is user active")
    private boolean active = true;
    @Enumerated(EnumType.STRING)
    @Column(name = "APPLICATION_ROLE", length = CommonColumnsLengths.MAX_USER_ROLE_LENGTH)
    @ColumnDescription(comment = "User application role as USER, SYSTEM_ADMIN")
    private ApplicationRoleType applicationRole;

    @Column(name = "EMAIL", length = CommonColumnsLengths.MAX_TEXT_LENGTH_128)
    @ColumnDescription(comment = "User email")
    private String emailAddress;

    @Column(name = "FULL_NAME", length = CommonColumnsLengths.MAX_TEXT_LENGTH_128)
    @ColumnDescription(comment = "User full name (name and lastname)")
    private String fullName;

    @Column(name = "SMP_THEME", length = CommonColumnsLengths.MAX_TEXT_LENGTH_64)
    @ColumnDescription(comment = "DomiSMP settings: theme for the user")
    private String smpTheme;

    @Column(name = "SMP_LOCALE", length = CommonColumnsLengths.MAX_TEXT_LENGTH_64)
    @ColumnDescription(comment = "DomiSMP settings: locale for the user")
    private String smpLocale;

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBCredential> userCredentials = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBDomainMember> domainMembers = new ArrayList<>();
    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBGroupMember> groupMembers = new ArrayList<>();

    @OneToMany(
            mappedBy = "user",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBResourceMember> resourceMembers = new ArrayList<>();
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String email) {
        this.emailAddress = email;
    }

    public ApplicationRoleType getApplicationRole() {
        return applicationRole;
    }

    public void setApplicationRole(ApplicationRoleType applicationRole) {
        this.applicationRole = applicationRole;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getSmpTheme() {
        return smpTheme;
    }

    public void setSmpTheme(String smpTheme) {
        this.smpTheme = smpTheme;
    }

    public String getSmpLocale() {
        return smpLocale;
    }

    public void setSmpLocale(String smpLocale) {
        this.smpLocale = smpLocale;
    }

    public List<DBCredential> getUserCredentials() {
        return userCredentials;
    }

    public List<DBDomainMember> getDomainMembers() {
        return domainMembers;
    }

    public List<DBGroupMember> getGroupMembers() {
        return groupMembers;
    }

    public List<DBResourceMember> getResourceMembers() {
        return resourceMembers;
    }

    @Override
    public String toString() {
        return "DBUser{" +
                "id=" + id +
                ", emailAddress='" + emailAddress + '\'' +
                ", username='" + username + '\'' +
                ", active=" + active +
                ", applicationRole=" + applicationRole +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DBUser dbUser = (DBUser) o;

        return Objects.equals(id, dbUser.id) &&
                StringUtils.equalsIgnoreCase(username, dbUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id, username);
    }
}
