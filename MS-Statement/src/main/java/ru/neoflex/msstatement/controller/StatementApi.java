package ru.neoflex.msstatement.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.exception.PrescoringFailedException;

import java.util.List;

@RestController
@RequestMapping("/statement")
public interface StatementApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json", array =
                    @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "406", description = "Prescoring failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Прескоринг и расчёт возможных условий кредита",
            description = """
                    По API приходит LoanStatementRequestDto, валидируется. \r\n
                    На основе LoanStatementRequestDto происходит прескоринг, если клиент его не проходит, \
                    то выбрасывается PrescoringFailedException с кодом 406. \r\n
                    Если проходит, то отправляется POST запрос на /deal/statement через RestClient \r\n
                    Ответ на запрос - список из четырёх LoanOfferDto, которые отсортированы \
                    по мере уменьшения процентной ставки.".
                    """
    )
    @PostMapping()
    List<LoanOfferDto> getOffers(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на кредит от клиента в виде LoanStatementRequestDto.",
            required = true) @RequestBody @Valid LoanStatementRequestDto request)
                                                    throws PrescoringFailedException,
                                                           RestClientResponseException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Расчёт возможных условий кредита",
            description = """
                    По API приходит LoanOfferDto, валидируется. \r\n
                    Отправляется POST запрос на /deal/offer/select через RestClient \
                    для выбора интересующего клиента предложения.
                    """
    )
    @PostMapping("/offer")
    void selectOffer(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Принятые клиентом условия кредита в виде LoanOfferDto.",
            required = true) @RequestBody @Valid LoanOfferDto offer)
                                                    throws RestClientResponseException;
}
