package org.example.positive;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.example.util.ConfigReader;


public class LoginTest {

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
    void testLogin() {;
        driver.get(ConfigReader.get("base.url")+"/authorization");
        driver.findElements(By.className("iconic-input")).get(0)
                .sendKeys(ConfigReader.get("UserLogin"));
        driver.findElements(By.className("iconic-input")).get(1)
                .sendKeys(ConfigReader.get("UserPassword"));

        wait.until(ExpectedConditions.elementToBeClickable(By.className("confirm-button")))
                .click();

      WebElement  blockCourse= new WebDriverWait(driver, Duration.ofSeconds(8)).until(ExpectedConditions.visibilityOfElementLocated(By.className("student-navbar__nav-list")));



      assertTrue(blockCourse.isDisplayed());
    }


}
