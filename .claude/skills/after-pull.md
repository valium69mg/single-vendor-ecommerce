# Skill: After git pull

Run this after any `git pull` that brings in changes from teammates.

## Step 1 — See what changed
```bash
git diff HEAD@{1} HEAD --name-only
```

## Step 2 — Map changed files to intelligence files

| Changed file pattern | Intelligence file to check |
|---|---|
| `src/main/resources/db/migration/V*.sql` | `.claude/CODEBASE.md` (data model, migration high-water mark) |
| `src/main/java/**/entity/**` | `.claude/CODEBASE.md` (data model), `memory/architecture.md` |
| `src/main/java/**/controller/**` | `.claude/CODEBASE.md` (routes table) |
| `src/main/java/**/service/**` | `.claude/patterns/service-example.md` (if a new pattern emerged) |
| `src/main/resources/messages*.properties` | No intelligence update needed — just be aware of new keys |
| `CLAUDE.md` | Re-read it — a convention or rule may have changed |
| `pom.xml` | Check if a new dependency was added; update `CLAUDE.md` tech stack if major |
| `docker-compose.yml` or `.env.example` | Check for new env vars; update `.claude/CODEBASE.md` env section |

## Step 3 — Specific checks by category

### New migration (V<n>__*.sql added)
- Confirm the high-water mark in `.claude/CODEBASE.md` matches the new highest `V<n>`
- If a new table was added: update the data model section in `CODEBASE.md`
- If a column was added to an existing entity: check that the JPA entity was also updated

### New entity or entity field
- Update `CODEBASE.md` data model table
- If it's a soft-delete entity: note `deleted_at` in the table

### New controller or route
- Add it to the routes table in `CODEBASE.md`

### Architecture change (new integration, new pattern)
- Update `memory/architecture.md` with the decision and WHY

### Something that surprised you or caused a bug
- Add it to `memory/gotchas.md` immediately

## Step 4 — Run tests to confirm nothing is broken
```bash
./mvnw test
```

## Rule
A stale intelligence file is worse than no intelligence file — Claude will confidently work from wrong context. When in doubt, update the file rather than leaving it stale.
