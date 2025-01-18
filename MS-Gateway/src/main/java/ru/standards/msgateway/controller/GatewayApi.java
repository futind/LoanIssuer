package ru.standards.msgateway.controller;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import ru.neoflex.loanissuerlibrary.dto.FinishRegistrationRequestDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;

import java.util.List;
import java.util.UUID;

public interface GatewayApi {

    @Operation(
            summary = "Прескоринг и получение предварительных кредитных предложений для клиента",
            description = """
                    На вход приходит LoanStatementRequestDto с данными о клиенте. Заявка отправляется \
                    на микросервис MS-Statement с помощью RestClient. Там происходит прескоринг, и если клиент \
                    его проходит, то заявка отправляется далее в MS-Deal, где сохраняется и отправляется запрос на \
                    MS-Calculator для расчёта возможных условий кредита.Ответ на запрос - список \
                    из четырёх LoanOfferDto, которые отсортированы по мере уменьшения процентной ставки. \r\n
                    Если же прескоринг не проходит, то возвращается ошибка с кодом 406.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json", array =
                    @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))
            }),
            @ApiResponse(responseCode = "400", description = "DTO validation failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "406", description = "Prescoring failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/statement")
    List<LoanOfferDto> prescoreAndGetOffers(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на кредит от клиента в виде LoanStatementRequestDto.",
            required = true
    )
                                            @RequestBody @Valid LoanStatementRequestDto request);

    @Operation(
            summary = "Выбор кредитного предложения клиентом",
            description = """
                    На вход отправляется выбранное клиентом кредитное предложение в виде LoanOfferDto. \
                    Он валидируется.\r\n
                    Отправляется запрос через RestClient на MS-Statement, который перенаправляет запрос на \
                    MS-Deal на эндпоинт /deal/offer/select. Выбранное клиентом предложение сохраняется в базу \
                    данных.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "DTO validation failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/statement/select")
    void selectOffer(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Выбранное клиентом кредитное предложение в виде LoanOfferDto.",
            required = true
    )
                     @RequestBody @Valid LoanOfferDto offer);

    @Operation(
            summary = "Завершение регистрации, скоринг и расчёт полных условий кредита",
            description = """
                    На вход приходит FinishRegistrationRequestDto - полные данные о клиенте, \
                    а также statementId - уникальный идентификационный номер заявки (UUID). \
                    DTO валидируется и отправляется на MS-Deal на эндпоинт /deal/calculate/{statementId}. \
                    Там происходит скоринг клиента, если он его не проходит, то в кредите будет отказано и \
                    вернётся ошибка с кодом 403, а клиенту придёт сообщение на электронную почту, что кредит был \
                    отклонён. Если же скоринг прошёл успешно, то рассчитываются полные \
                    условия кредита и график платежей для клиента, которые сохраняются в базу данных, а клиенту \
                    высылается сообщение электронную почту.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "DTO validation failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Credit application denied", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Statement was not found", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/statement/registration/{statementId}")
    void finishRegistration(@Parameter(name = "statementId", description = "UUID of the statement",
                                       example = "f43fc0a7-d98b-4aff-8af7-f42ce739a9cd", required = true)
                            @PathVariable("statementId") UUID statementId,
                            @RequestBody @Valid FinishRegistrationRequestDto request);

    @Operation(
            summary = "Заявка на формирование документов",
            description = """
                    На вход получаем уникальный идентификационный номер заявки. Отправляем запрос на \
                    MS-Deal на эндпоинт /deal/document/{statementId}/send - запрос на то, чтоб клиенту отправили \
                    кредитные документы (этим займётся MS-Dossier).\r\n
                    Если заявки с указанным UUID не существует, то получим 404, если кредит уже был отклонён, то \
                    получим 403.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Statement was not found", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/document/{statementId}")
    void createDocuments(@Parameter(name = "statementId", description = "UUID of the statement",
                                    example = "f43fc0a7-d98b-4aff-8af7-f42ce739a9cd", required = true)
                         @PathVariable("statementId") UUID statementId);

    @Operation(
            summary = "Запрос на подпись кредитных документов",
            description = """
                    Получаем UUID заявки, если такая есть, то генерируется SES-код и сохраняется в заявку и \
                    отправляется на почту клиенту. Если такой заявки нет или она уже была отклонена получим 404 и \
                    403 соответственно.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Statement was not found", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/document/{statementId}/sign")
    void signDocumentRequest(@Parameter(name = "statementId", description = "UUID of the statement",
                                        example = "f43fc0a7-d98b-4aff-8af7-f42ce739a9cd", required = true)
                             @PathVariable("statementId") UUID statementId);

    @Operation(
            summary = "Подписание документов с помощью SES-кода",
            description = """
                    На вход приходит UUID и код, который ввёл клиент. Если заявка была отклонена ранее, то \
                    получим ошибку с кодом 403, иначе переданный код будет сравниваться с сохранённым в базе данных \
                    и если они не совпадают, то получим ошибку с кодом 403, в случае успеха меняется статус заявки, \
                    сохраняется время подписи документов, меняется статус кредита и отправляется сообщение на \
                    электронную почту клиента.
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "SES-code verification failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @PostMapping("/document/{statementId}/sign/code")
    void verifySesCode(@Parameter(name = "statementId", description = "UUID of the statement",
                                  example = "f43fc0a7-d98b-4aff-8af7-f42ce739a9cd", required = true)
                       @PathVariable("statementId") UUID statementId,
                       @Parameter(name = "code", description = "SES-code for verification",
                                  example = "123456", required = true)
                       @RequestParam("code") String code);

}
