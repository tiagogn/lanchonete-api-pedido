package br.com.fiap.lanchonete.pedido.core.application.exceptions

class PedidoException: RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}