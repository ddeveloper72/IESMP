package eu.europa.ec.edelivery.smp.data.dao;

public class QueryNames {

    public static final String QUERY_CREDENTIAL_BY_CREDENTIAL_NAME_TYPE_TARGET = "DBCredential.getUserByCredentialNameTypeAndTarget";
    public static final String QUERY_CREDENTIALS_BY_CI_USERNAME_CREDENTIAL_TYPE_TARGET = "DBCredential.getUserByUsernameCredentialTypeAndTarget";
    public static final String QUERY_CREDENTIALS_BY_USERID_CREDENTIAL_TYPE_TARGET = "DBCredential.getUserByUserIdCredentialTypeAndTarget";

    public static final String QUERY_CREDENTIAL_ALL = "DBCredential.getAll";
    public static final String QUERY_CREDENTIAL_BY_CERTIFICATE_ID = "DBCredential.getCredentialByCertificateId";
    public static final String QUERY_CREDENTIAL_BY_CI_CERTIFICATE_ID = "DBCredential.getCredentialByCertificateIdCaseInsensitive";


    public static final String QUERY_DOMAIN_ALL = "DBDomain.getAll";
    public static final String QUERY_DOMAIN_CODE = "DBDomain.getDomainByCode";

    public static final String QUERY_DOMAIN_SMP_SML_ID = "DBDomain.getDomainBySmlSmpId";

    public static final String QUERY_DOMAIN_BY_USER_ROLES_COUNT = "DBDomain.getByUserAndRolesCount";
    public static final String QUERY_DOMAIN_BY_USER_ROLES = "DBDomain.getByUserAndRoles";

    public static final String QUERY_DOMAIN_BY_USER_GROUP_ROLES_COUNT = "DBDomain.getByUserAndGroupRolesCount";
    public static final String QUERY_DOMAIN_BY_USER_GROUP_ROLES = "DBDomain.getByUserAndGroupRoles";

    public static final String QUERY_DOMAIN_BY_USER_RESOURCE_ROLES_COUNT = "DBDomain.getByUserAndResourceRolesCount";
    public static final String QUERY_DOMAIN_BY_USER_RESOURCE_ROLES = "DBDomain.getByUserAndResourceRoles";
    public static final String QUERY_EXTENSION_ALL = "DBExtension.getAll";
    public static final String QUERY_EXTENSION_BY_IDENTIFIER = "DBExtension.getByIdentifier";

    public static final String QUERY_GROUP_ALL = "DBGroup.getAll";
    public static final String QUERY_GROUP_BY_DOMAIN = "DBGroup.getByDomain";
    public static final String QUERY_GROUP_BY_NAME_DOMAIN = "DBGroup.getByNameDomain";
    public static final String QUERY_GROUP_BY_NAME_DOMAIN_CODE = "DBGroup.getByNameDomainCode";

    public static final String QUERY_GROUP_BY_USER_GROUP_ROLES = "DBGroup.getByUserAndRoles";
    public static final String QUERY_GROUP_BY_DOMAIN_USER_GROUP_ROLES = "DBGroup.getByDomainAndUserAndGroupRoles";

    public static final String QUERY_GROUP_BY_DOMAIN_USER_RESOURCE_ROLES = "DBGroup.getByDomainAndUserAndResourceRoles";
    public static final String QUERY_GROUP_BY_USER_ROLES_COUNT = "DBGroup.getByUserAndGrouRolesCount";
    public static final String QUERY_GROUP_MEMBERS_COUNT = "DBGroupMember.getByGroupCount";
    public static final String QUERY_GROUP_MEMBERS_FILTER_COUNT = "DBGroupMember.getByGroupFilterCount";
    public static final String QUERY_GROUP_MEMBERS = "DBGroupMember.getByGroup";
    public static final String QUERY_GROUP_MEMBERS_FILTER = "DBGroupMember.getByGroupFilter";

    public static final String QUERY_DOMAIN_MEMBER_ALL = "DBDomainMember.getAll";
    public static final String QUERY_DOMAIN_MEMBER_BY_USER_DOMAINS_COUNT = "DBDomainMember.getByUserAndDomainsCount";
    public static final String QUERY_DOMAIN_MEMBER_BY_USER_DOMAINS = "DBDomainMember.getByUserAndDomains";

    public static final String QUERY_DOMAIN_MEMBERS_COUNT = "DBDomainMember.getByDomainCount";

    public static final String QUERY_DOMAIN_MEMBERS_FILTER_COUNT = "DBDomainMember.getByDomainFilterCount";
    public static final String QUERY_DOMAIN_MEMBERS = "DBDomainMember.getByDomain";
    public static final String QUERY_DOMAIN_MEMBERS_FILTER = "DBDomainMember.getByDomainFilter";

    public static final String QUERY_DOMAIN_RESOURCE_DEF_ALL = "DBDomainResourceDef.getAll";
    public static final String QUERY_DOMAIN_RESOURCE_DEF_DOMAIN_ALL = "DBDomainResourceDef.getAllForDomain";
    public static final String QUERY_DOMAIN_RESOURCE_DEF_DOMAIN_CODE_SEGMENT_URL = "DBDomainResourceDef.getByDomainCodeResDefURL";

