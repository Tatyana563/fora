package com.example.fora;

import com.example.fora.model.Category;
import com.example.fora.model.City;
import com.example.fora.model.MainGroup;
import com.example.fora.model.Section;
import com.example.fora.repository.*;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SectionParser {
    private static final Logger LOG = LoggerFactory.getLogger(SectionParser.class);

    private static final Set<String> SECTIONS;
    static {
        Set<String> sections = new HashSet<>();
        sections.add("Ноутбуки, компьютеры");
        sections.add("Комплектующие");
        sections.add("Оргтехника");
        sections.add("Смартфоны, планшеты");
        sections.add("Телевизоры, аудио, видео");
        sections.add("Техника для дома");
        sections.add("Техника для кухни");
        sections.add("Фото и видео");
        SECTIONS = Collections.unmodifiableSet(sections);
    }

    private static final String URL = "https://fora.kz/";

    private static final long ONE_SECOND_MS = 1000L;
    private static final long ONE_MINUTE_MS = 60 * ONE_SECOND_MS;
    private static final long ONE_HOUR_MS = 60 * ONE_MINUTE_MS;
    private static final long ONE_DAY_MS = 24 * ONE_HOUR_MS;
    private static final long ONE_WEEK_MS = 7 * ONE_DAY_MS;


    @Value("${fora.api.chunk-size}")
    private Integer chunkSize;
    @Value("${fora.thread-pool.pool-size}")
    private Integer threadPoolSize;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MainGroupRepository mainGroupRepository;
    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private ItemPriceRepository itemPriceRepository;
    @Autowired
    private CityRepository cityRepository;


    @Scheduled(fixedDelay = ONE_WEEK_MS)
    @Transactional
    public void getSections() throws IOException {
        Document newsPage = Jsoup.connect(URL).get();
        LOG.info("Получили главную страницу, ищем секции...");
        Elements sectionElements = newsPage.select("#js-categories-menu>li");
        for (Element sectionElement : sectionElements) {
            Element sectionElementLink = sectionElement.selectFirst(">a");
            String text = sectionElementLink.text();
            if (SECTIONS.contains(text)) {
                LOG.info("Получаем {}...", text);
                //TODO:remove city suffix from url (lastIndexOf('/'))
                String sectionUrl = sectionElementLink.absUrl("href");
                String sectionUrlWithoutCity = URLUtil.removeCityFromUrl(sectionUrl);
                Section section = sectionRepository.findOneByUrl(sectionUrlWithoutCity)
                        .orElseGet(() -> sectionRepository.save(new Section(text, sectionUrlWithoutCity)));
                LOG.info("Получили {}, ищем группы...", text);
                Elements groupsAndCategories = sectionElement.select(".category-submenu li");
                Element currentGroup = null;
                List<Element> categories = new ArrayList<>();
                for (int i = 0; i < groupsAndCategories.size(); i++) {
                    Element element = groupsAndCategories.get(i);
                    if (element.hasClass("parent-category")) {
                        // element is group
                        // 1. process previously found group and categories
                        // 2. reset group and list
                        processGroupWithCategories(section, currentGroup, categories);
                        currentGroup = element;
                        categories.clear();
                    } else {
                        // element is category
                        categories.add(element);
                    }
                }
                processGroupWithCategories(section, currentGroup, categories);
            }
        }
        parseCities(newsPage);
    }

    private void parseCities(Document page) {
        Elements cityElements = page.select(".js-city-select-radio");
        for (Element cityElement : cityElements) {
            String citySuffix = cityElement.attr("data-href").replace("/", "");
            String cityName = cityElement.parent().selectFirst("label > a").text();
//        Elements cityElements = page.select(".js-city-select-radio");
//        for (Element cityElement : cityElements) {
//            String citySuffix = cityElement.attr("data-href").replace("/", "");
//            String cityName = cityElement.parent().selectFirst(".js-city-select-radio+label > a").text();

            if (!cityRepository.existsByUrlSuffix(citySuffix)) {
                cityRepository.save(new City(cityName, citySuffix));
            }
        }

    }

    private void processGroupWithCategories(Section section, Element currentGroup, List<Element> categories) {
        if (currentGroup == null) {
            return;
        }
        Element groupLink = currentGroup.selectFirst(">a");

        String groupUrl = groupLink.absUrl("href");
        String groupText = groupLink.text();
        String groupUrlWithoutCity = URLUtil.removeCityFromUrl(groupUrl);
        LOG.info("Группа  {}", groupText);
        MainGroup group = mainGroupRepository.findOneByUrl(groupUrlWithoutCity)
                .orElseGet(() -> mainGroupRepository.save(new MainGroup(groupText, groupUrlWithoutCity, section)));
        if (categories.isEmpty()) {
            if (!categoryRepository.existsByUrl(groupUrl)) {
                categoryRepository.save(new Category(groupText, groupUrlWithoutCity, group));
            }
        } else {
            for (Element categoryElement : categories) {
                Element categoryLink = categoryElement.selectFirst(">a");
                String categoryUrl = categoryLink.absUrl("href");
                String categoryUrlWithoutCity = URLUtil.removeCityFromUrl(categoryUrl);
                String categoryText = categoryLink.text();
                LOG.info("\tКатегория  {}", categoryText);
                if (!categoryRepository.existsByUrl(categoryUrlWithoutCity)) {
                    categoryRepository.save(new Category(categoryText, categoryUrlWithoutCity, group));
                }

            }
        }
    }


    @Scheduled(initialDelay = 1200, fixedDelay = ONE_WEEK_MS)
    @Transactional
    public void getAdditionalArticleInfo() throws InterruptedException, IOException {
        LOG.info("Получаем дополнитульную информацию о товарe...");
        ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize);

        List<Category> categories;

        // 1. offset + limit
        // 2. page + pageSize
        //   offset = page * pageSize;  limit = pageSize;
        List<City> cities = cityRepository.findAll();
//        categories = categoryRepository.getChunk(PageRequest.of(page++, chunkSize)
        Map<String, String> cookies = new HashMap<>();
        for (int i = 0; i < cities.size(); i++) {
            LOG.info("-------------------------------------");
            LOG.info("Получаем списки товаров для {}", cities.get(i).getUrlSuffix());
            LOG.info("-------------------------------------");
            String urlWithCity = URL + cities.get(i).getUrlSuffix();
            Connection.Response response = Jsoup.connect(urlWithCity)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();
            cookies.putAll(response.cookies());
            int page = 0;
            while (!(categories = categoryRepository.getChunk(PageRequest.of(page++, chunkSize))).isEmpty()) {
                LOG.info("Получили из базы {} категорий", categories.size());
                CountDownLatch latch = new CountDownLatch(categories.size());
                for (Category category : categories) {
                    executorService.execute(new ItemsUpdateTask(itemRepository,
                            itemPriceRepository,
                            category,
                            cities.get(i),
                            cookies,
                            latch));

                }

                LOG.info("Задачи запущены, ожидаем завершения выполнения...");
                latch.await();
                LOG.info("Задачи выполнены, следующая порция...");

            }

            //disconnect;
//           try{
//           if ((cities.get(i + 1))!=null) {
//               Jsoup.connect(URL + cities.get(i + 1).getUrlSuffix()).method(Connection.Method.GET).execute();}
//           }
//           catch(Exception e){
//
//           };
        }
        executorService.shutdown();
    }
}

