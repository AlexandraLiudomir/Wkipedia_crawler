public class SubCatDescription {
    public String name;
    public String pageid;
    public Integer level;
    public String addrURL;
    public String filePath;
    public Integer localNumber;
    public Integer category;

    public SubCatDescription(int number){
        name = new String();
        level = 0;
        addrURL = new String();
        filePath = new String();
        localNumber = number;
        pageid = new String();
        category = localNumber;
    }
    public SubCatDescription(SubCatDescription parentDescription){
        name = new String();
        level = parentDescription.level + 1;
        addrURL = new String();
        filePath = new String(parentDescription.filePath);
        localNumber = 0;
        pageid = new String();
        category = parentDescription.category;
    }

}
