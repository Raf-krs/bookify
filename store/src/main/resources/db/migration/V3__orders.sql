create sequence recipients_seq;
create table if not exists recipients
(
    id       bigserial primary key,
    uuid     varchar(36),
    version  int8 not null,
    city     varchar(100),
    email    varchar(200),
    name     varchar(100),
    phone    varchar(10),
    street   varchar(100),
    zip_code varchar(10)
);
create unique index if not exists idx_recipients_uuid on recipients (uuid);
create unique index if not exists recipients_uuid_email on recipients (email);
alter table recipients alter column id set default nextval('recipients_seq');

create sequence orders_seq;
create table if not exists orders
(
    id           bigserial primary key,
    uuid         varchar(36) not null,
    version      int8 not null,
    created_at   timestamp default now() not null,
    delivery     varchar(20) not null,
    status       varchar(20) not null,
    updated_at   timestamp,
    recipient_id int8 not null
);
create unique index if not exists idx_orders_uuid on orders (uuid);
alter table orders add constraint orders_recipients_id_fk foreign key (recipient_id) references recipients;
alter table orders alter column id set default nextval('orders_seq');

create sequence order_items_seq;
create table if not exists order_items
(
    id       bigserial primary key,
    uuid     varchar(36) not null,
    version  int8 not null,
    quantity int4 not null,
    book_id  int8,
    order_id int8
);
create unique index if not exists idx_order_items_uuid on order_items (uuid);
alter table order_items add constraint fk_order_items_books foreign key (book_id) references books;
alter table order_items add constraint fk_order_items_orders foreign key (order_id) references orders on delete cascade;
alter table order_items alter column id set default nextval('order_items_seq');