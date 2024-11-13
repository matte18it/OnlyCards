import { Component, OnInit } from '@angular/core';
import { OrderService } from '../../service/order/order.service';
import { Order, OrderInfo } from '../../model/order';
import { HandlerService } from '../../service/error-handler/handler.service';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Status ,getStatusString} from '../../model/enum/statusEnum';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { OrderModalComponent } from '../order-modal/order-modal.component';
import { Title } from '@angular/platform-browser';

@Component({
  selector: 'app-admin-orders',
  templateUrl: './admin-orders.component.html',
  styleUrls: ['./admin-orders.component.css', '../admin-users/admin-users.component.css']
})
export class AdminOrdersComponent implements OnInit {

  orders: OrderInfo[] = [];
  sortColumn: string = 'created-date-desc';
  page: number = 1;
  form:FormGroup;
  pageSize: number = 10;
  totalElements: number = 0;

  constructor(private orderService: OrderService, private handler:HandlerService, private fb:FormBuilder, private modal:NgbModal, private title:Title) { 
    this.form= this.fb.group({
      buyer: [''],
      seller: [''],
});
  }
  type: string = 'admin-orders';

  ngOnInit(): void {
    this.title.setTitle("Only Cards | Gestione ordini" );  // setto il titolo della pagina
    this.getOrders();
    this.form.valueChanges.subscribe(() => {
      this.getOrders();
    });
    
  }



  protected hasRole(roleName: string): boolean {
    const roles = localStorage.getItem('roles')?.split(',') || [];
    return roles.includes(roleName);
  }
  protected sort(column: string) {
    if (this.sortColumn.startsWith(column)) {
      // Se la colonna è già ordinata, inverti la direzione
      this.sortColumn = this.sortColumn.endsWith('asc') ? `${column}-desc` : `${column}-asc`;
    } else {
      // Se è una nuova colonna, imposta ascendente come default
      this.sortColumn = `${column}-asc`;
      
    }
    this.getOrders();
  }
  protected getOrders() {
    const buyer = this.form.get('buyer')?.value;
    const seller = this.form.get('seller')?.value;
    const direction = this.sortColumn.endsWith('asc') ? 'asc' : 'desc';
    const orderBy = this.sortColumn.replace('-asc', '').replace('-desc', '');
    const page = this.page;

    this.orderService.getOrders(buyer, seller, orderBy, direction, page-1, this.pageSize).subscribe((response) => {
      if(response === null || response.body === null) {
        this.handler.throwErrorToast('Errore durante il recupero degli ordini');
        return;
      }
      this.orders = response.body.content;
      this.pageSize = response.body.size;
      this.totalElements = response.body.totalElements;
      
    }, (error) => {
      if(error.status === 401) {
        this.handler.throwGlobalWarning('Sessione scaduta, Si prega di effettuare il login');
        return;
      }
      if(error.status === 403) {
        this.handler.throwGlobalWarning('Non hai i permessi per accedere a questa pagina');
        return;
      }
      if(error.status === 429) {
        this.handler.throwGlobalWarning('Troppi tentativi, riprova più tardi');
        return;
      }
      this.handler.throwErrorToast('Errore durante il recupero degli ordini');
    });

  }
  getStatus(){
    return Status;
  }

  openModalOrder(id: string) {
    const modalRef = this.modal.open(OrderModalComponent, );
    modalRef.componentInstance.orderId = id;
    modalRef.componentInstance.status = this.orders.find(order => order.id === id)?.status || Status.PENDING;
    modalRef.result.then(
			(result) => {
        this.handler.throwSuccessToast('Ordine modificato con successo');
        this.getOrders();
			},
			(reason) => {
				
			},
		);}




}
