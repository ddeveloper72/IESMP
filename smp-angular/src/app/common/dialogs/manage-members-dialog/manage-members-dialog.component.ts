import {Component, Inject, Input, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {FormBuilder, FormControl, FormGroup} from "@angular/forms";
import {MembershipRoleEnum} from "../../enums/membership-role.enum";
import {lastValueFrom, Observable} from "rxjs";
import {SearchUserRo} from "../../model/search-user-ro.model";
import {MemberRo} from "../../model/member-ro.model";
import {DomainRo} from "../../model/domain-ro.model";
import {MemberTypeEnum} from "../../enums/member-type.enum";
import {AlertMessageService} from "../../alert-message/alert-message.service";
import {GroupRo} from "../../model/group-ro.model";
import {ResourceRo} from "../../model/resource-ro.model";
import {TranslateService} from "@ngx-translate/core";
import {MembershipService} from "../../services/membership.service";


@Component({
  templateUrl: './manage-members-dialog.component.html',
  styleUrls: ['./manage-members-dialog.component.css']
})
export class ManageMembersDialogComponent implements OnInit {

  memberForm: FormGroup;

  message: string;
  messageType: string = "alert-error";
  formTitle = "";

  currentFilter: string;

  _currentMember: MemberRo;
  _currentDomain: DomainRo;
  _currentGroup: GroupRo;
  _currentResource: ResourceRo;
  membershipType: MemberTypeEnum = MemberTypeEnum.DOMAIN;

  filteredOptions: Observable<SearchUserRo[]>;

  readonly memberRoles = Object.keys(MembershipRoleEnum).map(el => {
    return {key: el, value: MembershipRoleEnum[el]}
  });


  constructor(@Inject(MAT_DIALOG_DATA) public data: any,
              private membershipService: MembershipService,
              public dialogRef: MatDialogRef<ManageMembersDialogComponent>,
              private alertService: AlertMessageService,
              private formBuilder: FormBuilder,
              private translateService: TranslateService
  ) {
    dialogRef.disableClose = true;//disable default close operation
    this._currentDomain = data.domain;
    this._currentGroup = data.group;
    this._currentResource = data.resource;
    this.membershipType= data.membershipType;

    this.memberForm = formBuilder.group({
      'member-user': new FormControl({value: null}),
      'member-fullName': new FormControl({value: null}),
      'member-memberOf': new FormControl({value: null}),
      'member-roleType': new FormControl({value: null})
    });
    this.member = {
      ...data.member
    };
    this.currentFilter = "";
    (async () => await this.updateFormTitle()) ();
  }

  async updateFormTitle() {
    this.formTitle = this._currentMember.memberId
        ? await lastValueFrom(this.translateService.get("manage.members.dialog.title.edit.mode", {membershipType: this.membershipType}))
        : await lastValueFrom(this.translateService.get("manage.members.dialog.title.invite.mode", {membershipType: this.membershipType}));
  }

  get member(): MemberRo {
    let member = {...this._currentMember};
    member.username = this.memberForm.get('member-user').value;
    member.fullName = this.memberForm.get('member-fullName').value;
    member.memberOf = this.memberForm.get('member-memberOf').value;
    member.roleType = this.memberForm.get('member-roleType').value;
    return member;
  }

  get newMode(): boolean {

    return !this._currentMember?.memberId;
  }

  @Input() set member(value: MemberRo) {
    this._currentMember = value;

    if (!!value) {
      this.memberForm.controls['member-user'].setValue(value.username);
      // control disable enable did not work??
      if (this.newMode) {
        this.memberForm.controls['member-user'].enable();
      } else {
        this.memberForm.controls['member-user'].disable();
      }
      this.memberForm.controls['member-fullName'].setValue(value.fullName);
      this.memberForm.controls['member-memberOf'].setValue(value.memberOf);
      this.memberForm.controls['member-roleType'].setValue(value.roleType);
    } else {
      this.memberForm.controls['member-user'].setValue("");
      this.memberForm.controls['member-fullName'].setValue("");
      this.memberForm.controls['member-memberOf'].setValue("");
      this.memberForm.controls['member-roleType'].setValue("");
    }
    this.memberForm.markAsPristine();
  }

  ngOnInit() {
    this.filteredOptions = this.membershipService.getUserLookupObservable("");
  }

  get inviteTarget():string{
    switch (this.membershipType) {
      case MemberTypeEnum.DOMAIN:
        return " domain ["+this._currentDomain?.domainCode+"]"
      case MemberTypeEnum.GROUP:
        return " group ["+this._currentGroup?.groupName+"]"
      case MemberTypeEnum.RESOURCE:
        return " resource ["+this._currentResource?.resourceTypeIdentifier+"]"
    }
    return " target not selected!"
  }

  applyUserFilter(event: Event) {
    let filterValue = (event.target as HTMLInputElement).value;
    if (this.currentFilter == filterValue) {
      // ignore update
      return;
    }
    this.currentFilter = filterValue
    this.filteredOptions = this.membershipService.getUserLookupObservable(filterValue.trim().toLowerCase());
  }

  clearAlert() {
    this.message = null;
    this.messageType = null;
  }


  closeDialog() {
    this.dialogRef.close()
  }

  get submitButtonEnabled(): boolean {
    return this.memberForm.valid && this.memberForm.dirty;
  }

  public onSaveButtonClicked() {
      this.getAddMembershipService().subscribe((member: MemberRo) => {
        if (!!member) {
          this.closeDialog();
        }
      }, (error)=> {
        this.alertService.error(error.error?.errorDescription)
      });
    }


  protected getAddMembershipService(): Observable<MemberRo> {
    switch (this.membershipType) {
      case MemberTypeEnum.DOMAIN:
        return this.membershipService.addEditMemberToDomain(this._currentDomain.domainId, this.member)
      case MemberTypeEnum.GROUP:
        return  this.membershipService.addEditMemberToGroup(this._currentGroup.groupId,this._currentDomain.domainId, this.member)
      case MemberTypeEnum.RESOURCE:
        return  this.membershipService.addEditMemberToResource(this._currentResource, this._currentGroup,this._currentDomain, this.member)
    }
  }
}
