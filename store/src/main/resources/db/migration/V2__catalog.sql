create sequence authors_seq;
create table if not exists authors
(
    id         bigserial primary key,
    uuid       varchar(36) not null,
    version    int8 not null,
    created_at timestamp default now() not null,
    name       varchar(100) not null
);
create unique index if not exists idx_authors_uuid on authors (uuid);
alter table authors alter column id set default nextval('authors_seq');

create sequence books_seq;
create table if not exists books
(
    id        bigserial primary key,
    uuid      varchar(36) not null,
    version   int8 not null,
    available int8 not null,
    cover_id  int8,
    price     numeric(10, 2) not null,
    title     varchar(50) not null,
    year      int4 not null
);
create unique index if not exists idx_books_uuid on books (uuid);
create index if not exists idx_books_title on books (title);
alter table books alter column id set default nextval('books_seq');

create table if not exists books_authors
(
    books_id   int8 not null,
    authors_id int8 not null,
    primary key (books_id, authors_id)
);