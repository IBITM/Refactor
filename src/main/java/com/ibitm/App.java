package com.ibitm;

import java.awt.*;
import java.io.*;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import com.fasterxml.jackson.databind.ObjectMapper;

class App {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Invoice invoice = objectMapper.readValue(readFile("/invoices.json"), Invoice.class);
        Map<String, Play> plays = new HashMap<>();
        JsonNode node = objectMapper.readTree(readFile("/test.json"));
        for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
            String fieldName = it.next();
            plays.put(fieldName, objectMapper.readValue(node.get(fieldName).toString(), Play.class));
        }
        statement(invoice, plays);
    }


    public static void statement(Invoice invoice, Map<String, Play> plays) {
        int totalAmount = 0;
        int volumeCredits = 0;
        StringBuilder result = new StringBuilder(String.format("Statement for %s\n", invoice.customer));
        for (Performance perf : invoice.performances) {
            Play play = plays.get(perf.playID);
            int thisAmount = 0;
            switch (play.type) {
                case "tragedy":
                    thisAmount = 40000;
                    if (perf.audience > 30)
                        thisAmount += 1000 * (perf.audience - 30);
                    break;
                case "comedy":
                    thisAmount = 30000;
                    if (perf.audience > 20) {
                        thisAmount += 10000 + 500 * (perf.audience - 20);
                    }
                    thisAmount += 300 * perf.audience;
                    break;
                default:
                    throw new Error(String.format("unknown type %s", play.type));
            }
            volumeCredits += Math.max(perf.audience - 30, 0);
            if ("comedy".equals(play.type))
                volumeCredits += Math.floor(perf.audience / 5);
            result.append(String.format(" %s: %s (%d seats)\n", play.name, format(thisAmount / 100), perf.audience));
            totalAmount += thisAmount;
        }
        result.append(String.format("Amount owed is %s\n", format(totalAmount / 100)));
        result.append(String.format("You earned %d credits\n", volumeCredits));
        System.out.println(result.toString());
    }

    public static String format(double amount) {
        return String.format("$%.2f", amount);
    }

    public static String readFile(String strFile) throws IOException {
        InputStream is = App.class.getResourceAsStream(strFile);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        return new String(bytes);
    }
}

@Data
class Invoice implements Serializable {
    public String customer;
    public Performance[] performances;
}

@Data
class Performance implements Serializable {
    public String playID;
    public int audience;
}

@Data
class Play implements Serializable {
    public String name;
    public String type;
}