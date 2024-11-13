# 🃏OnlyCards🃏
La traccia è disponibile qua: [Traccia Progetto Enterprise Applications](https://github.com/matte18it/OnlyCards/blob/main/EA%20-%20Progetto%20formativo%202023_2024.pdf)

L'app Android e il sito web per la compravendita di prodotti offrono un marketplace completo in cui gli utenti possono acquistare e vendere vari tipi di prodotti (carte, buste di espansione, ecc.). La piattaforma permette di esplorare i prodotti disponibili, visualizzarne i dettagli e procedere direttamente all’acquisto. I tipi di utenti disponibili sulla piattaforma sono i seguenti:
<ul>
  <li>
    <strong>Admin</strong>: ha accesso a un pannello di amministrazione per la gestione completa della piattaforma. Può aggiungere, modificare e rimuovere prodotti, monitorare e modificare ordini, gestire i dati degli utenti e risolvere eventuali problemi.
  </li>
  <li>
    <strong>Utente Registrato (Acquirente e Venditore)</strong>: ogni utente registrato assume automaticamente il doppio ruolo di acquirente e venditore, senza la necessità di cambiare profilo. Questo permette agli utenti di acquistare prodotti messi in vendita da altri e, contemporaneamente, di vendere i propri sul marketplace.
  </li>
</ul>

Le funzionalità principali della piattaforma includono:
<ul>
  <li>
    <strong>Wishlist Pubbliche e Private</strong>: gli utenti possono creare wishlist di prodotti desiderati, impostandole come pubbliche o private. Ogni wishlist può essere condivisa tramite link, consentendo anche agli utenti non registrati di visualizzarle, e può essere condivisa con un singolo utente o con un gruppo di utenti.
  <li>
    <strong>Carrello e Checkout</strong>: gli utenti possono aggiungere prodotti al carrello e completare il processo di checkout con pagamenti sicuri.
  </li>
  <li>
    <strong>Gestione degli Ordini</strong>: ogni utente può monitorare lo stato dei propri ordini di acquisto e, come venditore, visualizzare gli ordini di vendita. L’admin ha accesso completo a tutti gli ordini per interventi o verifiche necessarie.
  </li>
</ul>

L'app Android è progettata in Kotlin con Jetpack Compose, offrendo un'interfaccia moderna e reattiva. Il sito web, sviluppato con Angular, è progettato per adattarsi a diversi dispositivi, consentendo agli utenti di navigare, acquistare e gestire i propri prodotti in modo semplice e intuitivo. Il backend è basato su un sistema di autenticazione e gestione dati realizzato con Spring Boot, garantendo un'esperienza utente fluida e sicura.

# 📼 Demo Video 📼
Guarda la demo del sito web di OnlyCards per scoprire come gli utenti possono esplorare il marketplace, cercare prodotti, aggiungerli alla wishlist e completare gli acquisti. Il sito è ottimizzato per offrire un’esperienza intuitiva su vari dispositivi, permettendo una navigazione fluida tra le funzionalità della piattaforma.
<div align="center">
  <a href="https://www.youtube.com/watch?v=eSI8P3kFCMo">
    <img src="https://img.youtube.com/vi/eSI8P3kFCMo/0.jpg" alt="Guarda il video su YouTube" width="600">
  </a>
</div>

<br>
Guarda la demo dell'app Android di OnlyCards, progettata in Kotlin con Jetpack Compose. L'app offre un'interfaccia moderna e reattiva che consente agli utenti di navigare nel marketplace, aggiungere prodotti al carrello, gestire wishlist e seguire il processo di checkout in modo semplice e veloce, tutto dal proprio dispositivo mobile.
<div align="center">
  <a href="https://youtu.be/umu2lZ5qsTo">
    <img src="https://img.youtube.com/vi/umu2lZ5qsTo/0.jpg" alt="Guarda il video su YouTube" width="600">
  </a>
</div>

# 💾 Database 💾
![Modello ER](https://github.com/matte18it/OnlyCards/blob/main/ModelloER.png)
Il database è configurato in un container Docker. Per creare il container, basta scaricare tutti i file presenti nella cartella "Database", posizionarli in una cartella, aprire un terminale all'interno di quella cartella ed eseguire il seguente comando: <strong>`docker compose up --build -d`</strong>. Il database sarà quindi popolato e pronto all'uso!
