package org.example.positive;



import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.example.util.ConfigReader;
import static org.junit.jupiter.api.Assertions.assertTrue;



public class WatchLessonVideo {
    private WebDriver driver;
    private WebDriverWait wait;


    private By LessonCard = By.cssSelector(".card-view-card");
    private By playButton = By.cssSelector(".card-view-card");

}

