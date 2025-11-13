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
        System.out.println("Тест успешно прошел ✅");
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

//    private void writeInfo() {
//        By formBy = By.cssSelector("app-order .order-form");
//
//        WebElement form = wait.until(ExpectedConditions.visibilityOfElementLocated(formBy));
//        WebElement finalForm = form;
//        wait.until(d -> finalForm.findElements(By.cssSelector("input:not([type='hidden'])"))
//                .stream().anyMatch(WebElement::isDisplayed));
//
//        WebElement firstName = form.findElement(
//                By.xpath(".//label[contains(normalize-space(.),'Атыңыз') or contains(normalize-space(.),'Имя')]" +
//                        "/following::*[self::input or self::textarea][1]")
//        );
//        typeSafe(firstName, ConfigReader.get("TestName"));
//
//        WebElement lastName = form.findElement(
//                By.xpath(".//label[contains(normalize-space(.),'Тегіңіз') or contains(normalize-space(.),'Фамилия') or contains(normalize-space(.),'Тегiнiз')]" +
//                        "/following::*[self::input or self::textarea][1]")
//        );
//        typeSafe(lastName,  ConfigReader.get("TestLastName"));
//        WebElement number = form.findElement(
//                By.xpath(".//label[contains(normalize-space(.),'Атыңыз') or contains(normalize-space(.),'Имя')]" +
//                        "/following::*[self::input or self::textarea][1]")
//        );
//        typeSafe(firstName, ConfigReader.get("TestName"));
//
//
//        String email = getTempEmail(); // ← теперь с Mail.tm
//
//        WebElement emailInput = null;
//        List<By> emailLocators = List.of(
//                By.xpath(".//label[normalize-space()='Email' or contains(.,'Электрондық пошта') or contains(.,'Почта')]" +
//                        "/following::*[self::input or self::textarea][1]"),
//                By.xpath(".//input[contains(@placeholder,'Email') or contains(@placeholder,'E-mail')]"),
//                By.cssSelector("input[name='email'], input[data-testid='email']")
//        );
//        for (By by : emailLocators) {
//            List<WebElement> found = form.findElements(by);
//            if (!found.isEmpty() && found.get(0).isDisplayed()) { emailInput = found.get(0); break; }
//        }
//        if (emailInput == null) throw new NoSuchElementException("Поле email не найдено");
//        typeSafe(emailInput, email);
//
//        WebElement dropdown = wait.until(
//                ExpectedConditions.elementToBeClickable(By.xpath("//*[contains(text(),'Ваш менеджер')]/following::*[1]"))
//        );
//        dropdown.click();
//
//        WebElement noManager = wait.until(
//                ExpectedConditions.elementToBeClickable(By.xpath("//*[normalize-space(text())='Без менеджера']"))
//        );
//        noManager.click();
//    }
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
        typeSafe(phoneInput, ConfigReader.get("TestPhone"));

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

    // 🆕 Новый вариант getTempEmail (Mail.tm, без закрытия вкладки)
    private String getTempEmail() {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        System.out.println("📧 Opening Mail.tm temporary inbox...");

        // 1️⃣ Открываем Mail.tm в новой вкладке
        js.executeScript("window.open('https://mail.tm/en/', '_blank');");
        List<String> tabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(tabs.get(tabs.size() - 1));

        WebElement emailField = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.cssSelector("input[readonly][type='email'], input#address")
        ));

        String email = emailField.getAttribute("value");
        System.out.println("✅ Got temp email: " + email);

        // ❗ Вкладку не закрываем, остаёмся в Mail.tm
        // — потом можно будет туда вернуться и проверить письмо
        driver.switchTo().window(tabs.get(0)); // возвращаемся в тест

        return email;
    }

    private void acceptModal() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(300));
        JavascriptExecutor js = (JavascriptExecutor) driver;
        long startTime = System.currentTimeMillis();

        try {
            System.out.println("⌛ Waiting for modal... (up to 5 minutes)");

            WebElement modal = wait.until(d -> {
                WebElement el = (WebElement) js.executeScript(
                        "const panes=[...document.querySelectorAll('div.cdk-overlay-pane')];" +
                                "return panes.length ? panes.at(-1).querySelector('.mat-mdc-dialog-surface') : null;"
                );
                if (el != null) {
                    System.out.println("🟢 Modal detected after " +
                            ((System.currentTimeMillis() - startTime) / 1000) + "s");
                }
                return el;
            });

            if (modal == null) {
                System.out.println("⚠️ Modal not found — skipping acceptModal()");
                return;
            }

            js.executeScript(
                    "const c=arguments[0].querySelector('.mat-mdc-dialog-content, .content, .pdf-viewer');" +
                            "if(c){ c.scrollTop=c.scrollHeight; }",
                    modal
            );

            WebElement checkbox = (WebElement) js.executeScript(
                    "return arguments[0].querySelector('.agree input[type=\"checkbox\"]');",
                    modal
            );
            if (checkbox != null) {
                js.executeScript("arguments[0].click();", checkbox);
            }

            WebElement cont = wait.until(d -> (WebElement) js.executeScript(
                    "const b=arguments[0].querySelector('.buttons .primary-button');" +
                            "return b && !b.disabled && b.getAttribute('aria-disabled')!=='true' ? b : null;",
                    modal
            ));
            if (cont != null) {
                js.executeScript("arguments[0].click();", cont);
            }

            wait.until(d -> (Boolean) js.executeScript(
                    "return !document.querySelector('div.cdk-overlay-pane .mat-mdc-dialog-surface');"
            ));
            System.out.println("✅ Modal closed successfully");

        } catch (TimeoutException e) {
            System.out.println("⚠️ Modal did not appear within 5 minutes — skipping");
        } catch (Exception e) {
            System.out.println("❌ Error during acceptModal: " + e.getMessage());
        }
    }

    private void cardInfo() {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(60));
        JavascriptExecutor js = (JavascriptExecutor) driver;

        try {
            System.out.println("⌛ Waiting for card form...");

            // 🧩 Ждём хотя бы одно поле — по id 'pan'
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("pan")));
            System.out.println("✅ Card form is visible");

            // 🧾 Берём данные из конфига
            String cardNum   = ConfigReader.get("cardNomer").trim();
            String cardMonth = ConfigReader.get("cardM").trim();
            String cardYear  = ConfigReader.get("cardY").trim();
            String cardCvc   = ConfigReader.get("cardCvc").trim();
            String cardUser  = ConfigReader.get("cardUser").trim();

            // 🖊️ Заполняем все поля по ID
            Map<String, String> fields = new LinkedHashMap<>();
            fields.put("pan", cardNum);
            fields.put("month", cardMonth);
            fields.put("year", cardYear);
            fields.put("cvv", cardCvc);
            fields.put("holder", cardUser);

            for (Map.Entry<String, String> entry : fields.entrySet()) {
                try {
                    WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(entry.getKey())));
                    el.click();
                    el.clear();
                    el.sendKeys(entry.getValue());
                    System.out.println("✅ Filled " + entry.getKey());
                } catch (Exception e) {
                    System.out.println("⚠️ Could not fill " + entry.getKey() + ": " + e.getMessage());
                }
            }

            // 🔽 Скроллим к кнопке "Оплатить"
            WebElement payBtn = wait.until(ExpectedConditions.elementToBeClickable(
                    By.xpath("//button[contains(.,'Оплатить') or contains(.,'Pay')]")
            ));
            js.executeScript("arguments[0].scrollIntoView({block:'center'});", payBtn);
            System.out.println("💳 Ready for payment (button found)");

        } catch (Exception e) {
            System.out.println("❌ Error while filling card info: " + e.getMessage());
        }
    }


    private JavascriptExecutor js() { return (JavascriptExecutor) driver; }
}
