import { Injectable } from '@angular/core';
import { Chart, registerables } from 'chart.js';

// Registra i plugin necessari
Chart.register(...registerables);

@Injectable({
  providedIn: 'root'
})

export class CardDetailsChartService {
  constructor() {}

  // ---------- VARIABILI -----------
  protected myChart: any;

  // ---------- COSTRUTTORE -----------
  public createChart(medieGiornaliere: number[], lastDate: string[]) {
    const labels: string[] = lastDate.map(date => {
      const [year, month, day] = date.split('-');
      return `${month}-${day}`;
    });

    // Distruggi il grafico precedente se esiste
    if (this.myChart) {
      this.myChart.destroy();
    }

    this.myChart = new Chart("medieGiornaliere", {
      type: "line",
      data: {
        labels: labels,
        datasets: [{
          data: medieGiornaliere,
          fill: true,
          borderColor: "#c51d39",
          borderWidth: 2,
          pointBackgroundColor: "#000000",
          tension: 0.1,
        }]
      },
      options: {
        scales: {
          x: {
            ticks: {
              maxRotation: 45,
              color: "#c51d39"
            },
            grid: {
              color: "#353b48"
            }
          },
          y: {
            ticks: {
              color: "#c51d39"
            },
            grid: {
              color: "#353b48"
            }
          }
        },
        plugins: {
          legend: {
            display: false,
          }
        }
      }
    });
  }
}
