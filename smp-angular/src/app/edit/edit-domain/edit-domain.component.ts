import {AfterViewInit, Component, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {EditDomainService} from "./edit-domain.service";
import {AlertMessageService} from "../../common/alert-message/alert-message.service";
import {MatDialog} from "@angular/material/dialog";
import {BeforeLeaveGuard} from "../../window/sidenav/navigation-on-leave-guard";
import {DomainRo} from "../../common/model/domain-ro.model";
import {CancelDialogComponent} from "../../common/dialogs/cancel-dialog/cancel-dialog.component";
import {MatPaginator} from "@angular/material/paginator";
import {MatSort} from "@angular/material/sort";
import {MatTabGroup} from "@angular/material/tabs";
import {MemberTypeEnum} from "../../common/enums/member-type.enum";


@Component({
  moduleId: module.id,
  templateUrl: './edit-domain.component.html',
  styleUrls: ['./edit-domain.component.css']
})
export class EditDomainComponent implements OnInit, AfterViewInit, BeforeLeaveGuard {

  membershipType:MemberTypeEnum = MemberTypeEnum.DOMAIN;
  displayedColumns: string[] = ['domainCode'];
  dataSource: MatTableDataSource<DomainRo> = new MatTableDataSource();
  selected: DomainRo;
  domainList: DomainRo[] = [];
  currenTabIndex: number = 0;
  handleTabClick: any;

  loading: boolean = false;

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;
  @ViewChild('domainTabs') domainTabs: MatTabGroup;

  constructor(private domainService: EditDomainService,
              private alertService: AlertMessageService,
              private dialog: MatDialog) {
    this.refreshDomains();
  }


  ngOnInit(): void {
    // filter predicate for search the domain
    this.dataSource.filterPredicate =
      (data: DomainRo, filter: string) => {
        return !filter || -1 != data.domainCode.toLowerCase().indexOf(filter.trim().toLowerCase())
      };
  }

  ngAfterViewInit():void {
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
    // MatTab has only onTabChanged which is a bit to late. Register new listener to  internal
    // _handleClick handler
    this.registerTabClick();
  }


  refreshDomains() {
    this.loading = true;
    this.domainService.getDomainsForDomainAdminUserObservable()
      .subscribe((result: DomainRo[]) => {
        this.updateDomainList(result)
        this.loading = false;
      }, (error: any) => {
        this.loading = false;
        this.alertService.error(error.error?.errorDescription)
      });
  }

  registerTabClick(): void {
    if (!this.domainTabs) {
      // tabs are not yet initialized
      return;
    }
    // Get the handler reference
    this.handleTabClick = this.domainTabs._handleClick;

    this.domainTabs._handleClick = (tab, header, newTabIndex) => {

      if (newTabIndex == this.currenTabIndex) {
        return;
      }

      if (this.isCurrentTabDirty()) {
        let canChangeTab = this.dialog.open(CancelDialogComponent).afterClosed().toPromise<boolean>();
        canChangeTab.then((canChange: boolean) => {
          if (canChange) {
            // reset
            this.handleTabClick.apply(this.domainTabs, [tab, header, newTabIndex]);
            this.currenTabIndex = newTabIndex;

          }
        });
      } else {
        this.handleTabClick.apply(this.domainTabs, [tab, header, newTabIndex]);
        this.currenTabIndex = newTabIndex;
      }
    }
  }

  updateDomainList(domainList: DomainRo[]) {
    this.domainList = domainList
    this.dataSource.data = this.domainList;

    if (!!this.domainList && this.domainList.length > 0) {
      this.selected = this.domainList[0];
    }
  }

  applyDomainFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();

    if (this.dataSource.paginator) {
      this.dataSource.paginator.firstPage();
    }
  }



  public domainSelected(domainSelected: DomainRo) {
    if (this.selected === domainSelected) {
      return;
    }
    if (this.isCurrentTabDirty()) {
      let canChangeTab = this.dialog.open(CancelDialogComponent).afterClosed().toPromise<boolean>();
      canChangeTab.then((canChange: boolean) => {
        if (canChange) {
          // reset
          this.selected = domainSelected;
        }
      });
    } else {
      this.selected = domainSelected;
    }
  }


  isCurrentTabDirty(): boolean {
    return false;
  }
  isDirty(): boolean {
    return  this.isCurrentTabDirty();
  }

  get canNotDelete(): boolean {
    return !this.selected;
  }

  get editMode(): boolean {
    return this.isCurrentTabDirty();
  }
}
