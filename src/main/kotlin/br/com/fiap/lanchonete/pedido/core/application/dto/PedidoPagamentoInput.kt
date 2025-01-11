package br.com.fiap.lanchonete.pedido.core.application.dto

import br.com.fiap.lanchonete.pedido.core.domain.StatusPagamento
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class PedidoPagamentoInput(
    val pedidoId: UUID,

    var pagamentoId: UUID? = null,

    val valorPago: BigDecimal,

    var status: StatusPagamento,

    val formaPagamento: String,

    var dataPagamento: LocalDateTime? = null,

    var mensagem: String? = null,
)
