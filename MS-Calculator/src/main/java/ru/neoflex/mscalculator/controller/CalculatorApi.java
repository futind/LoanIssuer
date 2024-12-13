package ru.neoflex.mscalculator.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.neoflex.mscalculator.dto.CreditDto;
import ru.neoflex.mscalculator.dto.LoanOfferDto;
import ru.neoflex.mscalculator.dto.LoanStatementRequestDto;
import ru.neoflex.mscalculator.dto.ScoringDataDto;
import ru.neoflex.mscalculator.exception.CreditDeniedException;

import java.util.List;

@RestController
@RequestMapping("/calculator")
public interface CalculatorApi {

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json", array =
                    @ArraySchema(schema = @Schema(implementation = LoanOfferDto.class)))
            }),
            @ApiResponse(responseCode = "400", description = "Dto validation failed", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Расчёт возможных условий кредита.",
            description = """
                    На вход получает LoanStatementRequestDto, он валидируется.\r\n
                    Если поля заполнены корректно происходит расчёт четырёх кредитных предложений \
                    на основании всех возможных комбинаций булевых полей \
                    isInsuranceEnabled и isSalaryClient.\r\n
                    Ответ на запрос - список из четырёх LoanOfferDto, которые сортируются \
                    по мере уменьшения процентной ставки."""
    )
    @PostMapping(path = "/offers")
    List<LoanOfferDto> getOffers(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Заявка на кредит от клиента в виде LoanStatementRequestDto.",
            required = true)
                                        @RequestBody
                                        @Valid LoanStatementRequestDto loanStatementRequestDto);

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK", content = {
                    @Content(mediaType = "application/json", schema =
                    @Schema(implementation = CreditDto.class))
            }),
            @ApiResponse(responseCode = "400", description = "Dto validation failed", content = {
                    @Content(mediaType = "application/json")
            }),
            @ApiResponse(responseCode = "403", description = "Credit application denied", content = {
                    @Content(mediaType = "application/json")
            })
    })
    @Operation(
            summary = "Полный расчёт параметров кредита.",
            description = """
                    На вход получает ScoringDataDto, он валидируется.\r\n
                    Происходит проверка кредитоспособности клиента. \r\n
                    Если клиент проходит её, то \
                    происходит полный расчёт параметров кредита: \
                    рассчитывается итоговая процентная ставка, полная стоимость кредита и \
                    ежемесячный платёж. На основе вычисленных данных создаётся график ежемесячных платежей, \
                    который представлен в виде списка из PaymentScheduleElementDto.\r\n
                    Если же клиент не прошёл проверку, то выбрасывается CreditDeniedException.\r\n
                    Ответ на запрос - CreditDto, насыщенный всеми рассчитанными параметрами."""
    )
    @PostMapping(path = "/calc")
    CreditDto calculateCredit(@io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                    Наиболее полные данные о клиенте, необходимые для расчёта
                    условий кредита, представленные в виде ScoringDataDto.""",
            required = true)
                                     @RequestBody @Valid ScoringDataDto scoringDataDto)
                                     throws CreditDeniedException;
}
