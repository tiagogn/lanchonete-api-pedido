package br.com.fiap.lanchonete.pedido.core.application.services

import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoPagamentoInput
import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.PedidoRepository
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.application.services.exceptions.ResourceNotFoundException
import br.com.fiap.lanchonete.pedido.core.domain.*
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
        pedidoRepository = mockk()
        clienteRepository = mockk()
        pedidoService = PedidoServiceImpl(pedidoRepository, clienteRepository)
    }

    @Test
    fun `should create a new order`() {
        val cliente = Cliente(
            id = UUID.randomUUID(),
            nome = "João Silva",
            email = "joao.silva@example.com",
            cpf = "12345678901"
        )

        val itens = listOf(
            ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 2, BigDecimal(15.00), "Lanche")
        )

        every { clienteRepository.findById(cliente.id!!) } returns Optional.of(cliente)
        every { pedidoRepository.save(any()) } returns Unit

        val pedidoCriado = pedidoService.criarPedido(cliente.id, itens)

        assertNotNull(pedidoCriado)
        assertEquals(cliente.id, pedidoCriado.cliente?.id)
        verify(exactly = 1) { clienteRepository.findById(cliente.id!!) }
        verify(exactly = 1) { pedidoRepository.save(any()) }
    }

    @Test
    fun `should throw exception when creating order for non-existing client`() {
        val clienteId = UUID.randomUUID()
        val itens = listOf(
            ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Hamburguer", 2, BigDecimal(15.00), "Lanche")
        )

        every { clienteRepository.findById(clienteId) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            pedidoService.criarPedido(clienteId, itens)
        }

        verify(exactly = 1) { clienteRepository.findById(clienteId) }
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
        every { pedidoRepository.save(pedido) } returns Unit

        val pedidoAtualizado = pedidoService.atualizarStatusPedido(pedidoId, StatusPedido.PRONTO)

        assertNotNull(pedidoAtualizado)
        assertEquals(StatusPedido.PRONTO, pedidoAtualizado.status)
        verify(exactly = 1) { pedidoRepository.findById(pedidoId) }
        verify(exactly = 1) { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should throw exception when updating order status to PRONTO but it is not in preparation`() {
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

        every { pedidoRepository.findById(pedidoId) } returns Optional.of(pedido)

        assertThrows<PedidoException> {
            pedidoService.atualizarStatusPedido(pedidoId, StatusPedido.PRONTO)
        }

        verify(exactly = 1) { pedidoRepository.findById(pedidoId) }
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
        every { pedidoRepository.save(pedido) } returns Unit

        val pedidoComPagamento = pedidoService.confirmarPagamento(pagamentoInput)

        assertNotNull(pedidoComPagamento)
        assertEquals(StatusPedido.RECEBIDO, pedidoComPagamento.status)
        verify(exactly = 1) { pedidoRepository.findById(pedidoId) }
        verify(exactly = 1) { pedidoRepository.save(pedido) }
    }

    @Test
    fun `should throw exception when confirming payment for a non-existing order`() {
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

        verify(exactly = 1) { pedidoRepository.findById(pedidoId) }
        verify(exactly = 0) { pedidoRepository.save(any()) }
    }
}

