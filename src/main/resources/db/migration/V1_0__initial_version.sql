create table cliente(
    id uuid primary key,
    nome text not null,
    email text not null,
    cpf varchar(11) not null
);

alter table cliente add constraint unique_cpf unique(cpf);

insert into cliente(id, nome, email, cpf) values('b74ac07d-0e7b-4b39-b046-3045547878b0', 'João Gomes', 'joao.gomes@email.com.br', '12345678901');