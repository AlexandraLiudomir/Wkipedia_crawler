import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class SubCatCrawler {
    private SubCatDescription description;
    private TreeMap<String,String> pages; // key is pagename, value is pageID (because it is sorted by key)
    private ArrayList<String> subCats;//child subcats name list
    private  ArrayList<SubCatCrawler> subCatCrawlers;
    private SubCatCrawler parent;
    private int pagesCount;//contains the number only for highest parent (category). subcats have zero here and if asked, they go upwards to that parent (look: getPagesCount, incPagesCount)
    private static final String pageQuery = "https://ru.wikipedia.org/w/api.php?format=xml&action=query&prop=extracts&explaintext&exsectionformat=plain&pageids=";
    private static final String subcatQuery = "https://ru.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmprop=title|type|ids&cmlimit=50&cmtitle=Category:";

    public SubCatCrawler(String filePath, String name, int number){
        description = new SubCatDescription(number);
        description.name = name;
        description.filePath = filePath;
        parent = null;
        description.addrURL = subcatQuery+description.name.replace(" ","_");
        pages = new TreeMap<>();
        subCats = new ArrayList<>();
        subCatCrawlers = new ArrayList<>(1);
        pagesCount = 0;
    }

    public SubCatCrawler(SubCatCrawler parent, String name, int num) {
        this.parent = parent;
        pages = new TreeMap<>();
        subCats = new ArrayList<>();
        subCatCrawlers = new ArrayList<>(1);
        description = new SubCatDescription(parent.description);
        description.name = name;
        description.localNumber = num;
        description.addrURL = subcatQuery+description.name;
        pagesCount = 0;
    }

    public void crawl(MultiThreadLauncher launcher) throws IOException {
        description.name = getID()+"_"+description.name.replace(":","_");
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
            while (reader.hasNext()) {
                int event = reader.next();
                if (event == XMLEvent.START_ELEMENT) {
                    if (reader.getName().toString().equals("cm")) {
                        if (reader.getAttributeValue(3).equals("page")) {
                            pages.put(reader.getAttributeValue(2), reader.getAttributeValue(0));
                        }
                        else if (reader.getAttributeValue(3).equals("subcat")) {
                            subCats.add(reader.getAttributeValue(2).substring(10).replace(" ","_"));
                        }
                    }
                }
            }
            Collections.sort(subCats);
        }
            catch (XMLStreamException e) {
            e.printStackTrace();
            }
    }

    private void crawlPages(MultiThreadLauncher launcher) {
        int i = 0;
        for(Map.Entry entry: pages.entrySet()) {
           incPagesCount();
           launcher.enqueue(new MiniCrawler(this, pageQuery + entry.getValue().toString(), entry.getKey().toString(), i));
           i++;
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
            return parent.getID() + "_" + String.format("%03d",id);
        }
        else{
            return String.format("%02d", id);
        }
    }

    public SubCatDescription getDescription(){
        return this.description;
    }

}
