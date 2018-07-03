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
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class SubCatCrawler {
    private SubCatDescription description;
    private ArrayList<String> pages; // список адресов страниц
    private ArrayList<String> subCats;// список адресов подкатегорий
    private  ArrayList<SubCatCrawler> subCatCrawlers;//краулеры подкатегорий
    private SubCatCrawler parent;
    private int pagesCount;//актуально только у самого первого предка (категории), см getPagesCount, incPagesCount
    private static final String pageQuery = "https://ru.wikipedia.org/w/api.php?format=xml&action=query&prop=extracts&explaintext&exsectionformat=plain&pageids=";
    private static final String subcatQuery = "https://ru.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmprop=title|type|ids&cmlimit=50&cmtitle=Category:";
    public static final String pageURLprefix = "https://ru.wikipedia.org/wiki/";

    public SubCatCrawler(String filePath, String name, int number){
        description = new SubCatDescription(number);
        description.name = name;
        description.filePath = filePath;
        parent = null;
        description.addrURL = subcatQuery+description.name;
        pages = new ArrayList<>();
        subCats = new ArrayList<>();
        subCatCrawlers = new ArrayList<>(1);
        pagesCount = 0;
    }

    public SubCatCrawler(SubCatCrawler parent, String name, int num) {
        this.parent = parent;
        pages = new ArrayList<>();
        subCats = new ArrayList<>();
        subCatCrawlers = new ArrayList<>(1);
        description = new SubCatDescription(parent.description);
        description.name = name;
        description.localNumber = num;
        description.addrURL = subcatQuery+description.name;
        pagesCount = 0;
    }

    public void crawl(MultiThreadLauncher launcher) throws IOException {
        description.name = description.name.replace(":","_");
        description.filePath = description.filePath+"/"+description.name;
        File directory = new File(description.filePath);
        if(!directory.exists())
            directory.mkdirs();
        getChildren();
        crawlPages(launcher);
        }

    private void getChildren() throws IOException
    {
        InputStream pageStream = new URL(this.description.addrURL).openStream();
        XMLInputFactory FACTORY = XMLInputFactory.newInstance();
        try {
            XMLStreamReader reader = FACTORY.createXMLStreamReader(pageStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if (reader.getName().toString().equals("cm")) {
                        if (reader.getAttributeValue(3).equals("page")) {
                            pages.add(reader.getAttributeValue(0));
                        }
                        else if (reader.getAttributeValue(3).equals("subcat")) {
                            subCats.add(reader.getAttributeValue(2).substring(10).replace(" ","_"));
                        }
                    }
                }
            }
            //Collections.sort(pages);
            Collections.sort(subCats);
        }
            catch (XMLStreamException e) {
            e.printStackTrace();
            }
    }

    private void crawlPages(MultiThreadLauncher launcher) {
       for (int i=0; i < pages.size();i++) {
           incPagesCount();
           launcher.enqueue(new MiniCrawler(this, pageQuery + pages.get(i), i));
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
