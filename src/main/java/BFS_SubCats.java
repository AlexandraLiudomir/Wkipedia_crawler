import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BFS_SubCats { // Breadth first search through subcategories tree - main class of the application
    private ArrayList<String> startingCats;
    private int maxPages;
    private String path;
    private MultiThreadLauncher threadLauncher;

    public BFS_SubCats(int threadCount, int period, int maxPages, String path, ArrayList<String> categories) {
        startingCats = categories;
        this.maxPages = maxPages;
        this.path = path;
        threadLauncher = new MultiThreadLauncher(threadCount,period);
    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        ArrayList<String> catsToCrawl = new ArrayList<>();
        int maxpages = 400;
        int threadCount = 1;
        int period = 1;
        String path = "";
        String csvName = "";

        try {
            InputStream settingsStream = new FileInputStream("src/settings.xml");
            XMLInputFactory FACTORY = XMLInputFactory.newInstance();
            XMLStreamReader reader = FACTORY.createXMLStreamReader(settingsStream);
            while (reader.hasNext()) {       // while not end of XML
                int event = reader.next();   // read next event
                if (event == XMLEvent.START_ELEMENT) {
                    switch (reader.getName().toString()){
                        case "threads": {
                            threadCount = Integer.parseInt(reader.getAttributeValue(reader.getNamespaceURI(),"count"));
                            period = Integer.parseInt(reader.getAttributeValue(reader.getNamespaceURI(),"period"));
                        }break;
                        case "Path":{
                            path = reader.getAttributeValue(reader.getNamespaceURI(),"folderPath");
                            csvName = reader.getAttributeValue(reader.getNamespaceURI(),"csvName");
                        }break;
                        case "cat":{
                            catsToCrawl.add(reader.getAttributeValue(reader.getNamespaceURI(),"name"));
                        }break;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        new BFS_SubCats(threadCount,period, maxpages, path, catsToCrawl).runCrawling(csvName);
        System.out.println(System.currentTimeMillis()-startTime);
    }

    public void runCrawling(String csvName){ //main procedure
        try {
            File directory = new File(path);
            if(!directory.exists())
                directory.mkdirs();
            CSVWriter.getInstance().setPath(path+"/"+csvName);

            for (int i =0; i< startingCats.size();i++){
                breadthFirstSearch(startingCats.get(i),i);
            }
            threadLauncher.terminate();
            CSVWriter.getInstance().terminate();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // Collecting pages in one category:
    //which is an adaptation of breadth first search (instead of searching target node we are trying to collect 400 pages)
    public void breadthFirstSearch(String startCat, int number) throws IOException {
        Integer stopLevel = null; // level on which we have collected pages count we wanted
        boolean goFurther = true; // should we keep adding subcats to queue
        SubCatCrawler currentNode;
        SubCatCrawler initNode = new SubCatCrawler(path, startCat, number);
        ConcurrentLinkedQueue<SubCatCrawler> queue = new ConcurrentLinkedQueue<>();
        queue.add(initNode);
        while (!queue.isEmpty()) {
            currentNode = queue.poll();
            if (!totalStop(stopLevel, currentNode.getLevel())) {
                currentNode.crawl(threadLauncher);
                if (stopLevel == null) {
                    if (stopCondition(currentNode)) {
                        goFurther = false;
                        stopLevel = currentNode.getLevel();
                    }
                }
            } else {
                break;
            }

            if (goFurther) {
                queue.addAll(currentNode.getChildren());
            }
        }
    }

    private boolean stopCondition(SubCatCrawler node){
        return (node.getPagesCount()>=maxPages);
    }

    private boolean totalStop(Integer stopLevel,  int curLevel){
       if((stopLevel==null)||(curLevel <= stopLevel)){ // if we have not meet the goal pages number yet or have not finished the level where we met the goal
           return false; // check further
       }
       else {
           return true;//full stop if we finished the level where we met the goal and went to deeper level
       }
    }
}
