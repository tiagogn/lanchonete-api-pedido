package br.com.fiap.lanchonete.pedido.core.domain

import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

data class Pagamento (

    var pagamentoId: UUID? = null,

    val valorPago: BigDecimal,

    var status: StatusPagamento,

    val formaPagamento: String,

    var dataPagamento: LocalDateTime? = null,

    var mensagem: String? = null,
){
    fun pagamentoAprovado(pedido: Pedido) {
        pedido.pagamento = this
        pedido.pedidoEmPreparacao()
    }

    fun pagamentoRecusado(pedido: Pedido) {
        status = StatusPagamento.RECUSADO
    }

    fun confirmarPagamento(pedido: Pedido) {
        if (pedido.pagamento.status == StatusPagamento.APROVADO)
            throw PedidoException("Pagamento do pedido ${pedido.codigo} já aprovado")

        if (valorPago.compareTo(pedido.total) == 0 && status == StatusPagamento.APROVADO)
            pagamentoAprovado(pedido)
        else
            pagamentoRecusado(pedido)
    }

    companion object {
        fun createPagamentoPendente(): Pagamento {
            return Pagamento(
                valorPago = 0.toBigDecimal(),
                status = StatusPagamento.PENDENTE,
                formaPagamento = ""
            )
        }
    }
}

enum class StatusPagamento {
    PENDENTE, APROVADO, RECUSADO
}
