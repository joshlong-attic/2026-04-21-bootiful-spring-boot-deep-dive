create table if not exists dogs
(
    id   serial primary key,
    name text not null
);
delete from dogs;
insert  into dogs (id, name) values (1, 'Rex');
insert  into dogs (id, name) values (2, 'Peanut');
insert  into dogs (id, name) values (3, 'Prancer');