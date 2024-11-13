// File che contiene variabili globali che possono essere utilizzate in tutto il progetto

export const environment = {
  // Variabile che contiene la key del captcha
  recaptcha: {
    siteKey: '6LfnIrkpAAAAACeAt87wyEK8234o_y1mEBr24z6u'
  },
  giochi: ["Pokémon", "Yu-Gi-Oh!", "Magic"],
  giochiUrl: ["pokemon", "yugioh", "magic"],
  backendUrl: "http://localhost:8080/api", backendBaseUrl: "http://localhost:8080",

  getSizeParameter(): number {
    const width = window.innerWidth || document.documentElement.clientWidth || document.body.clientWidth;

    // Modifica la size in base alle dimensioni della finestra
    if (width <= 576) {
      return 6;
    } else {
      return 12;
    }},
   formatDate(date:Date) {

     // Estrai il giorno, il mese e l'anno dalla data
     const day = String(date.getDate()).padStart(2, '0');
     const month = String(date.getMonth() + 1).padStart(2, '0'); // I mesi in JavaScript vanno da 0 a 11
     const year = date.getFullYear();
     // Estrai l'ora e i minuti dalla data
     const hours = String(date.getHours()).padStart(2, '0');
     const minutes = String(date.getMinutes()).padStart(2, '0');

     // Formatta la data come "dd/mm/yyyy hh:mm"
     return `${day}/${month}/${year} ${hours}:${minutes}`;
   },

  getCssClassMap(): Map<string, string> {
    return new Map([
      ['EN', 'fi fi-gb-eng'],
      ['IT', 'fi fi-it'],
      // aggiungere altre mappature CSS qui
    ]);
  },
  getLanguageNames(): Map<string, string> {
    return new Map([
      ['EN', 'Inglese'],
      ['IT', 'Italiano'],
      // aggiungere altre lingue qui
    ]);
  },
  getFeaturesNames(): Map<string, string> {
    return new Map([
      ['set', 'Set di appartenenza'],
      ['set card number', 'Numero Carta'],
      ['rarity', 'Rarità'],
      ['category', 'Tipo'],
      ['category 2', 'Tipo'],
      ['mana cost', 'Costo Mana'],
      ['level', 'Livello'],
      ['description', 'Descrizione'],
      // aggiungere altri nomi di feature qui
    ]);
  }
}

