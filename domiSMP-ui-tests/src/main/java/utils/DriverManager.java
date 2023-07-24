package utils;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;
import java.util.HashMap;

public class DriverManager {
    static TestRunData data = new TestRunData();


    public static WebDriver getDriver() {

        WebDriver driver;
        if ( StringUtils.equalsIgnoreCase(data.getRunBrowser(), "firefox")) {
            driver = getFirefoxDriver();
        } else {
            driver = getChromeDriver();
        }
        driver.manage().window().setSize(new Dimension(1920, 1080));
        driver.manage().window().maximize();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return driver;
    }

    private static WebDriver getChromeDriver() {
        System.setProperty("webdriver.chrome.driver", data.getChromeDriverPath());


        //Code added for auto download
        HashMap<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_settings.popups", 0);
        prefs.put("download.default_directory", data.downloadFolderPath());
        prefs.put("safebrowsing.enabled", "true");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-dev-shm-usage"); // overcome limited resource problems
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--headless=new");

        options.setExperimentalOption("prefs", prefs);
        return new ChromeDriver(options);
    }

    private static WebDriver getFirefoxDriver() {
        System.setProperty("webdriver.gecko.driver", data.getFirefoxDriverPath());

        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--headless=new");

        //code added for auto download
        options.addPreference("browser.download.folderList", 2);
        options.addPreference("browser.download.manager.showWhenStarting", false);
        options.addPreference("browser.download.dir", data.downloadFolderPath());
        options.addPreference("browser.helperApps.neverAsk.openFile", "application/ms-excel text/xml application/zip");
        options.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/ms-excel text/xml application/zip");
        return new FirefoxDriver(options);
    }

}
