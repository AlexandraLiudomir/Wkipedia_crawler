import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MultiThreadLauncher {
    private int delay;
    private int threadCount;
    private ScheduledExecutorService service;
    private ConcurrentLinkedQueue<MiniCrawler> pagesToGo;
    public MultiThreadLauncher(int threadCount, int delay){
        this.threadCount = threadCount;
        this.delay = delay;
        service = Executors.newScheduledThreadPool(threadCount);
        pagesToGo = new ConcurrentLinkedQueue<>();
        go();
    }

    public void go(){
        for (int i = 0; i < threadCount; i++) {
            service.scheduleAtFixedRate(dequeue, 0, delay, TimeUnit.MILLISECONDS);
        }
    }

    final Runnable dequeue = ()-> {
        if (!pagesToGo.isEmpty()){
            try {
                pagesToGo.poll().load();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    public void enqueue(MiniCrawler page){
        pagesToGo.add(page);
    }

    public void terminate(){
        while (!pagesToGo.isEmpty()){ }
        service.shutdown();
        try {
            service.awaitTermination(50,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
