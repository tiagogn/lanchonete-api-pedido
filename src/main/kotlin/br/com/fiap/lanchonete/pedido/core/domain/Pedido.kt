package br.com.fiap.lanchonete.pedido.core.domain

import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import java.math.BigDecimal
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

data class Pedido(

    var id: UUID? = null,

    val cliente: Cliente?,

    val itens: List<ItemPedido>,

    val total: BigDecimal = BigDecimal.ZERO,

    var status: StatusPedido = StatusPedido.RECEBIDO,

    val criadoEm: LocalDateTime = LocalDateTime.now(),

    var atualizadoEm: LocalDateTime = LocalDateTime.now(),

    var prontoEm: LocalDateTime? = null,

    var finalizadoEm: LocalDateTime? = null,

    var codigo: Long? = null,

    var pagamento: Pagamento = Pagamento.createPagamentoPendente()

) {
    fun pedidoEmPreparacao() {

        if (pagamento.status != StatusPagamento.APROVADO)
            throw PedidoException("Pedido não pode ser preparado, pois o pagamento não foi aprovado")

        if (status != StatusPedido.RECEBIDO)
            throw PedidoException("Pedido não pode ser preparado, pois já está em preparação")

        status = StatusPedido.EM_PREPARACAO
        atualizadoEm = LocalDateTime.now()
    }

    fun pedidoPronto() {

        if (status != StatusPedido.EM_PREPARACAO)
            throw PedidoException("Pedido não pode ser marcado como pronto, pois ainda não está em preparação")

        status = StatusPedido.PRONTO
        atualizadoEm = LocalDateTime.now()
        prontoEm = LocalDateTime.now()
    }

    fun pedidoFinalizado() {

        if (status != StatusPedido.PRONTO)
            throw PedidoException("Pedido não pode ser finalizado, pois ainda não está pronto")

        status = StatusPedido.FINALIZADO
        atualizadoEm = LocalDateTime.now()
        finalizadoEm = LocalDateTime.now()
    }

    fun tempoEspera(): String {
        return if (status == StatusPedido.PRONTO) {
            val tempoEspera = Duration.between(criadoEm, prontoEm)
            "${tempoEspera.toMinutes()} minutos"
        } else {
            "0 minutos"
        }
    }

}

enum class StatusPedido(
    val value: Int
) {
    RECEBIDO(1), EM_PREPARACAO(2), PRONTO(3), FINALIZADO(4);
}
