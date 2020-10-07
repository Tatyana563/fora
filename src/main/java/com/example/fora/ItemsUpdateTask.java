package com.example.fora;


import com.example.fora.model.Category;
import com.example.fora.model.Item;
import com.example.fora.repository.ItemRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemsUpdateTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ItemsUpdateTask.class);

    private final ItemRepository itemRepository;
    private final Category category;
    private final CountDownLatch latch;


    private static final String PAGE_URL_CONSTANT = "?sort=views&page=%d";
    private static final Integer numberOfProductsPerPage = 18;
    private static final Pattern PATTERN = Pattern.compile("Артикул:\\s*(\\S*)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(\\d*\\s*\\d*\\s*\\d*\\s*\\d*\\s*\\d*\\s*\\d*)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\D*)(\\s*)(\\D*)(\\d*)");

    public ItemsUpdateTask(ItemRepository itemRepository, Category category, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.category = category;
        this.latch = latch;
    }


    @Override
    public void run() {
        try {
            itemRepository.resetItemAvailability(category);
            String categoryUrl = category.getUrl();
            String firstPageUrl = String.format(categoryUrl + PAGE_URL_CONSTANT, 1);

            Document firstPage = Jsoup.connect(firstPageUrl).get();

            int totalPages = getTotalPages(firstPage);
            parseItems(firstPage);
            for (int i = 2; i <= totalPages; i++) {
                LOG.info("Получаем список товаров - страница {}", i);
                parseItems(Jsoup.connect(String.format(categoryUrl + PAGE_URL_CONSTANT, i)).get());

            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private int getTotalPages(Document firstPage) {
        //TODO: complete implementation
        Elements itemElements = firstPage.select(".catalog-container");
        Integer numberofPages = null;
        for (Element itemElement : itemElements) {
            String quantity = itemElement.select(".product-quantity").text();
            Integer amountOfProducts;


            Matcher matcher = QUANTITY_PATTERN.matcher(quantity);
            if (matcher.find()) {
                amountOfProducts = Integer.valueOf(matcher.group(4));

                int main = amountOfProducts / numberOfProductsPerPage;
                if (main != 0) {
                    if ((amountOfProducts % numberOfProductsPerPage != 0)) {
                        numberofPages = main + 1;
                    } else {
                        numberofPages = main;
                    }
                } else {
                    numberofPages = 1;
                }
            }

        }
        return numberofPages;
    }


    private void parseItems(Document itemPage) {

        Elements itemElements = itemPage.select(".catalog-list-item:not(.injectable-banner)");
        for (Element itemElement : itemElements) {
            String itemPhoto = itemElement.selectFirst(".image img").absUrl("src");
            Element itemLink = itemElement.selectFirst(".item-info>a");
            String itemUrl = itemLink.absUrl("href");
            String itemText = itemLink.text();

            //TODO: use regex to find price without currency.
            String itemPrice = itemElement.selectFirst(".price").text();
            String price = null;
            Matcher priceMatcher = PRICE_PATTERN.matcher(itemPrice);
            if (priceMatcher.find()) {
                price = priceMatcher.group(1).replaceAll("\\s*", "");
            }
            String itemDescription = itemElement.selectFirst(".list-unstyled").text();
            Matcher matcher = PATTERN.matcher(itemDescription);
            if (matcher.find()) {
                String itemCode = matcher.group(1);

                Item item = itemRepository.findOneByCode(itemCode).orElseGet(() -> new Item(itemCode));

                item.setModel(itemText);
                item.setImage(itemPhoto);
                item.setDescription(itemDescription);
                item.setPrice(Double.valueOf(price));
                item.setUrl(itemUrl);
                item.setAvailable(true);
                item.setCategory(category);
                itemRepository.save(item);
            }
        }
    }
}





