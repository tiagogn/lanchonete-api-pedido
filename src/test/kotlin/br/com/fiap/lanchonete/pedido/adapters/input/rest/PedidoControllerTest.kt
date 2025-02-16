package br.com.fiap.lanchonete.pedido.adapters.input.rest

import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.ItemPedidoRequest
import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.PedidoPagamentoRequest
import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.PedidoRequest
import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoOutput
import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoStatusOutput
import br.com.fiap.lanchonete.pedido.core.application.ports.input.PedidoService
import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import br.com.fiap.lanchonete.pedido.core.domain.ItemPedido
import br.com.fiap.lanchonete.pedido.core.domain.Pagamento
import br.com.fiap.lanchonete.pedido.core.domain.Pedido
import br.com.fiap.lanchonete.pedido.core.domain.StatusPagamento
import br.com.fiap.lanchonete.pedido.core.domain.StatusPedido
import com.fasterxml.jackson.databind.ObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

@WebMvcTest(PedidoController::class)
class PedidoControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    lateinit var pedidoService: PedidoService

    private val objectMapper = ObjectMapper()
    private lateinit var pedido: Pedido
    private lateinit var pedidoRequest: PedidoRequest
    private lateinit var pedidoPagamentoRequest: PedidoPagamentoRequest
    private lateinit var pedidoOutput: PedidoOutput
    private val pedidoId = UUID.randomUUID()

    @BeforeEach
    fun setup() {
        val itemPedido = ItemPedido(
            id = UUID.randomUUID(),
            produtoId = UUID.randomUUID(),
            nomeProduto = "X-Burger",
            quantidade = 2,
            precoUnitario = BigDecimal(10.00),
            categoria = "Lanche"
        )
        pedido = Pedido(
            id = pedidoId,
            cliente = null,
            itens = listOf(itemPedido),
            total = BigDecimal(20.00),
            status = StatusPedido.RECEBIDO,
            criadoEm = LocalDateTime.now(),
            atualizadoEm = LocalDateTime.now(),
            codigo = 1234,
            pagamento = Pagamento.createPagamentoPendente()
        )

        pedidoRequest = PedidoRequest(
            clienteId = UUID.randomUUID(),
            itens = listOf(
                ItemPedidoRequest(
                    produtoId = UUID.randomUUID(),
                    nomeProduto = "X-Burger",
                    quantidade = 1,
                    precoUnitario = BigDecimal(15.00),
                    categoria = "Lanche"
                )
            )
        )

        pedidoPagamentoRequest = PedidoPagamentoRequest(
            pedidoId = pedidoId,
            valor = 20.00,
            formaPagamento = "CARTAO",
            status = "APROVADO",
            pagamentoId = UUID.randomUUID(),
            dataPagamento = LocalDateTime.now().toString(),
            mensagem = "Pagamento realizado com sucesso"
        )

        pedidoOutput = PedidoOutput(
            id = pedidoId.toString(),
            codigo = 1234,
            valor = BigDecimal(20.00),
            status = StatusPedido.RECEBIDO.name,
            criadoEm = LocalDateTime.now(),
            clienteId = UUID.randomUUID().toString()
        )
    }

    @Test
    fun `should create an order`() {
        every { pedidoService.criarPedido(any(), any()) } returns pedido

        mockMvc.perform(
            post("/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(pedidoId.toString()))
            .andExpect(jsonPath("$.total").value(20.00))
            .andExpect(jsonPath("$.status").value(pedido.status.name))
    }

    @Test
    fun `should not mark order as ready if payment is not approved`() {
        every { pedidoService.pedidoPronto(pedidoId) } throws PedidoException("Pedido não pode ser marcado como pronto, pois ainda não está em preparação")

        mockMvc.perform(
            patch("/v1/pedidos/{pedidoId}/pronto", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should finalize an order`() {
        every { pedidoService.pedidoFinalizado(pedidoId) } returns pedido.copy(status = StatusPedido.FINALIZADO)

        mockMvc.perform(
            patch("/v1/pedidos/{pedidoId}/finalizado", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pedidoId").value(pedidoId.toString()))
            .andExpect(jsonPath("$.statusPedido").value(StatusPedido.FINALIZADO.name))
    }

    @Test
    fun `should return order by id`() {
        every { pedidoService.buscarPorId(pedidoId) } returns pedido

        mockMvc.perform(
            get("/v1/pedidos/{pedidoId}", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(pedidoId.toString()))
            .andExpect(jsonPath("$.total").value(20.00))
            .andExpect(jsonPath("$.pagamento").value(pedido.pagamento.status.name))
    }

    @Test
    fun `should return order status`() {
        every { pedidoService.buscarPorId(pedidoId) } returns pedido

        mockMvc.perform(
            get("/v1/pedidos/{pedidoId}/status", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.codigo").value(1234))
            .andExpect(jsonPath("$.status").value(pedido.status.name))
            .andExpect(jsonPath("$.pagamento").value(pedido.pagamento.status.name))
    }

    @Test
    fun `should confirm payment and update order status`() {
        val pagamentoAprovado = Pagamento(
            pagamentoId = UUID.randomUUID(),
            valorPago = BigDecimal(20.00),
            status = StatusPagamento.APROVADO,
            formaPagamento = "CARTAO",
            dataPagamento = LocalDateTime.now(),
            mensagem = "Pagamento aprovado"
        )
        every { pedidoService.confirmarPagamento(any()) } returns pedido.copy(
            pagamento = pagamentoAprovado,
            status = StatusPedido.EM_PREPARACAO
        )

        mockMvc.perform(
            post("/v1/pedidos/pagamento/{pedidoId}", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pedidoPagamentoRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.pedidoId").value(pedidoId.toString()))
            .andExpect(jsonPath("$.status").value(StatusPedido.EM_PREPARACAO.name))
    }

    @Test
    fun `should return list of grouped orders`() {
        val pedidosAgrupados = listOf(
            PedidoStatusOutput(
                status = "RECEBIDO",
                pedidos = listOf(pedidoOutput)
            )
        )
        every { pedidoService.listarPedidosAgrupadosPorStatus() } returns pedidosAgrupados

        mockMvc.perform(
            get("/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$[0].status").value(StatusPedido.RECEBIDO.name))
            .andExpect(jsonPath("$[0].pedidos[0].id").value(pedidoId.toString()))
    }
}