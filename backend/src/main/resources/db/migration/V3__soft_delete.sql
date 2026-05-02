alter table track add column deleted_at timestamptz;
alter table crate add column deleted_at timestamptz;

alter table track drop constraint track_owner_id_file_path_key;
create unique index track_owner_path_alive
    on track (owner_id, file_path)
    where deleted_at is null;

alter table crate drop constraint crate_owner_id_name_key;
create unique index crate_owner_name_alive
    on crate (owner_id, name)
    where deleted_at is null;
