package br.com.fiap.lanchonete.adapters.input.rest.request

import br.com.fiap.lanchonete.pedido.core.domain.Cliente
import java.util.*

data class ClienteRequest(
    var id: UUID? = null,
    val nome: String,
    val email: String,
    val cpf: String
) {
    fun toModel(): Cliente {
        return Cliente(
            id = this.id,
            nome = this.nome,
            email = this.email,
            cpf = this.cpf
        )
    }
}