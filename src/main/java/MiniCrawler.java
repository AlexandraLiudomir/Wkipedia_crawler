import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MiniCrawler  implements Runnable {
    private URL address;
    private int num;
    private ArticleDescription description;
    private SubCatCrawler parent;

    public MiniCrawler(SubCatCrawler parent, String pageURL, int localNum) throws MalformedURLException {
        super();
        this.description = new ArticleDescription(parent.getDescription());
        this.address = new URL(pageURL);
        this.parent = parent;
        this.num = localNum;
    }

    public void update(SubCatCrawler parent, String pageURL, int localNum) throws MalformedURLException {
        this.description = new ArticleDescription(parent.getDescription());
        this.address = new URL(pageURL);
        this.parent = parent;
        this.num = localNum;
    }

    @Override
    public void run() {
        //System.out.println(Thread.currentThread().getId());
        try {
            loadToTxt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadToTxt() throws IOException {
        InputStream pageStream = address.openStream();
        BufferedOutputStream out = null;
        boolean gotpage = false;
        XMLInputFactory FACTORY = XMLInputFactory.newInstance();
        this.description.addrURL = address.toString();
        try {
            XMLStreamReader reader = FACTORY.createXMLStreamReader(pageStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if ((!gotpage)&&(event == XMLEvent.START_ELEMENT)) {
                    if (reader.getName().toString() == "page") {
                        description.pageid = reader.getAttributeValue(1).toString();
                        description.name = new String(reader.getAttributeValue(3));
                        out = new BufferedOutputStream(new FileOutputStream(description.filePath+"/"+parent.getID()+'_'+new Integer(num).toString()+".txt"));
                        gotpage = true;
                    }
                }
                else if ((gotpage)&&(reader.hasText())) {
                    out.write(reader.getText().getBytes());
                }
            }
            out.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    public void loadToCSV(String pathCSV){

    }

}
