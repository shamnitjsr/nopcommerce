package com.training.pom;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AddNewCustomerPOM {
	
private WebDriver driver; 
	
	public AddNewCustomerPOM(WebDriver driver) {
		this.driver = driver; 
		PageFactory.initElements(driver, this);
	}
	
	@FindBy(xpath="//*[@id=\"Email\"]")
	private WebElement userName; 
	
	public void sendUserName(String userName) {
		this.userName.clear();
		this.userName.sendKeys(userName);
	}
	
	@FindBy(xpath="//*[@id=\"Password\"]")
	private WebElement password;
	
	public void sendPassword(String password) {
		this.password.clear(); 
		this.password.sendKeys(password); 
	}
	
	@FindBy(xpath="/html/body/div[6]/div/div/div/div/div[2]/div[1]/div/form/div[3]/input")
	private WebElement loginBtn;
	
	public void clickLoginBtn() {
		this.loginBtn.click(); 
	}
	
	@FindBy(xpath="/html/body/div[3]/div[2]/div/ul/li[4]/a/span")
	private WebElement click_customer1_tab; 
	
	public void clickCustomer1() {
		this.click_customer1_tab.click(); 
	}
	
	
	
	
	
	
	
	

}
