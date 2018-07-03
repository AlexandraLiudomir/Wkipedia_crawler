public class ArticleDescription extends  SubCatDescription{
    public Integer sizeSymbol;
    public String filename;


    public ArticleDescription(SubCatDescription parentDescription){
        super(parentDescription);
        this.sizeSymbol = 0;
        this.level = parentDescription.level;
        this.filePath = parentDescription.filePath;
        this.filename = new String();
    }

}
