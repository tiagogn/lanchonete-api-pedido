package br.com.fiap.lanchonete.pedido.core.application.services

import br.com.fiap.lanchonete.pedido.core.application.ports.input.ClienteService
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.application.services.exceptions.ResourceNotFoundException
import br.com.fiap.lanchonete.pedido.core.domain.Cliente

class ClienteServiceImpl(
    private val clienteRepository: ClienteRepository
) : ClienteService {
    override fun cadastrarCliente(cliente: Cliente): Cliente {
        return clienteRepository.save(cliente)
    }

    override fun buscarClientePorCPF(cpf: String): Cliente {
        return clienteRepository.findByCPF(cpf).orElseThrow { ResourceNotFoundException("Cliente não encontrado") }
    }
}