package com.example.fora;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
    private static final Pattern PATTERN = Pattern.compile("(.*)(Артикул:)(\\s*)(\\S*)(\\s*)(\\.*)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d*\\s*\\d*\\s*\\d*\\s*\\d*\\s*\\d*\\s*\\d*)");
    //private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\D*)(\\s*)(\\D*)(\\d*)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+)");
    private static final Integer PRODUCTS_PER_PAGE = 18;

    public static void main(String[] args) throws IOException {


        Document itemPage = Jsoup.connect("https://fora.kz/catalog/smartfony-plansety/smartfony/karaganda").get();
        Elements itemElements = itemPage.select(".catalog-container");
        for (Element itemElement : itemElements) {
            String quantity = itemElement.select(".product-quantity").text();
            Integer amountOfProducts;
            Integer numberfPages;
            Integer numberOfProductsPerPage = 18;
            Matcher matcher = QUANTITY_PATTERN.matcher(quantity);
            if (matcher.find()) {
                amountOfProducts = Integer.valueOf(matcher.group(1));
                amountOfProducts = 200;
                int main = amountOfProducts / 18;

                int tail = amountOfProducts % 18;
                if ((amountOfProducts % numberOfProductsPerPage != 0)) {
                    numberfPages = main + 1;
                }
            }
        }

    }
}


