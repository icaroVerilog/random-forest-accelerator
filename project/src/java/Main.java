package project.src.java;

import java.io.IOException;

import project.src.java.gpuGenerator.conditional.ConditionalGPUGenerator;
import project.src.java.parser.Parser;

public class Main {

    private static String dataset;
    public static void main(String[] args) throws IOException {
        dataset = "Iris";
        start();
    }

    public static void start() throws IOException{
        var trees = Parser.execute(dataset);
        ConditionalGPUGenerator.execute(trees);
    }
}