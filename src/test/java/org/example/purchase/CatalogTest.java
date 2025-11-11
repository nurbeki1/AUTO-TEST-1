package org.example.purchase;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.util.ConfigReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class CatalogTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait   = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.manage().window().maximize();
        driver.get(ConfigReader.get("base.url") + "/landing");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void catalogFlow() {
        firstPageLanding();
        openCatalog();
        awaitOrderPage();
        writeInfo();
        acceptModal();
        cardInfo();
    }

    private void firstPageLanding() {
        By pricingSection = By.id("pricing-section");
        By card = By.cssSelector(".pricing-card");
        By btn  = By.cssSelector(".btn-primary");

        WebElement block = wait.until(ExpectedConditions.visibilityOfElementLocated(pricingSection));
        js().executeScript("arguments[0].scrollIntoView({block:'start'});", block);

        wait.until(d -> {
            List<WebElement> cards = d.findElements(card);
            if (cards.isEmpty()) { js().executeScript("window.scrollBy(0, 500);"); return false; }
            boolean anyVisible = cards.stream().anyMatch(WebElement::isDisplayed);
            if (!anyVisible) js().executeScript("window.scrollBy(0, 500);");
            return anyVisible;
        });

        WebElement firstCard = driver.findElements(card).stream()
                .filter(WebElement::isDisplayed)
                .findFirst().orElseThrow(() -> new NoSuchElementException("Нет видимых .pricing-card"));

        WebElement addToCartBtn = firstCard.findElement(btn);
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", addToCartBtn);

        try {
            wait.until(ExpectedConditions.elementToBeClickable(addToCartBtn));
            new Actions(driver).moveToElement(addToCartBtn).pause(Duration.ofMillis(120)).click().perform();
        } catch (ElementClickInterceptedException e) {
            js().executeScript("arguments[0].click();", addToCartBtn);
        }
    }

    private void openCatalog() {
        By modalBy   = By.className("purchase-modal-content");
        By subjectBy = By.className("subject-item");
        By nextBy    = By.className("next-button");

        WebElement modal   = wait.until(ExpectedConditions.presenceOfElementLocated(modalBy));
        WebElement subject = modal.findElement(subjectBy);

        wait.until(ExpectedConditions.elementToBeClickable(subject)).click();
        wait.until(ExpectedConditions.elementToBeClickable(nextBy)).click();

        wait.until(ExpectedConditions.invisibilityOfElementLocated(modalBy));
    }

    private void awaitOrderPage() {
        if (driver.getWindowHandles().size() > 1) {
            List<String> hs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(hs.get(hs.size()-1));
        }
        wait.until(ExpectedConditions.or(
                ExpectedConditions.urlContains("order"),
                ExpectedConditions.presenceOfElementLocated(By.cssSelector("app-order"))
        ));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("app-order .order-form")));
    }

    private void writeInfo() {
        By formBy = By.cssSelector("app-order .order-form");

        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(formBy));
        WebElement finalForm = form;
        wait.until(d -> finalForm.findElements(By.cssSelector("input:not([type='hidden'])"))
                .stream().anyMatch(WebElement::isDisplayed));

        WebElement firstName = form.findElement(
                By.xpath(".//label[contains(normalize-space(.),'Атыңыз') or contains(normalize-space(.),'Имя')]" +
                        "/following::*[self::input or self::textarea][1]")
        );
        typeSafe(firstName, ConfigReader.get("TestName"));

        WebElement lastName = form.findElement(
                By.xpath(".//label[contains(normalize-space(.),'Тегіңіз') or contains(normalize-space(.),'Фамилия') or contains(normalize-space(.),'Тегiнiз')]" +
                        "/following::*[self::input or self::textarea][1]")
        );
        typeSafe(lastName,  ConfigReader.get("TestLastName"));

        String email = getTempEmail();

        WebElement phoneInput = null;
        List<By> phoneLocators = List.of(
                By.xpath(".//label[contains(normalize-space(.),'Телефон')]/following::*[self::input or self::textarea][1]"),
                By.xpath(".//input[contains(@placeholder,'Телефон') or contains(@placeholder,'Whatsapp') or contains(@placeholder,'ватсап')]"),
                By.cssSelector("input[name='phone'], input[data-testid='phone']")
        );
        for (By by : phoneLocators) {
            List<WebElement> found = form.findElements(by);
            if (!found.isEmpty() && found.get(0).isDisplayed()) { phoneInput = found.get(0); break; }
        }
        if (phoneInput == null) throw new NoSuchElementException("Поле телефона не найдено");
        typeSafe(phoneInput, "7779876543");

        WebElement emailInput = null;
        List<By> emailLocators = List.of(
                By.xpath(".//label[normalize-space()='Email' or contains(.,'Электрондық пошта') or contains(.,'Почта')]" +
                        "/following::*[self::input or self::textarea][1]"),
                By.xpath(".//input[contains(@placeholder,'Email') or contains(@placeholder,'E-mail')]"),
                By.cssSelector("input[name='email'], input[data-testid='email']")
        );
        for (By by : emailLocators) {
            List<WebElement> found = form.findElements(by);
            if (!found.isEmpty() && found.get(0).isDisplayed()) { emailInput = found.get(0); break; }
        }
        if (emailInput == null) throw new NoSuchElementException("Поле email не найдено");
        typeSafe(emailInput, email);
        WebElement dropdown = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(),'Ваш менеджер')]/following::*[1]"))
        );
        dropdown.click();

        WebElement noManager = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space(text())='Без менеджера']"))
        );
        noManager.click();


        if (noManager.isDisplayed()) {
            List<WebElement> paymentItems = wait.until(
                    ExpectedConditions.numberOfElementsToBeMoreThan(By.className("payment-item"), 1)
            );

            WebElement bankClick = paymentItems.get(1);

            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", bankClick);
            try {
                wait.until(ExpectedConditions.elementToBeClickable(bankClick)).click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", bankClick);
            }

            WebElement btnContinue = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.payment.flex.flex-col > button.primary-button")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({block:'center'});", btnContinue);
            try {
                btnContinue.click();
            } catch (ElementClickInterceptedException e) {
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btnContinue);
            }

        }


    }


    private void typeSafe(WebElement input, String value) {
        js().executeScript("arguments[0].scrollIntoView({block:'center'});", input);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(input));
            input.click();
            input.clear();
            input.sendKeys(value);
        } catch (ElementNotInteractableException | TimeoutException e) {
            js().executeScript(
                    "arguments[0].value=arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input',{bubbles:true}));",
                    input, value);
        }
    }
    private WebElement tryMany(Function<By, WebElement> finder, String name, By... locs) {
        for (By by : locs) {
            try {
                WebElement el = finder.apply(by);
                if (el != null) return el;
            } catch (TimeoutException | NoSuchElementException ignored) {
                System.out.println("[locator miss] " + name + " via: " + by);
            }
        }
        throw new NoSuchElementException("Поле не найдено: " + name);

    }

    private String getTempEmail() {
        String originalWindow = driver.getWindowHandle();

        ((JavascriptExecutor) driver).executeScript("window.open('https://temp-mail.org/en/', '_blank');");

        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mail")));

        wait.until(d -> {
            String value = emailField.getAttribute("value");
            return value != null && value.contains("@") && !value.toLowerCase().contains("loading");
        });

        String email = emailField.getAttribute("value");

        driver.close();
        driver.switchTo().window(originalWindow);
        return email;
    }



    private void acceptModal() {
        for (String h : driver.getWindowHandles()) {
            driver.switchTo().window(h);
            if (driver.getCurrentUrl().startsWith("http")) break;
        }

        WebDriverWait w = new WebDriverWait(driver, Duration.ofSeconds(30));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        WebElement modal = w.until(d -> (WebElement) js.executeScript(
                "const panes=[...document.querySelectorAll('div.cdk-overlay-pane')];" +
                        "return panes.length ? panes.at(-1).querySelector('.mat-mdc-dialog-surface') : null;"
        ));

        js.executeScript(
                "const c=arguments[0].querySelector('.mat-mdc-dialog-content, .content, .pdf-viewer');" +
                        "if(c){ c.scrollTop=c.scrollHeight; }",
                modal
        );

        WebElement checkbox = (WebElement) js.executeScript(
                "return arguments[0].querySelector('.agree input[type=\"checkbox\"]');",
                modal
        );
        js.executeScript("arguments[0].click();", checkbox);

        WebElement cont = w.until(d -> (WebElement) js.executeScript(
                "const b=arguments[0].querySelector('.buttons .primary-button');" +
                        "return b && !b.disabled && b.getAttribute('aria-disabled')!=='true' ? b : null;",
                modal
        ));
        try {
            cont.click();
        } catch (ElementClickInterceptedException e) {
            js.executeScript("arguments[0].click();", cont);
        }

        w.until(d -> (Boolean) js.executeScript(
                "return !document.querySelector('div.cdk-overlay-pane .mat-mdc-dialog-surface');"
        ));
    }



    private void cardInfo() {

        String cardNum   = ConfigReader.get("cardNomer").replace(" ", "");
        String cardMonth = ConfigReader.get("cardM").trim();
        String cardYear  = ConfigReader.get("cardY").trim();
        String cardCvc   = ConfigReader.get("cardCvc").trim();
        String cardUser  = ConfigReader.get("cardUser").trim();
        String phone     = ConfigReader.get("cardNumber").replace(" ", "");


        WebElement block = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".card"))
        );

        block.findElement(By.id("pan")).sendKeys(cardNum);
        block.findElement(By.id("month")).sendKeys(cardMonth);
        block.findElement(By.id("year")).sendKeys(cardYear);
        block.findElement(By.id("cvv")).sendKeys(cardCvc);
        block.findElement(By.id("holder")).sendKeys(cardUser);

        WebElement phoneField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input.tel__input")
        ));
        phoneField.sendKeys(phone);

        String email = ConfigReader.get("cardEmail");
        if (email == null || email.trim().isEmpty()) {
            email = getTempEmail();
        }

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("email")
        ));
        emailField.sendKeys(email);
    }



    private JavascriptExecutor js() { return (JavascriptExecutor) driver; }
}
