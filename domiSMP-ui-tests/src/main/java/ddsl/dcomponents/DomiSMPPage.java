package ddsl.dcomponents;


import ddsl.dobjects.DButton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pages.LoginPage;

public class DomiSMPPage extends DComponent {

    /**
     * Page object for the common components from Domismp like navigation, right menu. This contains the locators of the page and the methods for the behaviour of the page
     */
    private final static Logger LOG = LoggerFactory.getLogger(DomiSMPPage.class);

    @FindBy(css = "page-header > h1")
    protected WebElement pageTitle;
    @FindBy(id = "login_id")
    private WebElement loginBtnTop;
    @FindBy(id = "settingsmenu_id")
    private WebElement rightMenuBtn;
    @FindBy(id = "logout_id")
    private WebElement logoutMenuBtn;
    @FindBy(className = "smp-expired-password-dialog")
    private WebElement expiredPasswordDialog;

    @FindBy(css = "#okbuttondialog_id ")
    private WebElement dialogOKbutton;


    public DomiSMPPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(new AjaxElementLocatorFactory(driver, data.getTIMEOUT()), this);
    }

    public SideNavigationComponent getSidebar() {
        return new SideNavigationComponent(driver);
    }

    public BreadcrumpComponent getBreadcrump() {
        return new BreadcrumpComponent(driver);
    }

    public LoginPage goToLoginPage() {
        loginBtnTop.click();
        return new LoginPage(driver);
    }

    public LoginPage logout() {
        rightMenuBtn.click();
        logoutMenuBtn.click();
        return new LoginPage(driver);
    }

    public void refreshPage() {
        driver.navigate().refresh();
    }

    public AlertComponent getAlertArea() {
        return new AlertComponent(driver);
    }

    public DButton getExpiredDialoginbutton() {
        return weToDButton(dialogOKbutton);
    }

    public boolean isExpiredPopupEnabled() {
        try {
            if (dialogOKbutton.isDisplayed()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            LOG.info("Expiration poup not found", e);
            return false;
        }
    }

}
