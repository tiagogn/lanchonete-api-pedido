package br.com.fiap.lanchonete.pedido.core.application.ports.output.repository

import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import java.util.*

interface PedidoRepository {
    fun findByStatus(statusPedido: StatusPedido): List<Pedido>
    fun save(pedido: Pedido): Unit
    fun findById(id: UUID): Optional<Pedido>
    fun findAllByOrderByStatusNotIn(statusPedido: StatusPedido): List<Pedido>
}