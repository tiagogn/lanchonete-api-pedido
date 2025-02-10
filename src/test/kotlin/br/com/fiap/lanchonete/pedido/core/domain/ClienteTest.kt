package br.com.fiap.lanchonete.pedido.core.domain

import org.junit.jupiter.api.Test
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClienteTest {

    @Test
    fun `should create a cliente object correctly`() {
        val id = UUID.randomUUID()
        val nome = "John"
        val email = "john@example.com"
        val cpf = "123.456.789-00"

        val cliente = Cliente(id = id, nome = nome, email = email, cpf = cpf)

        assertNotNull(cliente.id)
        assertEquals(id, cliente.id)
        assertEquals(nome, cliente.nome)
        assertEquals(email, cliente.email)
        assertEquals(cpf, cliente.cpf)
    }
}
