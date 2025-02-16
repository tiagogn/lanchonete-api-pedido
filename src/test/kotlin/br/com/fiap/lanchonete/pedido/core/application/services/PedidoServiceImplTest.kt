package br.com.fiap.lanchonete.pedido.core.application.services

import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoPagamentoInput
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.PedidoRepository
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.application.services.exceptions.ResourceNotFoundException
import br.com.fiap.lanchonete.pedido.core.domain.Cliente
import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import br.com.fiap.lanchonete.pedido.core.domain.Pagamento
import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPagamento
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class PedidoServiceImplTest {

    private lateinit var pedidoRepository: PedidoRepository
    private lateinit var clienteRepository: ClienteRepository
    private lateinit var pedidoService: PedidoServiceImpl

    @BeforeEach
    fun setup() {
        pedidoRepository = mockk(relaxed = true)
        clienteRepository = mockk(relaxed = true)
        pedidoService = PedidoServiceImpl(pedidoRepository, clienteRepository)
    }

    @Test
    fun `should create a new order successfully`() {
        val cliente = Cliente(UUID.randomUUID(), "João Silva", "joao.silva@example.com", "12345678901")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 2, BigDecimal(15.00), "Lanche"))

        every { clienteRepository.findById(cliente.id!!) } returns Optional.of(cliente)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val pedidoCriado = pedidoService.criarPedido(cliente.id, itens)

        assertNotNull(pedidoCriado)
        assertEquals(cliente.id, pedidoCriado.cliente?.id)
        assertEquals(itens.size, pedidoCriado.itens.size)
        assertEquals(BigDecimal(30.00), pedidoCriado.total)

        verify { clienteRepository.findById(cliente.id!!) }
        verify { pedidoRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating order for non-existing client`() {
        val clienteId = UUID.randomUUID()
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 2, BigDecimal(15.00), "Lanche"))

        every { clienteRepository.findById(clienteId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            pedidoService.criarPedido(clienteId, itens)
        }

        verify { clienteRepository.findById(clienteId) }
        verify(exactly = 0) { pedidoRepository.save(any()) }
    }

    @Test
    fun `should update order status to PRONTO`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = Cliente(UUID.randomUUID(), "João Silva", "joao.silva@example.com", "12345678901"),
            itens = listOf(
                ItemPedido(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Hamburguer",
                    2,
                    BigDecimal(15.00),
                    "Lanche"
                )
            ),
            status = StatusPedido.EM_PREPARACAO
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val pedidoAtualizado = pedidoService.atualizarStatusPedido(pedidoId, StatusPedido.PRONTO)

        assertNotNull(pedidoAtualizado)
        assertEquals(StatusPedido.PRONTO, pedidoAtualizado.status)

        verify { pedidoRepository.findById(pedidoId) }
        verify { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should throw exception when updating order status for non-existing order`() {
        val pedidoId = UUID.randomUUID()

        every { pedidoRepository.findById(pedidoId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            pedidoService.atualizarStatusPedido(pedidoId, StatusPedido.PRONTO)
        }

        verify { pedidoRepository.findById(pedidoId) }
        verify(exactly = 0) { pedidoRepository.save(any()) }
    }

    @Test
    fun `should confirm payment for an order`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = Cliente(UUID.randomUUID(), "João Silva", "joao.silva@example.com", "12345678901"),
            itens = listOf(
                ItemPedido(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Hamburguer",
                    2,
                    BigDecimal(15.00),
                    "Lanche"
                )
            ),
            status = StatusPedido.RECEBIDO
        )

        val pagamentoInput = PedidoPagamentoInput(
            pedidoId = pedidoId,
            valorPago = BigDecimal(30.00),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão",
            dataPagamento = LocalDateTime.now(),
            pagamentoId = UUID.randomUUID(),
            mensagem = "Pagamento aprovado"
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val pedidoComPagamento = pedidoService.confirmarPagamento(pagamentoInput)

        assertNotNull(pedidoComPagamento)
        assertEquals(StatusPedido.RECEBIDO, pedidoComPagamento.status)

        verify { pedidoRepository.findById(pedidoId) }
        verify { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should throw exception when confirming payment for non-existing order`() {
        val pedidoId = UUID.randomUUID()
        val pagamentoInput = PedidoPagamentoInput(
            pedidoId = pedidoId,
            valorPago = BigDecimal(30.00),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão",
            dataPagamento = LocalDateTime.now(),
            pagamentoId = UUID.randomUUID(),
            mensagem = "Pagamento aprovado"
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            pedidoService.confirmarPagamento(pagamentoInput)
        }

        verify { pedidoRepository.findById(pedidoId) }
        verify(exactly = 0) { pedidoRepository.save(any()) }
    }

    @Test
    fun `should throw exception when updating order status to an invalid status`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = Cliente(UUID.randomUUID(), "João Silva", "joao.silva@example.com", "12345678901"),
            itens = listOf(
                ItemPedido(
                    UUID.randomUUID(),
                    UUID.randomUUID(),
                    "Hamburguer",
                    2,
                    BigDecimal(15.00),
                    "Lanche"
                )
            ),
            status = StatusPedido.EM_PREPARACAO
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)

        assertThrows<ResourceNotFoundException> {
            pedidoService.atualizarStatusPedido(
                pedidoId,
                StatusPedido.RECEBIDO
            )
        }

        verify { pedidoRepository.findById(pedidoId) }
        verify(exactly = 0) { pedidoRepository.save(any()) }
    }

    @Test
    fun `should return orders grouped by status`() {
        val pedido1 = Pedido(
            id = UUID.randomUUID(),
            codigo = 12345,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.EM_PREPARACAO
        )
        val pedido2 = Pedido(
            id = UUID.randomUUID(),
            codigo = 12344,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.PRONTO
        )

        every { pedidoRepository.findAllByOrderByStatusNotIn(StatusPedido.FINALIZADO) } returns listOf(pedido1, pedido2)

        val groupedOrders = pedidoService.listarPedidosAgrupadosPorStatus()

        assertEquals(2, groupedOrders.size)
        assertEquals("EM_PREPARACAO", groupedOrders[0].status)
        assertEquals("PRONTO", groupedOrders[1].status)

        verify { pedidoRepository.findAllByOrderByStatusNotIn(StatusPedido.FINALIZADO) }
    }

    @Test
    fun `should retrieve an order by ID`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.EM_PREPARACAO
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)

        val result = pedidoService.buscarPorId(pedidoId)

        assertEquals(pedido, result)
        verify { pedidoRepository.findById(pedidoId) }
    }

    @Test
    fun `should throw exception when retrieving non-existing order`() {
        val pedidoId = UUID.randomUUID()

        every { pedidoRepository.findById(pedidoId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            pedidoService.buscarPorId(pedidoId)
        }

        verify { pedidoRepository.findById(pedidoId) }
    }

    @Test
    fun `should update order status to EM_PREPARACAO when sending to kitchen`() {
        val pedidoId = UUID.randomUUID()
        val pagamento = Pagamento(
            pagamentoId = UUID.randomUUID(),
            valorPago = BigDecimal(30.00),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão",
            dataPagamento = LocalDateTime.now(),
            mensagem = "Pagamento aprovado"
        )
        val pedido = Pedido(
            id = pedidoId,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.RECEBIDO,
            pagamento = pagamento
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val updatedPedido = pedidoService.enviandoPedidoParaCozinha(pedidoId)

        assertEquals(StatusPedido.EM_PREPARACAO, updatedPedido.status)

        verify { pedidoRepository.findById(pedidoId) }
        verify { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should update order status to PRONTO when marking as ready`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.EM_PREPARACAO
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val updatedPedido = pedidoService.pedidoPronto(pedidoId)

        assertEquals(StatusPedido.PRONTO, updatedPedido.status)

        verify { pedidoRepository.findById(pedidoId) }
        verify { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should update order status to FINALIZADO when completing order`() {
        val pedidoId = UUID.randomUUID()
        val pedido = Pedido(
            id = pedidoId,
            cliente = null,
            itens = listOf(),
            status = StatusPedido.PRONTO
        )

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)
        every { pedidoRepository.save(any()) } returnsArgument 0

        val updatedPedido = pedidoService.pedidoFinalizado(pedidoId)

        assertEquals(StatusPedido.FINALIZADO, updatedPedido.status)

        verify { pedidoRepository.findById(pedidoId) }
        verify { pedidoRepository.save(pedido) }
    }
}
