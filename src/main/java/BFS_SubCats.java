import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BFS_SubCats { // Breadth first search through subcategories tree
    private ArrayList<String> startingCats;
    private ArrayList<SubCatCrawler> subCats;
    private int maxPages;
    private String path;
    private ExecutorService service;

    public ArrayList<MiniCrawler> crawlers;

    public BFS_SubCats(int threadCount, String path, String[] categories){
        subCats = new ArrayList<SubCatCrawler>();
        startingCats = new ArrayList<String >();
        for (int i=0;i<categories.length;i++) {
            startingCats.add(new String(categories[i]));
        }
        maxPages = 400;
        this.path = new String(path);
        service = Executors.newFixedThreadPool(threadCount);
    }

    public static void main(String[] args) {
        long runtime = System.currentTimeMillis();
        String[] catsToCrawl = new String[3];
        catsToCrawl[0] = "Игры";
        catsToCrawl[1] = "Культура";
        catsToCrawl[2] = "Наука";
        try {
            BFS_SubCats mainRunner = new BFS_SubCats(10,"D:\\Wikipedia_Crawler", catsToCrawl);
            for (int i =0; i< mainRunner.startingCats.size();i++){
            mainRunner.breadthFirstSearch(mainRunner.startingCats.get(i),i);
            }
            mainRunner.service.shutdown();
            mainRunner.service.awaitTermination(5, TimeUnit.MINUTES);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(System.currentTimeMillis()-runtime);
    }

    public void runCrawling(){

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
                    currentNode.crawlOnce(service);
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
