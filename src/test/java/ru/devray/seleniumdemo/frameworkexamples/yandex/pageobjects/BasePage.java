package ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import ru.devray.seleniumdemo.frameworkexamples.yandex.WebDriverWrapper;

public abstract class BasePage {

    static Logger log = LogManager.getRootLogger();

    protected WebDriverWrapper driver = WebDriverWrapper.getInstance();
    protected String pageName;
    protected String address;

    protected BasePage(String pageName, String address) {
        this.pageName = pageName;
        this.address = address;
    }

    /**
     * Закрывает текущую страницу
     */
    public void close(){
        log.info("Закрываю текущую страницу.");
        driver.close();
    }

    public void open(){
        log.info(String.format("Открываю страницу '%s' по адресу '%s'",
                this.pageName,
                this.address));
        driver.get(this.address);
    }

    /**
     * Дефолтная реализация проверки, что конкретной странице.
     * По сути мы проверяем, что страничный объект, которым мы пользуемся, все еще актуален
     * и совпадает с тем, что мы видим в браузере.
     */
    public void checkPageURLCorrect(){
        Assertions.assertTrue(driver.getCurrentUrl().contains(address));
    }
}
