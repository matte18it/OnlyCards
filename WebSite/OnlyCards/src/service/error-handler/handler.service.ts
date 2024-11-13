import { Injectable } from '@angular/core';
import {Router} from "@angular/router";
import Swal, {SweetAlertResult} from "sweetalert2";

@Injectable({
  providedIn: 'root'
})
export class HandlerService {

  constructor(private router:Router) { }

  throwGlobalError(error: string):Promise<SweetAlertResult<void>> {
    return Swal.fire({
      icon: 'error',
      text: error,
      background: 'var(--alert-background)',
      confirmButtonColor: 'var(--accent-color)',
      color: 'var(--text-color)'
    });
    }
  throwGlobalErrorUnreversable(error: string): Promise<SweetAlertResult<void>> {
    return Swal.fire({
      icon: 'error',
      text: error,
      background: 'var(--alert-background)',
      confirmButtonColor: 'var(--accent-color)',
      color: 'var(--text-color)',
      allowOutsideClick: false,
      allowEscapeKey: false,
      allowEnterKey: false,
      grow: 'fullscreen',
      showConfirmButton: false  // Rimuove il bottone di conferma se non vuoi permettere la chiusura
    });
  }
    throwGlobalSuccess(success: string):Promise<SweetAlertResult<void>> {
      return Swal.fire({
        icon: 'success',
        text: success,
        background: 'var(--alert-background)',
        confirmButtonColor: 'var(--accent-color)',
        color: 'var(--text-color)'
      });
    }
    throwGlobalWarning(warning: string):Promise<SweetAlertResult<void>> {
      return Swal.fire({
        icon: 'warning',
        text: warning,
        background: 'var(--alert-background)',
        confirmButtonColor: 'var(--accent-color)',
        color: 'var(--text-color)'
      });
    }

  throwSuccessToast(val: string) {
     return Swal.mixin({
      toast: true,
      position: 'bottom-end',
      showConfirmButton: false,
      timer: 4000,
      timerProgressBar: true,
      background: 'var(--alert-background)',
      confirmButtonColor: 'var(--accent-color)',
      color: 'var(--text-color)'
    }).fire({
      icon: 'success',
      title: val
    });
  }

  throwInfoToast(val: string) {
      return Swal.mixin({
        toast: true,
        position: 'bottom-end',
        showConfirmButton: false,
        timer: 4000,
        timerProgressBar: true,
        background: 'var(--alert-background)',
        confirmButtonColor: 'var(--accent-color)',
        color: 'var(--text-color)'
      }).fire({
        icon: 'info',
        title: val
      });
  }

  throwErrorToast(val: string) {
     Swal.mixin({
      toast: true,
       position: 'bottom-end',
      showConfirmButton: false,
      timer: 4000,
      timerProgressBar: true,
      background: 'var(--alert-background)',
      confirmButtonColor: 'var(--accent-color)',
      color: 'var(--text-color)'
    }).fire({
      icon: 'error',
      title: val
    });

  }

}
