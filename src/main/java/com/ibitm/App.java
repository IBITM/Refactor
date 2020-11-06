package com.ibitm;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import lombok.Data;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 需要注意的点:
 * <p>
 * 1. 在 JavaScript 可以嵌套函数，最外层的函数被称为顶层函数
 * 内层函数可以闭包访问外层函数的变量，这一点在 Java 中无法实现（受限于才疏学浅），
 * 这里，使用类变量来实现类似的功能
 * 闭包：内部函数可以访问函数外面的变量
 * 2. JavaScript 中的对象和 Json 基本上是一一对应的，但是相同的逻辑拿到 Java 中就会出现问题
 * 比如，在 plays.json 中
 * {
 * "hamlet": {"name": "Hamlet", "type": "tragedy"},
 * "as-like": {"name": "As You Like It", "type": "comedy"},
 * "othello": {"name": "Othello", "type": "tragedy"}
 * }
 * 很明显 hamlet、as-like、othello 不能作为类的成员变量（应该作为某个成员变量的取值）
 * 所以，要么修改 json 要么使用其他结构存储，这边是使用 Map<String,Play> 来存储 plays.json 中的内容。
 **/


// statement 顶级域
class StatementField {
    public static Map<String, Play> plays;
    public static Invoice invoice;

    private static String readFile(String strFile) throws IOException {
        InputStream is = StatementField.class.getResourceAsStream(strFile);
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        return new String(bytes);
    }

    static {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            invoice = objectMapper.readValue(readFile("/invoices.json"), Invoice.class);
            plays = new HashMap<>();
            // 使用 tree 模型来操作 json 对象
            JsonNode node = objectMapper.readTree(readFile("/plays.json"));
            for (Iterator<String> it = node.fieldNames(); it.hasNext(); ) {
                String fieldName = it.next();
                plays.put(fieldName, objectMapper.readValue(node.get(fieldName).toString(), Play.class));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void enrichPerFormance(Performance[] performances) {
        for (Performance performance : performances) {
            performance.play = playFor(performance);
            performance.amount = amountFor(performance);
            performance.volumeCredits = volumeCreditsFor(performance);
        }
    }

    public static String statement(Invoice invoice, Map<String, Play> plays) {
        return RenderPlainTextField.renderPlainText(createStatementData(invoice));
    }

    private static StatementDate createStatementData(Invoice invoice) {
        StatementDate statementData = new StatementDate();
        statementData.customer = invoice.customer;
        // 深拷贝
        statementData.performances = invoice.performances.clone();
        // 添加额外属性到 Performance 中
        enrichPerFormance(statementData.performances);

        statementData.totalAmount = totalAmount(statementData);
        statementData.totalVolumeCredits = totalVolumeCredits(statementData);
        return statementData;
    }

    private static Play playFor(Performance performance) {
        return plays.get(performance.playID);
    }

    private static int amountFor(Performance performance) {
        int thisAmount = 0;
        switch (performance.play.type) {
            case "tragedy":
                thisAmount = 40000;
                if (performance.audience > 30)
                    thisAmount += 1000 * (performance.audience - 30);
                break;
            case "comedy":
                thisAmount = 30000;
                if (performance.audience > 20) {
                    thisAmount += 10000 + 500 * (performance.audience - 20);
                }
                thisAmount += 300 * performance.audience;
                break;
            default:
                throw new Error(String.format("unknown type %s", performance.play.type));
        }
        return thisAmount;
    }

    // 每一次演出 performance 都会有一定的观众积分
    public static int volumeCreditsFor(Performance performance) {
        int volumeCredits = 0;
        volumeCredits += Math.max(performance.audience - 30, 0);
        if ("comedy".equals(performance.play.type))
            volumeCredits += Math.floor(performance.audience / 5);
        return volumeCredits;
    }

    private static int totalAmount(StatementDate statementData) {
        int totalAmount = 0;
        for (Performance performance : statementData.performances) {
            totalAmount += performance.amount;
        }
        return totalAmount;
    }

    private static int totalVolumeCredits(StatementDate statementData) {
        int volumeCredits = 0;
        for (Performance performance : statementData.performances) {
            volumeCredits += performance.volumeCredits;
        }
        return volumeCredits;
    }
}


class RenderPlainTextField {

    public static StatementDate statementData;

    public static String renderPlainText(StatementDate data) {
        // 为了实现顶级域，比较 trick 的做法
        statementData = data;

        StringBuilder result = new StringBuilder(String.format("Statement for %s\n", data.customer));
        for (Performance performance : data.performances) {
            result.append(String.format(" %s: %s (%d seats)\n", performance.play.name, format(performance.amount), performance.audience));
        }
        result.append(String.format("Amount owed is %s\n", format(data.totalAmount)));
        result.append(String.format("You earned %d credits\n", data.totalVolumeCredits));
        return result.toString();
    }

    private static String format(double amount) {
        return String.format("$%.2f", amount / 100);
    }
}


