package br.com.fiap.lanchonete.pedido.adapters.config

import br.com.fiap.lanchonete.pedido.core.application.ports.input.ClienteService
import br.com.fiap.lanchonete.pedido.core.application.ports.input.PedidoService
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.ClienteRepository
import br.com.fiap.lanchonete.pedido.core.application.ports.output.repository.PedidoRepository
import br.com.fiap.lanchonete.pedido.core.application.services.ClienteServiceImpl
import br.com.fiap.lanchonete.pedido.core.application.services.PedidoServiceImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ConfigBeans(
    private val clienteRepository: ClienteRepository,
    private val pedidoRepository: PedidoRepository,
) {

    @Bean
    fun clienteService(): ClienteService {
        return ClienteServiceImpl(clienteRepository)
    }

    @Bean
    fun pedidoService(): PedidoService {
        return PedidoServiceImpl(pedidoRepository, clienteRepository)
    }

}