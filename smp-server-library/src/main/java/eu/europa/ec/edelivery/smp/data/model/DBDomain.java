/*
 * Copyright 2018 European Commission | CEF eDelivery
 *
 * Licensed under the EUPL, Version 1.2 or - as soon they will be approved by the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 *
 * You may obtain a copy of the Licence attached in file: LICENCE-EUPL-v1.2.pdf
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and limitations under the Licence.
 */

package eu.europa.ec.edelivery.smp.data.model;

import eu.europa.ec.edelivery.smp.data.dao.utils.ColumnDescription;
import eu.europa.ec.edelivery.smp.data.enums.VisibilityType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static eu.europa.ec.edelivery.smp.data.dao.QueryNames.*;

/**
 * Created by gutowpa on 16/01/2018.
 */
@Entity
@Audited
@Table(name = "SMP_DOMAIN",
        indexes = {@Index(name = "SMP_DOM_UNIQ_CODE_IDX", columnList = "DOMAIN_CODE", unique = true)
        })
@NamedQuery(name = QUERY_DOMAIN_ALL, query = "SELECT d FROM DBDomain d order by d.id asc")
@NamedQuery(name = QUERY_DOMAIN_CODE, query = "SELECT d FROM DBDomain d WHERE d.domainCode = :domain_code")
@NamedQuery(name = QUERY_DOMAIN_SMP_SML_ID, query = "SELECT d FROM DBDomain d WHERE lower(d.smlSmpId) = lower(:sml_smp_id)")

@NamedNativeQuery(name = "DBDomain.updateNullSignAlias",
        query = "update SMP_DOMAIN set SIGNATURE_KEY_ALIAS=:alias WHERE SIGNATURE_KEY_ALIAS IS null")
@NamedNativeQuery(name = "DBDomain.updateNullSMLAlias",
        query = "update SMP_DOMAIN set SIGNATURE_KEY_ALIAS=:alias " +
                "WHERE SML_CLIENT_KEY_ALIAS IS null")

@NamedQuery(name = QUERY_DOMAIN_BY_USER_ROLES_COUNT, query = "SELECT count( distinct c) FROM DBDomain c JOIN DBDomainMember dm ON c.id = dm.domain.id " +
        " WHERE dm.role in (:membership_roles) and dm.user.id= :user_id")

@NamedQuery(name = QUERY_DOMAIN_BY_USER_ROLES, query = "SELECT distinct c FROM DBDomain c JOIN DBDomainMember dm ON c.id = dm.domain.id " +
        " WHERE dm.role in (:membership_roles) and dm.user.id= :user_id")

@NamedQuery(name = QUERY_DOMAIN_BY_USER_GROUP_ROLES_COUNT, query = "SELECT count( distinct d) FROM DBDomain d " +
        " JOIN DBGroup g ON d.id = g.domain.id " +
        " JOIN DBGroupMember gm ON g.id = gm.group.id " +
        " WHERE gm.role in (:membership_roles) and gm.user.id= :user_id")

@NamedQuery(name = QUERY_DOMAIN_BY_USER_GROUP_ROLES, query = "SELECT distinct d FROM DBDomain d " +
        " JOIN DBGroup g ON d.id = g.domain.id " +
        " JOIN DBGroupMember gm ON g.id = gm.group.id " +
        " WHERE gm.role in (:membership_roles) and gm.user.id= :user_id")

@NamedQuery(name = QUERY_DOMAIN_BY_USER_RESOURCE_ROLES_COUNT, query = "SELECT count(distinct d) FROM DBDomain d " +
        " JOIN DBGroup g ON d.id = g.domain.id " +
        " JOIN DBResource r ON  g.id = r.group.id " +
        " JOIN DBResourceMember rm ON r.id = rm.resource.id " +
        " WHERE rm.role in (:membership_roles) and rm.user.id= :user_id")


@NamedQuery(name = QUERY_DOMAIN_BY_USER_RESOURCE_ROLES, query = "SELECT distinct d FROM DBDomain d " +
        " JOIN DBGroup g ON d.id = g.domain.id " +
        " JOIN DBResource r ON  g.id = r.group.id " +
        " JOIN DBResourceMember rm ON r.id = rm.resource.id " +
        " WHERE rm.role in (:membership_roles) and rm.user.id= :user_id")

@org.hibernate.annotations.Table(appliesTo = "SMP_DOMAIN", comment = "SMP can handle multiple domains. This table contains domain specific data")
public class DBDomain extends BaseEntity {

