package com.example.fora;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test2 {
    private static final Pattern PRICE_PATTERN = Pattern.compile("(^([0-9]+\\s*)*)");
    public static void main(String[] args) throws IOException {

        Document itemPage = Jsoup.connect("https://fora.kz/catalog/smartfony-plansety/smartfony/karaganda").get();
        String itemPrice = itemPage.selectFirst(".price").text();
       String text="12355 23j kkkkk";
       // Matcher priceMatcher = PRICE_PATTERN.matcher(itemPrice);
        Matcher priceMatcher = PRICE_PATTERN.matcher(text);
        if (priceMatcher.find()) {
           String price = priceMatcher.group(0);
            System.out.println(price);
        }
    }
}
