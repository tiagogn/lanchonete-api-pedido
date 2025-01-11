package br.com.fiap.lanchonete.pedido.adapters.input.rest.response

import br.com.fiap.lanchonete.pedido.core.domain.Cliente

data class ClienteResponse(
    val id: String,
    val nome: String,
    val email: String,
    val cpf: String
)

fun Cliente.toResponse(): ClienteResponse {
    return ClienteResponse(
        id = this.id!!.toString(),
        nome = this.nome,
        email = this.email,
        cpf = this.cpf
    )
}