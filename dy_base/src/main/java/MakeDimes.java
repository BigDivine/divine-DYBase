import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class MakeDimes {

    public static void main(String[] args) {
        StringBuilder sw400 = new StringBuilder();
        //添加xml开始的标签
        String xmlStart = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                "<resources>\n";
        sw400.append(xmlStart);
        //添加内容
        for (int i = 0; i <= 200; i++) {
//            此处name后的标签名可以自定义"margin_"随意更改
            String start = "<dimen name=\"dy_" + i + "dp\">";
            String end = "dp</dimen>";
            sw400.append(start).append(i).append(end).append("\n");
        }
        int[] other=new int[]{210,220,230,240,250,300,400,500,600};
        for(int o : other){
            String start = "<dimen name=\"dy_" + o + "dp\">";
            String end = "dp</dimen>";
            sw400.append(start).append(o).append(end).append("\n");
        }

        sw400.append("\n");
        for (int j = 0; j <= 100; j++) {
//            此处name后的标签名可以自定义"margin_"随意更改
            String start = "<dimen name=\"dy_" + j + "sp\">";
            String end = "sp</dimen>";
            sw400.append(start).append(j).append(end).append("\n");
        }
        //添加xml的尾标签
        sw400.append("</resources>");
        String sw400file = "./dy_base/src/main/res/values/dimens.xml";
        writeFile(sw400file, sw400.toString());
    }

    public static void writeFile(String file, String text) {
        PrintWriter out = null;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            out.println(text);
        } catch (IOException e) {
            e.printStackTrace();
        }

        out.close();
    }
}
