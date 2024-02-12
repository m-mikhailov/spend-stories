package com.code.mikhaylov.spendstories.controller;

import com.code.mikhaylov.spendstories.client.PlaidClient;
import com.code.mikhaylov.spendstories.domain.FctBalance;
import com.plaid.client.model.AccountType;
import com.plaid.client.model.AccountsBalanceGetRequest;
import com.plaid.client.request.PlaidApi;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

import static java.util.stream.Collectors.*;

@RestController
@RequestMapping("/spend-stories")
public class SpendStoriesController {

    private final PlaidApi client;

    public SpendStoriesController(PlaidClient client) {
        this.client = client.getPlaidClient();
    }

    private record AccountKey(String accountId, String currencyCode, AccountType type) {
    }

    private record AccountValue(Double available, Double current, Double limit) {
    }

    private Double extractDoubleNullable(Double value) {
        return value != null ? value : 0;
    }

    @GetMapping("/balance/get")
    public List<FctBalance> getBalance(@RequestParam("access-token") String accessToken) throws IOException {
        var request = new AccountsBalanceGetRequest()
                .accessToken(accessToken);

        var response = client.accountsBalanceGet(request)
                .execute();

        //TODO Move into separate report builder class
        return response.body().getAccounts().stream()
                .collect(groupingBy(account -> new AccountKey(
                        account.getAccountId(),
                        account.getBalances().getIsoCurrencyCode(),
                        account.getType()
                ), mapping(account -> new AccountValue(
                        extractDoubleNullable(account.getBalances().getAvailable()),
                        extractDoubleNullable(account.getBalances().getCurrent()),
                        extractDoubleNullable(account.getBalances().getLimit())
                ), toList())))
                .entrySet()
                .stream()
                .map(entry -> {
                    var reportBuilder = FctBalance.builder()
                            .accountId(entry.getKey().accountId);
                    var values = entry.getValue();
                    var availableSum = values.stream().mapToDouble(AccountValue::available)
                            .sum();
                    var currentSum = values.stream().mapToDouble(AccountValue::current)
                            .sum();
                    var limitSum = values.stream().mapToDouble(AccountValue::limit)
                            .sum();
                    switch (entry.getKey().type) {
                        case DEPOSITORY -> {
                            if (availableSum == 0 && currentSum == 0) {
                                reportBuilder.balanceDeposit(0.0);
                            } else if (availableSum == 0 && currentSum > 0) {
                                reportBuilder.balanceDeposit(currentSum);
                            } else {
                                reportBuilder.balanceDeposit(availableSum);
                            }
                        }
                        case CREDIT -> {
                            if (availableSum == 0 && (limitSum - currentSum) == 0) {
                                reportBuilder.balanceCredit(0.0);
                            } else if (availableSum == 0 && (limitSum - currentSum) > 0) {
                                reportBuilder.balanceCredit(limitSum - currentSum);
                            } else {
                                reportBuilder.balanceCredit(availableSum);
                            }

                            if (currentSum == 0 || currentSum < 0) {
                                reportBuilder.creditDebt(0.0);
                            } else {
                                reportBuilder.creditDebt(availableSum);
                            }
                        }
                        case LOAN -> {
                            if (currentSum == 0) {
                                reportBuilder.loanDebt(0.0);
                            } else {
                                reportBuilder.loanDebt(availableSum);
                            }
                        }
                        case INVESTMENT -> {
                            if (availableSum == 0 && currentSum == 0) {
                                reportBuilder.balanceInvest(0.0);
                            } else if (availableSum == 0 && currentSum > 0) {
                                reportBuilder.balanceInvest(currentSum);
                            } else {
                                reportBuilder.balanceInvest(availableSum);
                            }
                        }
                        case OTHER -> {
                            if (availableSum == 0 && currentSum == 0) {
                                reportBuilder.balanceOther(0.0);
                            } else if (availableSum == 0 && currentSum > 0) {
                                reportBuilder.balanceOther(currentSum);
                            } else {
                                reportBuilder.balanceOther(availableSum);
                            }
                        }
                    }
                    return reportBuilder.build();
                }).toList();
    }

}
