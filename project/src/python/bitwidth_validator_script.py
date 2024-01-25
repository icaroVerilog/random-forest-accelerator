import sys
import pandas as pd
import math

DATASET_PATH = sys.argv[1]
DATASET = sys.argv[2]
CLASS_DATASET_LAST_COLUMN = True if sys.argv[3] == "true" else False
BITWIDTH = int(sys.argv[4])

dataset = pd.read_csv(DATASET_PATH + "/project/assets/datasets/" + DATASET + ".csv")

dataset_columns = list(dataset.columns)

COLUMNS = dataset.shape[1]

if CLASS_DATASET_LAST_COLUMN:
    class_column = dataset_columns[COLUMNS - 1]
else:
    class_column = dataset_columns[0]

dataset = dataset.drop(class_column, axis=1)
COLUMNS = dataset.shape[1]
LINES = dataset.shape[0]
dataset_columns = list(dataset.columns)

bigger_integer_value = 0
bigger_decimal_value = 0

for column in dataset_columns:
    values = dataset[column]

    for value in values:
        value_str = str(value)
        integer, decimal = value_str.split(".")

        if (int(integer) >= bigger_integer_value):
            bigger_integer_value = int(integer)
        elif (int(decimal) >= bigger_decimal_value):
            bigger_decimal_value = int(decimal)

if (bigger_integer_value > bigger_decimal_value):
    required_bitwidth = math.ceil(math.log2(bigger_integer_value))
else:
    required_bitwidth = math.ceil(math.log2(bigger_decimal_value))

if (BITWIDTH >= required_bitwidth):
    sys.exit(0)
else:
    sys.exit(required_bitwidth)

