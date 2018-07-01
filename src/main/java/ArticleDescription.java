public class ArticleDescription extends  SubCatDescription{
    public int sizeSymbol;
    public int totalNumber;

    public ArticleDescription(SubCatDescription parentDescription){
        super();
        this.sizeSymbol = 0;
        this.level = parentDescription.level;
        this.filePath = parentDescription.filePath;
    }

}
