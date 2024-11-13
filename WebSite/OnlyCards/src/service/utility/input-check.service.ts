import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})

export class InputCheckService {
  constructor() {}

  // ---------- INPUT CHECK ----------
  // Funzione per verificare l'username
  public checkUsername(username: string): boolean {
    // Verifica la lunghezza dell'username
    if (username.length < 3 || username.length > 20) {
      return false;
    }

    // Verifica se l'username contiene solo caratteri alfanumerici e il trattino basso "_"
    const usernameRegex: RegExp = /^[a-zA-Z0-9_]+$/;
    if (!usernameRegex.test(username)) {
      return false;
    }

    // Se tutti i controlli passano, restituisci true
    return true;
  }
  // Funzione per verificare l'email
  public checkEmail(email: string): boolean {
    const emailRegex: RegExp = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return emailRegex.test(email);
  }
  // Funzione per verificare la password
  public checkPassword(password: string): boolean {
    // Verifica che la lunghezza della password sia almeno 8 caratteri
    if (password.length < 8)
      return false;

    // Verifica che la password contenga almeno una lettera minuscola
    const lowercaseRegex: RegExp = /[a-z]/;
    if (!lowercaseRegex.test(password))
      return false;

    // Verifica che la password contenga almeno una lettera maiuscola
    const uppercaseRegex: RegExp = /[A-Z]/;
    if (!uppercaseRegex.test(password))
      return false;

    // Verifica che la password contenga almeno un numero
    const numberRegex: RegExp = /[0-9]/;
    if (!numberRegex.test(password))
      return false;

    // Verifica che la password contenga almeno un carattere speciale
    const specialCharRegex: RegExp = /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/;
    if (!specialCharRegex.test(password))
      return false;

    // Se tutte le verifiche passano, restituisci true
    return true;
  }

  checkPhoneNumber(phoneNumber: string) {
    const regex = /^\+?([0-9]{2,3})?\s?([0-9]{6,10})$/;
    return regex.test(phoneNumber);
  }
}
