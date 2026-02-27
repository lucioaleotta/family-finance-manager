# Checklist pre-demo clienti (15 minuti)

## 1) Stato servizi
- [ ] Frontend raggiungibile (`http://localhost:3000` o URL cloud)
- [ ] Backend raggiungibile (`/api` risponde)
- [ ] Database online (container/istanza attiva)

## 2) Flussi core
- [ ] Login con utente demo
- [ ] Creazione transazione
- [ ] Visualizzazione dashboard senza errori
- [ ] Navigazione pagine principali (Accounts, Transactions, Liquidity, Investments)

## 3) Password reset
- [ ] `Forgot password` accetta email senza errori
- [ ] Link reset ricevuto via email
- [ ] Reset password completato
- [ ] Login con nuova password funzionante

## 4) Qualità demo
- [ ] Dati demo realistici presenti
- [ ] Nessun secret in chiaro in schermata/log
- [ ] Console browser senza errori bloccanti
- [ ] Tempo risposta UI accettabile

## 5) Piano B
- [ ] Backup URL ambiente (staging/locale)
- [ ] Utente demo alternativo pronto
- [ ] Script avvio rapido disponibile (`docker compose -f docker-compose.local.yml up --build`)

## 6) Messaggio finale cliente
- [ ] Obiettivo demo dichiarato (cosa vedranno)
- [ ] Limiti noti comunicati chiaramente
- [ ] Prossimi step concordati (feedback + timeline)
