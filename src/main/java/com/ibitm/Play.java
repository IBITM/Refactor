package com.ibitm;

import lombok.Data;

import java.io.Serializable;

@Data
class Play implements Serializable {
    public String name;
    public String type;
}
