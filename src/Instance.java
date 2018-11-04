import java.util.ArrayList;
import java.util.List;

public class Instance {
     String name;
     String type;
     List<String> values;
     int index = 0;

    public Instance() {
        this.name = "";
        this.values = new ArrayList<>();
        this.type = "";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<String> getValues() {
        return values;
    }

    public void setValues(List<String> values) {
        this.values = values;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return "Instance{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", values=" + values +
                ", index=" + index +
                '}';
    }
}
