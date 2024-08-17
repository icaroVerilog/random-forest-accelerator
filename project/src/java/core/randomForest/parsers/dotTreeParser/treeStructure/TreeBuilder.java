package project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure;

import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.core.randomForest.parsers.dotTreeParser.treeStructure.Nodes.OuterNode;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TreeBuilder {

    private final static String  LINKED_NODE_ARROW           = "->";
    private final static String  LABEL_STRING                = "label";
    private final static String  INNER_NODE_INDICATOR_STRING = "ngini";
    private final static String  LABEL_EQUAL_STRING          = "label=";
    private final static Integer LABEL_BEGIN_OFFSET          = 7;
    private final static Integer NGINI_END_OFFSET            = 1;
    private final static String  NVALUE_STRING               = "nvalue";
    private final static String  CLOSED_BRACKET_STRING       = "] ;";
    private final static Integer NVALUE_BEGIN_OFFSET         = 9;
    private final static Integer CLOSED_BRACKET_OFFSET       = 1;
    private final static Pattern associationPattern          = Pattern.compile("([0-9]*) -> ([0-9]*)");

    private static List<String> featuresNames;
    private static List<String> classesNames;
    private final static ArrayList<int[]> nodeHierarchy = new ArrayList<>();
    private final static ArrayList<int[]> nodeLevel = new ArrayList<>();


    public static Tree execute(String filePath, List<String> featureNames, Set<String> classNames) throws Exception {
        Scanner scanner = new Scanner(new File(filePath));
        featuresNames = featureNames;
        classesNames = classNames.stream().collect(Collectors.toList());
        Tree tree = new Tree();

        while (scanner.hasNext()) {
            String line = scanner.nextLine();

            final boolean isAssociationLine = line.contains(LINKED_NODE_ARROW);
            final boolean isNewNode = line.contains(LABEL_STRING);

            if (isAssociationLine) {
                Matcher matcher = associationPattern.matcher(line);
                if (matcher.find()){
                    nodeHierarchy.add(
                        new int[]{
                            Integer.parseInt(matcher.group(1)),
                            Integer.parseInt(matcher.group(2))
                        }
                    );
                }

                linkNodes(tree, line);
            } else if (isNewNode) {
                createNewNode(tree, line);
            }
        }

        assignNodeLevel();
        var innerNodes = tree.getInnerNodes();
        var outerNodes = tree.getOuterNodes();

        for (Integer key: innerNodes.keySet()){
            if (innerNodes.get(key).getId() == 0){
                innerNodes.get(key).setLevel(0);
            } else {
                for (int index = 0; index < nodeLevel.size(); index++) {
                    if (nodeLevel.get(index)[0] == innerNodes.get(key).getId()){
                        innerNodes.get(key).setLevel(nodeLevel.get(index)[1]);
                    }
                }
            }
        }

        Integer maxDepth = 0;

        for (Integer key: outerNodes.keySet()){
            for (int index = 0; index < nodeLevel.size(); index++) {
                if (nodeLevel.get(index)[0] == outerNodes.get(key).getId()){
                    outerNodes.get(key).setLevel(nodeLevel.get(index)[1]);
                    if (nodeLevel.get(index)[1] > maxDepth){
                        maxDepth = nodeLevel.get(index)[1];
                    }
                }
            }
        }
        tree.setMaxDepth(maxDepth);
        return tree;
    }

    private static void createNewNode(Tree tree, String line) {
        if (line.contains(INNER_NODE_INDICATOR_STRING)){
            createInnerNode(tree, line);
        } else {
            createOuterNode(tree, line);
        }
    }

    private static void createOuterNode(Tree tree, String line) {

        var node = new OuterNode();
        node.setId(Integer.parseInt(line.split(" ")[0]));

        var begin = line.indexOf(NVALUE_STRING) + NVALUE_BEGIN_OFFSET;
        var end = line.indexOf(CLOSED_BRACKET_STRING) - CLOSED_BRACKET_OFFSET;
        final var values = (ArrayList<Integer>)Arrays
            .asList(line.substring(begin, end)
            .replaceAll("\\[", "")
            .replaceAll("]", "")
            .replaceAll(";", "")
            .replaceAll(" ", "")
            .replaceAll("\"", "")
            .split(","))
            .stream()
            .map(Integer::parseInt)
            .collect(Collectors.toList());
        
        node.setValues(values);
        node.setClassNumber(values.indexOf(values
                .stream()
                .max(Integer::compareTo)
                .get()));
        node.setClassName(classesNames.get(node.getClassNumber()));
        tree.newOuterNode(node);
    }

    private static void createInnerNode(Tree tree, String line) {

        var comparisson = generateComparisson(line);
        var node = generateNode(line, comparisson);
        tree.newInnerNode(node);
    }

    private static InnerNode generateNode(String line, Comparison comparison) {
        var node = new InnerNode();
        node.setComparisson(comparison);
        node.setId(Integer.parseInt(line.split(" ")[0]));
        var begin = line.indexOf(NVALUE_STRING);
        var end = line.length() - 1;

        final var values = (ArrayList<Integer>)Arrays
            .asList(line.substring(begin, end)
            .replaceAll("\\[", "")
            .replaceAll("]", "")
            .replaceAll(";", "")
            .replaceAll(" ", "")
            .replaceAll("\"", "")
            .replaceAll("nvalue=", "")
            .split(","))
            .stream()
            .map(Integer::parseInt)
            .collect(Collectors.toList());

        node.setValues(values);
        return node;
    }

    private static Comparison generateComparisson(String line) {
        var comparisson = new Comparison();
        
        var begin = line.indexOf(LABEL_EQUAL_STRING) + LABEL_BEGIN_OFFSET;
        var end = line.indexOf(INNER_NODE_INDICATOR_STRING) - NGINI_END_OFFSET;
        var comparissonParts = line.substring(begin, end).split(" ");

        /* TODO: o problema ta aqui*/

        comparisson.setColumn(
            Integer.parseInt(comparissonParts[0]
            .replace("[", "")
            .replace("]", "")
            .replace("x", "")));
        comparisson.setComparisonType(comparissonParts[1]);
        comparisson.setThreshold(Float.parseFloat(comparissonParts[2]));
        comparisson.setFeatureName(featuresNames.get(comparisson.getColumn()));
        return comparisson;
    }

    private static void linkNodes(Tree tree, String line) {
        String[] parts = line.split(" ");
        int rootId = Integer.parseInt(parts[0]);
        int sonId = Integer.parseInt(parts[2]);
        tree.linkNodes(rootId, sonId);
    }

    private static void assignNodeLevel(){
        int size = nodeHierarchy.size();

        for (int index = 0; index < size; index++) {
            int level = 1;

            int parent = 0;
            int child = 0;

            for (int i = nodeHierarchy.size() - 1; i >= 0; i--) {
                if (i == nodeHierarchy.size() - 1){
                    parent = nodeHierarchy.get(i)[0];
                    child = nodeHierarchy.get(i)[1];
                } else {
                    if (nodeHierarchy.get(i)[1] == parent){
                        level = level + 1;
                        parent = nodeHierarchy.get(i)[0];
                    }
                }

            }
            nodeHierarchy.remove(nodeHierarchy.size() - 1);

            int[] nodeLevelPair = new int[2];
            nodeLevelPair[0] = child;
            nodeLevelPair[1] = level;

            nodeLevel.add(nodeLevelPair);
        }
    }
}
