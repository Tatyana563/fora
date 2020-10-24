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

        Document itemPage = Jsoup.connect("https://fora.kz/catalog/tehnika-dla-doma/osvesenie/svetilniki/karaganda").get();
        Elements itemElements = itemPage.select(".catalog-list-item:not(.injectable-banner)");
        for (Element itemElement : itemElements) {
            String itemPhoto = itemElement.selectFirst(".image img").absUrl("src");
            Element itemLink = itemElement.selectFirst(".item-info>a");
            String itemUrl = itemLink.absUrl("href");
            String itemText = itemLink.text();

            String itemPrice = itemElement.selectFirst(".price").text();
            String price = null;
            Matcher priceMatcher = PRICE_PATTERN.matcher(itemPrice);
            if (priceMatcher.find()) {
                price = priceMatcher.group(0).replaceAll("\\s*", "");
                ;
            }
            String itemDescription = itemElement.selectFirst(".list-unstyled").text();

        }
    }
}
