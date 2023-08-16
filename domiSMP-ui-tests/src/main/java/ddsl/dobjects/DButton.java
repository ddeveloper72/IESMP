package ddsl.dobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class DButton extends DObject {
    /**
     * Generic wrapper for button element.
     */
    public DButton(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    @Override
    public String getText() {
        return element.findElement(By.cssSelector("span > span")).getText().trim();
    }
}
