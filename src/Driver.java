import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Driver {
    private static List<Instance> instanceList = new ArrayList<>();
    private static List<List<String>> data = new ArrayList<>();
    private static List<List<String>> testData = new ArrayList<>();
    private static Map<String, Double> probabilityDistributions = new HashMap<>();
    private static Map<String, Double> weights = new TreeMap<>();
    private static DecimalFormat df = new DecimalFormat("0.000000000000");
    private static List<Instance> vertexList = new ArrayList<>();
    private static Map<String, Instance> parentList = new TreeMap<>();
    private static Map<String, Double> probabilityDistributionsTan = new TreeMap<>();

    static class weight {
        double w;
        Instance instance1;
        Instance instance2;

        weight(double w, Instance instance1, Instance instance2) {
            this.w = w;
            this.instance1 = instance1;
            this.instance2 = instance2;
        }
    }


    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Please enter trainfile testfile n|t");
            System.exit(1);
        }

        String trainingFileName = args[0];
        String testFile = args[1];
        String classifierType = args[2];
        ArffReader.readArff(trainingFileName, instanceList, data);
        ArffReader.readTestArff(testFile, testData);
        if (classifierType.equals("n")) {
            naiveBayes();
        } else if (classifierType.equals("t")) {
            tan();
        }
    }

    private static double tan() {
        String y1 = instanceList.get(instanceList.size() - 1).values.get(0).replace(" ", "");
        String y2 = instanceList.get(instanceList.size() - 1).values.get(1).replace(" ", "");
        calculateProbabilitiesForNB();
        computeWeights();
        prims();
        calculateProbabilitiesForTAN();
        int correctClassified = 0;
        int incorrectlyClassified = 0;
        System.out.println();
        for (List<String> line : testData) {
            double numerator = probabilityDistributions.get(y1);
            double denominator = probabilityDistributions.get(y2);
            int index = 0;
            for (String l : line) {
                if (index == instanceList.size() - 1)
                    break;
                if (parentList.containsKey(instanceList.get(index).name.replace(" ", ""))) {
                    Instance parent = parentList.get(instanceList.get(index).name.replace(" ", ""));
                    String parent_value = line.get(parent.index);
                    String key1 = (instanceList.get(index).name + "=" + l + "|" + parent.name + "=" + parent_value + ",Y=" + y1).replace(" ", "");
                    String key2 = (instanceList.get(index).name + "=" + l + "|" + parent.name + "=" + parent_value + ",Y=" + y2).replace(" ", "");
                    numerator *= probabilityDistributionsTan.get(key1);
                    denominator *= probabilityDistributionsTan.get(key2);
                } else {
                    String key1 = (instanceList.get(index).name + "=" + l + "|" + y1).replace(" ", "");
                    String key2 = (instanceList.get(index).name + "=" + l + "|" + y2).replace(" ", "");
                    numerator *= probabilityDistributions.get(key1);
                    denominator *= probabilityDistributions.get(key2);
                }
                index += 1;
            }
            double p_of_y1_x = getProbability(numerator, numerator + denominator);
            double p_of_y2_x = getProbability(denominator, numerator + denominator);

            if (p_of_y1_x >= p_of_y2_x) {
                System.out.println(y1.replace(" ", "") + " " + line.get(line.size() - 1).replace(" ", "") + " " + df.format(p_of_y1_x));
                if (y1.equals(line.get(line.size() - 1)))
                    correctClassified += 1;
                else
                    incorrectlyClassified += 1;
            } else {
                System.out.println(y2.replace(" ", "") + " " + line.get(line.size() - 1).replace(" ", "") + " " + df.format(p_of_y2_x));
                if (y2.equals(line.get(line.size() - 1)))
                    correctClassified += 1;
                else
                    incorrectlyClassified += 1;
            }
        }

        System.out.println("\n" +correctClassified);
        return (double) (correctClassified) / (correctClassified + incorrectlyClassified);

    }

    private static void calculateProbabilitiesForTAN() {

        String y1 = instanceList.get(instanceList.size() - 1).values.get(0);
        String y2 = instanceList.get(instanceList.size() - 1).values.get(1);
        for (Instance attr : instanceList) {
            for (String value1 : attr.values) {
                if (parentList.containsKey(attr.name.replace(" ", ""))) {
                    Instance parent_of_x = parentList.get(attr.name.replace(" ", ""));
                    for (String value2 : parent_of_x.values) {
                        int no_x1_parentx1_y1 = getInstancesConditionalTAN(attr.index, value1, parent_of_x.index, value2, instanceList.size() - 1, y1);
                        int no_x1_parentx1_y2 = getInstancesConditionalTAN(attr.index, value1, parent_of_x.index, value2,
                                instanceList.size() - 1, y2);
                        int no_parentx1_y1 = getInstancesConditional(parent_of_x.index, value2, instanceList.size() - 1, y1);
                        int no_parentx1_y2 = getInstancesConditional(parent_of_x.index, value2, instanceList.size() - 1, y2);
                        double p_x1_given_parentx1_y1 = getProbability(no_x1_parentx1_y1 + 1, no_parentx1_y1 + attr.values.size());
                        String indexString = (attr.name + "=" + value1 + "|" + parent_of_x.name + "=" + value2 + ",Y=" + y1).replace(" ", "");
                        probabilityDistributionsTan.put(indexString, p_x1_given_parentx1_y1);
                        double p_x1_given_parentx1_y2 = getProbability(no_x1_parentx1_y2 + 1, no_parentx1_y2 + attr.values.size());
                        indexString = (attr.name + "=" + value1 + "|" + parent_of_x.name + "=" + value2 + ",Y=" + y2).replace(" ", "");
                        probabilityDistributionsTan.put(indexString, p_x1_given_parentx1_y2);
                    }
                }
            }
        }
    }

    private static weight getMaximumWeightEdge(List<Instance> vertexListTemp) {
        List<weight> weight_list = new ArrayList<>();
        for (Instance v : vertexListTemp) {
            for (Instance attr : instanceList) {
                if (attr.name.replace(" ", "").equals(v.name.replace(" ", "")) || attr.name.replace(" ", "").equalsIgnoreCase("class") || v.name.replace(" ", "").equalsIgnoreCase("class"))
                    continue;
                String indexStr = (v.name + "," + attr.name + "|Y").replace(" ", "");
                if (!vertexListTemp.contains(attr))
                    weight_list.add(new weight(weights.get(indexStr), v, attr));
            }
        }
        weight_list.sort((o1, o2) -> {
            if (o1.w <= o2.w)
                return -1;
            else return 1;
        });
        double highest_weight = weight_list.get(weight_list.size() - 1).w;
        for (weight w : weight_list) {
            if (w.w == highest_weight)
            return w;
        }
        throw new RuntimeException("Something wrong here");
    }

    private static void prims() {
        vertexList.add(instanceList.get(0));
        while (vertexList.size() < instanceList.size() - 1) {
            weight edge = getMaximumWeightEdge(vertexList);
            vertexList.add(edge.instance2);
            parentList.put(edge.instance2.name.replace(" ",""), edge.instance1);
        }
        for (Instance attr : instanceList) {
            if (attr.name.replace(" ", "").equalsIgnoreCase("class")) {
                break;
            }
            if (parentList.containsKey(attr.name.replace(" ", ""))) {
                System.out.println(attr.name.replace(" ", "") + " " + parentList.get(attr.name.replace(" ", "")).name.replace(" ", "") + " " + instanceList.get(instanceList.size() - 1).name);
            } else {
                System.out.println(attr.name.replace(" ", "") + " " + instanceList.get(instanceList.size() - 1).name.replace(" ", ""));
            }
        }

    }

    private static double log(double x) {
        return (Math.log(x) / Math.log(2));
    }

    private static void computeWeights() {
        String y1 = instanceList.get(instanceList.size() - 1).values.get(0);
        String y2 = instanceList.get(instanceList.size() - 1).values.get(1);
        Integer no_of_y1 = getInstances(instanceList.size() - 1, y1);
        Integer no_of_y2 = getInstances(instanceList.size() - 1, y2);
        double summation = 0;
        for (Instance attr1 : instanceList) {
            if (attr1.index == instanceList.size() - 1)
                continue;
            for (Instance attr2 : instanceList) {
                if (attr2.index == instanceList.size() - 1)
                    continue;
                if (attr1.name.equals(attr2.name)) {
                }
                else {
                    for (String value1 : attr1.values) {
                        for (String value2 : attr2.values) {
                            int no_x1_x2_y1 = getInstancesConditionalTAN(attr1.index, value1, attr2.index, value2,
                                    instanceList.size() - 1, y1);
                            double p_no_x1_x2_y1 = getProbability(no_x1_x2_y1 + 1, data.size() + (attr1.values.size() * attr2.values.size() * 2));
                            double p_x1_x2_given_y1 = getProbability(no_x1_x2_y1 + 1,
                                    no_of_y1 + (attr1.values.size() * attr2.values.size()));

                            int no_x1_x2_y2 = getInstancesConditionalTAN(attr1.index, value1, attr2.index, value2,
                                    instanceList.size() - 1, y2);
                            double p_no_x1_x2_y2 = getProbability(no_x1_x2_y2 + 1,
                                    data.size() + (attr1.values.size() * attr2.values.size() * 2));
                            double p_x1_x2_given_y2 = getProbability(no_x1_x2_y2 + 1,
                                    no_of_y2 + (attr1.values.size() * attr2.values.size()));

                            String keyIndex = (attr1.name + "=" + value1 + "|" + y1).replace(" ", "");
                            double p_x1_y1 = probabilityDistributions.get(keyIndex);
                            keyIndex = (attr2.name + "=" + value2 + "|" + y2).replace(" ", "");
                            double p_x2_y2 = probabilityDistributions.get(keyIndex);
                            keyIndex = (attr1.name + "=" + value1 + "|" + y2).replace(" ", "");
                            double p_x1_y2 = probabilityDistributions.get(keyIndex);
                            keyIndex = (attr2.name + "=" + value2 + "|" + y1).replace(" ", "");
                            double p_x2_y1 = probabilityDistributions.get(keyIndex);

                            double sum1 = p_no_x1_x2_y1 * log((p_x1_x2_given_y1) / (p_x1_y1 * p_x2_y1));
                            double sum2 = p_no_x1_x2_y2 * log((p_x1_x2_given_y2) / (p_x1_y2 * p_x2_y2));
                            summation += sum1 + sum2;
                        }
                    }
                    String keyString = (attr1.name + "," + attr2.name + "|Y").replace(" ", "");
                    weights.put(keyString, summation);
                    summation = 0;
                }
            }
        }
    }

    private static int getInstancesConditionalTAN(int attribute1Index, String attribute1Value, int attribute2Index, String attribute2Value, int attribute3Index,
                                                  String attribute3Value) {

        int count = 0;
        for (List<String> line : data) {
            if (line.get(attribute1Index).replace(" ", "").equals(attribute1Value.replace(" ", "")) && line.get(attribute2Index).replace(" ", "").equals(attribute2Value.replace(" ", ""))
                    && line.get(attribute3Index).replace(" ", "").equals(attribute3Value.replace(" ", "")))
                count += 1;
        }
        return count;
    }

    private static double naiveBayes() {
        calculateProbabilitiesForNB();
        for (Instance attr : instanceList) {
            if (attr.index == instanceList.size() - 1) {
                break;
            }
            System.out.println(attr.name.replace(" ", "") + " " + instanceList.get(instanceList.size() - 1).name.replace(" ", ""));
        }
        String y1 = instanceList.get(instanceList.size() - 1).values.get(0);
        String y2 = instanceList.get(instanceList.size() - 1).values.get(1);
        instanceList.sort(new Comparator<Instance>() {
            @Override
            public int compare(Instance o1, Instance o2) {
                return 0;
            }
        });
        int correctClassified = 0;
        int incorrectlyClassified = 0;
        System.out.println();
        for (List<String> line : testData) {
            Double numerator = probabilityDistributions.get(y1.replace(" ", ""));
            Double denominator = probabilityDistributions.get(y2.replace(" ", ""));
            int index = 0;
            for (String l : line) {
                if (index == instanceList.size() - 1)
                    break;
                String keyString1 = instanceList.get(index).name + "= " + l + "|" + y1;
                numerator *= probabilityDistributions.get(keyString1.replace(" ", ""));
                String keyString2 = instanceList.get(index).name + "=" + l + "|" + y2;
                denominator *= probabilityDistributions.get(keyString2.replace(" ", ""));
                index += 1;
            }
            double p_of_y1_line = getProbability(numerator, numerator + denominator);
            double p_of_y2_line = getProbability(denominator, numerator + denominator);
            if (p_of_y2_line > p_of_y1_line) {
                System.out.println(y2.replace(" ", "") + " " + line.get(line.size() - 1) + " " + df.format(p_of_y2_line));
                if (y2.replace(" ", "").equals(line.get(line.size() - 1).replace(" ", ""))) {
                    correctClassified += 1;
                } else {
                    incorrectlyClassified += 1;
                }
            } else {
                System.out.println(y1.replace(" ", "") + " " + line.get(line.size() - 1) + " " + df.format(p_of_y1_line));
                if (y1.replace(" ", "").equals(line.get(line.size() - 1).replace(" ", "")))
                    correctClassified += 1;
                else
                    incorrectlyClassified += 1;
            }
        }

        System.out.println("\n"+correctClassified);
        return (double) (correctClassified) / (incorrectlyClassified + correctClassified);
    }

    private static Double getProbability(Integer instance_count, Integer total_instances) {
        return (double) instance_count / total_instances;
    }

    private static Double getProbability(Double instance_count, Double total_instances) {
        return (double) instance_count / total_instances;
    }

    private static Integer getInstancesConditional(int attribute1Index, String attribute1Value, int attribute2Index, String attribute2Value) {
        int count = 0;
        for (List<String> line : data) {
            if (line.get(attribute1Index).replace(" ", "").equals(attribute1Value.replace(" ", "")) && line.get(attribute2Index).replace(" ", "").equals(attribute2Value.replace(" ", "")))
                count += 1;
        }
        return count;
    }

    private static void calculateProbabilitiesForNB() {
        String y1 = instanceList.get(instanceList.size() - 1).values.get(0);
        String y2 = instanceList.get(instanceList.size() - 1).values.get(1);
        Integer no_of_y1 = getInstances(instanceList.size() - 1, y1);
        Integer no_of_y2 = getInstances(instanceList.size() - 1, y2);
        Double p_of_y1 = getProbability(no_of_y1 + 1, data.size() + 2);
        Double p_of_y2 = getProbability(no_of_y2 + 1, data.size() + 2);
        probabilityDistributions.put(y1.replace(" ", ""), p_of_y1);
        probabilityDistributions.put(y2.replace(" ", ""), p_of_y2);
        List<List<String>> countList = new ArrayList<>();
        for (Instance attr : instanceList) {
            int totalCount1 = 0;
            for (String value : attr.values) {
                int n1 = getInstancesConditional(attr.getIndex(), value, instanceList.size() - 1, y1);
                int n2 = getInstancesConditional(attr.getIndex(), value, instanceList.size() - 1, y2);
                countList.add(Arrays.asList(value, String.valueOf(n1 + 1), String.valueOf(n2 + 1)));
                totalCount1 += 1;
            }
            for (List<String> c : countList) {
                double p_of_x_y1 = getProbability(Integer.valueOf(c.get(1)), no_of_y1 + totalCount1);
                double p_of_x_y2 = getProbability(Integer.valueOf(c.get(2)), no_of_y2 + totalCount1);
                String index1 = attr.name + "=" + c.get(0) + "|" + y1;
                probabilityDistributions.put(index1.replace(" ", ""), p_of_x_y1);
                String index2 = attr.name + "=" + c.get(0) + "|" + y2;
                probabilityDistributions.put(index2.replace(" ", ""), p_of_x_y2);
            }
            countList = new ArrayList<>();
        }
    }

    private static Integer getInstances(Integer attributeIndex, String attributeValue) {
        int count = 0;
        for (List<String> line : data)
            if (line.get(attributeIndex).replace(" ", "").equals(attributeValue.replace(" ", ""))) {
                count += 1;
            }
        return count;
    }

}
