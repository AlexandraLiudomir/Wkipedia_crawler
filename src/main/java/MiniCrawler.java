import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.net.URL;

public class MiniCrawler  {
    private ArticleDescription description;
    private SubCatCrawler parent;
    private static final String pageURLprefix = "https://ru.wikipedia.org/wiki/";

    public MiniCrawler(SubCatCrawler parent, String pageURL, String name, int localNum){
        super();
        this.description = new ArticleDescription(parent.getDescription());
        this.description.addrURL = pageURL;
        this.parent = parent;
        this.description.localNumber = localNum;
        this.description.name = name;
    }

    public void load() throws IOException {
        loadToTxt();
        loadToCSV();
    }

    private void loadToTxt() throws IOException {
        URL address = new URL(this.description.addrURL);
        InputStream pageStream = address.openStream();
        XMLInputFactory FACTORY = XMLInputFactory.newInstance();
        String text;
        boolean gowrite = false;
        description.filename = parent.getID()+"_"+String.format("%03d",description.localNumber);
        description.addrURL = pageURLprefix+description.name.replace(" ","_");
        BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(description.filePath+"/"+description.filename+".txt"));
        try {
            XMLStreamReader reader = FACTORY.createXMLStreamReader(pageStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if ((event == XMLEvent.START_ELEMENT)&&(reader.getName().toString().equals("page"))){
                    gowrite = true;
                }
                else if (gowrite && reader.hasText()) {
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
