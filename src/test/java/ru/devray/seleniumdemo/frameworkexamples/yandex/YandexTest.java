package ru.devray.seleniumdemo.frameworkexamples.yandex;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects.MainPage;
import ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects.SearchResultsPage;
import ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects.SearchTargetPage;

public class YandexTest {

    MainPage mainPage = new MainPage();
    SearchResultsPage searchResultsPage = new SearchResultsPage();
    SearchTargetPage searchTargetPage = new SearchTargetPage();

    /**
     * Тестовый сценарий для поисковика Яндекс.
     * 1. Открыть страницу yandex.ru
     * 2. Проверить, отображаются ли над поисковой строкой кнопки "Маркет", "Видео", "Картинки", "Карты"
     * (проверяется наличие элементов логотип + текст).
     * 3. Ввести в поле поиска запрос "porsche 356B 1:18 model"
     * 4. Нажать кнопку найти
     * 5. Проверить, что по данному поисковому запросу получено как минимум два результата
     * 6. Проверить, что по данному поисковому запросу есть как минимум 3 страницы результатов
     * 7. Перейти на 3-ю страницу результатов
     * 8. Проверить, что мы все еще находимся в поисковике Яндекс (URL)
     * 9. Открыть 2-й поисковый результат в выдаче на данной странице
     * 10. Убедиться что произошел переход со страницы поисковика на целевую страницу
     */
    @Test
    void testSearch(){
        mainPage.open(); //1
        mainPage.checkAllServiceMenuItemsPresent(); //2
        mainPage.setSearchQuery("porsche 356B 1:18 model"); //3
        mainPage.clickSearchButton(); //4
        searchResultsPage.checkResultsCount(2); //5
        searchResultsPage.checkResultPagesCount(3); //6
        searchResultsPage.goToResultPageNumber(3); //7
        searchResultsPage.checkPageURLCorrect(); //8
        searchResultsPage.clickSearchResult(2); //9
        searchTargetPage.checkPageURLCorrect(); //10
    }

    @AfterEach
    void tearDown(){
        WebDriverWrapper.getInstance().quit();
    }
}
