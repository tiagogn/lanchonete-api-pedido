package br.com.fiap.lanchonete.pedido.core.application.dto

import java.math.BigDecimal
import java.time.LocalDateTime

data class PedidoStatusOutput(
    val status: String,
    val pedidos: List<PedidoOutput>
)

data class PedidoOutput(
    val id: String,
    val codigo: Long,
    val valor: BigDecimal,
    val status: String,
    val criadoEm: LocalDateTime,
    val clienteId: String
)