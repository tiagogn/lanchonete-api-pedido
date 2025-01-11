package br.com.fiap.lanchonete.pedido.adapters.input.rest

import br.com.fiap.lanchonete.pedido.adapters.input.rest.response.ErrorResponse
import br.com.fiap.lanchonete.pedido.core.application.exceptions.PedidoException
import br.com.fiap.lanchonete.pedido.core.application.services.exceptions.ResourceNotFoundException
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ErrorController {

    val logger = org.slf4j.LoggerFactory.getLogger(ErrorController::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(e: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        logger.error("Recurso não encontrado", e)
        return ResponseEntity(
            ErrorResponse(e.message),
            org.springframework.http.HttpStatus.NOT_FOUND
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleResourceNotFoundException(e: MissingServletRequestParameterException): ResponseEntity<ErrorResponse> {
        logger.error("Parâmetro não informado", e)
        return ResponseEntity(
            ErrorResponse("Parâmetro não informado"),
            org.springframework.http.HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(DuplicateKeyException::class)
    fun handleResourceNotFoundException(e: DuplicateKeyException): ResponseEntity<ErrorResponse> {
        logger.error("Registro duplicado", e)
        return ResponseEntity(
            ErrorResponse("Registro já existe"),
            org.springframework.http.HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(PedidoException::class)
    fun handlePedidoException(e: PedidoException): ResponseEntity<ErrorResponse> {
        logger.error("Problemas ao processar o pedido ", e)
        return ResponseEntity(
            ErrorResponse(e.message!!),
            org.springframework.http.HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleResourceNotFoundException(e: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Erro interno", e)
        return ResponseEntity(
            ErrorResponse("Erro interno"),
            org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}