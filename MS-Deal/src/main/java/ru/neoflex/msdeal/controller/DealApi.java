package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.msdeal.dto.FinishRegistrationRequestDto;
import ru.neoflex.msdeal.dto.LoanOfferDto;
import ru.neoflex.msdeal.dto.LoanStatementRequestDto;
import ru.neoflex.msdeal.exception.CreditDeniedException;
import ru.neoflex.msdeal.exception.StatementNotFoundException;

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
                    Сохраняем заявку.
                    """
    )
    @PostMapping("/offer/select")
    void select(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Принятые клиентом условия кредита в виде LoanOfferDto.",
            required = true)
                       @RequestBody @Valid LoanOfferDto offer);

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
                    обновляем статус заявки на CC_DENIED, обновляем историю и выкидываем также 403.
                    """
    )
    @PostMapping("calculate/{statementId}")
    void finishRegistration(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дополнительные данные о клиенте, необходимые для скоринга.",
            required = true)
                                   @RequestBody @Valid FinishRegistrationRequestDto request,
                                   @PathVariable("statementId") String statementId)
            throws CreditDeniedException, RestClientResponseException, StatementNotFoundException;

}
