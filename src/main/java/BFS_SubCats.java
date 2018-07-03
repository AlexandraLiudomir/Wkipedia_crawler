import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BFS_SubCats { // Breadth first search through subcategories tree
    private ArrayList<String> startingCats;
    private LinkedList<SubCatCrawler> subCats;
    private int maxPages;
    private String path;
    public MultiThreadLauncher threadLauncher;

    public BFS_SubCats(int threadCount, int delay, int maxPages, String path, String[] categories) {
        subCats = new LinkedList<>();
        startingCats = new ArrayList<>();

        for (int i=0;i<categories.length;i++) {
            startingCats.add(new String(categories[i]));
        }
        this.maxPages = maxPages;
        this.path = new String(path);



        threadLauncher = new MultiThreadLauncher(threadCount,delay);

    }

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        String[] catsToCrawl = {"Игры" /*,Искусство","Автомобили"*/};
        int maxpages = 400;
        int threadCount = 10;
        int delay = 1;
        String path = "D:\\Wikipedia_Crawler";
        String csvName = "summary.csv";
        new BFS_SubCats(threadCount,delay, maxpages, path, catsToCrawl).runCrawling(csvName);
        System.out.println(System.currentTimeMillis()-startTime);
    }

    public void runCrawling(String csvName){
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
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    // Сбор статей по одной категории:
    // модиификация обхода дерева в ширину (вместо поиска определенного узла пытаемя набрать 400 страниц)
    public void breadthFirstSearch(String startCat, int number) throws IOException, InterruptedException {
            Integer stopLevel = null; // уровень, на котором набралось требуемое число страниц
            boolean goFurther = true; // добавляем ли дочерние узлы в очередь
            SubCatCrawler currentNode;
            subCats.clear();
            subCats.add(new SubCatCrawler(path,startCat,number));
            ConcurrentLinkedQueue<SubCatCrawler> queue = new ConcurrentLinkedQueue<SubCatCrawler>();
            queue.add(subCats.get(0));
            while(! queue.isEmpty() ) {
                currentNode = queue.poll();
                if (!totalStop(stopLevel,currentNode.getLevel())){
                    currentNode.crawl(threadLauncher);
                    if (stopLevel == null){
                        if(stopCondition(currentNode)){
                            goFurther = false;
                            stopLevel = currentNode.getLevel();
                        }
                    }
                }
                else
                {
                    break;
                }

                if (goFurther) {
                    for (int i = 0; i < currentNode.childrenCount();i++ ) {    // все преемники текущего узла, ...
                        subCats.add(currentNode.createChild(i));
                        queue.add(currentNode.createChild(i));
                        }
                    }
                }
    }

    private boolean stopCondition(SubCatCrawler node){
        return (node.getPagesCount()>=maxPages);
    }

    private boolean totalStop(Integer stopLevel,  int curLevel){
       if((stopLevel==null)||(curLevel <= stopLevel)){ // если проверили не все страницы на уровне остановки,
           return false; // проверяем дальше
       }
       else {
           return true;//полная остановка, если проверили все узлы на уровне,
           // на котором получили нужное число страниц и перешли к следующему
       }
    }
}
