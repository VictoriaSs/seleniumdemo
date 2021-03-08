package ru.devray.seleniumdemo.simpleexamples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import javax.print.DocFlavor;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SimpleTest {

    public ChromeDriver driver;

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
    void myCurrentTest(){
        //Класс для хранения настроек и опций запуска нашего экземпляра хром бразузера.
        //Будет передан параметров в конструктор самого ChromeDriver
        ChromeOptions options = new ChromeOptions();
        //Данная опция отключает верхнюю информационную плашку с надписью
        //"Chrome is being controlled by automated test software"
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

        System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver.exe");
        driver = new ChromeDriver(options);
        driver.manage().window().maximize();
        driver.manage().deleteAllCookies();

        //Шаг 1. Открыть страницу yandex.ru
        driver.get("https://yandex.ru");

        //Шаг 2.
        //Находим элемент Маркет
        checkMenuItemPresent("market", "Маркет");
        //Находим элемент Видео
        checkMenuItemPresent("video", "Видео");
        //Находим элемент Картинки
        checkMenuItemPresent("images", "Картинки");
        //Находим элемент Карты
        checkMenuItemPresent("maps", "Карты");

        //Шаг 3.
        String searchQuery = "porsche 356B 1:18 model";

        WebElement searchField = driver.findElement(By.xpath("//div[@class='search2__input']/span/span/input"));
        searchField.sendKeys(searchQuery);

        //Шаг 4.
        //Чтобы отправить введенный поисковый запрос есть и другой вариант -
        //в поле запроса нажать клавишу Enter.
        //для этого можно просто модифицировать searchQuery и дописать символ \n
        //String searchQuery = "porsche 356B 1:18 model \n";
        //либо передать нажатие клавиши Enter более явно, отдельным вызовом
        //searchField.sendKeys(Keys.ENTER);
        //Но здесь мы сделаем все по классике и найдем кнопку поиска для дальнейшего клика
        WebElement searchButton = driver.findElement(By.xpath("//div[@class='search2__button']/button"));
        searchButton.click();

        //Шаг 5.
        //Поскольку результаты поисковой выдачи - динамически запрашиваемые и подгружаемые данные,
        //мы сначала попадаем на "скелет" страницы результатов поисков, не наполненной никакими данными.
        //Уже через несколько миллисекунд страница наполнится результатами, для нас этот процесс незаметен,
        //как если бы мы сразу получили страницу выдачи. А вот селениум работает очень быстро.
        //Если сразу после ввода в поиск запроса мы попросим селениум найти элемент списка поисковых результатов -
        // - он уткнется в еще ненаполненный "скелет" страницы. Поэтому мы должны попросить его подождать
        //определенных событий, определенных условий, прежде чем он продолжит работу.
        //Ниже приведен самописный метод, реализующий логику ожидания.
        waitFor(ExpectedConditions.presenceOfElementLocated(By.xpath("//ul[@id='search-result']")));

        //Обратите внимение, что здесь вместо driver.findElement используется driver.findElements,
        //для того чтобы вернуть сразу множество всех элементов, подходящих под указанный xpath.
        //В данном случае можно было бы ограничиться xpath-ом
        //"//ul[@id='search-result']/li", но проблема в том, что он представляет множество результатов,
        //включая добавленные яндексом в выдачу результаты поиска по Яндекс.Видео и Яндекс.Картинки, которые нельзя
        //считать за отдельные результаты. Поэтому нижеуказанный xpath расширен и фильтрует эти дополнения от яндекса
        List<WebElement> searchResults = driver.findElements(By.xpath("//ul[@id='search-result']/li[not(@data-fast-name)]"));
        Assertions.assertTrue(searchResults.size() >= 2,
                "Недостаточное количество поисковых результатов, < 2");

        //Шаг 6.
        List<WebElement> searchResultPages = driver.findElements(By.xpath("//div[@class='pager__items']/a"));
        Assertions.assertTrue(searchResultPages.size() >= 3,
                "Недостаточное количество страниц с результатами, < 3");

        //Шаг 7. Перейти на 3-ю страницу результатов
        //Сейчас можно было бы осуществить новый поиск элемента, представляющего собой
        //кнопку 3-й страницы,
        //WebElement thirdResultPage = driver.findElements(By.xpath("//div[@class='pager__items']/a[text()='3']"));
        //...но зачем тратить время, если мы уже получили все кнопки страниц выше:)
        WebElement thirdResultPage = searchResultPages.stream().filter(el -> el.getText().equals("3")).findFirst().get();
        thirdResultPage.click();

        //Шаг 8. Проверить, что мы все еще находимся в поисковике Яндекс (URL)
        String currentUrl = driver.getCurrentUrl();
        Assertions.assertTrue(currentUrl.contains("yandex.ru"),
                "Неожиданное поведение - мы не находимся на странице яндекса.");

        //Шаг 9. Открыть 2-й поисковый результат в выдаче на данной странице
        WebElement secondSearchResult = driver.findElement(
                By.xpath("//ul[@id='search-result']/li[@data-cid='1']/div/h2/a"));
        secondSearchResult.click();

        //Шаг 10. Убедиться что произошел переход со страницы поисковика на целевую страницу
        //Следующая, казалось бы, очевидная проверка, не отработает корректно -
        // - оказывается мы все еще на странице яндекса.
        //Assertions.assertFalse(currentUrl.contains("yandex.ru")
        //Почему так? Потому что яндекс при клике по ссылке из поисковой выдачи, открывает ссылку в новой вкладке.
        //А веб-драйвер и наш код продолжают работать с предыдущей вкладкой (яндекс поиска).
        //Поэтому сначала нам надо переключиться на новую вкладку, а уже затем наша проверка выше сможет отработать.
        //Определяем собственный метод (познакомьтесь с его реализацией):
        switchToNewTab();

        //И долгожданная проверка, что мы НЕ находимся на яндексе, что означает что открыт сайт из поисковой выдачи.
        currentUrl = driver.getCurrentUrl();
        Assertions.assertFalse(currentUrl.contains("yandex.ru"),
                "Неожиданное поведение - мы находимся на странице яндекса, вместо целевой страницы из выдачи.");

        //customWait - наш собственный метод, явная пауза чтобы полюбоваться результатами и конечным состоянием теста.
        //Философский вопрос - чем можно заменить это ожидание?
        //Как мы могли бы понять, что тест прошел, не таращась в экран и не наблюдая лично за результатом?
        customWait(5000);

        //А теперь второй философский вопрос на подумать - куда же нам впихнуть наше логгирование? Прям в код теста?
        //Перед каждым действием и по завершении? Насколько детальным будет лог, сделаем ли мы несколько уровней логов?
        //Не сделает ли это код теста еще более раздутым и менее читаемым? (спойлер - ДА, сделает)
        //driver.quit();
    }

    @AfterEach
    void tearDown(){
        driver.quit();
    }

    private void switchToNewTab(){
        //Получаем список всех имеющихся вкладок/окон
        //Кстати, обратите внимание на реализацию метода getWindowHandles() -
        // - он возвращает новый LinkedHashSet. Вопрос - почему так? Почему отдается новый объект вместо существующего?
        //Почему в качестве возвращаемого типа используется именно LinkedHashSet?
        Set<String> browserTabIds = driver.getWindowHandles();

        //Теперь получаем id своей текущей (с точки зрения драйвера) вкладки.
        //Делаем это для того, чтобы мы могли в browserTabIds выбрать НЕ текущую вкладку.
        String currentTabId = driver.getWindowHandle();

        //Получаем id новой вкладки.
        String newTab = browserTabIds.stream().filter(tab -> !tab.equals(currentTabId)).findFirst().get();

        //Переходим на новую вкладку:
        driver.switchTo().window(newTab);
    }

    private WebElement waitFor(ExpectedCondition condition){
        WebDriverWait wait = new WebDriverWait(driver, 5, 200);

        for (int i = 0; i < 3; i++){
            try {
                return (WebElement) wait.until(condition);
            } catch (StaleElementReferenceException | NoSuchElementException e){
                continue;
            }
        }

        throw new NoSuchElementException("Long wait returned no result, element not found");
    }

    private void checkMenuItemPresent(String elementDataId, String expectedMenuItemName){
        WebElement menuOption = driver.findElement(By.xpath(String.format("//a[@data-id='%s']", elementDataId)));
        WebElement menuOptionLogo = menuOption.findElement(By.xpath("div[@class='services-new__icon']"));
        WebElement menuOptionText = menuOption.findElement(By.xpath("div[@class='services-new__item-title']"));

        Assertions.assertEquals(menuOptionText.getText(),
                expectedMenuItemName,
                String.format("Название элемента меню '%s' не соответствует ожидаемому!", expectedMenuItemName));
        Assertions.assertNotNull(menuOptionLogo);
    }

    /**
     * На самом деле это не тест, а набор шагов и инструкций:)
     * Цель этого метода - просто познакомить вас с базовым
     * функционалом Selenium и тем, как написать простой
     * автоматизированный сценарий.
     */
    @Test
    void demoTest(){
        //Устанавливаем системную переменную пути для исполняемого файла webdriver
        System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver.exe");

        //При вызове конструктора new ChromeDriver() автоматически запускается исполняемый файл
        //chromedriver.exe, путь к которому мы прописали выше в системной переменной. Так
        //в системе появится процесс WebDriver-а.
        //Класс ChromeDriver - всего лишь оболочка, клиентский класс, который будет делать HTTP-запросы
        //к запущенному в нашей системе WebDriver-процессу (представляющему собой REST-сервер).
        ChromeDriver driver = new ChromeDriver();

        //просто ждем 5 секунд, чтобы насладиться магией автоматически запущенного Google Chrome.
        customWait(5000);

        //Закрываем браузер, убиваем процесс WebDriver.
        driver.quit();
    }

    /**
     * Пример, подчеркивающий отличие driver.get() от driver.navigate().to()
     *
     * Спойлер - отличаются они только тем, что navigate сохраняет историю перемещений по страницам,
     * а соответственно, поддерживает операции перейти на страницу назад, перейти на страницу вперед.
     */
    @Disabled
    @Test
    void navigateToTest(){
        System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver.exe");

        ChromeDriver driver = new ChromeDriver();
        driver.navigate().to("https://bready.ru/");
        WebElement touristGearSubmenu = driver.findElement(By.xpath("//div[@class='navigation']/nav/div/ul/li[1]"));
        touristGearSubmenu.click();
        customWait(2000);

        WebElement contactsLink = driver.findElement(By.xpath("//a[@href='/company/contacts/']"));
        contactsLink.click();
        customWait(2000);

        driver.navigate().back();
        customWait(2000);

        driver.navigate().forward();
        customWait(2000);

        //Обратите внимание, что методы customWait по сути оборачивают метод Thread.sleep(),
        //использование которого не приветствуется без крайней необходимости в автотестах.
        //Тем не менее, для большей наглядности автотестов (ввиду быстродействия селениум)
        //использование такого метода оправдано.
    }

    public static void customWait(int milliseconds){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