    private static final long serialVersionUID = 1008583888835630004L;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "SMP_DOMAIN_SEQ")
    @GenericGenerator(name = "SMP_DOMAIN_SEQ", strategy = "native")
    @Column(name = "ID")
    @ColumnDescription(comment = "Unique domain id")
    Long id;

    @Column(name = "DOMAIN_CODE", length = CommonColumnsLengths.MAX_DOMAIN_CODE_LENGTH, nullable = false, unique = true)
    @ColumnDescription(comment = "Domain code used as http parameter in rest webservices")
    String domainCode;

    @Column(name = "SML_SUBDOMAIN", length = CommonColumnsLengths.MAX_SML_SUBDOMAIN_LENGTH)
    @ColumnDescription(comment = "SML subdomain")
    String smlSubdomain;
    @Column(name = "SML_SMP_ID", length = CommonColumnsLengths.MAX_SML_SMP_ID_LENGTH)
    @ColumnDescription(comment = "SMP ID used for SML integration")
    String smlSmpId;
    @Column(name = "SML_CLIENT_KEY_ALIAS", length = CommonColumnsLengths.MAX_CERT_ALIAS_LENGTH)
    @ColumnDescription(comment = "Client key alias used for SML integration")
    String smlClientKeyAlias;
    @Column(name = "SIGNATURE_KEY_ALIAS", length = CommonColumnsLengths.MAX_CERT_ALIAS_LENGTH)
    @ColumnDescription(comment = "Signature key alias used for SML integration")
    String signatureKeyAlias;
    @Column(name = "SIGNATURE_ALGORITHM", length = CommonColumnsLengths.MAX_CERT_ALIAS_LENGTH)
    @ColumnDescription(comment = "Set signature algorithm. Ex.: http://www.w3.org/2001/04/xmldsig-more#rsa-sha256")
    String signatureAlgorithm;
    @Column(name = "SIGNATURE_DIGEST_METHOD", length = CommonColumnsLengths.MAX_CERT_ALIAS_LENGTH)
    @ColumnDescription(comment = "Set signature hash method. Ex.: http://www.w3.org/2001/04/xmlenc#sha256")
    String signatureDigestMethod;

    @Column(name = "SML_REGISTERED", nullable = false)
    @ColumnDescription(comment = "Flag for: Is domain registered in SML")
    private boolean smlRegistered = false;

    @Column(name = "SML_CLIENT_CERT_AUTH", nullable = false)
    @ColumnDescription(comment = "Flag for SML authentication type - use ClientCert header or  HTTPS ClientCertificate (key)")
    private boolean smlClientCertAuth = false;

    @Column(name = "DEFAULT_RESOURCE_IDENTIFIER")
    @ColumnDescription(comment = "Default resourceType code")
    private String defaultResourceTypeIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(name = "VISIBILITY", length = CommonColumnsLengths.MAX_TEXT_LENGTH_64)
    @ColumnDescription(comment = "The visibility of the domain: PUBLIC, INTERNAL")
    private VisibilityType visibility = VisibilityType.PUBLIC;

    @OneToMany(
            mappedBy = "domain",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBGroup> domainGroups = new ArrayList<>();

    @OneToMany(
            mappedBy = "domain",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    private List<DBDomainResourceDef> domainResourceDefs = new ArrayList<>();

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    public String getSmlSubdomain() {
        return smlSubdomain;
    }

    public void setSmlSubdomain(String smlSubdomain) {
        this.smlSubdomain = smlSubdomain;
    }

    public String getSmlSmpId() {
        return smlSmpId;
    }

    public void setSmlSmpId(String smlSmpId) {
        this.smlSmpId = smlSmpId;
    }

    public String getSmlClientKeyAlias() {
        return smlClientKeyAlias;
    }

    public void setSmlClientKeyAlias(String smlClientKeyAlias) {
        this.smlClientKeyAlias = smlClientKeyAlias;
    }

    public String getSignatureKeyAlias() {
        return signatureKeyAlias;
    }

    public void setSignatureKeyAlias(String keyAlias) {
        this.signatureKeyAlias = keyAlias;
    }

    public String getSignatureAlgorithm() {
        return signatureAlgorithm;
    }

    public void setSignatureAlgorithm(String signatureAlgorithm) {
        this.signatureAlgorithm = signatureAlgorithm;
    }

    public String getSignatureDigestMethod() {
        return signatureDigestMethod;
    }

    public void setSignatureDigestMethod(String signatureDigestMethod) {
        this.signatureDigestMethod = signatureDigestMethod;
    }

    public boolean isSmlRegistered() {
        return smlRegistered;
    }

    public void setSmlRegistered(boolean smlRegistered) {
        this.smlRegistered = smlRegistered;
    }

    public boolean isSmlClientCertAuth() {
        return smlClientCertAuth;
    }

    public String getDefaultResourceTypeIdentifier() {
        return defaultResourceTypeIdentifier;
    }

    public void setDefaultResourceTypeIdentifier(String defaultResourceTypeCode) {
        this.defaultResourceTypeIdentifier = defaultResourceTypeCode;
    }

    public void setSmlClientCertAuth(boolean smlClientCertAuth) {
        this.smlClientCertAuth = smlClientCertAuth;
    }

    public List<DBDomainResourceDef> getDomainResourceDefs() {
        return domainResourceDefs;
    }

    public VisibilityType getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityType visibility) {
        this.visibility = visibility;
    }

    public List<DBGroup> getDomainGroups() {
        if (domainGroups == null) {
            domainGroups = new ArrayList<>();
        }
        return domainGroups;
    }

    @Override
    public String toString() {
        return "DBDomain{" +
                "id=" + id +
                ", domainCode='" + domainCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        DBDomain dbDomain = (DBDomain) o;

        return new EqualsBuilder().appendSuper(super.equals(o))
                .append(smlRegistered, dbDomain.smlRegistered)
                .append(smlClientCertAuth, dbDomain.smlClientCertAuth)
                .append(id, dbDomain.id).append(domainCode, dbDomain.domainCode)
                .append(smlSubdomain, dbDomain.smlSubdomain)
                .append(smlSmpId, dbDomain.smlSmpId)
                .append(smlClientKeyAlias, dbDomain.smlClientKeyAlias)
                .append(signatureKeyAlias, dbDomain.signatureKeyAlias)
                .append(signatureAlgorithm, dbDomain.signatureAlgorithm)
                .append(signatureDigestMethod, dbDomain.signatureDigestMethod)
                .append(defaultResourceTypeIdentifier, dbDomain.defaultResourceTypeIdentifier)
                .append(visibility, dbDomain.visibility).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).appendSuper(super.hashCode()).append(id).append(domainCode).toHashCode();
    }
}
