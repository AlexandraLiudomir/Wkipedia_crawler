import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;

public class MiniCrawler  {
    private ArticleDescription description;
    private SubCatCrawler parent;

    public MiniCrawler(SubCatCrawler parent, String pageURL, int localNum) throws MalformedURLException {
        super();
        this.description = new ArticleDescription(parent.getDescription());
        this.description.addrURL = new String(pageURL);
        this.parent = parent;
        this.description.localNumber = localNum;
    }

    public void load() throws IOException {
        loadToTxt();
        loadToCSV();
    }

    private void loadToTxt() throws IOException {
        URL address = new URL(this.description.addrURL);
        InputStream pageStream = address.openStream();
        BufferedOutputStream out = null;
        boolean gotpage = false;
        XMLInputFactory FACTORY = XMLInputFactory.newInstance();
        String text;
        description.filename = parent.getID()+"_"+description.localNumber;
        try {
            XMLStreamReader reader = FACTORY.createXMLStreamReader(pageStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if ((!gotpage)&&(event == XMLEvent.START_ELEMENT)) {
                    if (reader.getName().toString() == "page") {
                        description.pageid = reader.getAttributeValue(1).toString();
                        description.name = new String(reader.getAttributeValue(3));
                        description.addrURL = SubCatCrawler.pageURLprefix+new String(description.name.replace(" ","_"));

                        out = new BufferedOutputStream(new FileOutputStream(description.filePath+"/"+description.filename.toString()+".txt"));
                        gotpage = true;
                    }
                }
                else if ((gotpage)&&(reader.hasText())) {
                    text = reader.getText();
                    out.write(text.getBytes());
                    description.sizeSymbol += text.length();
                }
            }
            out.flush();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }

    private void loadToCSV() throws IOException, NullPointerException {
        CSVWriter.getInstance().write(description);
    }

}
