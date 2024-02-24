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


def to_bin(decimal_value, num_bits):
    binary_value = bin(decimal_value)[2:]

    if len(binary_value) > num_bits:
        sys.exit(10)
        # raise ValueError("O número de bits especificado é menor que a representação binária do valor decimal.")
    elif len(binary_value) < num_bits:
        binary_value = '0' * (num_bits - len(binary_value)) + binary_value
    return binary_value


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


def process_dataset(dataset):
    for column in dataset.columns:
        if dataset[column].dtype in ['float64', 'float32']:
            dataset[column], max_decimals = process_column(dataset[column])
    return dataset


dataset_columns = list(dataset.columns)

COLUMNS = dataset.shape[1]

if CLASS_DATASET_LAST_COLUMN:
    class_column = dataset_columns[COLUMNS - 1]
else:
    class_column = dataset_columns[0]

dataset = dataset.drop(class_column, axis=1)
COLUMNS = dataset.shape[1]
LINES = dataset.shape[0]

dataset_column_order = []

counter = COLUMNS - 1
for index in range(0, COLUMNS):
    dataset_column_order.append(counter)
    counter = counter - 1

dataset = dataset.iloc[:, dataset_column_order]

binary_dataset = []

if PRECISION == "integer":
    integer_part_list = []
    decimal_part_list = []
    integer_part_list_bin = []
    decimal_part_list_bin = []

    dataset = process_dataset(dataset)

    for line in dataset.values:
        line = line.tolist()

        for index in range(len(line)):
            integer_part = str(line[index])
            integer_part_list.append(integer_part)

    for value in integer_part_list:
        integer_part_list_bin.append(to_bin(int(value), BITWIDTH))

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += integer_part_list_bin[0]
            integer_part_list_bin = integer_part_list_bin[1:]
        binary_dataset.append(line)

if PRECISION == "decimal":
    integer_part_list = []
    decimal_part_list = []
    integer_part_list_bin = []
    decimal_part_list_bin = []

    for line in dataset.values:
        line = line.tolist()

        for index in range(len(line)):
            value = str(line[index])
            integer_part, decimal_part = value.split(".")
            integer_part_list.append(integer_part)
            decimal_part_list.append(decimal_part)

    for value in integer_part_list:
        integer_part_list_bin.append(to_bin(int(value), BITWIDTH))

    for value in decimal_part_list:
        decimal_part_list_bin.append(to_bin(int(value), BITWIDTH))

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += integer_part_list_bin[0]
            integer_part_list_bin = integer_part_list_bin[1:]
        binary_dataset.append(line)

    for index in range(LINES):
        line = ""
        for index2 in range(COLUMNS):
            line += decimal_part_list_bin[0]
            decimal_part_list_bin = decimal_part_list_bin[1:]
        binary_dataset[index] = binary_dataset[index] + line

with open(f"{DATASET_PATH}/project/target/FPGA/{DATASET}_{APPROACH}_run/dataset.bin", "w") as file:
    for entry in binary_dataset:
        file.write(entry + "\n")

sys.exit(0)
