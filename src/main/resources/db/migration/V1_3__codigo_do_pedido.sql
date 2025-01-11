alter table pedido
    add column codigo integer not null;

create sequence pedido_codigo_seq start 100 increment 1;

alter table pedido add column pagamento_payload jsonb;