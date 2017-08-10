import io.appium.java_client.AppiumDriver;
import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testobject.appium.junit.TestObjectAppiumSuite;
import org.testobject.appium.junit.TestObjectAppiumSuiteWatcher;
import org.testobject.rest.api.appium.common.TestObject;
import org.testobject.rest.api.appium.common.TestObjectCapabilities;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

@TestObject(testObjectApiKey = "YOUR_API_KEY", testObjectSuiteId = 123)
@RunWith(TestObjectAppiumSuite.class)
public class SuitesTestSetup {

	private AppiumDriver driver;

	/* Sets the test name to the name of the test method. */
	@Rule
	public TestName testName = new TestName();

	/* Takes care of sending the result of the tests over to TestObject. */
	@Rule
	public TestObjectAppiumSuiteWatcher resultWatcher = new TestObjectAppiumSuiteWatcher();

	private final static String EXPECTED_RESULT_FOUR = "4";
	private final static String EXPECTED_RESULT_NAN = "NaN";

	/* This is the setup that will be run before the test. */
	@Before
	public void setUp() throws Exception {
		URL apiEndpoint = getTestObjectApiEndpoint();

		DesiredCapabilities capabilities = new DesiredCapabilities();

		capabilities.setCapability("testobject_api_key", resultWatcher.getApiKey());
		capabilities.setCapability("testobject_test_report_id", resultWatcher.getTestReportId());

		// We generate a random UUID for later lookup in logs for debugging
		String testUUID = UUID.randomUUID().toString();
		System.out.println("TestUUID: " + testUUID);
		capabilities.setCapability("testobject_testuuid", testUUID);

		String cacheDevice = System.getenv("TESTOBJECT_CACHE_DEVICE");
		if (cacheDevice != null && cacheDevice.trim().isEmpty() == false) {
			capabilities.setCapability("testobject_cache_device", cacheDevice);
		}

		String TESTOBJECT_SESSION_CREATION_TIMEOUT = System.getenv("TESTOBJECT_SESSION_CREATION_TIMEOUT");
		if (TESTOBJECT_SESSION_CREATION_TIMEOUT != null) {
			capabilities.setCapability("testobject_session_creation_timeout", TESTOBJECT_SESSION_CREATION_TIMEOUT);
		}

		String TESTOBJECT_SESSION_CREATION_RETRY = System.getenv("TESTOBJECT_SESSION_CREATION_RETRY");
		if (TESTOBJECT_SESSION_CREATION_RETRY != null) {
			capabilities.setCapability("testobject_session_creation_retry", TESTOBJECT_SESSION_CREATION_RETRY);
		}

		driver = new AndroidDriver(resultWatcher.getTestObjectOrLocalAppiumEndpointURL(), capabilities);
		
		System.out.println(driver.getCapabilities().getCapability("testobject_test_report_url"));
		System.out.println(driver.getCapabilities().getCapability("testobject_test_live_view_url"));

		resultWatcher.setRemoteWebDriver(driver, apiEndpoint);

	}

	/* A simple addition, it expects the correct result to appear in the result field. */
	@Test
	public void twoPlusTwoOperation() {

		driver.getPageSource();

        /* Add two and two. */
		driver.findElement(By.id("net.ludeke.calculator:id/digit2")).click();
		driver.findElement(By.id("net.ludeke.calculator:id/plus")).click();
		driver.findElement(By.id("net.ludeke.calculator:id/digit2")).click();
		driver.findElement(By.id("net.ludeke.calculator:id/equal")).click();

        /* Check if within given time the correct result appears in the designated field. */
		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(
				driver.findElement(By.xpath("//android.widget.EditText[1]")), EXPECTED_RESULT_FOUR));

	}

	/* A simple zero divided by zero operation. */
	@Test
	public void zerosDivisionOperation() {

        /* In the main panel... */
		MobileElement digit0 = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/digit0")));
		digit0.click();

		MobileElement div = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/div")));
		div.click();

		digit0.click();

		MobileElement buttonEquals = (MobileElement) (driver.findElement(By.id("net.ludeke.calculator:id/equal")));
		buttonEquals.click();

		MobileElement resultField = (MobileElement) (driver.findElement(By.xpath("//android.widget.EditText[1]")));

		(new WebDriverWait(driver, 30)).until(ExpectedConditions.textToBePresentInElement(resultField, EXPECTED_RESULT_NAN));

	}

	static URL getTestObjectApiEndpoint() throws MalformedURLException {
		String apiEndpoint = System.getenv("TESTOBJECT_API_ENDPOINT");
		if (apiEndpoint == null) {
			return TestObjectCapabilities.TESTOBJECT_API_ENDPOINT;
		} else {
			return new URL(apiEndpoint);
		}
	}

}
