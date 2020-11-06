package com.ibitm;

import lombok.Data;

import java.util.Map;

@Data
class StatementDate {
    // Invoice
    public String customer;
    public Performance[] performances;
    //Play
    public Map<String, Play> plays;

    public int totalAmount;
    public int totalVolumeCredits;
}