    public static final String QUERY_DOMAIN_RESOURCE_DEF_DOMAIN_ID_RESDEF_IDENTIFIER = "DBDomainResourceDef.getByDomainIdAdResDefIdentifier";

    public static final String QUERY_DOMAIN_RESOURCE_DEF_DOMAIN_RES_DEF = "DBDomainResourceDef.getByDomainResDef";

    public static final String QUERY_RESOURCE_BY_IDENTIFIER_RESOURCE_DEF_DOMAIN = "DBResource.getResByIdentifierAndResourceDefAndDomain";
    public static final String QUERY_RESOURCES_BY_DOMAIN_ID_COUNT = "DBResource.getResByDomainIdCount";
    public static final String QUERY_RESOURCES_BY_DOMAIN_ID_RESOURCE_DEF_ID_COUNT = "DBResource.getResByDomainIdAndResourceDefCount";


    public static final String QUERY_RESOURCE_ALL_FOR_USER = "DBResource.getPublicSearch";

    public static final String QUERY_RESOURCE_ALL_FOR_USER_COUNT = "DBResource.getPublicSearchCount";

    public static final String QUERY_RESOURCE_FILTER = "DBResource.getResourcesByFilter";
    public static final String QUERY_RESOURCE_FILTER_COUNT = "DBResource.getResourcesByFilterCount";

    public static final String QUERY_RESOURCE_MEMBER_ALL = "DBResourceMember.getAll";
    public static final String QUERY_RESOURCE_MEMBER_BY_USER_RESOURCE_COUNT = "DBResourceMember.getByUserAndResourceCount";
    public static final String QUERY_RESOURCE_MEMBER_BY_USER_DOMAIN_RESOURCE_COUNT = "DBResourceMember.getByUserAndDomainResourceCount";
    public static final String QUERY_RESOURCE_MEMBER_BY_USER_DOMAIN_RESOURCE_ROLE_COUNT = "DBResourceMember.getByUserAndDomainRoleResourceCount";
    public static final String QUERY_RESOURCE_MEMBER_BY_USER_GROUP_RESOURCES_ROLE_COUNT = "DBResourceMember.getByUserAndGroupsResourcesAndRoleCount";
    public static final String QUERY_RESOURCE_MEMBER_BY_USER_GROUP_RESOURCES_COUNT = "DBResourceMember.getByUserAndGroupsResourcesCount";
    public static final String QUERY_RESOURCE_MEMBERS_COUNT = "DBResourceMember.getByResourceCount";
    public static final String QUERY_RESOURCE_MEMBERS_FILTER_COUNT = "DBResourceMember.getByResourceFilterCount";
    public static final String QUERY_RESOURCE_MEMBERS = "DBResourceMember.getByResource";
    public static final String QUERY_RESOURCE_MEMBERS_FILTER = "DBResourceMember.getByResourceFilter";

    public static final String QUERY_RESOURCE_MEMBER_BY_USER_RESOURCE= "DBResourceMember.getByUserAndResource";

    public static final String QUERY_SUBRESOURCE_BY_IDENTIFIER_RESOURCE_SUBRESDEF = "DBSubresource.getByIdentifierAndResourceAndSubresourceDef";

    public static final String QUERY_SUBRESOURCE_BY_IDENTIFIER_RESOURCE_ID = "DBSubresource.getByIdentifierAndResourceId";
    public static final String QUERY_SUBRESOURCE_BY_RESOURCE_SUBRESDEF = "DBSubresource.getAllForResourceAndTypeIdentifier";

    public static final String QUERY_SUBRESOURCE_BY_RESOURCE_ID = "DBSubresource.getAllForResourceId";
    public static final String QUERY_SUBRESOURCE_DEF_ALL = "DBSubresource.getAll";
    public static final String QUERY_SUBRESOURCE_DEF_BY_IDENTIFIER = "DBResourceDef.getAllByIdentifier";
    public static final String QUERY_SUBRESOURCE_DEF_URL_SEGMENT = "DBResourceDef.getAllByUrlSegment";

    public static final String QUERY_RESOURCE_DEF_ALL = "DBResourceDef.getAll";
    public static final String QUERY_RESOURCE_DEF_BY_DOMAIN = "DBResourceDef.getByDomain";
    public static final String QUERY_RESOURCE_DEF_URL_SEGMENT = "DBResourceDef.getResourceDefByURLSegment";
    public static final String QUERY_RESOURCE_DEF_BY_IDENTIFIER = "DBResourceDef.getResourceDefByIdentifier";
    public static final String QUERY_RESOURCE_DEF_BY_IDENTIFIER_EXTENSION = "DBExtResourceDef.getByIdentifierExtension";

    public static final String QUERY_DOCUMENT_FOR_RESOURCE = "DBDocument.getForResource";

    public static final String QUERY_DOCUMENT_VERSION_CURRENT_FOR_RESOURCE = "DBDocumentVersion.forCurrentForResource";
    public static final String QUERY_DOCUMENT_VERSION_LIST_FOR_RESOURCE = "DBDocumentVersion.getAllForResource";


