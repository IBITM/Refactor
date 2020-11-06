package com.ibitm;

import lombok.Data;

import java.io.Serializable;

@Data
class Invoice implements Serializable {
    public String customer;
    public Performance[] performances;
}
