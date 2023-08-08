package pages;

import ddsl.DomiSMPPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class LoginPage extends DomiSMPPage {

    /**
     * Page object for the Login page. This contains the locators of the page and the methods for the behaviour of the page
     */

    private final static Logger LOG = LoggerFactory.getLogger(LoginPage.class);

    @FindBy(id = "username_id")
    private WebElement username;
    @FindBy(id = "password_id")
    private WebElement password;
    @FindBy(id = "loginbutton_id")
    private WebElement loginBtn;

    public LoginPage(WebDriver driver) {
        super(driver);
        LOG.debug(".... init");
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    public DomiSMPPage login(String user, String pass) throws Exception {
        HashMap<String, String> usr = new HashMap<>();
        usr.put("username", user);
        usr.put("pass", pass);
        LOG.debug("Login started " + usr.get("username") + " / " + usr.get("pass"));

        goToLoginPage();
        weToDInput(username).fill(usr.get("username"));
        weToDInput(password).fill(usr.get("pass"));
        weToDButton(loginBtn).click();
        wait.forXMillis(500);
        if (isExpiredPopupEnabled()) {
            LOG.info("Expired password dialog is present.");
            closeExpirationPopupIfEnabled();
        }
        return new DomiSMPPage(driver);

    }

}

