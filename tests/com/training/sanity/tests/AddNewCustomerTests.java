package com.training.sanity.tests;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.training.generics.ScreenShot;
import com.training.pom.AddNewCustomerPOM;
import com.training.utility.DriverFactory;
import com.training.utility.DriverNames;

public class AddNewCustomerTests {

	private WebDriver driver;
	private String baseUrl;
	private AddNewCustomerPOM addNewCustomerPOM;
	private static Properties properties;
	private ScreenShot screenShot;

	@BeforeClass
	public static void setUpBeforeClass() throws IOException {
		properties = new Properties();
		FileInputStream inStream = new FileInputStream("./resources/others.properties");
		properties.load(inStream);
	}

	@BeforeMethod
	public void setUp() throws Exception {
		driver = DriverFactory.getDriver(DriverNames.CHROME);
		addNewCustomerPOM = new AddNewCustomerPOM(driver);
		baseUrl = properties.getProperty("baseURL");
		screenShot = new ScreenShot(driver);
		// open the browser
		driver.get(baseUrl);
	}

	@AfterMethod
	public void tearDown() throws Exception {
		Thread.sleep(1000);
		driver.quit();
	}

	@Test
	public void validLoginTest() throws InterruptedException {
		addNewCustomerPOM.sendUserName("admin@yourstore.com");
		addNewCustomerPOM.sendPassword("admin");
		addNewCustomerPOM.clickLoginBtn();
		Thread.sleep(5000);
		addNewCustomerPOM.clickCustomer1();
		Thread.sleep(5000);
		addNewCustomerPOM.clickCustomer2();

		Thread.sleep(5000);
		addNewCustomerPOM.clickAddNewBtn();

		Thread.sleep(5000);
		addNewCustomerPOM.sendEmail("shamnitjsr@gmail.com");

		Thread.sleep(5000);
		addNewCustomerPOM.sendPass("Joinblr@123");

		//FirstName
		Thread.sleep(5000);
		addNewCustomerPOM.sendFN("shambhu");
		
		//LastName
		Thread.sleep(5000);
		addNewCustomerPOM.sendLN("jayswal");
		
		//Gender
		Thread.sleep(5000);
		addNewCustomerPOM.clickGender();
		//DateOfBirthDay
		Thread.sleep(5000);
		addNewCustomerPOM.clickDOB();
		//companyName
		Thread.sleep(5000);
		addNewCustomerPOM.sendCompanyName("IBM");
		//IsTaxExempt
		Thread.sleep(5000);
		addNewCustomerPOM.clickTE();
		//NewsLetter
		Thread.sleep(5000);
		addNewCustomerPOM.clickNewsLetter();
		//TestStore2
		Thread.sleep(5000);
		addNewCustomerPOM.clickTS();
		
		 //Select ManagerOfVender
		/*
		 
		Thread.sleep(5000);
		addNewCustomerPOM.selectMOV();
		*/
		
		//AdminComment
		Thread.sleep(5000);
		addNewCustomerPOM.sendAdminComment("this is Shambhu Kumar");
		
		screenShot.captureScreenShot("second");
	}

}
