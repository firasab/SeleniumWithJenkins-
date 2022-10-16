
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import core.OpenBrowsers;
import core.ReadCsvFile;
import core.TakeScreenShot;
import core.WriteCsvFile;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.BingHome;
import pages.BingResults;



public class TestWeb {
    WebDriver driver;
    TakeScreenShot takeScr;
    int x =0;

    ArrayList<String> outputHeaders = new ArrayList<String>();
    ArrayList<ArrayList<String>> outputData = new ArrayList<ArrayList<String>>();
    @BeforeSuite
    public void beforeSuite() throws InterruptedException {
        driver = OpenBrowsers.openchromeWithOptions();
        //driver = OpenBrowsers.openBrowser("chrome");
        takeScr = new TakeScreenShot(driver);
        outputHeaders.add("hotel_id");
        outputHeaders.add("name");
        outputHeaders.add("city");
        outputHeaders.add("address");
        outputHeaders.add("link ");
        outputHeaders.add("rate");
        Thread.sleep(10000);
        driver.manage().window().maximize();
    }

    @DataProvider
    public static Object[][] getData() throws Exception{

        List<String[]> lines = ReadCsvFile.readAllLines("input.csv");
        lines.remove(0);
        Object[][] data = new Object[lines.size()][lines.get(0).length];
        int index = 0;
        for(String[] line : lines) {
            data[index] = line;
            index++;
        }
        return data;
    }
    @Test(dataProvider = "getData")
    public void testExpediaLinks(String hotel_id, String name, String city, String	address) throws IOException, InterruptedException {
        String searchText = name + " " + city + " expedia";
        JavascriptExecutor js = (JavascriptExecutor) driver;

        ArrayList<String> currOutput = new ArrayList<String>();
        currOutput.add(hotel_id);
        currOutput.add(name);
        currOutput.add(city);
        currOutput.add(address);
        BingHome home = new BingHome(driver);
        home.search(searchText);
        BingResults resPage = new BingResults(driver);
        List<String> results = resPage.getLinks();

        for (String link : results) {

            if (link.startsWith("https://www.expedia.com/") && link.endsWith("Hotel-Information")) {
                currOutput.add(link);
                driver.get(link);
                Thread.sleep(5000);
                WebElement rate = driver.findElement(By.xpath("//*[@id=\"app-layer-base\"]/div[1]/div[2]/div[1]/div[2]/div[3]/div/div[1]/div/div[2]/div/h3"));
                currOutput.add(rate.getText());


                outputData.add(currOutput);
                takeScr.takeScreenShot("BeforeTheCheck"+x+".png");

                js.executeScript("document.getElementById('hotels-check-in-btn').click();");
                js.executeScript("document.querySelector('[aria-label=\"Oct 20, 2022\"]').click();");
                js.executeScript("document.getElementById('hotels-check-out-btn').click();");
                js.executeScript("document.querySelector('[aria-label=\"Oct 30, 2022\"]').click();");
                js.executeScript("document.querySelector('[data-stid=\"apply-date-picker\"]').click();");
                takeScr.takeScreenShot("AfterTheCheck"+x+".png");
                x++;
                
                break;
            }
        }




    }


        @BeforeMethod
    public void beforeMethod() {
        driver.get("https://www.bing.com/");
    }
    @AfterSuite
    public void afterSuite() {
        driver.quit();
        List<String[]> data = new ArrayList<String[]>();
        for(ArrayList<String> row: outputData) {
            String[] row_data = new String[row.size()];
            for(int i= 0;i<row.size();i++) {
                row_data[i] = row.get(i);
            }
            data.add(row_data);
        }
        String[] headers = new String[outputHeaders.size()];
        for(int i= 0;i<outputHeaders.size();i++) {
            headers[i] = outputHeaders.get(i);
        }
        WriteCsvFile.writeDataLineByLine("output.csv", data, headers);

    }
}