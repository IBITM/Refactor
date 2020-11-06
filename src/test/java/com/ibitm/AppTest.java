package com.ibitm;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Unit test for simple App.
 */
public class AppTest {

    public static String expect = "Statement for BigCo\n" +
            " Hamlet: $650.00 (55 seats)\n" +
            " As You Like It: $580.00 (35 seats)\n" +
            " Othello: $500.00 (40 seats)\n" +
            "Amount owed is $1730.00\n" +
            "You earned 47 credits\n";

    /**
     * 单元测试的要求
     */
    @Test
    public void statementTest() {
        String actual = StatementField.statement(StatementField.invoice, StatementField.plays);
        Assert.assertEquals(expect, actual);
    }
}
