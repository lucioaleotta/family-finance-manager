# Standard Logging Applicativo (Backend)

## Obiettivo
Definire uno standard unico di logging per:
- tracciabilità end-to-end delle request;
- diagnosi rapida incident;
- consistenza tra locale e produzione.

## Principi
1. **Correlazione**: ogni request HTTP deve avere `correlationId` (header `X-Correlation-Id` o generato server-side).
2. **Struttura**: pattern log uniforme con timestamp, livello, thread, logger e `cid`.
3. **Livelli coerenti**:
   - `ERROR`: errore che impatta funzionalità;
   - `WARN`: anomalia non bloccante;
   - `INFO`: eventi business/operativi rilevanti;
   - `DEBUG`: dettagli diagnostici (solo in sviluppo o mirato).
4. **No dati sensibili**: vietato loggare password, token completi, segreti, PII non necessaria.

## Pattern standard
Pattern logback adottato:

```text
%d{yyyy-MM-dd'T'HH:mm:ss.SSSXXX} %-5level [%thread] %logger{36} [cid:%X{correlationId}] - %msg%n
```

## Correlation ID
- Header in ingresso: `X-Correlation-Id`
- Se assente: generato UUID lato backend.
- Il valore viene:
  - aggiunto in MDC (`correlationId`),
  - ritornato in risposta (`X-Correlation-Id`),
  - rimosso a fine richiesta.

## HTTP request logging
Per ogni request si registra un evento `INFO` con:
- metodo HTTP
- path
- status code
- durata in ms
- correlation id

## Configurazione file
- `backend/src/main/resources/logback-spring.xml`
- proprietà `logging.level.*` in `application.yml` e override per ambienti.

## Linee guida per gli sviluppatori
- Usare sempre logger SLF4J (`LoggerFactory.getLogger(...)`).
- Messaggi in forma action-oriented (es. "Password reset email sent to ...").
- Inserire `WARN` quando si gestisce fallback o comportamento degradato.
- Evitare stacktrace ridondanti su errori già gestiti altrove.

## Esempi
- ✅ `INFO`: "HTTP GET /api/accounts -> 200 in 12ms"
- ✅ `WARN`: "Unable to send password reset email ... fallback to logged link"
- ❌ `INFO`: dump completo JWT / password / secret
