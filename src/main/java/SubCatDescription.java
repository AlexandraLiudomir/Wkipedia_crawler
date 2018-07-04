public class SubCatDescription {
    public String name;
    public Integer level;
    public String addrURL;
    public String filePath;
    public Integer localNumber;
    public Integer category;

    public SubCatDescription(int number){
        name = null;
        level = 0;
        addrURL = null;
        filePath = null;
        localNumber = number;
        category = localNumber;
    }
    public SubCatDescription(SubCatDescription parentDescription){
        name = null;
        level = parentDescription.level + 1;
        addrURL = null;
        filePath = parentDescription.filePath;
        localNumber = 0;
        category = parentDescription.category;
    }

}
