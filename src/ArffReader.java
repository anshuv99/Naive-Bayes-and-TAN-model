import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ArffReader {
    public static List<List<String>> readArff(String fileName, List<Instance> instanceList, List<List<String>> data) throws IOException {
        FileReader file = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(file);
        String line = "";
        int index = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("@attribute")) {
                String[] attributeLineSplit = line.split(" ", 3);
                if (!attributeLineSplit[2].contains("{")) {
                    Instance attr = new Instance();
                    attr.setName(attributeLineSplit[1].replace("\'", " "));
                    attr.setType("real");
                    attr.index = index;
                    instanceList.add(attr);
                }
            else {
                    Instance attr = new Instance();
                    attr.setName(attributeLineSplit[1].replace("\'", " "));
                    attr.setType("class");
                    attr.index = index;
                    String attributeValueList = attributeLineSplit[2].replace("{", "");
                    attributeValueList = attributeValueList.replace("}", "");
                    String []attributeValues = attributeValueList.split(",");
                    attr.setValues(Arrays.asList(attributeValues));
                    instanceList.add(attr);

                }
                index++;
            }
            else if ( !line.startsWith("@data") && !line.startsWith("@relation") && !line.startsWith("%")){
                data.add(Arrays.asList(line.split(",")));
            }
        }
        return data;
    }

    public static List<List<String>> readTestArff(String fileName, List<List<String>> data) throws IOException {

        FileReader file = new FileReader(fileName);
        BufferedReader bufferedReader = new BufferedReader(file);
        String line = "";
        int index = 0;
        while ((line = bufferedReader.readLine()) != null) {
            if(line.startsWith("@attribute")){
                index++;
            }
            else if(!line.startsWith("@data") && !line.startsWith("@relation") && !line.startsWith("%")){
                data.add(Arrays.asList(line.split(",")));
            }
        }
        return data;
    }
}
