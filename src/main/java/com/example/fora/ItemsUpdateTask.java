package com.example.fora;


import com.example.fora.model.Category;
import com.example.fora.model.City;
import com.example.fora.model.Item;
import com.example.fora.repository.CityRepository;
import com.example.fora.repository.ItemRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ItemsUpdateTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ItemsUpdateTask.class);

    private final ItemRepository itemRepository;
    private final Category category;
    //TODO: make decision single task -> single city or List.
    private final List<City> cities;
    private final CountDownLatch latch;
    @Autowired
    private CityRepository cityRepository;

    private static final String PAGE_URL_CONSTANT = "?sort=views&page=%d";
    private static final Integer NUMBER_OF_PRODUCTS_PER_PAGE = 18;
    private static final Pattern PATTERN = Pattern.compile("Артикул:\\s*(\\S*)");
    private static final Pattern PRICE_PATTERN = Pattern.compile("(^([0-9]+\\s*)*)");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("(\\d+)");

    public ItemsUpdateTask(ItemRepository itemRepository, Category category, List<City> cities, CountDownLatch latch) {
        this.itemRepository = itemRepository;
        this.category = category;
        this.cities = cities;
        this.latch = latch;
    }


    @Override
    public void run() {
        try {
          //  itemRepository.resetItemAvailability(category);
            String categoryUrl = category.getUrl();
            //TODO: iterate over cities
            //TODO: categoryUrl + city + page params
        List<String> allCities= cityRepository.getAllCities();
            for(int i=0;i<allCities.size();i++) {


                String firstPageUrl = String.format(categoryUrl +allCities.get(i)+ PAGE_URL_CONSTANT, 1);

                Document firstPage = Jsoup.connect(firstPageUrl).get();
                if (firstPage != null) {
                    int totalPages = getTotalPages(firstPage);
                    parseItems(firstPage);
                    for (int j = 2; j <= totalPages; j++) {
                        LOG.info("Получаем список товаров ({}) - страница {}", category.getName(), i);
                        parseItems(Jsoup.connect(String.format(categoryUrl + PAGE_URL_CONSTANT, i)).get());

                    }
                }
            }

        }catch (IOException e) {
            e.printStackTrace();
        } finally {
            latch.countDown();
        }
    }

    private int getTotalPages(Document firstPage) {
        Element itemElement = firstPage.selectFirst(".catalog-container");
        if (itemElement != null) {
            Integer numberofPages = null;

            String quantity = itemElement.select(".product-quantity").text();
            Integer amountOfProducts;
            Matcher matcher = QUANTITY_PATTERN.matcher(quantity);
            if (matcher.find()) {
                amountOfProducts = Integer.valueOf(matcher.group(1));

                int main = amountOfProducts / NUMBER_OF_PRODUCTS_PER_PAGE;
                if (main != 0) {
                    if ((amountOfProducts % NUMBER_OF_PRODUCTS_PER_PAGE != 0)) {
                        numberofPages = main + 1;
                    } else {
                        numberofPages = main;
                    }
                } else {
                    numberofPages = 1;
                }
            }
            return numberofPages;
        } else return 0;
    }


    private void parseItems(Document itemPage) {

        Elements itemElements = itemPage.select(".catalog-list-item:not(.injectable-banner)");
        for (Element itemElement : itemElements) {
            try {
                parseSingleItem(itemElement);
            } catch (Exception e) {
                LOG.error("Не удалось распарсить продукт", e);
            }

        }
    }

    private void parseSingleItem(Element itemElement) {
        String itemPhoto = itemElement.selectFirst(".image img").absUrl("src");
        Element itemLink = itemElement.selectFirst(".item-info>a");
        String itemUrl = itemLink.absUrl("href");
        String itemText = itemLink.text();

        String externalCode = getProductExternalId(itemUrl);
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

//        String itemPrice = itemElement.selectFirst(".price").text();
//        Matcher priceMatcher = PRICE_PATTERN.matcher(itemPrice);
//        if (priceMatcher.find()) {
//            String price = priceMatcher.group(0).replaceAll("\\s*", "");
//            item.setPrice(Double.valueOf(price));
//        }

        item.setModel(itemText);
        item.setImage(itemPhoto);
        item.setDescription(itemDescription);
        item.setUrl(itemUrl);
        item.setCategory(category);
        itemRepository.save(item);

        //TODO: save cityItemPrice (get by city and item, or create new one)
    }

    private String getProductExternalId(String itemUrl) {
        //TODO: parse url to get externalId
        //https://fora.kz/catalog/smartfony-plansety/smartfony/samsung-galaxy-a01-core-red_616857/karaganda
        int index1 = itemUrl.lastIndexOf("_");
        int index2 = itemUrl.lastIndexOf("/");
        String substring = itemUrl.substring(index1 + 1, index2);
        return substring;

    }

}





