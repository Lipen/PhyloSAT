import os
import natsort
import json

__author__ = 'VVorobyeva'


def parse_file(res_file_name, hnum_pattern):
    with open(res_file_name, "r") as f:
        data = f.read()
        lines = data.split("\n")
        results = []
        rfile = lines[0]
        hnum = -1
        runtime = lines[-1]
        for line in lines:
            if line.find(hnum_pattern) > -1:
                hnum = int(line[len(hnum_pattern):])
                res = json.dumps((rfile, hnum, runtime))
                results.append(res)
        return results


def main(dir_name, hnum_pattern):
    res = []
    for root, folders, files in os.walk(dir_name):
        for f in files:
            file = os.path.join(root, f)
            # print(file)
            res.extend(parse_file(file, hnum_pattern))
    print(len(res))
    res = natsort.natsorted(res)
    with open(dir_name + ".txt", 'w') as fw:
        for r in res[:-1]:
            fw.write(r)
            fw.write(",\n")
        fw.write(res[-1])

def compare(first, second):
    print first, second
    with open(first, "r") as fread:
        data_h = fread.read()
        data_h = json.loads("[" + data_h + "]")
    with open(second, "r") as fread:
        data_p = fread.read()
        data_p = json.loads("[" + data_p + "]")
    for datah in data_h:
        for datap in data_p:
            if datah[0] == datap[0]:
                if not datap[1] == datah[1]:
                    print "File: {}, hnum_first: {}, hnum_second: {}".format(datah[0], datah[1], datap[1])
                    print "First_{}, Second_{}".format(datah[2], datap[2])


main("res_hybro", "Hybridization Number: ")
main("res_pirn", "The minimum number of reticulation = ")
compare("res_hybro.txt", "res_pirn.txt")