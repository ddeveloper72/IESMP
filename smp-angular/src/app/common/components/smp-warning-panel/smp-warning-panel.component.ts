import { Component, Input } from '@angular/core';

@Component({
  selector: 'smp-warning-panel',
  templateUrl: './smp-warning-panel.component.html',
})
export class SmpWarningPanelComponent {
  @Input() padding:boolean = true;
  @Input() label:string;
  @Input() icon:string;
  @Input() type:string = 'warning';

}
