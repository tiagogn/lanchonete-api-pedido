package br.com.fiap.lanchonete.pedido.adapters.output.persistence

import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.domain.Cliente
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Qualifier("clienteRepository")
class ClienteRepositoryAdapter(
    private val jdbcClient: JdbcClient
) : ClienteRepository {
    override fun save(cliente: Cliente): Cliente {
        if (cliente.id == null) {
            persist(cliente)
        } else {
            update(cliente)
        }
        return cliente
    }

    private fun persist(cliente: Cliente) {
        cliente.id = UUID.randomUUID()
        jdbcClient.sql(
            """
            INSERT INTO cliente (id, nome, email, cpf)
            VALUES (:id, :nome, :email, :cpf)
        """
        )
            .params(
                mapOf(
                    "id" to cliente.id,
                    "nome" to cliente.nome,
                    "email" to cliente.email,
                    "cpf" to cliente.cpf
                )
            )
            .update()
    }

    private fun update(cliente: Cliente) {
        jdbcClient.sql(
            """
            UPDATE cliente
            SET nome = :nome, email = :email, cpf = :cpf
            WHERE id = :id
        """
        )
            .params(
                mapOf(
                    "id" to cliente.id,
                    "nome" to cliente.nome,
                    "email" to cliente.email,
                    "cpf" to cliente.cpf
                )
            )
            .update()
    }

    override fun findByCPF(cpf: String): Optional<Cliente> {
        return jdbcClient.sql("SELECT * FROM cliente WHERE cpf = :cpf")
            .params(mapOf("cpf" to cpf))
            .query { rs, _ ->
                Cliente(
                    id = UUID.fromString(rs.getString("id")),
                    nome = rs.getString("nome"),
                    email = rs.getString("email"),
                    cpf = rs.getString("cpf")
                )
            }
            .optional()
    }

    override fun findById(id: UUID?): Optional<Cliente> {
        if (id == null)
            return Optional.empty()

        return jdbcClient.sql(
            """
            SELECT id, nome, email, cpf
            FROM cliente
            WHERE id = :id
        """
        )
            .params(
                mapOf(
                    "id" to id
                )
            )
            .query { rs, _ ->
                Cliente(
                    id = UUID.fromString(rs.getString("id")),
                    nome = rs.getString("nome"),
                    email = rs.getString("email"),
                    cpf = rs.getString("cpf")
                )
            }
            .optional()
    }

}