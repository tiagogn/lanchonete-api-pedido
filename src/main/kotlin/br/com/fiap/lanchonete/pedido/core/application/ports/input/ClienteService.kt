package br.com.fiap.lanchonete.pedido.core.application.ports.input

import br.com.fiap.lanchonete.pedido.core.domain.Cliente

interface ClienteService {
    fun cadastrarCliente(cliente: Cliente): Cliente
    fun buscarClientePorCPF(cpf: String): Cliente
}