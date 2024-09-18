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

import eu.europa.ec.edelivery.smp.data.dao.utils.ColumnDescription;
import eu.europa.ec.edelivery.smp.data.enums.CredentialTargetType;
import eu.europa.ec.edelivery.smp.data.enums.CredentialType;
import eu.europa.ec.edelivery.smp.data.model.BaseEntity;
import eu.europa.ec.edelivery.smp.data.model.CommonColumnsLengths;
import eu.europa.ec.edelivery.smp.data.model.DBUserDeleteValidationMapping;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.time.OffsetDateTime;
import java.util.Objects;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;
@Entity
@Audited
@Table(name = "SMP_CREDENTIAL",
        indexes = {
            @Index(name = "SMP_CRD_USER_NAME_TYPE_IDX", columnList = "CREDENTIAL_NAME, CREDENTIAL_TYPE, CREDENTIAL_TARGET",  unique = true),
            @Index(name = "SMP_CRD_USER_NAME_RESET_IDX", columnList = "RESET_TOKEN, CREDENTIAL_TYPE, CREDENTIAL_TARGET",  unique = true)
        })
@org.hibernate.annotations.Table(appliesTo = "SMP_CREDENTIAL", comment = "Credentials for the users")
@NamedQuery(name = QUERY_CREDENTIAL_ALL, query = "SELECT u FROM DBCredential u")
@NamedQuery(name = QUERY_CREDENTIALS_BY_CI_USERNAME_CREDENTIAL_TYPE_TARGET, query = "SELECT c FROM DBCredential c " +
        "WHERE upper(c.user.username) = upper(:username) and c.credentialType = :credential_type and c.credentialTarget = :credential_target")
@NamedQuery(name = QUERY_CREDENTIALS_BY_TYPE_RESET_TOKEN, query = "SELECT c FROM DBCredential c " +
        "WHERE c.credentialType = :credential_type and c.credentialTarget = :credential_target and c.resetToken=:reset_token")

@NamedQuery(name = QUERY_CREDENTIALS_BY_USERID_CREDENTIAL_TYPE_TARGET, query = "SELECT c FROM DBCredential c " +
        "WHERE c.user.id = :user_id and c.credentialType = :credential_type and c.credentialTarget = :credential_target order by c.id")
// case-insensitive search
@NamedQuery(name = QUERY_CREDENTIAL_BY_CREDENTIAL_NAME_TYPE_TARGET, query = "SELECT c FROM DBCredential c " +
        "WHERE c.name = :credential_name and c.credentialType = :credential_type and c.credentialTarget = :credential_target")
@NamedQuery(name = QUERY_CREDENTIAL_BY_CERTIFICATE_ID, query = "SELECT u FROM DBCredential u WHERE u.certificate.certificateId = :certificate_identifier")
@NamedQuery(name = QUERY_CREDENTIAL_BY_CI_CERTIFICATE_ID, query = "SELECT u FROM DBCredential u WHERE upper(u.certificate.certificateId) = upper(:certificate_identifier)")

@NamedQuery(name = QUERY_CREDENTIAL_BEFORE_EXPIRE,
        query = "SELECT distinct c FROM DBCredential c WHERE c.credentialType=:credential_type  " +
                " AND c.expireOn IS NOT NULL" +
                " AND c.expireOn <= :start_alert_send_date " +
                " AND c.expireOn > :expire_test_date" +
                " AND (c.expireAlertOn IS NULL OR c.expireAlertOn < :lastSendAlertDate )")
@NamedQuery(name = QUERY_CREDENTIAL_EXPIRED,
        query = "SELECT distinct c FROM DBCredential c WHERE  c.credentialType=:credential_type" +
                " AND  c.expireOn IS NOT NULL" +
                " AND c.expireOn > :endAlertDate " +
                " AND c.expireOn <= :expire_test_date" +
                " AND (c.expireAlertOn IS NULL " +
                "   OR c.expireAlertOn <= c.expireOn " +
                "   OR c.expireAlertOn < :lastSendAlertDate )")
// native queries to validate if user is owner of the credential
@NamedNativeQuery(name = "DBCredentialDeleteValidation.validateUsersForOwnership",
        resultSetMapping = "DBCredentialDeleteValidationMapping",
        query = "SELECT S.ID as ID, S.USERNAME as USERNAME, " +
                "    C.CERTIFICATE_ID as certificateId, COUNT(S.ID) as  ownedCount  FROM " +
                " SMP_USER S LEFT JOIN SMP_CERTIFICATE C ON (S.ID=C.ID) " +
                " INNER JOIN SMP_RESOURCE_MEMBER SG ON (S.ID = SG.FK_USER_ID) " +
                " WHERE S.ID IN (:idList)" +
                " GROUP BY S.ID, S.USERNAME, C.CERTIFICATE_ID")

