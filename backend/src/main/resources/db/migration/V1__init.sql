create table app_user (
    id           bigserial primary key,
    email        text not null unique,
    display_name text not null,
    created_at   timestamptz not null default now()
);

create table track (
    id          bigserial primary key,
    owner_id    bigint not null references app_user(id),
    file_path   text not null,
    title       text,
    artist      text,
    album       text,
    duration_ms integer,
    created_at  timestamptz not null default now(),
    unique (owner_id, file_path)
);
create index idx_track_owner on track(owner_id);

create table crate (
    id          bigserial primary key,
    owner_id    bigint not null references app_user(id),
    name        text not null,
    description text,
    created_at  timestamptz not null default now(),
    unique (owner_id, name)
);
create index idx_crate_owner on crate(owner_id);

create table crate_track (
    crate_id bigint not null references crate(id) on delete cascade,
    track_id bigint not null references track(id) on delete cascade,
    position integer,
    added_at timestamptz not null default now(),
    primary key (crate_id, track_id)
);
create index idx_crate_track_track on crate_track(track_id);

create table tag (
    id         bigserial primary key,
    owner_id   bigint not null references app_user(id),
    name       text not null,
    created_at timestamptz not null default now(),
    unique (owner_id, name)
);
create index idx_tag_owner on tag(owner_id);

create table track_tag (
    track_id bigint not null references track(id) on delete cascade,
    tag_id   bigint not null references tag(id) on delete cascade,
    primary key (track_id, tag_id)
);
create index idx_track_tag_tag on track_tag(tag_id);

insert into app_user (email, display_name)
values ('me@local', 'me');
