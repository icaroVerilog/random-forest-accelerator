package project.src.java.approaches.fpga;

import project.src.java.approaches.fpga.datasetParser.DatasetParser;
import project.src.java.approaches.fpga.datasetParser.datasetStructure.DatasetStructure;
import project.src.java.approaches.fpga.generator.VerilogTreeGenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

public class FPGA {

    DatasetStructure dataset;

    public FPGA(String datasetName) throws IOException {
        DatasetParser datasetParser = new DatasetParser();
        this.dataset = datasetParser.readDataset(datasetName);

        generateVerilog();
    }

    public boolean generateVerilog() throws IOException {

        VerilogTreeGenerator treeGen = new VerilogTreeGenerator("tree", this.dataset);
        treeGen.generate();

        return true;
    }

}
