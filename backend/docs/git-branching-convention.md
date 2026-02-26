# Git Branching Convention

## Branch principali
- `main`: branch stabile e deployabile.
- `sviluppo/evolutiva`: branch di integrazione per nuove evolutive.

## Branch di lavoro consigliati
Creare i branch di lavoro partendo da `sviluppo/evolutiva`.

- `feature/<nome-breve>` per nuove funzionalità.
- `fix/<nome-breve>` per correzioni non urgenti.
- `hotfix/<nome-breve>` per fix urgenti da rilasciare velocemente.

Esempi:
- `feature/accounts-export-csv`
- `fix/dashboard-totals-rounding`
- `hotfix/login-redirect-loop`

## Flusso operativo
1. Aggiorna il branch base:
   - `git checkout sviluppo/evolutiva`
   - `git pull`
2. Crea il branch di lavoro:
   - `git checkout -b feature/<nome-breve>`
3. Sviluppa, committa e pubblica:
   - `git push -u origin feature/<nome-breve>`
4. Apri PR verso `sviluppo/evolutiva`.
5. Quando le evolutive sono stabili, apri PR da `sviluppo/evolutiva` verso `main`.

## Regole pratiche
- Branch naming in minuscolo e con trattini (`kebab-case`).
- Evita branch troppo lunghi nel tempo: integra spesso su `sviluppo/evolutiva`.
- Prima della PR esegui la checklist: [git-pre-push-checklist.md](git-pre-push-checklist.md).