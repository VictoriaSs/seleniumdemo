package ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects;

import org.junit.jupiter.api.Assertions;

public class SearchTargetPage extends BasePage{
    public SearchTargetPage() {
        super("Одна из страниц по ссылке из поисковой выдачи", "http://blank");
    }

    @Override
    public void checkPageURLCorrect() {
        log.info("Проверяю, что мы больше не находимся на одной из страниц поисковика Яндекс.");
        Assertions.assertFalse(driver.getCurrentUrl().contains("yandex"));
    }
}
