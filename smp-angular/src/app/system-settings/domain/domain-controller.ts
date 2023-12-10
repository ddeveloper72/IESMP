import {SearchTableController} from '../../common/search-table/search-table-controller';
import {MatDialog, MatDialogConfig, MatDialogRef} from '@angular/material/dialog';
import {DomainDetailsDialogComponent} from './domain-details-dialog/domain-details-dialog.component';
import {DomainRo} from '../../common/model/domain-ro.model';
import {EntityStatus} from '../../common/enums/entity-status.enum';
import {GlobalLookups} from "../../common/global-lookups";
import {SearchTableValidationResult} from "../../common/search-table/search-table-validation-result.model";
import {SearchTableEntity} from "../../common/search-table/search-table-entity.model";
import {SmpConstants} from "../../smp.constants";
import {HttpClient} from "@angular/common/http";

export class DomainController implements SearchTableController {

  constructor(protected http: HttpClient, protected lookups: GlobalLookups, public dialog: MatDialog) {
  }

  public showDetails(row): MatDialogRef<any> {
    return this.dialog.open(DomainDetailsDialogComponent);
  }

  public edit(row): MatDialogRef<any> {
    return this.dialog.open(DomainDetailsDialogComponent, row);
  }

  public delete(row: any) {
  }

  newDialog(config): MatDialogRef<any> {
    if (config && config.data && config.data.edit) {
      return this.edit(config.data);
    } else {
      return this.showDetails(config.data);
    }
  }

  public newRow(): DomainRo {
    return {
      index: null,
      domainCode: '',
      smlSubdomain: '',
      smlSmpId: '',
      smlParticipantIdentifierRegExp: '',
      smlClientKeyAlias: '',
      signatureKeyAlias: '',
      status: EntityStatus.NEW,
      smlRegistered: false,
      smlClientCertAuth: false,
    }
  }
  public dataSaved() {
    this.lookups.refreshDomainLookupForLoggedUser();
  }

  validateDeleteOperation(rows: Array<SearchTableEntity>){
    var deleteRowIds = rows.map(rows => rows.id);
    return  this.http.put<SearchTableValidationResult>(SmpConstants.REST_INTERNAL_DOMAIN_VALIDATE_DELETE, deleteRowIds);
  }

  public newValidationResult(result: boolean, message: string): SearchTableValidationResult {
    return {
      validOperation: result,
      stringMessage: '',
    }
  }

  isRowExpanderDisabled(row: SearchTableEntity): boolean {
    return false;
  }

  isRecordChanged(oldModel, newModel): boolean {
    for (let property in oldModel) {
      let isEqual = this.isEqual(newModel[property],oldModel[property]);
      console.log("Property: "+property+" new: " +newModel[property] +  "old: " +oldModel[property] + " val: " + isEqual  );
      if (!isEqual) {
        return true; // Property has changed
      }
    }
    return false;
  }

  isEqual(val1, val2): boolean {
    return (this.isEmpty(val1) && this.isEmpty(val2)
      || val1 === val2);
  }

  isEmpty(str): boolean {
    return (!str || 0 === str.length);
  }
}
