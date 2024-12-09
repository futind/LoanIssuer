package ru.neoflex.msdeal.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import ru.neoflex.msdeal.dto.*;
import ru.neoflex.msdeal.dto.enumeration.ApplicationStatus;
import ru.neoflex.msdeal.exception.CreditDeniedException;
import ru.neoflex.msdeal.model.ClientEntity;
import ru.neoflex.msdeal.model.CreditEntity;
import ru.neoflex.msdeal.model.StatementEntity;
import ru.neoflex.msdeal.service.ClientService;
import ru.neoflex.msdeal.service.CreditService;
import ru.neoflex.msdeal.service.StatementService;

import java.util.List;
import java.util.UUID;

@Tag(name = "ms_deal")
@Slf4j
@RestController
@RequestMapping("/deal")
public class DealController {

    private final ClientService clientService;
    private final StatementService statementService;
    private final CreditService creditService;
    private final RestClient restClient;

    private final String CALCULATOR_SERVICE_PORT = "8080";
    private final String URI_BASE = "http://localhost:";


    public DealController(ClientService clientService,
                          StatementService statementService,
                          CreditService creditService,
                          RestClient restClient) {
        this.clientService = clientService;
        this.statementService = statementService;
        this.creditService = creditService;
        this.restClient = restClient;
    }

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
    public List<LoanOfferDto> getOffers(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на кредит от клиента в виде LoanStatementRequestDto.",
            required = true)
                                            @RequestBody @Valid LoanStatementRequestDto request) {

        log.info("Received a valid request to /deal/statement; amount: {}, term: {}",
                request.getAmount(),
                request.getTerm());

        ClientEntity clientEntity = clientService.createClientWithRequest(request);

        StatementEntity statementEntity = statementService.createStatementWithRequest(clientEntity);

        List<LoanOfferDto> offers;

        log.info("Making a request to {}/calculator/offers...", URI_BASE + CALCULATOR_SERVICE_PORT);
        offers = restClient.post()
                .uri(URI_BASE + CALCULATOR_SERVICE_PORT + "/calculator/offers")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(new ParameterizedTypeReference<List<LoanOfferDto>>() {});

        assert offers != null;

        for (LoanOfferDto offer : offers) {
            offer.setStatementId(statementEntity.getStatementId());
        }

        log.info("Returning loan offers to the client...");

        return offers;
    }

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
    public void select(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Принятые клиентом условия кредита в виде LoanOfferDto.",
            required = true)
                           @RequestBody @Valid LoanOfferDto offer) {
        statementService.setAppliedOffer(offer);
    }


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
    public void finishRegistration(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Дополнительные данные о клиенте, необходимые для скоринга.",
            required = true)
                                       @RequestBody @Valid FinishRegistrationRequestDto request,
                                       @PathVariable("statementId") String statementId)
                                        throws CreditDeniedException, RestClientResponseException {
        UUID statementUUID = UUID.fromString(statementId);

        StatementEntity statementEntity = statementService.findById(statementUUID);
        log.info("Found statement with provided UUID");
        UUID clientUUID = statementEntity.getClient().getClientId();

        clientService.enrichClient(request, clientUUID);
        ScoringDataDto scoringDataDto = statementService.enrichScoringData(request, statementUUID);

        CreditDto creditDto;

        try {
            creditDto = restClient.post()
                    .uri(URI_BASE + CALCULATOR_SERVICE_PORT + "/calculator/calc")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(scoringDataDto)
                    .retrieve()
                    .body(CreditDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                statementService.changeStatementStatus(statementService.findById(statementUUID),
                                                        ApplicationStatus.CC_DENIED);
                log.warn("Credit was denied. Statement status was updated in the database.");
                throw new CreditDeniedException(e.getMessage());
            } else {
                throw e;
            }
        }

        assert creditDto != null;
        CreditEntity creditEntity = creditService.saveCredit(creditDto);

        statementEntity = statementService.setCredit(statementUUID, creditEntity);
        statementService.changeStatementStatus(statementEntity, ApplicationStatus.CC_APPROVED);
    }

}
