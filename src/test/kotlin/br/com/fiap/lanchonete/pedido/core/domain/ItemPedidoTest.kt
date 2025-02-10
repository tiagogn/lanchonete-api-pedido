package br.com.fiap.lanchonete.pedido.core.domain

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ItemPedidoTest {

    @Test
    fun `should create an item pedido object correctly`() {
        val id = UUID.randomUUID()
        val produtoId = UUID.randomUUID()
        val nomeProduto = "Hamburguer"
        val quantidade = 2
        val precoUnitario = BigDecimal(25.00)
        val categoria = "Lanche"

        val itemPedido = ItemPedido(
            id = id,
            produtoId = produtoId,
            nomeProduto = nomeProduto,
            quantidade = quantidade,
            precoUnitario = precoUnitario,
            categoria = categoria
        )

        assertNotNull(itemPedido.id)
        assertEquals(id, itemPedido.id)
        assertEquals(produtoId, itemPedido.produtoId)
        assertEquals(nomeProduto, itemPedido.nomeProduto)
        assertEquals(quantidade, itemPedido.quantidade)
        assertEquals(precoUnitario, itemPedido.precoUnitario)
        assertEquals(categoria, itemPedido.categoria)
    }

    @Test
    fun `should calculate the correct subtotal for an item pedido`() {
        val precoUnitario = BigDecimal(25.00)
        val quantidade = 2
        val itemPedido = ItemPedido(
            id = UUID.randomUUID(),
            produtoId = UUID.randomUUID(),
            nomeProduto = "Hamburguer",
            quantidade = quantidade,
            precoUnitario = precoUnitario,
            categoria = "Lanche"
        )

        val subTotal = itemPedido.getSubTotal()

        val expectedSubTotal = precoUnitario * quantidade.toBigDecimal()
        assertEquals(expectedSubTotal, subTotal)
    }
}
