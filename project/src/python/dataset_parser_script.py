import sys
import pandas as pd

# TODO: em alguns datasets como o do vinho, o valor decimal é muito grande, fazendo com que exceda o numero de bits necessarios

DATASET_PATH = sys.argv[1]
DATASET = sys.argv[2]
CLASS_DATASET_LAST_COLUMN = True if sys.argv[3] == "true" else False
BITWIDTH = int(sys.argv[4])
APPROACH = sys.argv[5]
PRECISION = sys.argv[6]

dataset = pd.read_csv(DATASET_PATH + "/project/assets/datasets/" + DATASET + ".csv")


def max_decimal_places(value):
    # Esta função retorna o número máximo de casas decimais em um valor
    if isinstance(value, float):
        decimal_part = str(value).split('.')[-1]
        return len(decimal_part)
    return 0


def process_column(column):
    # Esta função processa uma única coluna
    max_decimals = column.apply(max_decimal_places).max()
    multiplier = 10 ** max_decimals
    return (column * multiplier).astype(int), max_decimals


def process_df(df):
    for column in df.columns:
        if df[column].dtype in ['float64', 'float32']:
            df[column], max_decimals = process_column(df[column])
            # print(f"Coluna '{column}' processada com {max_decimals} casas decimais")
    df.to_csv("a.csv", index=False)
    return df


dataset_columns = list(dataset.columns)

COLUMNS = dataset.shape[1]

if CLASS_DATASET_LAST_COLUMN:
    class_column = dataset_columns[COLUMNS - 1]
else:
    class_column = dataset_columns[0]

dataset = dataset.drop(class_column, axis=1)
COLUMNS = dataset.shape[1]
LINES = dataset.shape[0]


def to_bin(decimal_value, num_bits):
    binary_value = bin(decimal_value)[2:]

    if len(binary_value) > num_bits:
        sys.exit(10)
        # raise ValueError("O número de bits especificado é menor que a representação binária do valor decimal.")
    elif len(binary_value) < num_bits:
        binary_value = '0' * (num_bits - len(binary_value)) + binary_value
    return binary_value


def build_binary_dataset(exponent_array, fraction_array, precision):
    binary_dataset = []

    if precision == "decimal":
        for index in range(LINES):
            line = ""
            for index2 in range(COLUMNS):
                line += exponent_array[0]
                exponent_array = exponent_array[1:]
            binary_dataset.append(line)

        for index in range(LINES):
            line = ""
            for index2 in range(COLUMNS):
                line += fraction_array[0]
                fraction_array = fraction_array[1:]
            binary_dataset[index] = binary_dataset[index] + line
        return binary_dataset

    elif precision == "integer":
        for index in range(LINES):
            line = ""
            for index2 in range(COLUMNS):
                line += exponent_array[0]
                exponent_array = exponent_array[1:]
            binary_dataset.append(line)
        return binary_dataset


binary_dataset = []

if PRECISION == "integer":
    exponent_part_list = []
    fraction_part_list = []
    exponent_part_list_bin = []
    fraction_part_list_bin = []

    dataset = process_df(dataset)

    for line in dataset.values:
        line = line.tolist()

        for index in range(len(line)):
            exponent_part = str(line[index])
            exponent_part_list.append(exponent_part)

    for value in exponent_part_list:
        exponent_part_list_bin.append(to_bin(int(value), BITWIDTH * 2))

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += exponent_part_list_bin[0]
            exponent_part_list_bin = exponent_part_list_bin[1:]
        binary_dataset.append(line)

if PRECISION == "decimal":
    exponent_part_list = []
    fraction_part_list = []
    exponent_part_list_bin = []
    fraction_part_list_bin = []

    for line in dataset.values:
        line = line.tolist()

        for index in range(len(line)):
            value = str(line[index])
            exponent_part, fraction_part = value.split(".")
            exponent_part_list.append(exponent_part)
            fraction_part_list.append(fraction_part)

    for value in exponent_part_list:
        exponent_part_list_bin.append(to_bin(int(value), BITWIDTH))

    for value in fraction_part_list:
        fraction_part_list_bin.append(to_bin(int(value), BITWIDTH))

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += exponent_part_list_bin[0]
            exponent_part_list_bin = exponent_part_list_bin[1:]
        binary_dataset.append(line)

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += fraction_part_list_bin[0]
            fraction_part_list_bin = fraction_part_list_bin[1:]
        binary_dataset[index] = binary_dataset[index] + line

with open(f"{DATASET_PATH}/project/target/FPGA/{APPROACH}/{DATASET}/dataset.bin", "w") as file:
    for entry in binary_dataset:
        file.write(entry + "\n")

sys.exit(0)
