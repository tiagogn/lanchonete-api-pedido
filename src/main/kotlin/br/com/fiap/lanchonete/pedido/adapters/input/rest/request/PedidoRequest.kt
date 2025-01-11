package br.com.fiap.lanchonete.pedido.adapters.input.rest.request

import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import java.math.BigDecimal
import java.util.UUID

data class PedidoRequest(
    val clienteId: UUID?,
    val itens: List<ItemPedidoRequest>
)

data class ItemPedidoRequest(
    val produtoId: UUID,
    val nomeProduto: String,
    val quantidade: Int,
    val precoUnitario: BigDecimal,
    val categoria: String
)

fun ItemPedidoRequest.toModel(): ItemPedido {
    return ItemPedido(
        id = null,
        produtoId = this.produtoId,
        nomeProduto = this.nomeProduto,
        quantidade = this.quantidade,
        precoUnitario = this.precoUnitario,
        categoria = this.categoria
    )
}