    public static final String QUERY_DOCUMENT_VERSION_CURRENT_FOR_SUBRESOURCE = "DBDocumentVersion.forCurrentForSubresource";
    public static final String QUERY_DOCUMENT_VERSION_LIST_FOR_SUBRESOURCE = "DBDocumentVersion.getAllForSubresource";

    public static final String QUERY_GROUP_MEMBER_ALL = "DBGroupMember.getAll";
    public static final String QUERY_GROUP_MEMBER_BY_USER_GROUPS_COUNT = "DBGroupMember.getByUserAndGroupsCount";
    public static final String QUERY_GROUP_MEMBER_BY_USER_DOMAIN_GROUPS_COUNT = "DBGroupMember.getByUserAndDomainGroupsCount";
    public static final String QUERY_GROUP_MEMBER_BY_USER_GROUPS = "DBGroupMember.getByUserAndGroups";

    public static final String QUERY_RESOURCE_MEMBER_BY_USER_RESOURCES = "DBResourceMember.getByUserAndResources";
    public static final String QUERY_GROUP_MEMBER_BY_USER_DOMAIN_GROUPS_ROLE_COUNT = "DBGroupMember.getByUserAndDomainGroupsAndRoleCount";

    public static final String QUERY_USER_BY_CI_USERNAME = "DBUser.getUserByUsernameInsensitive";

    public static final String QUERY_USER_BY_CREDENTIAL_NAME_TYPE_TARGET = "DBUser.getUserByCredentialNameTypeTarget";
    public static final String QUERY_USER_BY_CI_CREDENTIAL_NAME_TYPE_TARGET = "DBUser.getUserByCaseInsensitiveCredentialNameTypeTarget";

    public static final String QUERY_USER_COUNT = "DBUser.getUsersCount";
    public static final String QUERY_USER_FILTER_COUNT = "DBUser.getUsersByFilterCount";
    public static final String QUERY_USERS = "DBUser.getUsers";
    public static final String QUERY_QUERY_USERS_FILTER = "DBUser.getUsersByFilter";
    // ALERTS
    public static final String QUERY_CREDENTIAL_BEFORE_EXPIRE = "DBCredential.getCredentialBeforeExpireAlerts";
    public static final String QUERY_CREDENTIAL_EXPIRED =  "DBCredential.getCredentiaExpiredAlerts";


    public static final String PARAM_NAME = "name";
    public static final String PARAM_CODE = "code";
    public static final String PARAM_IDENTIFIER = "identifier";
    public static final String PARAM_ID = "id";

    public static final String PARAM_USER_FILTER = "user_filter";



    public static final String PARAM_URL_SEGMENT = "url_segment";
    public static final String PARAM_EXTENSION_ID = "extension_id";
    public static final String PARAM_USER_ID = "user_id";

    public static final String PARAM_CERTIFICATE_IDENTIFIER = "certificate_identifier";

    public static final String PARAM_RESOURCE_ID = "resource_id";
    public static final String PARAM_RESOURCE_IDS = "resource_ids";
    public static final String PARAM_SUBRESOURCE_ID = "subresource_id";
    // resource identifier value
    public static final String PARAM_RESOURCE_IDENTIFIER = "resource_identifier";
    public static final String PARAM_RESOURCE_SCHEME = "resource_scheme";

    public static final String PARAM_SUBRESOURCE_IDENTIFIER = "subresource_identifier";
    public static final String PARAM_SUBRESOURCE_SCHEME = "subresource_scheme";
    // resource identifier schem
    public static final String PARAM_RESOURCE_FILTER = "resource_filter";
    public static final String PARAM_RESOURCE_DEF_ID = "resource_def_id";
    public static final String PARAM_RESOURCE_DEF_IDENTIFIER = "resource_def_identifier";
    public static final String PARAM_SUBRESOURCE_DEF_ID = "subresource_def_id";


    public static final String PARAM_SUBRESOURCE_DEF_IDENTIFIER = "subresource_def_identifier";
    public static final String PARAM_DOMAIN_ID = "domain_id";
    public static final String PARAM_DOMAIN_CODE = "domain_code";
    public static final String PARAM_DOMAIN_SML_SMP_ID = "sml_smp_id";

    public static final String PARAM_DOMAIN_IDS = "domain_ids";

    public static final String PARAM_DOCUMENT_ID = "document_id";

    public static final String PARAM_GROUP_ID = "group_id";
    public static final String PARAM_GROUP_IDS = "group_ids";
    public static final String PARAM_MEMBERSHIP_ROLE = "membership_role";

    public static final String PARAM_MEMBERSHIP_ROLES = "membership_roles";
    public static final String PARAM_USER_USERNAME = "username";

    public static final String IDENTIFIER_VALUE = "identifier_value";
    public static final String IDENTIFIER_SCHEME = "identifier_scheme";

    public static final String PARAM_CREDENTIAL_NAME = "credential_name";
    public static final String PARAM_CREDENTIAL_TYPE = "credential_type";
    public static final String PARAM_CREDENTIAL_TARGET = "credential_target";





    private QueryNames() {
    }
}
