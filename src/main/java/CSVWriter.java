import com.sun.istack.internal.NotNull;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class CSVWriter {
    private static CSVWriter ourInstance = new CSVWriter();

    public static CSVWriter getInstance() {
        return ourInstance;
    }
    private String path;
    private BufferedOutputStream out;

    private CSVWriter() { }

    public void setPath(@NotNull String path) throws IOException {
        this.path = path;
        out = new BufferedOutputStream(new FileOutputStream(path));
        out.write(new StringBuilder().append("File id").append(" , ")
                .append("Название статьи").append(" , ")
                .append("URL").append(" , ")
                .append("Категория").append(" , ")
                .append("Уровень").append(" , ")
                .append("Размер в символах").append("\n").toString().getBytes());
    }

    public void write(ArticleDescription page) throws IOException, NullPointerException {
        out.write(createRecord(page).getBytes());
        out.flush();
    }

    private String createRecord(ArticleDescription page){
        return new StringBuilder().append(page.filename).append(" , ")
                .append(page.name).append(" , ")
                .append(page.addrURL).append(" , ")
                .append(page.category.toString()).append(" , ")
                .append(page.level.toString()).append(" , ")
                .append(page.sizeSymbol.toString()).append("\n").toString();
       // return String.join(",",page.filename,page.name,page.addrURL,page.category.toString(),page.level.toString(),page.sizeSymbol.toString(), "\n");
    }

    public void terminate() throws IOException {
        out.flush();
        out.close();
    }
}
