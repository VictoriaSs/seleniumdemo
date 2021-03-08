package ru.devray.seleniumdemo.frameworkexamples.yandex.pageobjects;

import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

/**
 * Главная страница yandex.ru
 * Используемый паттерн - PageObject
 */
public class MainPage extends BasePage{

    public MainPage() {
        super("Главная страница", "https://yandex.ru");
    }

    public enum Service{
        MARKET("market", "Маркет"),
        VIDEO("video", "Видео"),
        IMAGES("images", "Картинки"),
        MAPS("maps", "Карты");

        String elementDataId;
        String menuItemName;

        Service(String elementDataId, String menuItemName) {
            this.elementDataId = elementDataId;
            this.menuItemName = menuItemName;
        }
    }

    public void search(String searchQuery){
        setSearchQuery(searchQuery);
        clickSearchButton();
    }

    public void setSearchQuery(String searchQuery){
        log.info("Ввожу запрос в поисковую строку.");
        WebElement searchField = driver.findElement(By.xpath("//div[@class='search2__input']/span/span/input"));
        searchField.sendKeys(searchQuery);
    }

    public void clickSearchButton(){
        log.info("Кликаю по кнопке 'Найти'.");
        WebElement searchButton = driver.findElement(By.xpath("//div[@class='search2__button']/button"));
        searchButton.click();
    }

    public void checkAllServiceMenuItemsPresent(){
        log.info("Проверяю наличие всех иконок и названий сервисов (над строкой поиска).");
        for(Service service : Service.values()){
            checkServiceMenuItemPresent(service);
        }
    }

    public void checkServiceMenuItemPresent(Service service){
        log.info(String.format("Проверяю наличие иконки и названия сервиса '%s'", service.menuItemName));
        checkServiceMenuItemPresent(service.elementDataId, service.menuItemName);
    }

    private void checkServiceMenuItemPresent(String elementDataId, String expectedMenuItemName){
        WebElement menuOption = driver.findElement(By.xpath(String.format("//a[@data-id='%s']", elementDataId)));
        WebElement menuOptionLogo = menuOption.findElement(By.xpath("div[@class='services-new__icon']"));
        WebElement menuOptionText = menuOption.findElement(By.xpath("div[@class='services-new__item-title']"));

        Assertions.assertEquals(menuOptionText.getText(),
                expectedMenuItemName,
                String.format("Название элемента меню '%s' не соответствует ожидаемому!", expectedMenuItemName));
        Assertions.assertNotNull(menuOptionLogo);
    }
}
