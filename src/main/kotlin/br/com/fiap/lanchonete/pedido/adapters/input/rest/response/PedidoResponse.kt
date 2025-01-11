package br.com.fiap.lanchonete.pedido.adapters.input.rest.response

import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PedidoResponse(
    var id: UUID?,
    val cliente: ClienteResponse?,
    val itens: List<ItemPedidoResponse>,
    val total: BigDecimal,
    val status: StatusPedido,
    val criadoEm: LocalDateTime,
    val atualizadoEm: LocalDateTime,
    val tempoEspera: String,
    val codigo: Long
)

data class ItemPedidoResponse(
    var id: UUID?,
    val nomeProduto: String,
    val quantidade: Int,
    val precoUnitario: BigDecimal
)

data class PedidoStatusResponse(
    val codigo: Long,
    val status: StatusPedido,
    val pagamento: String
)

fun Pedido.toResponse(): PedidoResponse {
    return PedidoResponse(
        id = this.id,
        cliente = this.cliente?.toResponse(),
        itens = this.itens.map { it.toResponse() },
        total = this.total,
        status = this.status,
        criadoEm = this.criadoEm,
        atualizadoEm = this.atualizadoEm,
        tempoEspera = this.tempoEspera(),
        codigo = this.codigo!!
    )
}

fun ItemPedido.toResponse(): ItemPedidoResponse {
    return ItemPedidoResponse(
        id = this.id,
        nomeProduto = this.nomeProduto,
        quantidade = this.quantidade,
        precoUnitario = this.precoUnitario
    )
}

data class AtualizacaoPedidoStatusResponse(
    val pedidoId: String,
    val statusPedido: StatusPedido,
    val mensagem: String
)