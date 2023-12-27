create sequence users_seq;
create table if not exists users
(
    id         bigserial primary key,
    uuid       varchar(36) not null,
    version    int8 not null,
    created_at timestamp not null,
    password   varchar(200) not null,
    updated_at timestamp,
    email      varchar(200) not null,
    role       varchar(10) not null
);
create unique index if not exists idx_users_email on users (email);
create unique index if not exists idx_users_uuid on users (uuid);
alter table users alter column id set default nextval('users_seq');
