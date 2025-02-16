package br.com.fiap.lanchonete.pedido.core.domain

import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PedidoTest {

    @Test
    fun `should create a pedido object correctly`() {
        val id = UUID.randomUUID()
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val total = itens.sumOf { it.getSubTotal() }

        val pedido = Pedido(id = id, cliente = cliente, itens = itens, total = total)

        assertNotNull(pedido.id)
        assertEquals(id, pedido.id)
        assertEquals(cliente, pedido.cliente)
        assertEquals(itens, pedido.itens)
        assertEquals(total, pedido.total)
    }

    @Test
    fun `should change status to EM_PREPARACAO when pedidoEmPreparacao is called`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()

        assertEquals(StatusPedido.EM_PREPARACAO, pedido.status)
        assertNotNull(pedido.atualizadoEm)
    }

    @Test
    fun `should throw PedidoException when pedidoEmPreparacao is called with unapproved payment`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.PENDENTE, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        val exception = assertFailsWith<PedidoException> {
            pedido.pedidoEmPreparacao()
        }

        assertEquals("Pedido não pode ser preparado, pois o pagamento não foi aprovado", exception.message)
    }

    @Test
    fun `should change status to PRONTO when pedidoPronto is called`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()
        pedido.pedidoPronto()

        assertEquals(StatusPedido.PRONTO, pedido.status)
        assertNotNull(pedido.prontoEm)
    }

    @Test
    fun `should throw PedidoException when pedidoPronto is called without emPreparacao status`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        val exception = assertFailsWith<PedidoException> {
            pedido.pedidoPronto()
        }

        assertEquals("Pedido não pode ser marcado como pronto, pois ainda não está em preparação", exception.message)
    }

    @Test
    fun `should change status to FINALIZADO when pedidoFinalizado is called`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()
        pedido.pedidoPronto()
        pedido.pedidoFinalizado()

        assertEquals(StatusPedido.FINALIZADO, pedido.status)
        assertNotNull(pedido.finalizadoEm)
    }

    @Test
    fun `should throw PedidoException when pedidoFinalizado is called without pronto status`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        val exception = assertFailsWith<PedidoException> {
            pedido.pedidoFinalizado()
        }

        assertEquals("Pedido não pode ser finalizado, pois ainda não está pronto", exception.message)
    }

    @Test
    fun `should return tempoEspera in minutes when pedido is pronto`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()
        pedido.pedidoPronto()

        val tempoEspera = pedido.tempoEspera()

        assertEquals("0 minutos", tempoEspera)
    }

    @Test
    fun `should return tempoEspera in minutes when pedido is pronto and calculated time is positive`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()
        Thread.sleep(1000)
        pedido.pedidoPronto()

        val tempoEspera = pedido.tempoEspera()

        assertTrue(tempoEspera.contains("minutos"))
    }

    @Test
    fun `should throw PedidoException when pedidoEmPreparacao is called with already emPreparacao status`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(cliente = cliente, itens = itens, pagamento = pagamento, total = BigDecimal(50))

        pedido.pedidoEmPreparacao()

        val exception = assertFailsWith<PedidoException> {
            pedido.pedidoEmPreparacao()
        }

        assertEquals("Pedido não pode ser preparado, pois já está em preparação", exception.message)
    }

    @Test
    fun `should not throw exception when pedidoPronto is called with already pronto status`() {
        val cliente = Cliente(UUID.randomUUID(), "John", "john@example.com", "123.456.789-00")
        val pagamento =
            Pagamento(valorPago = BigDecimal(50), status = StatusPagamento.APROVADO, formaPagamento = "Cartão")
        val itens =
            listOf(ItemPedido(UUID.randomUUID(), UUID.randomUUID(), "Produto A", 2, BigDecimal(10), "Categoria 1"))
        val pedido = Pedido(
            cliente = cliente,
            itens = itens,
            pagamento = pagamento,
            total = BigDecimal(50),
            status = StatusPedido.RECEBIDO
        )

        pedido.pedidoEmPreparacao()
        pedido.pedidoPronto()

        assertEquals(StatusPedido.PRONTO, pedido.status)
    }
}
