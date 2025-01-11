package br.com.fiap.lanchonete.pedido.adapters.output.persistence

import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ItemPedidoRespositoryAdapter(
    private val jdbcClient: JdbcClient
) {
    fun persist(itemPedido: ItemPedido, pedidoId: UUID) {
        val id = UUID.randomUUID()
        jdbcClient.sql(
            """
            INSERT INTO item_pedido (id, pedido_id, produto_id, nome_produto, quantidade, preco_unitario, categoria) 
            VALUES (:id, :pedido_id, :produto_id, :nome_produto, :quantidade, :preco_unitario, :categoria)
        """
        )
            .params(
                mapOf(
                    "id" to id,
                    "pedido_id" to pedidoId,
                    "produto_id" to itemPedido.produtoId,
                    "nome_produto" to itemPedido.nomeProduto,
                    "quantidade" to itemPedido.quantidade,
                    "preco_unitario" to itemPedido.precoUnitario,
                    "categoria" to itemPedido.categoria
                )
            )
            .update()

        itemPedido.id = id
    }

    fun findByPedidoId(pedidoId: UUID): List<ItemPedido> {
        return jdbcClient.sql(
            """
            SELECT * FROM item_pedido WHERE pedido_id = :pedido_id
        """
        )
            .params(
                mapOf(
                    "pedido_id" to pedidoId
                )
            )
            .query { rs, _ ->
                ItemPedido(
                    id = UUID.fromString(rs.getString("id")),
                    produtoId = UUID.fromString(rs.getString("produto_id")),
                    nomeProduto = rs.getString("nome_produto"),
                    quantidade = rs.getInt("quantidade"),
                    precoUnitario = rs.getBigDecimal("preco_unitario"),
                    categoria = rs.getString("categoria")
                )
            }.list()
    }
}