@SqlResultSetMapping(name = "DBCredentialDeleteValidationMapping", classes = {
        @ConstructorResult(targetClass = DBUserDeleteValidationMapping.class,
                columns = {@ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "username", type = String.class),
                        @ColumnResult(name = "certificateId", type = String.class),
                        @ColumnResult(name = "ownedCount", type = Integer.class)})
})
public class DBCredential extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SMP_CREDENTIAL_SEQ")
    @GenericGenerator(name = "SMP_CREDENTIAL_SEQ", strategy = "native")
    @Column(name = "ID")
    @ColumnDescription(comment = "Unique id")
    Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "FK_USER_ID")
    private DBUser user;

    @Column(name = "CREDENTIAL_ACTIVE", nullable = false)
    @ColumnDescription(comment = "Is credential active")
    private boolean active = true;

    // username
    @Column(name = "CREDENTIAL_NAME", length = CommonColumnsLengths.MAX_IDENTIFIER_VALUE_VALUE_LENGTH, nullable = false)
    @ColumnDescription(comment = "Unique username identifier. The Username must not be null")
    private String name;

    @Column(name = "CREDENTIAL_DESC",
            length = CommonColumnsLengths.MAX_IDENTIFIER_VALUE_VALUE_LENGTH)
    @ColumnDescription(comment = "Credential description")
    private String description;
    @Column(name = "CREDENTIAL_VALUE", length = CommonColumnsLengths.MAX_PASSWORD_LENGTH)
    @ColumnDescription(comment = "Credential value - it can be encrypted value")
    private String value;
    @Column(name = "CHANGED_ON")
    @ColumnDescription(comment = "Last date when credential was changed")
    private OffsetDateTime changedOn;
    @Column(name = "ACTIVE_FROM")
    @ColumnDescription(comment = "Date when credential starts to be active")
    private OffsetDateTime activeFrom;
    @Column(name = "EXPIRE_ON")
    @ColumnDescription(comment = "Date when password will expire")
    private OffsetDateTime expireOn;
    @Column(name = "LAST_ALERT_ON")
    @ColumnDescription(comment = "Generated last password expire alert")
    private OffsetDateTime expireAlertOn;
    @Column(name = "LOGIN_FAILURE_COUNT")
    @ColumnDescription(comment = "Sequential login failure count")
    private Integer sequentialLoginFailureCount;
    @Column(name = "LAST_FAILED_LOGIN_ON")
    @ColumnDescription(comment = "Last failed login attempt")
    private OffsetDateTime lastFailedLoginAttempt;
    @Enumerated(EnumType.STRING)
    @Column(name = "CREDENTIAL_TYPE", nullable = false)
    @ColumnDescription(comment = "Credential type:  USERNAME, ACCESS_TOKEN, CERTIFICATE, CAS")
    private CredentialType credentialType = CredentialType.USERNAME_PASSWORD;
    @Enumerated(EnumType.STRING)
    @Column(name = "CREDENTIAL_TARGET", nullable = false)
    @ColumnDescription(comment = "Credential target UI, API")
    private CredentialTargetType credentialTarget = CredentialTargetType.UI;
    @Column(name = "RESET_TOKEN", length = CommonColumnsLengths.MAX_PASSWORD_LENGTH)
    @ColumnDescription(comment = "Reset token for credential reset")
    private String resetToken;
    @Column(name = "RESET_EXPIRE_ON")
    @ColumnDescription(comment = "Date time when reset token will expire")
    private OffsetDateTime resetExpireOn;

    @OneToOne(mappedBy = "credential",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true)
    private DBCertificate certificate;

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public DBUser getUser() {
        return user;
    }

    public void setUser(DBUser user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public OffsetDateTime getChangedOn() {
        return changedOn;
    }

    public void setChangedOn(OffsetDateTime changedOn) {
        this.changedOn = changedOn;
    }

    public OffsetDateTime getActiveFrom() {
        return activeFrom;
    }

    public void setActiveFrom(OffsetDateTime activeFrom) {
        this.activeFrom = activeFrom;
    }

    public OffsetDateTime getExpireOn() {
        return expireOn;
    }

    public void setExpireOn(OffsetDateTime expireOn) {
        this.expireOn = expireOn;
    }

    public OffsetDateTime getExpireAlertOn() {
        return expireAlertOn;
    }

    public void setExpireAlertOn(OffsetDateTime expireAlertOn) {
        this.expireAlertOn = expireAlertOn;
    }

    public Integer getSequentialLoginFailureCount() {
        return sequentialLoginFailureCount;
    }

    public void setSequentialLoginFailureCount(Integer sequentialLoginFailureCount) {
        this.sequentialLoginFailureCount = sequentialLoginFailureCount;
    }

    public OffsetDateTime getLastFailedLoginAttempt() {
        return lastFailedLoginAttempt;
    }

    public void setLastFailedLoginAttempt(OffsetDateTime lastFailedLoginAttempt) {
        this.lastFailedLoginAttempt = lastFailedLoginAttempt;
    }

    public CredentialType getCredentialType() {
        return credentialType;
    }

    public void setCredentialType(CredentialType credentialType) {
        this.credentialType = credentialType;
    }

    public CredentialTargetType getCredentialTarget() {
        return credentialTarget;
    }

    public void setCredentialTarget(CredentialTargetType credentialTarget) {
        this.credentialTarget = credentialTarget;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public OffsetDateTime getResetExpireOn() {
        return resetExpireOn;
    }

    public void setResetExpireOn(OffsetDateTime resetExpireOn) {
        this.resetExpireOn = resetExpireOn;
    }

    public DBCertificate getCertificate() {
        return certificate;
    }

    public void setCertificate(DBCertificate certificate) {
        this.certificate = certificate;
        if (certificate != null) {
            certificate.setCredential(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DBCredential credential = (DBCredential) o;
        return Objects.equals(id, credential.id) &&
                Objects.equals(name, credential.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
    }
}
