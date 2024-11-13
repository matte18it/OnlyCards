# Usa l'immagine ufficiale di MariaDB come base
FROM mariadb:latest

# Imposta le variabili d'ambiente per il database
ENV MYSQL_ROOT_PASSWORD=Only.Cards24
ENV MYSQL_DATABASE=OnlyCards

# Copia il file onlycards.sql per l'inizializzazione del database
COPY ./onlycards.sql /docker-entrypoint-initdb.d/

# Copia il file di configurazione personalizzato
COPY ./my.cnf /etc/mysql/my.cnf

# Espone la porta 3306 per la connessione al database
EXPOSE 3306

# Comando di default per eseguire MariaDB
CMD ["mariadbd"]