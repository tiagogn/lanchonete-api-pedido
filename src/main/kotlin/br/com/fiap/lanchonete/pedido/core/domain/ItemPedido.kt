package br.com.fiap.lanchonete.pedido.core.domain

import java.math.BigDecimal
import java.util.UUID

data class ItemPedido(

    var id: UUID? = null,

    var produtoId: UUID,

    val nomeProduto: String,

    val quantidade: Int,

    val precoUnitario: BigDecimal,

    val categoria: String
) {

    fun getSubTotal(): BigDecimal {
        return precoUnitario * quantidade.toBigDecimal()
    }
}
