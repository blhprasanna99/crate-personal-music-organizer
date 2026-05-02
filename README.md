# Crate — Personal Music Organizer

A self-hosted music library organizer built around a **polyhierarchical taxonomy** —
tracks live in multiple "crates" simultaneously, crates can have multiple parents,
and contents resolve recursively across the graph. Files stay where they are on
disk; Crate is just the catalog and the organizing layer.

Built as a portfolio project to exercise real-world system-design problems:
graph schemas in SQL, soft-delete semantics, saved queries, and a clean
backend/frontend split.

## What it does

| Capability | How |
|---|---|
| **Catalog music files** | Point Crate at a folder; it walks recursively, reads ID3/FLAC/M4A tags via jaudiotagger, inserts new tracks, skips already-imported paths. |
| **Group tracks into crates** | Manual membership (`crate_track`). One track in many crates. |
| **Build hierarchies** | Crates can have multiple parents (DAG). Cycle detection at write time via recursive CTE. |
| **View recursively** | `?recursive=true` walks descendants and unions track membership. |
| **Tag tracks** | Free-form tags as a second classification axis, orthogonal to crates. |
| **Filter / search** | Reusable filter engine (`tag`, `artistEq`, `albumEq`, `titleContains`, `noTag`, `addedWithinDays`) combined with implicit AND. |
| **Save searches** | Smart crates persist a filter set as a named entity; resolve fresh on every read. |
| **Soft-delete** | Three modes — DETACH (remove from one crate), TRASH (hide, restorable), PURGE (hard delete). |
| **Restore** | Trashed tracks/crates can be restored — partial unique indexes prevent name/path collisions. |

## Stack

| Layer | Tech |
|---|---|
| Database | PostgreSQL 16 + pgvector (Docker) |
| Migrations | Flyway |
| Backend | Spring Boot 3.5, Java 21, Maven |
| ORM | Spring Data JPA + Hibernate; raw SQL via JdbcClient where graphs/CTEs are needed |
| Frontend | React 18, TypeScript, Vite, TanStack Query |

## Quickstart

Requirements: Docker, Java 21, Node 18+.

```bash
# 1. Postgres
docker compose up -d

# 2. Backend (port 8081)
cd backend
./mvnw spring-boot:run

# 3. Frontend (port 5173, proxies /api to 8081)
cd frontend
npm install
npm run dev
```

Open http://localhost:5173.

## API surface

All endpoints are owner-scoped to a default user (`id=1`, seeded by V1).
Auth is intentionally not built — single-user app.

### Tracks
- `GET    /api/tracks` — list (`?trashed=true` for trash)
- `GET    /api/tracks/{id}`
- `POST   /api/tracks` — manual create
- `PATCH  /api/tracks/{id}`
- `DELETE /api/tracks/{id}` — `?mode=trash` (default) or `?mode=purge`
- `POST   /api/tracks/{id}/restore`
- `POST   /api/tracks/import` — bulk import from a folder
- `POST   /api/tracks/search` — ad-hoc filter query

### Crates
- `GET    /api/crates`, `GET /{id}`, `POST`, `PATCH /{id}`, `DELETE /{id}` (trash/purge), `POST /{id}/restore`
- `GET    /api/crates/{id}/tracks` — direct membership; `?recursive=true` for descendants
- `PUT    /api/crates/{crateId}/tracks/{trackId}` — add track
- `DELETE /api/crates/{crateId}/tracks/{trackId}` — remove (DETACH)
- `PUT    /api/crates/{childId}/parents/{parentId}` — add parent edge (with cycle detection)
- `DELETE /api/crates/{childId}/parents/{parentId}` — remove edge

### Smart crates
- Standard CRUD on `/api/smart-crates`
- `GET /api/smart-crates/{id}/tracks` — runs the saved criteria fresh

### Tags
- Standard CRUD on `/api/tags`
- `GET    /api/tracks/{trackId}/tags`
- `PUT    /api/tracks/{trackId}/tags/{tagId}` — attach
- `DELETE /api/tracks/{trackId}/tags/{tagId}` — detach

### Filter ops (criteria array)

```json
{ "criteria": [
    { "op": "tag", "value": "house" },
    { "op": "addedWithinDays", "value": 30 }
] }
```

| Op | Value | Meaning |
|---|---|---|
| `tag` | string | track has this tag |
| `noTag` | (none) | track has no tags |
| `artistEq` | string | artist exact match |
| `albumEq` | string | album exact match |
| `titleContains` | string | case-insensitive substring on title |
| `addedWithinDays` | number | created in last N days |

Filters combine with implicit AND.

## Schema

Five Flyway migrations build the catalog incrementally:

| Version | What |
|---|---|
| `V1__init.sql` | `app_user`, `track`, `crate`, `crate_track`, `tag`, `track_tag`; default user seeded |
| `V2__crate_parent.sql` | DAG parent table; PK + check + index |
| `V3__soft_delete.sql` | `deleted_at` on `track` and `crate`; original unique constraints replaced with **partial unique indexes** scoped to alive rows |
| `V4__smart_crate.sql` | `smart_crate` with `criteria JSONB`; partial unique on `(owner_id, name)` |

## Design notes

A few decisions worth knowing if you're reading the code:

- **`@SQLRestriction` on entities, not `@SQLDelete`.** Trash uses a native `UPDATE` to avoid Hibernate's `@ManyToMany` cascade wiping membership rows before the soft-delete fires. Read the comment trail in `Block 6` for the full hunt.
- **Partial unique indexes** make trash-then-restore work without the unique constraint blocking a re-imported file path.
- **Cycle detection lives in the service**, not in a trigger. Recursive CTE checks ancestors of the proposed parent; rejects if the proposed child is found.
- **One filter engine, two surfaces.** `POST /api/tracks/search` and smart-crate resolution call the same `FilterEngine`. Adding a new op extends both at once.
- **JSONB criteria** mapped via Hibernate's `@JdbcTypeCode(SqlTypes.JSON)` directly to `List<Filter>` on the entity.

## Future work

- **Block 9 — semantic search.** Schema would add `embedding vector(N)` on `track`,
  with an HNSW index. Spring AI provides the embedding client (Ollama for local /
  free, OpenAI for hosted). Search endpoint embeds the query string and runs
  `ORDER BY embedding <=> ?`. ~45 min of wiring once an embedding provider is chosen.
- **Frontend polish** — smart-crate criteria builder UI, drag-drop for crate-track
  membership, hierarchy view, trash management, tag UI.
- **Auth** — currently single-user; multi-user is a Spring Security + per-request
  user lookup change. The `owner_id` plumbing is already there.

## License

See [`LICENSE`](LICENSE).
