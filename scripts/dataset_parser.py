# import sys
# import pandas as pd
# import os
#
# # TODO: em alguns datasets como o do vinho, o valor decimal é muito grande, fazendo com que exceda o numero de bits necessarios
# BASE_PATH = sys.argv[1]
# DATASET = sys.argv[2]
# CLASS_DATASET_LAST_COLUMN = True if sys.argv[3] == "true" else False
# BITWIDTH = int(sys.argv[4])
# # PRECISION = sys.argv[6]
# PRECISION = "integer"
#
# dataset = pd.read_csv(BASE_PATH + "/datasets/" + DATASET)
#
#
# def to_bin(decimal_value, num_bits):
#     binary_value = bin(decimal_value)[2:]
#
#     if len(binary_value) > num_bits:
#         sys.exit(10)
#         # raise ValueError("O número de bits especificado é menor que a representação binária do valor decimal.")
#     elif len(binary_value) < num_bits:
#         binary_value = '0' * (num_bits - len(binary_value)) + binary_value
#     return binary_value
#
#
# def max_decimal_places(value):
#     # Esta função retorna o número máximo de casas decimais em um valor
#     if isinstance(value, float):
#         decimal_part = str(value).split('.')[-1]
#         return len(decimal_part)
#     return 0
#
#
# def process_column(column):
#     # Esta função processa uma única coluna
#     max_decimals = column.apply(max_decimal_places).max()
#     multiplier = 10 ** max_decimals
#     print(column * multiplier)
#     return (column * multiplier).astype(int), max_decimals
#
#
# def process_dataset(dataset):
#     for column in dataset.columns:
#         if dataset[column].dtype in ['float64', 'float32']:
#             dataset[column], max_decimals = process_column(dataset[column])
#     return dataset
#
#
# dataset_columns = list(dataset.columns)
#
# COLUMNS = dataset.shape[1]
#
# if CLASS_DATASET_LAST_COLUMN:
#     class_column = dataset_columns[COLUMNS - 1]
# else:
#     class_column = dataset_columns[0]
#
# dataset = dataset.drop(class_column, axis=1)
# COLUMNS = dataset.shape[1]
# LINES = dataset.shape[0]
#
# dataset_column_order = []
#
# counter = COLUMNS - 1
# for index in range(0, COLUMNS):
#     dataset_column_order.append(counter)
#     counter = counter - 1
#
# dataset = dataset.iloc[:, dataset_column_order]
# lines = []
# binary_dataset = []
#
# if PRECISION == "integer":
#     dataset = process_dataset(dataset)
#     for line in dataset.values:
#         line = line.tolist()
#         binary_value = ""
#
#         for value in line:
#             binary_value = binary_value + to_bin(value, BITWIDTH)
#         binary_dataset.append(binary_value)
#
#
# if PRECISION == "decimal":
#     integer_part_list = []
#     decimal_part_list = []
#     integer_part_list_bin = []
#     decimal_part_list_bin = []
#
#     for line in dataset.values:
#         line = line.tolist()
#
#         for index in range(len(line)):
#             value = str(line[index])
#             integer_part, decimal_part = value.split(".")
#             integer_part_list.append(integer_part)
#             decimal_part_list.append(decimal_part)
#
#     for value in integer_part_list:
#         integer_part_list_bin.append(to_bin(int(value), BITWIDTH))
#
#     for value in decimal_part_list:
#         decimal_part_list_bin.append(to_bin(int(value), BITWIDTH))
#
#     for index in range(LINES):
#         line = ""
#         for index2 in range(COLUMNS):
#             line += integer_part_list_bin[0]
#             integer_part_list_bin = integer_part_list_bin[1:]
#         binary_dataset.append(line)
#
#     for index in range(LINES):
#         line = ""
#         for index2 in range(COLUMNS):
#             line += decimal_part_list_bin[0]
#             decimal_part_list_bin = decimal_part_list_bin[1:]
#         binary_dataset[index] = binary_dataset[index] + line
#
# path = BASE_PATH + "/output/datasets"
#
# if os.path.exists(path):
#     if os.path.exists(path):
#         pass
#     else:
#         os.mkdir(path)
# else:
#     os.mkdir(path)
#
#
# with open(f"{path}/{DATASET}", "w") as file:
#     for entry in binary_dataset:
#         file.write(entry + "\n")
#
# sys.exit(0)

import sys
import os
import pandas as pd
import struct
import math
import numpy as np

