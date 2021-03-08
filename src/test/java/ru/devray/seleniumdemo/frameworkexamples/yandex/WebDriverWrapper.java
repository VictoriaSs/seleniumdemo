package ru.devray.seleniumdemo.frameworkexamples.yandex;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;
import java.util.Set;

/**
 * Класс-обертка над драйвером.
 * Служит для "украшения", допиливания, доведения до ума - в общем, называйте это как хотите -
 * - корневого функционала WebDriver.
 *
 * Используемые паттерны: Singleton, Decorator
 */
public class WebDriverWrapper {

    private static Logger log = LogManager.getRootLogger();

    private ChromeDriver driver = null;
    private WebDriverWait wait = null;
    boolean colorElements = false;

    private static WebDriverWrapper instance = null;

    /**
     * WebDriverWrapper построен в соответствии с паттерном Singleton (Одиночка).
     * Всегда на все классы будет доступен единственный экземпляр WebDriverWrapper.
     * Экземпляр хранится в поле instance, конструктор специально объявлен приватным.
     * @return
     */
    public static WebDriverWrapper getInstance() {
        if (instance == null) {
            instance = new WebDriverWrapper();
            log.debug("Создал экземпляр обертки WebDriver (синглтон).");
        }
        return instance;
    }

    private WebDriverWrapper(){
        log.debug("Инициализирую экземпляр обертки WebDriver.");

        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver.exe");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        colorElements = true;

        wait = new WebDriverWait(driver, 5, 200);
    }

    /**
     * Переводит фокус на родительский фрейм.
     * Элементы DOM-дерева родительского фрейма становятся доступными для взаимодействия.
     */
    public WebDriverWrapper switchToParentFrame() {
        log.debug("Переключаюсь на родительский фрейм.");
        driver.switchTo().parentFrame();
        return this;
    }

    /**
     * Переход по заданному адресу URL.
     * @param url
     * @return
     */
    public WebDriverWrapper get(String url) {
        log.debug("Перехожу по адресу [" + url + "]");
        driver.navigate().to(url);
        return this;
    }

    public WebDriverWrapper close(){
        String tabTitle = driver.getTitle();
        log.debug(String.format("Закрываю текущую вкладку [%s]", tabTitle));
        driver.close();
        return this;
    }

    public void quit(){
        log.debug("Закрываю все вкладки и окна, завершаю процесс webdriver.");
        driver.quit();
    }

    /**
     * Поиск элемента по заданному локатору.
     * От своего библиотечного собрата отличается большей надежностью
     * и встроенными механизмами ожидания.
     * @param locator
     * @return
     * @throws TimeoutException
     */
    public WebElement findElement(By locator) throws TimeoutException {
        WebElement element = null;
        int maxCount = 3;
        for (int count = 0; count < maxCount; count++) {
            log.debug(String.format("Пробую найти элемент [%s] - попытка #%d", locator.toString(), count));

            try {
                element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
                element = wait.until(ExpectedConditions.elementToBeClickable(locator));

                //Подсветка на странице найденного нами элемента
                if (colorElements) {
                    driver.executeScript("arguments[0]['style']['backgroundColor']='darksalmon';", element);
                }

                log.debug("Элемент найден.");
                return element;
            } catch (StaleElementReferenceException e) {
                log.debug("DOM-дерево изменилось. Искомый объект устарел.");
                //продолжаем попытки поиска
            } catch (WebDriverException e) {
                log.debug("Элемент не был найден.");
            }
        }
        log.error("Элемент не был найден.");
        throw new NoSuchElementException("Элемент не был найден.");
    }

    //TODO improve to have more flexible search strategy, like the 'findElement' one.
    public List<WebElement> findElements(By locator){
        log.debug(String.format("Пытаюсь найти все элементы, подходящие по локатору '%s'", locator.toString()));
        return driver.findElements(locator);
    }

    public String getCurrentUrl(){
        log.debug("Определяю текущий URL страницы..");
        return driver.getCurrentUrl();
    }

    //TODO switch to tab by tab name/partial name
    public void switchToNewTab(){
        log.debug("Переключаюсь на новую вкладку.");
        Set<String> browserTabIds = driver.getWindowHandles();
        String currentTabId = driver.getWindowHandle();
        String newTab = browserTabIds.stream().filter(tab -> !tab.equals(currentTabId)).findFirst().get();
        driver.switchTo().window(newTab);
    }

    /**
     * Ожидаем, пока элемент приобретет желаемое состояние.
     * Например - появится в DOM-дереве / станет видимым / активным / кликабельным / ...
     * @param condition
     * @return
     */
    public WebElement waitFor(ExpectedCondition condition){
        WebDriverWait wait = new WebDriverWait(driver, 5, 200);

        for (int i = 0; i < 3; i++){
            try {
                return (WebElement) wait.until(condition);
            } catch (StaleElementReferenceException | NoSuchElementException e){
                continue;
            }
        }

        throw new NoSuchElementException("Элемент так и не был найден.");
    }

    /**
     * Метод для организации пауз или замедления тестового сценария. Используется для отладки.
     * @param milliseconds
     */
    @Deprecated
    public void customWait(int milliseconds){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
