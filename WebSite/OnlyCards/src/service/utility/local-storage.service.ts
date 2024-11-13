import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LocalStorageService {

  constructor() {}

  // Metodo per salvare un oggetto nel local storage
  public setItem(key: string, value: string) : void {
    localStorage.setItem(key, value);
  }

  // Metodo per prendere un oggetto dal local storage
  public getItem(key: string): string | null {
    return localStorage.getItem(key);
  }

  // Metodo per rimuovere un oggetto dal local storage
  public removeItem(key: string): void {
    localStorage.removeItem(key);
  }

  // Metodo per pulire tutto
  public clear(): void {
    localStorage.clear();
  }

  removeAll() {
    localStorage.clear();
  }
}