BASE_PATH = sys.argv[1]
DATASET = sys.argv[2]
CLASS_DATASET_LAST_COLUMN = True if sys.argv[3] == "true" else False
BITWIDTH = int(sys.argv[4])
PRECISION = "integer"

def round_decimal(value, decimal_places):
    """
    Arredonda um valor decimal para um número específico de casas decimais.

    Parameters:
    value (float): O valor a ser arredondado.
    decimal_places (int): Número de casas decimais desejadas.

    Returns:
    float: Valor arredondado.
    """
    factor = 10.0 ** decimal_places
    return math.floor(value * factor + 0.5) / factor

def can_represent(value, bit_size):
    """
    Verifica se o valor pode ser representado com a quantidade de bits especificada.

    Parameters:
    value (float): O valor a ser verificado.
    bit_size (int): Tamanho da representação em bits (16 ou 32).

    Returns:
    bool: True se o valor pode ser representado, False caso contrário.
    """
    if bit_size == 16:
        # Verifica se o valor está dentro do intervalo de half-precision (IEEE 754 de 16 bits)
        min_val, max_val = -65504, 65504
    elif bit_size == 32:
        # Verifica se o valor está dentro do intervalo de single-precision (IEEE 754 de 32 bits)
        min_val, max_val = -3.4028235e+38, 3.4028235e+38
    else:
        raise ValueError("bit_size deve ser 16 ou 32")

    return min_val <= value <= max_val

def float_to_ieee754(value, bit_size=32, decimal_places=None):
    """
    Converte um valor float para a representação IEEE 754.

    Parameters:
    value (float): O valor a ser convertido.
    bit_size (int): Tamanho da representação em bits (16 ou 32).
    decimal_places (int, optional): Número de casas decimais para arredondamento.

    Returns:
    str: Representação em binário como string.
    """
    if decimal_places is not None:
        value = round_decimal(value, decimal_places)

    if not can_represent(value, bit_size):
        raise ValueError(f"O valor {value} não pode ser representado com {bit_size} bits")

    if bit_size == 16:
        # IEEE 754 half-precision (16 bits) não é diretamente suportado por struct,
        # então vamos usar uma biblioteca como numpy ou uma implementação própria
        packed_value = np.float16(value).tobytes()
    elif bit_size == 32:
        packed_value = struct.pack('>f', value)  # IEEE 754 single-precision (32 bits)
    else:
        raise ValueError("bit_size deve ser 16 ou 32")

    # Converte o valor empacotado para uma representação em binário
    binary_representation = ''.join(f'{byte:08b}' for byte in packed_value)
    return binary_representation

def convert_dataframe(df, bit_size=32, decimal_places=None):
    """
    Converte todos os valores numéricos em um DataFrame para a representação IEEE 754.

    Parameters:
    df (pd.DataFrame): O DataFrame a ser convertido.
    bit_size (int): Tamanho da representação em bits (16 ou 32).
    decimal_places (int, optional): Número de casas decimais para arredondamento.

    Returns:
    list: Lista de strings binárias dos valores convertidos.
    """
    lines = []

    for _, row in df.iterrows():
        binary_strings = [float_to_ieee754(value, bit_size, decimal_places) for value in row]
        line = ''.join(binary_strings)
        lines.append(line)

    return lines

def save_to_txt(binary_strings, filename):
    """
    Salva a lista de strings binárias em um arquivo .txt.

    Parameters:
    binary_strings (list): Lista de strings binárias.
    filename (str): Nome do arquivo .txt.
    """
    with open(filename, 'w') as f:
        for binary_string in binary_strings:
            f.write(binary_string + '\n')



dataset = pd.read_csv(BASE_PATH + "/datasets/" + DATASET)

dataset_columns = list(dataset.columns)

COLUMNS = dataset.shape[1]

if CLASS_DATASET_LAST_COLUMN:
    class_column = dataset_columns[COLUMNS - 1]
else:
    class_column = dataset_columns[0]

dataset = dataset.drop(class_column, axis=1)
bin_data = convert_dataframe(dataset, bit_size=BITWIDTH, decimal_places=None)

path = BASE_PATH + "/output/datasets"

if os.path.exists(path):
    if os.path.exists(path):
        pass
    else:
        os.mkdir(path)
else:
    os.mkdir(path)

save_to_txt(bin_data,f"{path}/{DATASET}")
sys.exit(0)