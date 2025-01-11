package br.com.fiap.lanchonete.pedido.core.application.services.exceptions

class ResourceNotFoundException(
    override val message: String
): RuntimeException() {
}