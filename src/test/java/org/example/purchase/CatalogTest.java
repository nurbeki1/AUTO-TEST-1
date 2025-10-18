package org.example.purchase;

import org.example.util.ConfigReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Collections;
import java.util.List;

import java.time.Duration;
import java.util.Map;

public class CatalogTest {

    private WebDriver driver;
    private CatalogTest catalog;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(100));
        catalog = new CatalogTest();
        driver.get(ConfigReader.get("base.url") + "/landing");
        driver.manage().window().maximize();

    }

    @AfterEach
    void tearDown() throws InterruptedException {
        if (driver != null) {
            Thread.sleep(3600);
            driver.quit();
        }
    }


    @Test
    void Catalog() throws InterruptedException {
        firstPageLanding();
        openCatalog();
        Thread.sleep(3600);

        buyCourse();
        WriteInfo();
        cardInfo();
    }

    private void firstPageLanding() throws InterruptedException {
        WebElement catalogueBlock = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("pricing-section"))
        );

        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", catalogueBlock);

        List<WebElement> cartsLanding = (wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.className("pricing-card"), 0)));
        WebElement firstCard = cartsLanding.get(1);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", firstCard);
        WebElement addToCartBtn = firstCard.findElement(By.className("btn-primary"));

        wait.until(ExpectedConditions.elementToBeClickable(addToCartBtn)).click();

        try {
            addToCartBtn.click();
        } catch (ElementClickInterceptedException e) {
            JavascriptExecutor js = (JavascriptExecutor) driver;
            Map<String, Long> coords = (Map<String, Long>) js.executeScript(
                    "const r = arguments[0].getBoundingClientRect();" +
                            "return {x: Math.floor(r.left + r.width / 2), y: Math.floor(r.top + r.height / 2)};",
                    addToCartBtn
            );
            long x = coords.get("x");
            long y = coords.get("y");

            WebElement blockingElement = (WebElement) js.executeScript(
                    "return document.elementFromPoint(arguments[0], arguments[1]);", x, y
            );

            if (blockingElement != null) {
                System.out.println("🚫 Клик заблокирован элементом: <"
                        + blockingElement.getTagName() + "> "
                        + "class='" + blockingElement.getAttribute("class") + "'");
                js.executeScript("arguments[0].style.outline='3px solid red'", blockingElement);
                js.executeScript("arguments[0].style.pointerEvents='none'", blockingElement);
            } else {
                System.out.println("ℹ️ Не удалось определить блокирующий элемент (вернулся null).");
            }
            js.executeScript("arguments[0].click();", addToCartBtn);
        }



    }

    private void openCatalog() {
        WebElement streamBlocks = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.className("purchase-modal-content"))
        );


        WebElement addToCartBtn = streamBlocks.findElement(By.className("subject-item"));
        addToCartBtn.click();
        WebElement priceText = streamBlocks.findElement(By.className("price-amount"));
        String price = priceText.getText();

        if (!price.equals(0)) {
            WebElement addToCartBtn2 = streamBlocks.findElement(By.className("next-button"));
            addToCartBtn2.click();
        }else {
            System.out.println("ҰБТ-да тапсыратын пәндер комбинациясын таңдаңыз\n");
        }

    }

    private void buyCourse() {
        WebElement entryBasketBtn = driver.findElement(By.className("cart"));
        wait.until(ExpectedConditions.elementToBeClickable(entryBasketBtn)).click();
        //нужно ждать курс, внутри корзина

        WebElement listCourses = driver.findElement(By.className("stream-name"));
        wait.until(ExpectedConditions.visibilityOfElementLocated((By) listCourses));
        WebElement buyStreamBtn = driver.findElement(By.className("button-primary"));
        buyStreamBtn.click();
    }


    // Осы жерде через платформу мен почта қою керекпін, и ол почта арқылы уже регистрация өтіп кету керекпін
    private void WriteInfo() {

        WebElement infoBlock = driver.findElement(By.className("order-form"));
        infoBlock.findElement(By.cssSelector("[formcontrolname='firstName']")).sendKeys(ConfigReader.get("TestName"));
        infoBlock.findElement(By.cssSelector("[formcontrolname='lastName']")).sendKeys(ConfigReader.get("TestLastName"));


        driver.get(ConfigReader.get("temple.url"));
        WebElement emailField = driver.findElement(By.className("emailbox-input"));

        String email = openEmailSite();

        infoBlock.findElements(By.className("iconic-input")).get(0).sendKeys("7777777777");
        infoBlock.findElements(By.className("iconic-input")).get(1).sendKeys(email);


    }

    private String openEmailSite() {
        String mainTab = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(ConfigReader.get("temple.url"));

        WebDriverWait localWait = new WebDriverWait(driver, Duration.ofSeconds(20));

        By emailInput = By.id("mail");

        WebElement emailField = localWait.until(
                ExpectedConditions.visibilityOfElementLocated(emailInput)
        );

        localWait.until(d -> {
            String v = emailField.getAttribute("value");
            return v != null && !v.isBlank() && !v.toLowerCase().contains("loading");
        });

        String email = emailField.getAttribute("value");

        driver.close();
        driver.switchTo().window(mainTab);

        return email;
    }




    private void cardInfo() {
        WebElement cardBlock = driver.findElement(By.className("card"));


       cardBlock.findElement(By.id("pan")).sendKeys(ConfigReader.get("cardNomer"));
       cardBlock.findElement(By.id("month")).sendKeys(ConfigReader.get("cardM"));
       cardBlock.findElement(By.id("year")).sendKeys(ConfigReader.get("cardY"));
       cardBlock.findElement(By.id("cvv")).sendKeys(ConfigReader.get("cardCvc"));
       cardBlock.findElement(By.id("holder")).sendKeys(ConfigReader.get("cardUser"));

        String email = openEmailSite();

        driver.findElement(By.className("tel__input")).sendKeys(ConfigReader.get("cardNumber"));
        driver.findElement(By.id("email")).sendKeys(email);
    }






    private void registrationPage() {
        String mainTab = driver.getWindowHandle();
        driver.switchTo().newWindow(WindowType.TAB);
        driver.get(ConfigReader.get("temple.url"));
        WebElement senderBlock = driver.findElement(By.className("inbox-dataList"));
        wait.until(ExpectedConditions.visibilityOfElementLocated((By) senderBlock));
        wait.until(ExpectedConditions.elementToBeClickable(new By.ByTagName("li"))).click();
        wait.until(ExpectedConditions.elementToBeClickable(new By.ByTagName("a"))).click();
    }
}
//Финиш когда студент акк кірген кезде, сатып алған курс көрініп тұру керек