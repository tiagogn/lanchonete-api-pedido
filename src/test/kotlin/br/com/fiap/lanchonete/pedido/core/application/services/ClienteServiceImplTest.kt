package br.com.fiap.lanchonete.pedido.core.application.services

import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.application.services.exceptions.ResourceNotFoundException
import br.com.fiap.lanchonete.pedido.core.domain.Cliente
import io.mockk.every
import io.mockk.verify
import io.mockk.mockk
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ClienteServiceImplTest {

    private lateinit var clienteRepository: ClienteRepository
    private lateinit var clienteService: ClienteServiceImpl

    @BeforeEach
    fun setup() {
        clienteRepository = mockk()
        clienteService = ClienteServiceImpl(clienteRepository)
    }

    @Test
    fun `should register a client`() {
        val cliente = Cliente(
            id = UUID.randomUUID(),
            nome = "João Silva",
            email = "joao.silva@example.com",
            cpf = "12345678901"
        )

        every { clienteRepository.save(cliente) } returns cliente

        val savedCliente = clienteService.cadastrarCliente(cliente)

        assertNotNull(savedCliente)
        assertEquals(cliente, savedCliente)
        verify(exactly = 1) { clienteRepository.save(cliente) }
    }

    @Test
    fun `should find a client by CPF`() {
        val cliente = Cliente(
            id = UUID.randomUUID(),
            nome = "Maria Oliveira",
            email = "maria.oliveira@example.com",
            cpf = "98765432100"
        )

        every { clienteRepository.findByCPF(cliente.cpf) } returns Optional.of(cliente)

        val foundCliente = clienteService.buscarClientePorCPF(cliente.cpf)

        assertNotNull(foundCliente)
        assertEquals(cliente, foundCliente)
        verify(exactly = 1) { clienteRepository.findByCPF(cliente.cpf) }
    }

    @Test
    fun `should throw exception when client is not found by CPF`() {
        val cpf = "00000000000"

        every { clienteRepository.findByCPF(cpf) } returns Optional.empty()

        assertThrows<ResourceNotFoundException> {
            clienteService.buscarClientePorCPF(cpf)
        }
        verify(exactly = 1) { clienteRepository.findByCPF(cpf) }
    }
}
