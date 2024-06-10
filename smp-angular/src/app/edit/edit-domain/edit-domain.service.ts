import {Injectable} from '@angular/core';
import {Observable, Subject} from 'rxjs';

import {HttpClient, HttpParams} from '@angular/common/http';
import {SecurityService} from "../../security/security.service";
import {User} from "../../security/user.model";
import {SmpConstants} from "../../smp.constants";
import {DomainRo} from "../../common/model/domain-ro.model";
import {GroupRo} from "../../common/model/group-ro.model";
import {ResourceDefinitionRo} from "../../system-settings/admin-extension/resource-definition-ro.model";
import {DomainPropertyRo} from "../../common/model/domain-property-ro.model";
import {
  AlertMessageService
} from "../../common/alert-message/alert-message.service";

@Injectable()
export class EditDomainService {

  private domainPropertyUpdateSubject: Subject<DomainPropertyRo[]> = new Subject<DomainPropertyRo[]>();

  constructor(
    private http: HttpClient,
    private securityService: SecurityService,
    private alertService: AlertMessageService) {
  }

  /**
   * Method fetches all domains where logged user is admin
   */
  public getDomainsForDomainAdminUserObservable():Observable<DomainRo[]> {
    return this.getDomainsForUserRoleTypeObservable("domain-admin")
  }

  /**
   * Method fetches all domains where logged user is admin
   */
  public getDomainsForGroupAdminUserObservable():Observable<DomainRo[]>  {
    return this.getDomainsForUserRoleTypeObservable("group-admin")
  }

  /**
   * Method fetches all domains where logged user is admin
   */
  public getDomainsForResourceAdminUserObservable():Observable<DomainRo[]>  {
    return this.getDomainsForUserRoleTypeObservable("resource-admin")
  }

  public getDomainsForUserRoleTypeObservable(type: string) :Observable<DomainRo[]> {
    let params: HttpParams = new HttpParams()
      .set(SmpConstants.PATH_QUERY_FILTER_TYPE, type);

    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.get<DomainRo[]>(SmpConstants.REST_EDIT_DOMAIN
      .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId), {params});
  }

  public getDomainGroupsObservable(domainId: string): Observable<GroupRo[]> {
    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.get<GroupRo[]>(SmpConstants.REST_EDIT_DOMAIN_GROUP
      .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
      .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domainId));
  }

  public deleteDomainGroupObservable(domainId: string, groupId: string): Observable<GroupRo> {
    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.delete<GroupRo>(SmpConstants.REST_EDIT_DOMAIN_GROUP_DELETE
      .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
      .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domainId)
      .replace(SmpConstants.PATH_PARAM_ENC_GROUP_ID, groupId));
  }

  public createDomainGroupObservable(domainId: string, group: GroupRo): Observable<GroupRo> {
    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.put<GroupRo>(SmpConstants.REST_EDIT_DOMAIN_GROUP_CREATE
        .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
        .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domainId)
      , group);
  }

  public saveDomainGroupObservable(domainId: string, group: GroupRo): Observable<GroupRo> {
    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.post<GroupRo>(SmpConstants.REST_EDIT_DOMAIN_GROUP_UPDATE
        .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
        .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domainId)
        .replace(SmpConstants.PATH_PARAM_ENC_GROUP_ID, group.groupId)
      , group);
  }


  public getDomainResourceDefinitionsObservable(domain: DomainRo): Observable<ResourceDefinitionRo[]> {
    const currentUser: User = this.securityService.getCurrentUser();
    return this.http.get<ResourceDefinitionRo[]>(SmpConstants.REST_EDIT_DOMAIN_RESOURCE_DEFS
        .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
        .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domain?.domainId)
    );
  }

  public getDomainProperties(domain: DomainRo): void {
    const currentUser: User = this.securityService.getCurrentUser();
    this.http.get<DomainPropertyRo[]>(SmpConstants.REST_EDIT_DOMAIN_PROPERTIES
      .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
      .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domain.domainId))
      .subscribe({
        next: (result: DomainPropertyRo[]): void => {
          this.notifyPropertiesUpdated(domain, result);
        },
        error: (error: any): void => {
          this.alertService.error(error.error?.errorDescription)
        }
      });
  }

  /**
   * Update domain property list
   * @param domain Domain to update
   * @param domainProperties List of domain properties
   */
  public updateDomainProperties(domain: DomainRo, domainProperties: DomainPropertyRo[] ): void {
    const currentUser: User = this.securityService.getCurrentUser();
    this.http.post<DomainPropertyRo[]>(SmpConstants.REST_EDIT_DOMAIN_PROPERTIES
        .replace(SmpConstants.PATH_PARAM_ENC_USER_ID, currentUser.userId)
        .replace(SmpConstants.PATH_PARAM_ENC_DOMAIN_ID, domain.domainId),
      domainProperties)
      .subscribe({
        next: (result: DomainPropertyRo[]): void => {
          this.notifyPropertiesUpdated(domain, result);
        },
        error: (error: any): void => {
          this.alertService.error(error.error?.errorDescription)
        }
      });
  }

  onDomainPropertyUpdatedEvent(): Observable<DomainPropertyRo[]> {
    return this.domainPropertyUpdateSubject.asObservable();
  }

  notifyPropertiesUpdated(domainRo: DomainRo, properties: DomainPropertyRo[]) {
    this.domainPropertyUpdateSubject.next(properties);
  }

}
