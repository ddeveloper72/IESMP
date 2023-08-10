package ddsl.dobjects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;


public class DSelect extends DObject {

    Select select = new Select(element);

    public DSelect(WebDriver driver, WebElement element) {
        super(driver, element);
    }

    public void selectByVisibleText(String value) {
        select.selectByVisibleText(value);
    }

    public void selectValue(String value) {
        select.selectByValue(value);
    }
    public String getCurrentValue() {
        return select.getAllSelectedOptions().get(0).getText();
    }

}
