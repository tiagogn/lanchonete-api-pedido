package br.com.fiap.lanchonete.pedido.adapters.input.rest.request

import java.util.UUID

data class PedidoPagamentoRequest(
    val pedidoId: UUID,
    val valor: Double,
    val formaPagamento: String,
    val status: String,
    val pagamentoId: UUID,
    val dataPagamento: String,
    val mensagem: String
){
    fun toDataPagamentoAsDate() = java.time.LocalDateTime.parse(dataPagamento)
}