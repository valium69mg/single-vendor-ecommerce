# Skill: Add a Database Migration

## Where migrations live
`src/main/resources/db/migration/`

## Naming convention
`V<n>__<short_description>.sql`

- `<n>` is the next integer after the current high-water mark (currently **V28**, so next is **V29**)
- `<short_description>` uses underscores, lowercase, describes the change
- Examples: `V29__add_verification_codes_table.sql`, `V29__add_orders_table.sql`

## Steps to add a migration

1. Check the current highest version:
   ```bash
   ls src/main/resources/db/migration/ | sort -V | tail -5
   ```

2. Create the new file:
   ```
   src/main/resources/db/migration/V<next>__<description>.sql
   ```

3. Write the SQL. Keep these rules:
   - Use `IF NOT EXISTS` / `IF EXISTS` guards where supported to make scripts re-runnable in dev resets
   - Add FK constraints with named `CONSTRAINT fk_...` clauses for clarity
   - For UUID columns: `DEFAULT uuid_generate_v4()` (the extension is enabled via earlier migration)
   - For timestamps: `DEFAULT now()` is fine; Hibernate `@CreationTimestamp` / `@UpdateTimestamp` manages these at the app layer
   - For soft-delete columns: `deleted_at TIMESTAMP NULL`

4. If the migration adds a new column to an existing entity, update the JPA entity class too.
   - Column mapping: `@Column(name = "column_name")`
   - Nullable columns: do not add `nullable = false` unless the DB column is also NOT NULL

5. Migrations run automatically on startup — no manual command needed.

## Critical rules
- **NEVER modify an existing migration file.** Flyway checksums them. Any edit breaks all existing deployments.
- If you made a mistake in the last migration and it has NOT been applied yet (dev only), you may delete and recreate it. If it has been applied, write a corrective V<n+1> migration.
- Do not use `DROP TABLE` or `DROP COLUMN` unless explicitly asked. Prefer `ALTER TABLE ... ADD COLUMN` or adding constraints.

## Before you commit
- [ ] Filename follows `V<n>__description.sql` convention (correct number, underscores, no spaces)
- [ ] No existing migration file was modified
- [ ] If a new entity column was added, the JPA entity was updated
- [ ] Run `./mvnw clean package -DskipTests` to verify the app starts and Flyway applies without errors
- [ ] Update `.claude/CODEBASE.md` data model section if schema changed significantly
