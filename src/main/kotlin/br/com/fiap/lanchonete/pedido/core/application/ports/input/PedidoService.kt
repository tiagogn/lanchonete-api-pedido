package br.com.fiap.lanchonete.pedido.core.application.ports.input

import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoPagamentoInput
import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoStatusOutput
import java.util.*

interface PedidoService {
    fun criarPedido(clienteId: UUID?, itens: List<ItemPedido>): Pedido
    fun atualizarStatusPedido(pedidoId: UUID, novoStatus: StatusPedido): Pedido
    fun listarPedidosAgrupadosPorStatus(): List<PedidoStatusOutput>
    fun buscarPorId(pedidoId: UUID): Pedido
    fun enviandoPedidoParaCozinha(pedidoId: UUID): Pedido
    fun pedidoPronto(pedidoId: UUID): Pedido
    fun pedidoFinalizado(pedidoId: UUID): Pedido
    fun confirmarPagamento(pedidoPagamentoInput: PedidoPagamentoInput): Pedido
}