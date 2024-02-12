package com.code.mikhaylov.spendstories.domain;

import lombok.*;

@Builder
@Getter
@Setter
public class FctBalance {

    private String accountId;
    private Double balanceDeposit;
    private Double balanceCredit;
    private Double creditDebt;
    private Double loanDebt;
    private Double balanceInvest;
    private Double balanceOther;
}
