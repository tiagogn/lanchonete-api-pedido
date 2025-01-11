package br.com.fiap.lanchonete.pedido.adapters.input.rest.response

import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoStatusOutput
import java.math.BigDecimal
import java.time.LocalDateTime

class PedidosAgrupadosResponseMapper {
    companion object {
        fun toResponse(pedidoStatusOutput: List<PedidoStatusOutput>): List<PedidosAgrupadosResponse> {
            return pedidoStatusOutput.map {
                PedidosAgrupadosResponse(
                    status = it.status,
                    pedidos = it.pedidos.map { pedido ->
                        PedidoAgrupadoResponse(
                            id = pedido.id,
                            codigo = pedido.codigo,
                            valor = pedido.valor,
                            status = pedido.status,
                            criadoEm = pedido.criadoEm,
                            clienteId = pedido.clienteId
                        )
                    }.toList()
                )
            }
        }
    }
}

data class PedidosAgrupadosResponse(
    val status: String,
    val pedidos: List<PedidoAgrupadoResponse>,
)

data class PedidoAgrupadoResponse(
    val id: String,
    val codigo: Long,
    val valor: BigDecimal,
    val status: String,
    val criadoEm: LocalDateTime,
    val clienteId: String
)
