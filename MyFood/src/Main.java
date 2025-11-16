import easyaccept.EasyAccept;

public class Main {
    public static void main(String[] args) {
        String facade = "myfood.Facade";

        //Milestone 1
        EasyAccept.main(new String[]{facade, "tests/us1_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/us1_2.txt"});
        EasyAccept.main(new String[]{facade, "tests/us2_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/us2_2.txt"});
        EasyAccept.main(new String[]{facade, "tests/us3_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/us3_2.txt"});
        EasyAccept.main(new String[]{facade, "tests/us4_1.txt"});
        EasyAccept.main(new String[]{facade, "tests/us4_2.txt"});

        //Milestone 2
        //EasyAccept.main(new String[]{facade, "tests/us5_1.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us5_2.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us6_1.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us6_2.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us7_1.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us7_2.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us8_1.txt"});
        //EasyAccept.main(new String[]{facade, "tests/us8_2.txt"});
    }
}