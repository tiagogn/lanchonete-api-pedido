package br.com.fiap.lanchonete.pedido.adapters.output.persistence

import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.PedidoRepository
import br.com.fiap.lanchonete.pedido.core.domain.Pagamento
import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.util.*

@Repository
@Qualifier("pedidoRepository")
class PedidoRepositoryAdapter(
    private val jdbcClient: JdbcClient,
    private val itemPedidoRespositoryAdapter: ItemPedidoRespositoryAdapter,
    private val clienteRepositoryAdapter: ClienteRepositoryAdapter,
    private val objectMapper: ObjectMapper
) : PedidoRepository {
    override fun save(pedido: Pedido): Unit {
        if (pedido.id == null) {
            pedido.id = UUID.randomUUID()
            persist(pedido)
        } else {
            update(pedido)
        }
    }

    override fun findById(id: UUID): Optional<Pedido> {
        return jdbcClient.sql(
            """
            SELECT * FROM pedido WHERE id = :id
        """
        )
            .params(
                mapOf(
                    "id" to id
                )
            )
            .query { rs, _ -> mapRowPedido(rs) }
            .optional()
    }

    override fun findByStatus(statusPedido: StatusPedido): List<Pedido> {
        return jdbcClient.sql(
            """
            SELECT * FROM pedido WHERE status = :status
        """
        )
            .params(
                mapOf(
                    "status" to statusPedido.name
                )
            )
            .query { rs, _ -> mapRowPedido(rs) }
            .list()
    }

    private fun persist(pedido: Pedido): Pedido {
        pedido.codigo = getCodigoPedido()
        jdbcClient.sql(
            """
            INSERT INTO pedido (id, cliente_id, total, status, criado_em, atualizado_em, pronto_em, finalizado_em, codigo, pagamento_payload) 
            VALUES (:id, :cliente_id, :total, :status, :criado_em, :atualizado_em, :pronto_em, :finalizado_em, :codigo, :pagamento_payload::jsonb)
        """
        )
            .params(mapParams(pedido))
            .update()

        val persistItemPedido = ItemPedidoRespositoryAdapter(jdbcClient)::persist
        pedido.itens.forEach { persistItemPedido(it, pedido.id!!) }
        return pedido
    }

    private fun getCodigoPedido(): Long {
        return jdbcClient.sql(
            """
            SELECT nextval('pedido_codigo_seq')
        """
        )
            .query { rs, _ -> rs.getLong(1) }
            .single()
    }

    private fun update(pedido: Pedido): Pedido {
        jdbcClient.sql(
            """
            UPDATE pedido 
            SET cliente_id = :cliente_id, total = :total, status = :status, 
                criado_em = :criado_em, atualizado_em = :atualizado_em, 
                pronto_em = :pronto_em, finalizado_em = :finalizado_em,
                pagamento_payload = :pagamento_payload::jsonb
            WHERE id = :id
        """
        )
            .params(mapParams(pedido))
            .update()
        return pedido
    }

    private fun mapParams(pedido: Pedido): Map<String, Any?> {
        return mapOf(
            "id" to pedido.id,
            "cliente_id" to pedido.cliente?.id,
            "total" to pedido.total,
            "status" to pedido.status.name,
            "criado_em" to pedido.criadoEm,
            "atualizado_em" to pedido.atualizadoEm,
            "pronto_em" to pedido.prontoEm,
            "finalizado_em" to pedido.finalizadoEm,
            "codigo" to pedido.codigo,
            "pagamento_payload" to objectMapper.writeValueAsString(pedido.pagamento)
        )
    }

    private fun mapRowPedido(
        rs: java.sql.ResultSet
    ): Pedido {
        val pedidoId = UUID.fromString(rs.getString("id"))
        return Pedido(
            id = pedidoId,
            cliente = clienteRepositoryAdapter.findById(getClienteId(rs)).orElse(null),
            itens = itemPedidoRespositoryAdapter.findByPedidoId(pedidoId),
            total = rs.getBigDecimal("total"),
            status = StatusPedido.valueOf(rs.getString("status")),
            criadoEm = rs.getTimestamp("criado_em").toLocalDateTime(),
            atualizadoEm = rs.getTimestamp("atualizado_em").toLocalDateTime(),
            prontoEm = rs.getTimestamp("pronto_em")?.toLocalDateTime(),
            finalizadoEm = rs.getTimestamp("finalizado_em")?.toLocalDateTime(),
            codigo = rs.getLong("codigo"),
            pagamento = objectMapper.readValue(rs.getString("pagamento_payload").let { it ?: "{}" }, Pagamento::class.java)
        )
    }

    private fun getClienteId(rs: ResultSet): UUID? {
        return rs.getString("cliente_id")?.let { UUID.fromString(it) }
    }

    private fun getPagamentoId(rs: java.sql.ResultSet): UUID? {
        return rs.getString("pagamento_id")?.let { UUID.fromString(it) }
    }

    override fun findAllByOrderByStatusNotIn(statusPedido: StatusPedido): List<Pedido> {
        return jdbcClient.sql(
            """
            SELECT * FROM pedido p where p.status not in(:statusPedido) ORDER BY p.status, p.criado_em
        """
        )
            .params(
                mapOf(
                    "statusPedido" to statusPedido.name
                )
            )
            .query { rs, _ -> mapRowPedido(rs) }
            .list()
    }

}