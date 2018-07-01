public class SubCatDescription {
    public String name;
    public String pageid;
    public int level;
    public String addrURL;
    public String filePath;
    public int localNumber;

    public SubCatDescription(int number){
        name = new String();
        level = 0;
        addrURL = new String();
        filePath = new String();
        localNumber = number;
        pageid = new String();
    }
    public SubCatDescription(SubCatDescription parentDescription){
        name = new String();
        level = parentDescription.level + 1;
        addrURL = new String();
        filePath = new String(parentDescription.filePath);
        localNumber = 0;
        pageid = new String();
    }

}
