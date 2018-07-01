public class ArticleDescription extends  SubCatDescription{
    public int sizeSymbol;

    public ArticleDescription(SubCatDescription parentDescription){
        super(parentDescription);
        this.sizeSymbol = 0;
        this.level = parentDescription.level;
        this.filePath = parentDescription.filePath;
    }

}
