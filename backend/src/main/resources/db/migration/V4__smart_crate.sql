create table smart_crate (
    id          bigserial primary key,
    owner_id    bigint not null references app_user(id),
    name        text not null,
    description text,
    criteria    jsonb not null,
    created_at  timestamptz not null default now(),
    deleted_at  timestamptz
);

create unique index smart_crate_owner_name_alive
    on smart_crate (owner_id, name)
    where deleted_at is null;
