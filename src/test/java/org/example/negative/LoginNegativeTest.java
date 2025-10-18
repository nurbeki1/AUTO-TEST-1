package org.example.negative;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.example.util.ConfigReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class LoginNegativeTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().setup();
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(100));
    }
    @AfterEach
    void tearDown() throws InterruptedException {
        if (driver != null ) {
            Thread.sleep(3600);
            driver.quit();
        }
    }
    @Test
    void testLogin() {
        driver.get(ConfigReader.get("base.url")+"/authorization");
        driver.findElements(By.className("iconic-input")).get(0)
                .sendKeys(ConfigReader.get("UserLogin"));
        driver.findElements(By.className("iconic-input")).get(1)
                .sendKeys(ConfigReader.get("UserPassword"));

        wait.until(ExpectedConditions.elementToBeClickable(By.className("confirm-button")))
                .click();

      WebElement toastTitle = new WebDriverWait(driver, Duration.ofSeconds(8)).until(ExpectedConditions.visibilityOfElementLocated(By.id("toast-container")));

      String text1 = toastTitle.getText();
      System.out.println("TOAST = " + text1);


      assertTrue(text1.contains("Логин"));
    }


}
