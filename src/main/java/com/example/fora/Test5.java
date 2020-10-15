package com.example.fora;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Test5 {
    public static void main(String[] args) throws IOException {
        Document page = Jsoup.connect("https://fora.kz/").get();
        Elements sectionElements = page.select(".js-city-select-radio");
        for (Element itemElement : sectionElements) {
            String cityUrl = itemElement.attr("data-href").replace("/", "");

        }
    }
}
