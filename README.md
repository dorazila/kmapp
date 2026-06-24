# Kimia WebApp

Prima migrazione web della applicazione JavaFX Kimia verso Spring Boot + Thymeleaf.

## Cosa contiene

- Spring Boot 3.3.x
- Thymeleaf
- SQLite JDBC
- Database `kimia_data_normalizzato.db` incluso in `src/main/resources/data/`
- Model Java originali riutilizzati
- `HtmlBuilder` originale riutilizzato per anteprima offerta
- Pagine web iniziali:
  - `/` dashboard
  - `/catalogo` catalogo prodotti
  - `/carrello` carrello
  - `/offerta` generazione offerta
  - `/offerta/storico` storico offerte
  - `/ap` analisi prezzi
  - `/ap/storico` storico analisi

## Avvio

Serve Java 25+ e Maven.

```bash
mvn spring-boot:run
```

Poi aprire:

```text
http://localhost:8080
```

## Database

Al primo avvio l'app copia il database incluso in:

```text
./data/kimia_data_normalizzato.db
```

Per usare un database esterno:

```bash
mvn spring-boot:run -Dkimia.db.path=/percorso/kimia_data_normalizzato.db
```

oppure impostare la variabile ambiente:

```bash
KIMIA_DB_PATH=/percorso/kimia_data_normalizzato.db
```

## Stato del progetto

Questa è una base funzionante di migrazione, non ancora una sostituzione completa della JavaFX.

Già migrato:

- catalogo prodotti
- carrello in sessione web
- generazione anteprima offerta HTML
- salvataggio storico offerte
- consultazione analisi prezzi
- storico analisi prezzi

Da completare in una seconda iterazione:

- filtri per sistemi di lavoro nel catalogo
- modifica web avanzata delle analisi prezzi con salvataggio snapshot
- ricarica offerta salvata nel carrello
- esportazione PDF server-side
- autenticazione utenti
- sincronizzazione MySQL aziendale
