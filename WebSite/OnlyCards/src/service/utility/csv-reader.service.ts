import { Injectable } from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {catchError} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})

export class CsvReaderService {
  constructor(private http: HttpClient) {}

  // Metodo per caricare il csv file
  public loadCSVFile(path: string): Observable<string> {
    return this.http.get(path, { responseType: 'text' }).pipe(
      catchError(error => {
        console.error('Errore nel caricamento del file CSV:', error);
        return of(''); // Ritorna una stringa vuota in caso di errore
      })
    );
  }

  // Metodo per caricare i dati dal file
  public parseCSV(testoCSV: string, nomeCampo: string): string[] {
    const righe = testoCSV.split('\n');
    if (righe.length === 0) {
      throw new Error('Il file CSV è vuoto.');
    }

    const intestazioni = righe[0].split(';').map(heading => heading.trim());
    const indiceCampo = intestazioni.indexOf(nomeCampo);

    if (indiceCampo === -1) {
      throw new Error(`Campo '${nomeCampo}' non trovato.`);
    }

    return righe.slice(1)  // Salta l'intestazione
      .map(riga => riga.split(';')[indiceCampo]?.trim())  // Estrai il valore del campo
      .filter(valore => valore !== undefined && valore !== '');
  }
}
