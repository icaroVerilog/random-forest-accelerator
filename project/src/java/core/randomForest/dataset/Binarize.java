package project.src.java.core.randomForest.dataset;

import project.src.java.util.FileBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class Binarize {

	private int batchSize = 10000;

	public void execute(String dataset, String basePath) throws FileNotFoundException {
		var path = basePath + "/datasets/" + dataset;
		var scanner = new Scanner(new File(path));
		var header = scanner.nextLine().split(",");

		int counter = 0;

		boolean alreadyWrite = false;

		StringBuilder binaryEntries = new StringBuilder();
		while(scanner.hasNext()) {
			String[] line = scanner.nextLine().split(",");

			StringBuilder binaryRow = new StringBuilder();

			for (int index = 0; index < line.length-1; index++) {
				binaryRow.append(toIEEE754(Double.parseDouble(line[index]), 16));
			}

			binaryRow.append("\n");

			if (counter >= batchSize){

				FileBuilder.execute(
					String.valueOf(binaryEntries),
					String.format("output/datasets/%s", dataset),
					alreadyWrite
				);

				binaryEntries.append(binaryRow);
				binaryEntries = new StringBuilder();
				alreadyWrite = true;
			} else {
				binaryEntries.append(binaryRow);
			}

			counter++;
		}
		System.out.println(binaryEntries);

		FileBuilder.execute(
			String.valueOf(binaryEntries),
			String.format("output/datasets/%s", dataset),
			alreadyWrite
		);

	}

	// TODO: testar se converte corretamente
	protected String toIEEE754(double value, int precision) {
		if (precision == 32) {
			int intBits = Float.floatToIntBits((float) value);
			return String.format("%32s", Integer.toBinaryString(intBits)).replace(' ', '0');
		}
		else if (precision == 16) {
			int intBits = Float.floatToIntBits((float) value);
			int sign = (intBits >> 31) & 0x1;
			int exponent = ((intBits >> 23) & 0xFF) - 127 + 15;
			int mantissa = (intBits >> 13) & 0x3FF;

			if (exponent <= 0) {
				if (exponent < -10) {
					exponent = 0;
					mantissa = 0;
				} else {
					mantissa = (mantissa | 0x400) >> (1 - exponent);
					exponent = 0;
				}
			} else if (exponent >= 31) {
				exponent = 31;
				mantissa = 0;
			}

			int halfPrecisionBits = (sign << 15) | (exponent << 10) | mantissa;
			StringBuilder binaryString = new StringBuilder(Integer.toBinaryString(halfPrecisionBits));

			while (binaryString.length() < 16) {
				binaryString.insert(0, "0");
			}
			return binaryString.toString();
		}
		else if (precision == 64) {
			long longBits = Double.doubleToLongBits(value);
			StringBuilder binaryString = new StringBuilder(Long.toBinaryString(longBits));

			while (binaryString.length() < 64) {
				binaryString.insert(0, "0");
			}

			return binaryString.toString();
		}
		else {
			return "";
		}
	}
}
