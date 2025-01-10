package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.loanissuerlibrary.dto.DocumentDataDto;
import ru.neoflex.loanissuerlibrary.dto.FinishRegistrationRequestDto;
import ru.neoflex.loanissuerlibrary.dto.LoanOfferDto;
import ru.neoflex.loanissuerlibrary.dto.LoanStatementRequestDto;
import ru.neoflex.loanissuerlibrary.exception.CreditDeniedException;
import ru.neoflex.loanissuerlibrary.exception.SesCodeVerificationFailed;
import ru.neoflex.loanissuerlibrary.exception.StatementChangeBlocked;
import ru.neoflex.loanissuerlibrary.exception.StatementNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/deal")
public interface DealApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json", array =
                    @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Расчёт возможных условий кредита",
            description = """
                    По API приходит LoanStatementRequestDto, валидируется. \r\n
                    На основе LoanStatementRequestDto создаётся сущность ClientEntity и сохраняется в БД. \r\n
                    Создаётся Statement со связью на только что созданный Client и сохраняется в БД. \r\n
                    Отправляется POST запрос на /calculator/offers МС Калькулятор через RestClient \r\n
                    Каждому элементу из списка List<LoanOfferDto> присваивается id созданной заявки (Statement) \r\n
                    Ответ на запрос - список из четырёх LoanOfferDto, которые сортируются \
                    по мере уменьшения процентной ставки.".
                    """
    )
    @PostMapping("/statement")
    List<LoanOfferDto> getOffers(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на кредит от клиента в виде LoanStatementRequestDto.",
            required = true)
                                 @RequestBody @Valid LoanStatementRequestDto request);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Сохраняет выбранные клиентом условия кредита в заявку.",
            description = """
                    Приходит LoanOfferDto - выбранные клиентом условия кредита. \r\n
                    По statementId в этом DTO достаётся заявка - StatementEntity. \
                    В ней обновляем статус, историю статусов и устанавливаем принятое \
                    предложение в поле applied_offer. \r\n
                    Сохраняем заявку. Отправляем событие в топик finish-registration. 
                    """
    )
    @PostMapping("/offer/select")
    void select(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Принятые клиентом условия кредита в виде LoanOfferDto.",
            required = true)
                @RequestBody @Valid LoanOfferDto offer) throws StatementNotFoundException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Credit application denied", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Полный расчёт параметров кредита и сохранение его в БД.",
            description = """
                    Нам приходит FinishRegistrationRequestDto содержащий недостающие данные \
                    о клиенте, необходимые для скоринга, и параметр statementId - UUID заявки. \r\n
                    С помощью UUID достаём заявку, и с помощью данных из неё и ClientEntity, что берём \
                    из неё собираем данные для скоринга и отправляем их на /calculator/calc в \
                    MS-Calculator. \r\n
                    Нам вернётся либо CreditDto, из которого мы создадим сущность CreditEntity и положим её \
                    в базу данных со статусом CALCULATED (и заодно положим ссылку на этот кредит в заявку), \
                    далее обновляем статус заявки на CC_APPROVED, обновляем историю, сохраняем заявку;\r\n
                    либо придёт код 403, который значит, что в кредите отказано - в таком случае \
                    обновляем статус заявки на CC_DENIED, обновляем историю и выкидываем также 403.\r\n
                    Если всё в порядке, то отправляет событие в create-documents топик Kafka.
                    """
    )
    @PostMapping("calculate/{statementId}")
    void finishRegistration(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дополнительные данные о клиенте, необходимые для скоринга.",
            required = true)
                            @RequestBody @Valid FinishRegistrationRequestDto request,
                            @PathVariable("statementId") String statementId)
            throws CreditDeniedException, RestClientResponseException,
                   StatementNotFoundException, StatementChangeBlocked;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Credit application denied", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Заявка на формирование документов (и запрос на отправку их клиенту).",
            description = """
                    На вход получаем UUID заявки, если заявка не была отклонена ранее, \
                    то меняем статус заявки на PREPARE_DOCUMENTS, отправляем событие в топик \
                    send-documents с помощью Kafka. MS-Dossier создаёт документы и отправляет на почту \
                    клиенту. Dossier также делает запрос на эндпоинт PUT deal/admin/statement/{statementId/status}, \
                    что меняет статус заявки на DOCUMENTS_CREATED
                    """
    )
    @PostMapping("document/{statementId}/send")
    void sendDocuments(@Parameter(
            name = "statementId",
            description = "UUID of the statement",
            example = "f43fc0a7-d98b-4aff-8af7-f42ce739a9cd",
            required = true
    )
                       @PathVariable("statementId") String statementId) throws StatementNotFoundException,
                                                                               StatementChangeBlocked;


    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Формирование, сохранение SES-кода, а также его передача в MS-Dossier.",
            description = """
                    На вход получаем UUID заявки, если заявка не была отклонена ранее, \
                    то сохраняем SES-код в заявку. \r\n
                    Отправляем событие в топик send-ses с помощью Kafka. MS-Dossier отправляет на почту клиенту \
                    код.
                    """
    )
    @PostMapping("document/{statementId}/sign")
    void signDocuments(@PathVariable("statementId") String statementId) throws StatementChangeBlocked,
                                                                               StatementNotFoundException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "SES-code verification failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Верификация SES-кода, подпись документов.",
            description = """
                    На вход получаем UUID заявки и SES-код. Если заявка не была отклонена ранее, \
                    то сравниваем переданный нам SES-код с сохранённым в заявке. При совпадении \
                    сначала меняем статус заявки на DOCUMENTS_SIGNED, затем меняем статус заявки на \
                    CREDIT_ISSUED и сохраняем время подписи документов. Также меняем статус кредита на \
                    ISSUED. \r\n
                    Отправляем событие в топик credit-issued с помощью Kafka. MS-Dossier отправляет на почту \
                    клиенту подтверждение, что кредит выдан.
                    """
    )
    @PostMapping("document/{statementId}/code")
    void signingCode(@RequestParam String SesCode, @PathVariable("statementId") String statementId)
            throws StatementChangeBlocked, SesCodeVerificationFailed, StatementNotFoundException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Смена статуса заявки на DOCUMENTS_CREATED",
            description = """
                    На вход получаем UUID заявки, если заявка не была отклонена ранее, \
                    то меняем статус заявки на DOCUMENTS_CREATED.
                    """
    )
    @PutMapping("admin/statement/{statementId}/status")
    void documentsCreatedStatusChange(@PathVariable("statementId") String statementId) throws StatementChangeBlocked,
                                                                                              StatementNotFoundException;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json",
                             schema = @Schema(implementation = DocumentDataDto.class))
            }),
            @ApiResponse(responseCode = "404", description = "Entity not found", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Statement change blocked", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Формирование данных для DocumentDataDto.",
            description = """
                    На вход получаем UUID заявки, если заявка не была отклонена ранее, \
                    то на выход подаём данные, необходимые для формирования документов.
                    """
    )
    @GetMapping("document/{statementId}/data")
    DocumentDataDto getDocumentData(@PathVariable("statementId") String statementId) throws StatementChangeBlocked,
                                                                                            StatementNotFoundException;
}
