# Git pre-push checklist

Checklist rapida per evitare push con file generati o indesiderati.

## 1) Verifica stato branch e working tree
```bash
git status -sb
```
- Controlla branch corrente.
- Assicurati di sapere cosa è modificato/staged.

## 2) Controlla cosa finirà davvero nel commit
```bash
git diff --cached --name-only
```
- Deve contenere solo file sorgente/documentazione attesi.
- Se trovi file non voluti: `git restore --staged <file>`.

## 3) Escludi build artifacts e file locali
Conferma in `.gitignore` la presenza di regole tipo:
- `backend/target/`
- `*.class`
- `*.log`
- `.env`

Se hai già tracciato in passato file da ignorare:
```bash
git rm -r --cached backend/target
```

## 4) Fai un controllo veloce del diff
```bash
git diff --stat
git diff --cached --stat
```
- Verifica che il volume delle modifiche sia coerente.

## 5) Commit pulito e messaggio chiaro
```bash
git commit -m "<tipo>: <descrizione breve>"
```
Esempi tipo: `feat`, `fix`, `chore`, `refactor`, `docs`, `test`.

## 6) Push e verifica finale
```bash
git push
git status -sb
```
- Dopo il push, il repo dovrebbe risultare pulito.

---

## Comandi “salvataggio” utili

Rimuovere dallo staging tutto quello che hai aggiunto per errore:
```bash
git restore --staged .
```

Scartare modifiche locali a file generati:
```bash
git restore backend/target
```

Vedere solo file tracciati dentro target (se sospetti tracking accidentale):
```bash
git ls-files backend/target
```
