package com.ibitm;

import lombok.Data;

import java.io.Serializable;

@Data
class Performance implements Serializable {
    public String playID;
    public int audience;
    public Play play;
    // 一次演出的花销
    public int amount;
    // 一次演出的观众积分
    public int volumeCredits;
}
