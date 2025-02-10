package br.com.fiap.lanchonete.pedido.adapters.input.rest

import br.com.fiap.lanchonete.adapters.input.rest.request.ClienteRequest
import br.com.fiap.lanchonete.pedido.core.application.ports.input.ClienteService
import br.com.fiap.lanchonete.pedido.core.domain.Cliente
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
import java.util.UUID

@WebMvcTest(ClienteController::class)
class ClienteControllerTest(@Autowired private val mockMvc: MockMvc) {

    @MockkBean
    lateinit var clienteService: ClienteService

    private val objectMapper = ObjectMapper()
    private lateinit var cliente: Cliente
    private lateinit var clienteRequest: ClienteRequest
    private val clienteId = UUID.randomUUID()
    private val cpf = "12345678901"

    @BeforeEach
    fun setup() {
        cliente = Cliente(
            id = clienteId,
            nome = "João Silva",
            email = "joao.silva@email.com",
            cpf = cpf
        )

        clienteRequest = ClienteRequest(
            nome = "João Silva",
            email = "joao.silva@email.com",
            cpf = cpf
        )
    }

    @Test
    fun `should create a client`() {
        every { clienteService.cadastrarCliente(any()) } returns cliente

        mockMvc.perform(
            post("/v1/clientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(clienteRequest))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(clienteId.toString()))
            .andExpect(jsonPath("$.nome").value(cliente.nome))
            .andExpect(jsonPath("$.email").value(cliente.email))
            .andExpect(jsonPath("$.cpf").value(cliente.cpf))
    }

    @Test
    fun `should find client by CPF`() {
        every { clienteService.buscarClientePorCPF(cpf) } returns cliente

        mockMvc.perform(
            get("/v1/clientes/cpf/{cpf}", cpf)
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(clienteId.toString()))
            .andExpect(jsonPath("$.nome").value(cliente.nome))
            .andExpect(jsonPath("$.email").value(cliente.email))
            .andExpect(jsonPath("$.cpf").value(cliente.cpf))
    }
}
