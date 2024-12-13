package ru.neoflex.mscalculator.util;

import ru.neoflex.mscalculator.dto.LoanOfferDto;

import java.util.Comparator;

public class RateComparator implements Comparator<LoanOfferDto> {
    @Override
    public int compare(LoanOfferDto o1, LoanOfferDto o2) {
        return o1.getRate().compareTo(o2.getRate());
    }
}