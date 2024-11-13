import { Component, OnInit} from '@angular/core';
import { UserService } from '../../service/user/user.service';
import { User } from '../../model/user';
import { FormBuilder, FormGroup } from '@angular/forms';
import { HttpResponse } from "@angular/common/http";
import { NgbModal } from "@ng-bootstrap/ng-bootstrap";
import { UserEditModalComponent } from "../user-edit-modal/user-edit-modal.component";
import { Address } from "../../model/address";

@Component({
  selector: 'app-admin-users',
  templateUrl: './admin-users.component.html',
  styleUrls: ['./admin-users.component.css']
})
export class AdminUsersComponent implements OnInit {

  users: User[] = [];
  sortColumn = 'username'; // Imposta il default su username
  sortDirection = 'asc'; // Imposta il default su ascendente
  type: string = 'admin-users';
  page: number = 0;
  size: number = 10;
  totalElements: number = 0;
  currentPagination: number = 1;
  form: FormGroup;  // Aggiungiamo il form per la ricerca


  constructor(private userService: UserService, private modalService: NgbModal, private fb: FormBuilder) {
    this.form = this.fb.group({
      username: [''],  // Campo di ricerca per il nome utente
      email: ['']  // Campo di ricerca per l'email
    });
  }

  ngOnInit(): void {
    this.loadUsers();

    // Ricarica gli utenti ogni volta che cambia il campo di ricerca
    this.form.valueChanges.subscribe(() => {
      this.loadUsers();
    });
  }

  loadUsers(): void {
    const username = this.form.get('username')?.value || '';
    const email = this.form.get('email')?.value || '';
    const direction = this.sortDirection;
    const orderBy = this.sortColumn;
    const page = this.page; // Le API potrebbero essere 0-based per la paginazione

    this.userService.getUsers(username, email, orderBy, direction, page, this.size).subscribe(response => {
      if (response && response.body) {
        this.users = response.body.content;
        this.size = response.body.size;
        this.totalElements = response.body.totalElements;
      }
    });
  }

  sort(column: string): void {
    if (this.sortColumn === column) {
      this.sortDirection = this.sortDirection === 'asc' ? 'desc' : 'asc';
    } else {
      this.sortColumn = column;
      this.sortDirection = 'asc';
    }
    this.loadUsers();
  }




  updatePage(): void {
    this.page = this.currentPagination - 1; // Setta il valore corretto per la pagina (zero-indexed)
    console.log('Changing to page:', this.page); // Log per verificare il cambio di pagina
    this.loadUsers(); // Ricarica gli utenti per la nuova pagina
  }


  openEditModal(user: User): void {
    const modalRef = this.modalService.open(UserEditModalComponent);
    modalRef.componentInstance.user = user;

    this.userService.getUserAddresses(user.id).subscribe((addresses: Address[] | null) => {
      modalRef.componentInstance.addresses = addresses || [];
    });

    modalRef.componentInstance.onSave.subscribe((updatedUser: User) => {
      this.userService.updateUser(updatedUser.id, updatedUser).subscribe(() => {
        this.loadUsers();
        modalRef.close();
      });
    });

    modalRef.componentInstance.onDelete.subscribe((userId: string) => {
      this.userService.deleteUser(userId).subscribe(() => {
        this.loadUsers();
        modalRef.close();
      });
    });

    modalRef.componentInstance.onClose.subscribe(() => {
      modalRef.close();
    });
  }

  protected hasRole(roleName: string): boolean {
    const roles = localStorage.getItem('roles')?.split(',') || [];
    return roles.includes(roleName);
  }
}
