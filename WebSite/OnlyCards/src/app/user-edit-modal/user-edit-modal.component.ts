import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { FormGroup, FormBuilder, Validators, ValidationErrors } from '@angular/forms';
import { User } from '../../model/user';
import { Address } from "../../model/address";
import { UserService } from "../../service/user/user.service";
import { Role } from "../../model/role";

@Component({
  selector: 'app-user-edit-modal',
  templateUrl: './user-edit-modal.component.html',
  styleUrls: ['./user-edit-modal.component.css']
})
export class UserEditModalComponent implements OnInit {
  @Input() user!: User;
  @Input() addresses: Address[] = [];

  roles: Role[] = [];
  editUserForm!: FormGroup;
  isAdmin: boolean = false;

  @Output() onSave: EventEmitter<User> = new EventEmitter();
  @Output() onClose: EventEmitter<void> = new EventEmitter();
  @Output() onDelete: EventEmitter<string> = new EventEmitter();

  constructor(private fb: FormBuilder, private userService: UserService) {}

  ngOnInit(): void {
    this.isAdmin = this.hasUserRole('ROLE_ADMIN'); // Controlla se l'utente è admin
    this.createForm();

    if (this.user) {
      this.loadUserAddresses();
      this.loadUserRoles();
    }
  }

  // Funzione per creare il form con i controlli per username, email, ruoli, etc.
  createForm(): void {
    this.editUserForm = this.fb.group({
      username: [this.user?.username || '', [Validators.required, Validators.minLength(3)]],
      email: [this.user?.email || '', [Validators.required, Validators.email]],
      cellphoneNumber: [this.user?.cellphoneNumber || ''],
      blocked: [this.user?.blocked || false],
      admin: [this.hasUserRole('ROLE_ADMIN')],  // Checkbox "Admin"
      buyer: [false],
      seller: [false]
    }, {
      validators: this.atLeastOneRoleValidator()
    });

    // Se l'utente è admin, disabilita tutti i controlli dei ruoli
    if (this.isAdmin) {
      this.editUserForm.get('admin')?.disable();
      this.editUserForm.get('buyer')?.disable();
      this.editUserForm.get('seller')?.disable();
    } else {
      // Se non è admin, disabilita solo il controllo "admin"
      this.editUserForm.get('admin')?.disable();
    }
  }

  // Validatore personalizzato per assicurarsi che almeno un ruolo sia selezionato
  atLeastOneRoleValidator() {
    return (form: FormGroup): ValidationErrors | null => {
      const isAdmin = form.get('admin')?.value;
      const buyer = form.get('buyer')?.value;
      const seller = form.get('seller')?.value;

      // Se l'utente non è admin e nessun ruolo è selezionato, restituisci errore
      if (!isAdmin && !buyer && !seller) {
        return { noRoleSelected: true };
      }
      return null;
    };
  }


  // Metodo per caricare i ruoli dell'utente
  loadUserRoles(): void {
    if (this.user.id) {
      this.userService.getUserRoles(this.user.id).subscribe({
        next: (roles: Role[]) => {
          this.roles = roles;
          this.updateRoleCheckboxes();
        },
        error: (err) => {
          console.error('Errore durante il caricamento dei ruoli', err);
        }
      });
    }
  }

  // Funzione per aggiornare lo stato delle checkbox in base ai ruoli caricati
  updateRoleCheckboxes(): void {
    if (this.hasUserRole('ROLE_ADMIN')) {
      this.editUserForm.get('admin')?.setValue(true);
      this.editUserForm.get('buyer')?.disable();
      this.editUserForm.get('seller')?.disable();
    } else {
      this.editUserForm.get('admin')?.disable();
      this.editUserForm.get('buyer')?.enable();
      this.editUserForm.get('seller')?.enable();

      // Imposta le checkbox per "Buyer" e "Seller" in base ai ruoli dell'utente
      if (this.hasUserRole('ROLE_BUYER')) {
        this.editUserForm.get('buyer')?.setValue(true);
      }
      if (this.hasUserRole('ROLE_SELLER')) {
        this.editUserForm.get('seller')?.setValue(true);
      }
    }
  }

  // Funzione per verificare se l'utente ha un certo ruolo
  hasUserRole(role: string): boolean {
    return this.roles.some(r => r.name === role);
  }

  // Caricamento degli indirizzi dell'utente
  loadUserAddresses(): void {
    if (this.user.id) {
      this.userService.getUserAddresses(this.user.id).subscribe({
        next: (addresses: Address[]) => {
          this.addresses = addresses;
        },
        error: (err) => {
          this.addresses = [];
        }
      });
    }
  }

  closeModal(): void {
    this.onClose.emit();
  }

  // Metodo per salvare le modifiche all'utente
  saveUser(): void {
    const updatedRoles = this.roles.filter(role => !['ROLE_ADMIN', 'ROLE_BUYER', 'ROLE_SELLER'].includes(role.name));

    if (this.editUserForm.get('admin')?.value) {
      updatedRoles.push({ name: 'ROLE_ADMIN' });
    }
    if (this.editUserForm.get('buyer')?.value) {
      updatedRoles.push({ name: 'ROLE_BUYER' });
    }
    if (this.editUserForm.get('seller')?.value) {
      updatedRoles.push({ name: 'ROLE_SELLER' });
    }

    const updatedUser: User = {
      ...this.user,
      ...this.editUserForm.value,
      roles: updatedRoles
    };

    this.onSave.emit(updatedUser);
    this.closeModal();
  }

  deleteUser(): void {
    if (confirm(`Sei sicuro di voler eliminare questo utente?`)) {
      this.onDelete.emit(this.user.id);
      this.closeModal();
    }
  }
}
