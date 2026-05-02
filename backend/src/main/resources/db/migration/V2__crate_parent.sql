create table crate_parent (
    child_id  bigint not null references crate(id) on delete cascade,
    parent_id bigint not null references crate(id) on delete cascade,
    primary key (child_id, parent_id),
    check (child_id <> parent_id)
);

create index idx_crate_parent_parent on crate_parent(parent_id);
