create sequence uploads_seq;
create table if not exists uploads
(
    id           bigserial primary key,
    uuid         varchar(36) not null,
    version      int8 not null,
    content_type varchar(50) not null,
    created_at   timestamp default now() not null,
    file         bytea not null,
    filename     varchar(255) not null
);
create unique index if not exists idx_uploads_uuid on uploads (uuid);
alter table uploads alter column id set default nextval('uploads_seq');