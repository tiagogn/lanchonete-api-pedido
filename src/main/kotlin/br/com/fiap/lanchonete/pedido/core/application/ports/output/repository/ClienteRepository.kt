package br.com.fiap.lanchonete.pedido.core.application.ports.output.repository

import br.com.fiap.lanchonete.pedido.core.domain.Cliente
import java.util.*

interface ClienteRepository {
    fun save(cliente: Cliente): Cliente
    fun findByCPF(cpf: String): Optional<Cliente>
    fun findById(id: UUID?): Optional<Cliente>
}