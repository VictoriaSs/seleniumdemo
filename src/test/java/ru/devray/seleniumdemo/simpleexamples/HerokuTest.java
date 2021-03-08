package ru.devray.seleniumdemo.simpleexamples;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class HerokuTest {

    public ChromeDriver driver;

    @BeforeEach
    void setUp(){
        ChromeOptions options = new ChromeOptions();
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        System.setProperty("webdriver.chrome.driver", "src/test/resources/bin/chromedriver.exe");
        driver = new ChromeDriver(options);
    }

    @AfterEach
    void tearDown(){
        if (driver != null){
            driver.quit();
        }
    }

    /**
     * Пример того, как можно обрабатывать базовую авторизацию (Basic Auth).
     * Особенность в том, что диалоговое окно ввода логина-пароля не находится на
     * самой странице, а рендерится средствами самого браузера, поэтому обычный
     * подход с локаторами нам не подойдет.
     */
    @Test
    void testBasicAuthorization(){
        String actualUrl = "https://the-internet.herokuapp.com/basic_auth";
        String login = "admin";
        String password = "admin";

        //подставляем параметры авторизации напрямую в URL.
        String urlWithCredentials = "http://%s:%s@the-internet.herokuapp.com/basic_auth";

        driver.get(String.format(urlWithCredentials, login, password));

        WebElement welcomeMessage = driver.findElement(By.xpath("//div[@class='example']/p"));
        Assertions.assertNotNull(welcomeMessage, "Авторизация не была пройдена.");

        customWait(5000);
    }

    /**
     * Пример взаимодействия с чек-боксами (CheckBox).
     * У чекбокса существует всего два возможных состояние - выбран или не выбран.
     */
    @Test
    void testHandlingCheckboxes(){
        String url = "https://the-internet.herokuapp.com/checkboxes";

        driver.get(url);
        //Осторожно, в XPath индексация курильщика! Обратите внимание что индексация элементов идет с 1.
        WebElement firstCheckBox = driver.findElement(By.xpath("//div[@class='example']/form/input[1]"));
        WebElement secondCheckBox = driver.findElement(By.xpath("//div[@class='example']/form/input[2]"));

        //На тестовой странице первый чекбокс в выключенном состоянии, второй во включенном.
        //Приведем их в одинаковое состояние, выключив второй.
        //Строго говоря, у чекбоксов нету методов типа включить/выключить, мы просто делаем клик по элементу.
        secondCheckBox.click();

        //Поэтому, совершая клик на произвольном чекбоксе, не зная его изначальное состояние
        //(включенный или выключенный), мы не знаем, в какое состояние его приведт клик.
        //Однако, существует удобное свойсто для элемента, isSelected(), которой для чекбокса как
        //раз позволит узнать его состояние. Таким образом, для гаранитрованного включения чекбокса, и
        //для отсутствия ложного выключения, код включения мог бы выглядеть так:
        if (!firstCheckBox.isSelected()) {
            //чекбокс станет включенным, если был выключенным, и останется включенным
            //если был включенным.
            firstCheckBox.click();
        }

        //делаем второй чекбокс включенным
        secondCheckBox.click();

        Assertions.assertTrue(firstCheckBox.isSelected(), "Чекбокс не включен.");
        Assertions.assertTrue(secondCheckBox.isSelected(), "Чекбокс не включен.");
    }

    /**
     * Тест на загрузку файла с локального компьютера в веб-приложение
     */
    @Test
    void testFileUpload(){
        String url = "https://the-internet.herokuapp.com/upload";

        driver.get(url);

        //Обратите внимание - кликать на кнопку 'Choose File' будет не лучшим решением,
        //т.к. после клика возникнет файловый диалог, отрисованный операционной системой, а не браузером,
        //и селениум окажется тут бессильным. Вместо этого, мы обойдем файловый диалог, и напрямую
        //введем в элемент input путь до нашего файла.
        //* Примечание - указанный путь до файла должен быть АБСОЛЮТНЫМ, т.е. полным, начинающимся
        //с корня файловой системы.

        //Используем данный самописный метод чтобы получить абсолютный путь к нашему ресурсному файлу
        String resourceName = "example-upload.file";
        String absolutePath = getAbsolutePath(resourceName);

        driver.findElement(By.xpath("//div[@class='example']/form/input[@id='file-upload']"))
                .sendKeys(absolutePath);

        //Нажимаем на кнопку загрузки файла
        WebElement uploadButton = driver.findElement(By.xpath("//input[@id='file-submit']"));
        uploadButton.click();

        //Проверяем что на странице появилось сообщение об успешной загрузке.
        WebElement successText = driver.findElement(By.xpath("//div[@class='example']/h3"));
        Assertions.assertEquals(successText.getText(), "File Uploaded!", "Файл не был загружен.");

        //Проверяем название загруженного файла - должно соответствовать имени нашего файла.
        WebElement fileUploadedName = driver.findElement(By.xpath("//div[@id='uploaded-files']"));
        Assertions.assertEquals(fileUploadedName, resourceName,
                "Имя загруженного файла не соответствует исходному файлу.");

        //customWait(5000);
    }

    /**
     * Первый приходящий в голову вопрос - что такое iframe?
     * В HTML тег iframe (inline frame) используется для того, чтобы встроить в текущий html-документ,
     * где объявлен тег iframe, еще один HTML-документ. Например, если вы, как веб-девелопер, хотите
     * встроить в вашу страничку какой-то заимствованный функционал, виджет, калькулятор, календарь,
     * кнопки ретвита и счетчик твитов, и т.д. Все это пойдет в начинку iframe-а.
     */
    @Test
    void testIFrame(){
        String url = "https://the-internet.herokuapp.com/iframe";

        driver.get(url);

        //Допустим перед нами стоит простоя задача ввода текста в iframe,
        //в текстовое поле приложения онлайн-блокнота. Можем ли мы просто так взять,
        //и набросить локатор в стиле "//body[@id='tinymce']/p" ? Набросать можем, только он не отработает:)
        //Причина - мы находимся в контексте (в окружении элементов) родительского HTML документа, в который
        //этот самый iframe встроен. И чтобы с ним работать - на него надо явно переключиться.
        //Например, по его id свойству.
        driver.switchTo().frame("mce_0_ifr");

        //Вот теперь мы можем обратиться к полю онлайнового текстового редактора.
        WebElement editorInput = driver.findElement(By.xpath("//body[@id='tinymce']/p"));
        editorInput.clear();
        editorInput.sendKeys("Look at me, I'm typing inside an iframe.");

        //Кстати, чтобы вернуться обратно в контекст родительского фрейма - надо использовать метод:
        driver.switchTo().parentFrame();
        //До его вызова мы будем оставаться в контексте дочернего iframe. И не будем иметь доступа к
        //элементам родительского фрейма. Попробуйте закомментировать строку выше и перезапустить тест.

        //Обращение к элементу родительского фрейма
        WebElement menuView = driver.findElement(By.xpath("//span[text()='View']"));
        menuView.click();

        customWait(5000);
    }

    /**
     * Пробуем взаимодействовать с алертами.
     */
    @Test
    void testJavaScriptAlerts(){
        String url = "https://the-internet.herokuapp.com/javascript_alerts";

        driver.get(url);

        //Алерты существуют в трех различных вариантах.
        //1. Простой алерт, представляющий собой всплывающее информационное сообщение.
        WebElement jsAlertButton = driver.findElement(By.xpath("//button[contains(text(), 'JS Alert')]"));
        jsAlertButton.click();
        //Для взаимодействия с алертом, нужно на него переключиться:
        Alert alert = driver.switchTo().alert();
        alert.accept();

        //2. Диалоговый алерт, представляющий собой диалоговое окно, позволяющее согласиться или отказаться.
        WebElement jsConfirmButton = driver.findElement(By.xpath("//button[contains(text(), 'JS Confirm')]"));
        jsConfirmButton.click();
        alert = driver.switchTo().alert();
        alert.dismiss();

        //3. Диалоговый алерт с полем ввода, позволяет ввести некоторые данные.
        String testInputData = "Entering-sample-data";
        WebElement jsPromptButton = driver.findElement(By.xpath("//button[contains(text(), 'JS Prompt')]"));
        jsPromptButton.click();
        alert = driver.switchTo().alert();
        alert.sendKeys(testInputData);
        alert.accept();

        WebElement resultText = driver.findElement(By.xpath("//p[@id='result']"));
        Assertions.assertTrue(resultText.getText().contains(testInputData));
    }

    public String getAbsolutePath(String resourceName){
        URL res = driver.getClass().getClassLoader().getResource(resourceName);
        File file = null;
        try {
            file = Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String absolutePath = file.getAbsolutePath();
        return absolutePath;
    }

    public static void customWait(int milliseconds){
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
