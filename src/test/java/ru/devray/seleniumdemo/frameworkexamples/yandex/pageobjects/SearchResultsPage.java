package ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;

public class SearchResultsPage extends BasePage{
    public SearchResultsPage() {
        super("Страница с результатами поиска", "https://yandex.ru/search/");
    }

    public void checkResultsCount(int minimum){
        log.info(String.format("Проверяю, что количество поисковых результатов на странице >= %d", minimum));
        driver.waitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//ul[@id='search-result']")));
        List<WebElement> searchResults = driver.findElements(By.xpath("//ul[@id='search-result']/li[not(@data-fast-name)]"));
        Assertions.assertTrue(searchResults.size() >= minimum,
                String.format("Недостаточное количество поисковых результатов, < %d", minimum));
    }

    public void checkResultPagesCount(int minimum){
        log.info(String.format("Проверяю, что количество поисковых страниц с результами >= %d", minimum));
        List<WebElement> searchResultPages = driver.findElements(By.xpath("//div[@class='pager__items']/a"));
        Assertions.assertTrue(searchResultPages.size() >= minimum,
                String.format("Недостаточное количество страниц с результатами, < %d", minimum));
    }

    public void goToResultPageNumber(int pageNumber){
        log.info(String.format("Перехожу на страницу результатов #%d", pageNumber));
        WebElement thirdResultPage = driver.findElement(
                By.xpath(String.format("//div[@class='pager__items']/a[text()='%d']", pageNumber)));
        thirdResultPage.click();
    }

    public void clickSearchResult(int rowNumber){
        log.info(String.format("Кликаю по результату поиска #%d", rowNumber));
        WebElement searchResult = driver.findElement(
                By.xpath(String.format("//ul[@id='search-result']/li[@data-cid='%d']/div/h2/a", rowNumber - 1)));
        searchResult.click();
        log.info("Переключаюсь на вкладку с целевой страницей (найденной через поисковик)");
        driver.switchToNewTab();
    }
}
