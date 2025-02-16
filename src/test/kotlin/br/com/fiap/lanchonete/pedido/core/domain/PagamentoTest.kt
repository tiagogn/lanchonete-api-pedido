package br.com.fiap.lanchonete.pedido.core.domain

import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*

class PagamentoTest {

    @Test
    fun `should approve payment and move order to preparation`() {
        val pedido = Pedido(
            id = UUID.randomUUID(),
            cliente = null,
            itens = listOf(),
            total = BigDecimal(100),
            status = StatusPedido.RECEBIDO
        )

        val pagamento = Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão",
            dataPagamento = LocalDateTime.now(),
            pagamentoId = UUID.randomUUID()
        )

        pagamento.pagamentoAprovado(pedido)

        assertEquals(pagamento, pedido.pagamento)
        assertEquals(StatusPagamento.APROVADO, pedido.pagamento.status)
    }

    @Test
    fun `should refuse payment by changing status to RECUSADO`() {
        val pedido = mockk<Pedido>(relaxed = true)
        val pagamento = Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.PENDENTE,
            formaPagamento = "Cartão"
        )

        pagamento.pagamentoRecusado(pedido)

        assertEquals(StatusPagamento.RECUSADO, pagamento.status)
    }

    @Test
    fun `should throw exception when confirming payment if already approved`() {
        val pedido = mockk<Pedido>(relaxed = true)
        every { pedido.pagamento } returns Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão"
        )
        every { pedido.codigo } returns 12345

        val pagamento = Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão"
        )

        val exception = assertThrows(PedidoException::class.java) {
            pagamento.confirmarPagamento(pedido)
        }

        assertEquals("Pagamento do pedido 12345 já aprovado", exception.message)
    }

    @Test
    fun `should approve payment when amount matches order total and status is approved`() {
        val pedido = Pedido(
            id = UUID.randomUUID(),
            cliente = null,
            itens = listOf(),
            total = BigDecimal(100),
            status = StatusPedido.RECEBIDO
        )

        val pagamento = Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão"
        )

        pagamento.confirmarPagamento(pedido)

        assertEquals(pagamento, pedido.pagamento)
        assertEquals(StatusPagamento.APROVADO, pedido.pagamento.status)
    }

    @Test
    fun `should refuse payment when amount does not match order total`() {
        val pedido = mockk<Pedido>(relaxed = true)
        every { pedido.total } returns BigDecimal(150)
        every { pedido.pagamento } returns Pagamento(
            valorPago = BigDecimal(0),
            status = StatusPagamento.PENDENTE,
            formaPagamento = "Cartão"
        )

        val pagamento = Pagamento(
            valorPago = BigDecimal(100),
            status = StatusPagamento.APROVADO,
            formaPagamento = "Cartão"
        )

        pagamento.confirmarPagamento(pedido)

        assertEquals(StatusPagamento.RECUSADO, pagamento.status)
    }

    @Test
    fun `should create a pending payment`() {
        val pagamento = Pagamento.createPagamentoPendente()

        assertNotNull(pagamento)
        assertEquals(BigDecimal.ZERO, pagamento.valorPago)
        assertEquals(StatusPagamento.PENDENTE, pagamento.status)
        assertEquals("", pagamento.formaPagamento)
    }
}
