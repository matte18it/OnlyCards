import { Component, inject, Input, OnInit } from '@angular/core';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';
import { OrderService } from '../../service/order/order.service';
import { OrderDetails, OrderEdit, OrderInfo } from '../../model/order';
import { HandlerService } from '../../service/error-handler/handler.service';
import { AbstractControl, FormBuilder, FormGroup, ValidationErrors, ValidatorFn } from '@angular/forms';
import { Status, getStatusEnum } from '../../model/enum/statusEnum';

@Component({
  selector: 'app-order-modal',
  templateUrl: './order-modal.component.html',
  styleUrl: './order-modal.component.css'
})
export class OrderModalComponent implements OnInit{


getKeyValue(value: string): string {
return getStatusEnum(value);
}
getStatusList(): string[] {
return Object.values(Status);
}
  activeModal = inject(NgbActiveModal);

	@Input() orderId!: string;
  @Input() status!: Status;
  order: OrderDetails | undefined;
  form: FormGroup;
  constructor(private orderService:OrderService, private handler:HandlerService, private fb:FormBuilder) { 
    this.form = this.fb.group({
      status: ['']
    });
  }

  ngOnInit(): void {
    this.form.patchValue({
      status: this.status
    });
    this.form.get('status')?.setValidators(this.statusNotCancelledValidator());
    if(this.isNonModifiableStatus()) {
      this.form.get('status')?.disable();
      console.log('disabled');
    }

    this.orderService.getOrderById(this.orderId).subscribe(response => {
      if(!response || !response.body) {
        this.handler.throwErrorToast('Impossibile recuperare i dettagli dell\'ordine');
        this.activeModal.dismiss('Error');
        return;
      }
      this.order = response.body;
   
    }, error => {
      this.handler.throwErrorToast('Impossibile recuperare i dettagli dell\'ordine')
      this.activeModal.dismiss('Error');
    });

  }
  isNonModifiableStatus(): boolean {
    return this.status.toString() === 'CANCELLED' || this.status.toString() === 'DELIVERED';
  }
  noChangeInForm(): boolean {
   return this.form.get('status')?.value === this.status;
    }
    statusNotCancelledValidator(): ValidatorFn {

      return (control: AbstractControl): ValidationErrors | null => {
        if(this.status.toString() === 'SHIPPED') {
          return control.value === 'CANCELLED' || control.value==='PENDING' ? { 'statusNotAllowed': { value: control.value } } : null;
        }
        return null;
      };
    }

    editOrder() {
      const status = this.form.get('status')?.value;
      if(!status) {
        this.handler.throwErrorToast('Seleziona uno stato');
        return;
      }
      const orderEdit = new OrderEdit(status);
      this.orderService.updateOrder(this.orderId, orderEdit).subscribe(response => {
        if(!response) {
          this.handler.throwErrorToast('Errore durante l\'aggiornamento dello stato');
          return;
        }
        this.activeModal.close('Success');
      }, error => {
        this.handler.throwErrorToast('Errore durante l\'aggiornamento dello stato');
      });
    }

}
