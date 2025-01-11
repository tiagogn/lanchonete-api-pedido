CREATE TABLE IF NOT EXISTS pedido (
    id UUID PRIMARY KEY,
    cliente_id UUID,
    total DECIMAL(19, 2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    criado_em TIMESTAMP NOT NULL,
    atualizado_em TIMESTAMP NOT NULL,
    pronto_em TIMESTAMP,
    finalizado_em TIMESTAMP,
    CONSTRAINT fk_cliente
        FOREIGN KEY (cliente_id)
        REFERENCES cliente(id)
);
