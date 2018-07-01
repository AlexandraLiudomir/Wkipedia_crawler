import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SubCatCrawler {
    private SubCatDescription description;
    private ArrayList<String> pages; // список адресов страниц
    private ArrayList<String> subCats;// список адресов подкатегорий
    private ArrayList<MiniCrawler> pageCrawlers;//краулеры страниц
    private  ArrayList<SubCatCrawler> subCatCrawlers;//краулеры подкатегорий
    public SubCatCrawler parent;
    private int pagesCount;//актуально только у самого первого предка (категории), см getPagesCount, incPagesCount
    public static final String pageQuery = "https://ru.wikipedia.org/w/api.php?format=xml&action=query&prop=extracts&explaintext&exsectionformat=plain&pageids=";
    public static final String subcatQuery = "https://ru.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmprop=title|type|ids&cmlimit=50&cmtitle=Category:";

    public SubCatCrawler(String filePath, String name, int number){
        description = new SubCatDescription(number);
        description.name = name;
        description.filePath = filePath;
        parent = null;
        description.addrURL = subcatQuery+description.name;
        pages = new ArrayList<String>();
        subCats = new ArrayList<String>();
        pageCrawlers = new ArrayList<MiniCrawler>(1);
        subCatCrawlers = new ArrayList<SubCatCrawler>(1);
        pagesCount = 0;
    }

    public SubCatCrawler(SubCatCrawler parent, String name, int num) {
        this.parent = parent;
        pages = new ArrayList<String>();
        subCats = new ArrayList<String>();
        pageCrawlers = new ArrayList<MiniCrawler>(1);
        subCatCrawlers = new ArrayList<SubCatCrawler>(1);
        description = new SubCatDescription(parent.description);
        description.name = name;
        description.localNumber = num;
        description.addrURL = subcatQuery+description.name;
        pagesCount = 0;
    }

    public void crawlOnce(ExecutorService service) throws IOException {
        description.name = description.name.replace(":","_");
        description.filePath = description.filePath+"/"+description.name;
        File directory = new File(description.filePath);
        if(!directory.exists())
            directory.mkdirs();
        getChildren();
        crawlPages(service);
        }

    private void getChildren() throws IOException
    {
        InputStream pageStream = new URL(this.description.addrURL).openStream();
        BufferedOutputStream out = null;
        XMLInputFactory FACTORY = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = FACTORY.createXMLStreamReader(pageStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if (reader.getName().toString() == "cm") {
                        if (reader.getAttributeValue(3).toString().equals("page")) {
                            pages.add(reader.getAttributeValue(0).toString());
                        }
                        else if (reader.getAttributeValue(3).toString().equals("subcat")) {
                            subCats.add(reader.getAttributeValue(2).toString().substring(10).replace(" ","_"));
                        }
                    }
                }
            }
        }
            catch (XMLStreamException e) {
            e.printStackTrace();
            }
    }

    private void crawlPages(ExecutorService service) throws IOException {
       for (int i=0; i < pages.size();i++) {
           pageCrawlers.add(new MiniCrawler(this, pageQuery + pages.get(i), i));
           incPagesCount();
       }
        for (int i=0; i < pageCrawlers.size();i++){
            //pageCrawlers.get(i).loadToTxt();
            service.submit(pageCrawlers.get(i));
        }
    }
    public void createChildren(){
        for (int i=0; i < subCats.size();i++){
            subCatCrawlers.add(new SubCatCrawler(this, subCats.get(i), i));
        }
    }

    public SubCatCrawler createChild(int num){
        SubCatCrawler crawler = new SubCatCrawler(this, subCats.get(num), num);
        subCatCrawlers.add(crawler);
        return crawler;
    }

    public int childrenCount(){
        return subCats.size();
    }

    public int getPagesCount(){
        if (parent==null){
            return this.pagesCount;
        }
        else{
            return parent.getPagesCount();
        }
    }

    public void incPagesCount(){
        if (parent==null){
            this.pagesCount++;
        }
        else{
            parent.incPagesCount();
        }
    }

    public int getLevel(){
        return this.description.level;
    }

    public String getID(){
        Integer id = this.description.localNumber;
        if (parent != null) {
            return parent.getID() + "_" + id.toString();
        }
        else{
            return id.toString();
        }
    }

    public SubCatDescription getDescription(){
        return this.description;
    }

}
