package com.example.fora;


import com.example.fora.model.Category;
import com.example.fora.model.City;
import com.example.fora.model.Item;
import com.example.fora.model.ItemPrice;
import com.example.fora.repository.ItemPriceRepository;
import com.example.fora.repository.ItemRepository;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemsUpdateTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ItemsUpdateTask.class);
    private static final String PAGE_URL_FORMAT = "?sort=views&page=%d";
    private static final Integer NUMBER_OF_PRODUCTS_PER_PAGE = 18;
    private static final Pattern PATTERN = Pattern.compile("Артикул:\\s*(\\S*)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(^([0-9]+\\s*)*)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+)");
    private static final int ONE_MINUTE_MS = 60 * 1000;

    private final ItemRepository itemRepository;
    private final ItemPriceRepository itemPriceRepository;
    private final Category category;
    private final City city;
    private final Map<String, String> cookies;
    private final CountDownLatch latch;

    public ItemsUpdateTask(ItemRepository itemRepository, ItemPriceRepository itemPriceRepository, Category category, City city, Map<String, String> cookies, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.itemPriceRepository = itemPriceRepository;
        this.category = category;
        this.city = city;
        this.cookies = cookies;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            LOG.warn("Начиначем обработку категории '{}'", category.getName());
            String pageUrlFormat = category.getUrl() + PAGE_URL_FORMAT;
            String firstPageUrl = String.format(pageUrlFormat, 1);
            Document firstPage = Jsoup.connect(firstPageUrl)
                    .cookies(cookies)
                    .timeout(ONE_MINUTE_MS)
                    .get();
            if (firstPage != null) {
                int totalPages = getTotalPages(firstPage);
                parseItems(firstPage);
                for (int pageNumber = 2; pageNumber <= totalPages; pageNumber++) {
                    LOG.info("Получаем список товаров ({}) - страница {}", category.getName(), pageNumber);
                    Document connect = Jsoup.connect(String.format(pageUrlFormat, pageNumber)).get();
                    parseItems(connect);

                }
            }

        } catch (IOException ioException) {
            LOG.error("Не получилось распарсить категорию", ioException);
        } finally {
            LOG.warn("Обработка категории '{}' завершена", category.getName());
            latch.countDown();
        }
    }

    private int getTotalPages(Document firstPage) {
        Element itemElement = firstPage.selectFirst(".catalog-container");
        if (itemElement != null) {
            int numberOfPages = 0;

            String quantity = itemElement.select(".product-quantity").text();
            Integer amountOfProducts;
            Matcher matcher = QUANTITY_PATTERN.matcher(quantity);
            if (matcher.find()) {
                amountOfProducts = Integer.valueOf(matcher.group(1));

                int main = amountOfProducts / NUMBER_OF_PRODUCTS_PER_PAGE;
                if (main != 0) {
                    if ((amountOfProducts % NUMBER_OF_PRODUCTS_PER_PAGE != 0)) {
                        numberOfPages = main + 1;
                    } else {
                        numberOfPages = main;
                    }
                } else {
                    numberOfPages = 1;
                }
            }
            return numberOfPages;
        } else return 0;
    }


    private void parseItems(Document itemsPage) {
//        if (!isValidCity(itemsPage)) {
//            LOG.error("Используется другой город {}", itemsPage.selectFirst("a.current-city").text());
//            return;
 //       }

        Elements itemElements = itemsPage.select(".catalog-list-item:not(.injectable-banner)");

        for (Element itemElement : itemElements) {
            try {
                parseSingleItem(itemElement);
            } catch (Exception e) {
                LOG.error("Не удалось распарсить продукт", e);
            }

        }
    }

    private boolean isValidCity(Document page) {
        return city.getName().equalsIgnoreCase(page.selectFirst("a.current-city").text());
    }

    private void parseSingleItem(Element itemElement) {
        String itemPhoto = itemElement.selectFirst(".image img").absUrl("src");
        Element itemLink = itemElement.selectFirst(".item-info>a");
        String itemUrl = itemLink.absUrl("href");
        String itemText = itemLink.text();

        String externalCode = URLUtil.extractExternalIdFromUrl(itemUrl);
        if (externalCode != null && externalCode.isEmpty()) {
            LOG.warn("Продукт без кода: {}\n{}", itemText, itemUrl);
            return;
        }

        Item item = itemRepository.findOneByExternalId(externalCode).orElseGet(() -> new Item(externalCode));

        String itemDescription = itemElement.selectFirst(".list-unstyled").text();
        Matcher matcher = PATTERN.matcher(itemDescription);
        if (matcher.find()) {
            String itemCode = matcher.group(1);
            item.setCode(itemCode);
        }

        item.setModel(itemText);
        item.setImage(itemPhoto);
        item.setDescription(itemDescription);
        //TODO: remove city from url
        String itemUrlWithoutCity = URLUtil.removeCityFromUrl(itemUrl);
        item.setUrl(itemUrlWithoutCity);
        item.setCategory(category);
        itemRepository.save(item);

        String itemPriceString = itemElement.selectFirst(".price").text();
        Matcher priceMatcher = PRICE_PATTERN.matcher(itemPriceString);
        if (priceMatcher.find()) {
            String price = priceMatcher.group(0).replaceAll("\\s*", "");

            ItemPrice itemPrice = itemPriceRepository.findOneByItemAndCity(item, city).orElseGet(() -> {
                ItemPrice newItemPrice = new ItemPrice();
                newItemPrice.setItem(item);
                newItemPrice.setCity(city);
                return newItemPrice;
            });

            itemPrice.setPrice(Double.valueOf(price));
            itemPriceRepository.save(itemPrice);
        }
    }

}





