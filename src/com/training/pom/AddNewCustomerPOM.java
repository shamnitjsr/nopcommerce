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
	
	@FindBy(xpath="/html/body/div[3]/div[2]/div/ul/li[4]/ul/li[1]/a/span")
	private WebElement click_customer2_tab; 
	
	public void clickCustomer2() {
		this.click_customer2_tab.click(); 
	}
	
	@FindBy(xpath="/html/body/div[3]/div[3]/div/form[1]/div[1]/div/a")
	private WebElement click_addNew; 
	
	public void clickAddNewBtn() {
		this.click_addNew.click(); 
	}
	
	@FindBy(id="Email")
	private WebElement email; 
	
	public void sendEmail(String email) {
		this.email.sendKeys(email);
	}
	
	@FindBy(id="Password")
	private WebElement pass; 
	
	public void sendPass(String password) {
		this.pass.sendKeys(password);
	}
	
	//FirstName
	@FindBy(id="FirstName")
	private WebElement fn; 
	
	public void sendFN(String fn) {
		this.fn.sendKeys(fn);
	}
	
	//LastName
	@FindBy(name="LastName")
	private WebElement ln; 
	
	public void sendLN(String ln) {
		this.ln.sendKeys(ln);
	}
	
	//Gender
	@FindBy(xpath="//*[@id=\"Gender_Male\"]")
	private WebElement gender; 
	
	public void clickGender() {
		this.gender.click();
	}
	
	//DateOfBirthday
	@FindBy(xpath="//*[@id=\"customer-info\"]/div[2]/div[1]/div[6]/div[2]/span[1]/span/span/span")
	private WebElement db; 
	
	public void clickDOB() {
		this.db.click();
	}
	
	//companyName
	@FindBy(xpath="//*[@id=\"Company\"]")
	private WebElement company; 
	
	public void sendCompanyName(String company) {
		this.company.sendKeys(company);
	}
	
	//IsTaxExempt
	@FindBy(xpath="//*[@id=\"IsTaxExempt\"]")
	private WebElement te; 
	
	public void clickTE() {
		this.te.click();
	}
	
	//NewsLetter
	@FindBy(xpath="//*[@id=\"customer-info\"]/div[2]/div[1]/div[9]/div[2]/div[1]/label/input")
	private WebElement nl; 
	
	public void clickNewsLetter() {
		this.nl.click();
	}
	
	//TestStore2
	@FindBy(xpath="/html/body/div[3]/div[3]/div/form/div[3]/div/nop-panels/nop-panel/div/div[2]/div[1]/div[9]/div[2]/div[2]/label/input")
	private WebElement ts; 
	
	public void clickTS() {
		this.ts.click();
	}
	
	//Select ManagerOfVender
	@FindBy(xpath="//*[@id=\"VendorId\"]")
	private WebElement mov;
	
	public void selectMOV() {
		this.mov.click();
		
	}
	
	//AdminComment
	@FindBy(xpath="//*[@id=\"AdminComment\"]")
	private WebElement ac;
	
	public void sendAdminComment(String ac) {
		this.ac.sendKeys(ac);;
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

}
