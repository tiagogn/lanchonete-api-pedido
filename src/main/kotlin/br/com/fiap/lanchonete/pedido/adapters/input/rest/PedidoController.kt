package br.com.fiap.lanchonete.pedido.adapters.input.rest

import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.PedidoPagamentoRequest
import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.PedidoRequest
import br.com.fiap.lanchonete.pedido.adapters.input.rest.request.toModel
import br.com.fiap.lanchonete.pedido.adapters.input.rest.response.*
import br.com.fiap.lanchonete.pedido.core.application.dto.PedidoPagamentoInput
import br.com.fiap.lanchonete.pedido.core.application.ports.input.PedidoService
import br.com.fiap.lanchonete.pedido.core.domain.StatusPagamento
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal
import java.util.*

@RestController
@RequestMapping("/v1/pedidos")
class PedidoController(
    private val pedidoService: PedidoService
) {

    @PostMapping
    fun criarPedido(@RequestBody(required = true) request: PedidoRequest): ResponseEntity<PedidoResponse> {
        val itens = request.itens.map { it.toModel() }
        val pedido = pedidoService.criarPedido(request.clienteId, itens)
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido.toResponse())
    }

    @PatchMapping("/{pedidoId}/pronto")
    fun pedidoPronto(
        @PathVariable(required = true) pedidoId: UUID,
    ): ResponseEntity<AtualizacaoPedidoStatusResponse> {
        val pedido = pedidoService.pedidoPronto(pedidoId)
        val response = AtualizacaoPedidoStatusResponse(
            pedidoId = pedido.id.toString(),
            statusPedido = pedido.status,
            mensagem = "Pedido pronto para retirada"
        )
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @PatchMapping("/{pedidoId}/finalizado")
    fun pedidoFinalizado(
        @PathVariable(required = true) pedidoId: UUID,
    ): ResponseEntity<AtualizacaoPedidoStatusResponse> {
        val pedido = pedidoService.pedidoFinalizado(pedidoId)
        val response = AtualizacaoPedidoStatusResponse(
            pedidoId = pedido.id.toString(),
            statusPedido = pedido.status,
            mensagem = "Pedido finalizado e entregue"
        )
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("")
    fun listarPedidos(): ResponseEntity<List<PedidosAgrupadosResponse>> {
        val pedidos = pedidoService.listarPedidosAgrupadosPorStatus()
        val response = PedidosAgrupadosResponseMapper.toResponse(pedidos)
        return ResponseEntity.status(HttpStatus.OK).body(response)
    }

    @GetMapping("/{pedidoId}")
    fun buscarPorId(@PathVariable(required = true) pedidoId: UUID): PedidoResponse {
        val pedido = pedidoService.buscarPorId(pedidoId)
        return PedidoResponse(
            id = pedido.id,
            cliente = pedido.cliente?.toResponse(),
            itens = pedido.itens.map { it.toResponse() },
            total = pedido.total,
            status = pedido.status,
            criadoEm = pedido.criadoEm,
            atualizadoEm = pedido.atualizadoEm,
            tempoEspera = pedido.tempoEspera(),
            codigo = pedido.codigo!!
        )
    }

    @GetMapping("/{pedidoId}/status")
    fun buscarStatus(@PathVariable(required = true) pedidoId: UUID): PedidoStatusResponse {
        val pedido = pedidoService.buscarPorId(pedidoId)
        return PedidoStatusResponse(
            status = pedido.status,
            codigo = pedido.codigo!!,
            pagamento = pedido.pagamento.status.name
        )
    }

    @PostMapping("/pagamento/{pedidoId}")
    fun pagamentoPedido(@PathVariable(required = true) pedidoId: UUID, @RequestBody pedidoPagamentoRequest: PedidoPagamentoRequest): ResponseEntity<PedidoPagamentoResponse> {
        val pedido = pedidoService.confirmarPagamento(PedidoPagamentoInput(
            pedidoId = pedidoId,
            valorPago = BigDecimal.valueOf(pedidoPagamentoRequest.valor),
            formaPagamento = pedidoPagamentoRequest.formaPagamento,
            status = StatusPagamento.valueOf(pedidoPagamentoRequest.status),
            pagamentoId = pedidoPagamentoRequest.pagamentoId,
            dataPagamento = pedidoPagamentoRequest.toDataPagamentoAsDate(),
            mensagem = pedidoPagamentoRequest.mensagem
        ))
        return ResponseEntity.status(HttpStatus.OK).body(
            PedidoPagamentoResponse(
                pedidoId = pedido.id.toString(),
                status = pedido.status.toString(),
                mensagem = "Pagamento ${pedido.pagamento.status.name}"
            )
        )
    }

}
