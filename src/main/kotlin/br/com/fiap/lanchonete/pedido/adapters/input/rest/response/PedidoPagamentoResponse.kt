package br.com.fiap.lanchonete.pedido.adapters.input.rest.response

data class PedidoPagamentoResponse (
    val pedidoId: String,
    val status: String,
    val mensagem: String
)