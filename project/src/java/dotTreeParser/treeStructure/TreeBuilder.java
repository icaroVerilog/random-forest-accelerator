package project.src.java.dotTreeParser.treeStructure;

import project.src.java.dotTreeParser.treeStructure.Nodes.InnerNode;
import project.src.java.dotTreeParser.treeStructure.Nodes.OuterNode;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TreeBuilder {

    private final static String LINKED_NODE_ARROW = "->";
    private final static String LABEL_STRING = "label";
    private final static String INNER_NODE_INDICATOR_STRING = "ngini";
    private final static String LABEL_EQUAL_STRING = "label=";
    private final static Integer LABEL_BEGIN_OFFSET = 7;
    private final static Integer NGINI_END_OFFSET = 1;
    private final static String NVALUE_STRING = "nvalue";
    private final static String CLOSED_BRACKET_STRING = "] ;";
    private final static Integer NVALUE_BEGIN_OFFSET = 9;
    private final static Integer CLOSED_BRACKET_OFFSET = 1;

    private static List<String> featuresNames;
    private static List<String> classesNames;

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
                linkNodes(tree, line);
            } else if (isNewNode) {
                createNewNode(tree, line);
            }
        }
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

    private static InnerNode generateNode(String line, Comparisson comparisson) {
        var node = new InnerNode();
        node.setComparisson(comparisson);
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

    private static Comparisson generateComparisson(String line) {
        var comparisson = new Comparisson();
        
        var begin = line.indexOf(LABEL_EQUAL_STRING) + LABEL_BEGIN_OFFSET;
        var end = line.indexOf(INNER_NODE_INDICATOR_STRING) - NGINI_END_OFFSET;
        var comparissonParts = line.substring(begin, end).split(" ");

        /* o problema ta aqui*/

        comparisson.setColumn(
            Integer.parseInt(comparissonParts[0]
            .replace("[", "")
            .replace("]", "")
            .replace("x", "")));
        comparisson.setComparissonType(comparissonParts[1]);
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
}
