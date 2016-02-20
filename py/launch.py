import datetime
import os
import sys

__author__ = 'vvorobyeva'

def rewrite(file_name):
    with open(file_name, "r") as f:
        data = f.read()
    data = data.replace(":1.0", "")
    with open(file_name, "w") as f:
        f.write(data)


def launch(comm, result_dir):
    t1 = datetime.datetime.now()
    time = t1.strftime("%H_%M_%S")
    result_file = os.path.join(result_dir, '{}.txt'.format(time))
    with open(result_file, "a+") as r_f:
        r_f.write("{}\n\n".format(comm.split(" ")[-1]))
#    rewrite(comm[-1])
    comm += '>> {} 2>&1'.format(result_file)
    os.system(comm)
    t2 = datetime.datetime.now()
    with open(result_file, "a+") as r_f:
        r_f.write("\n\nRuntime: {}".format(t2 - t1))


def main(cmd, dir_name, result_tag):
    time = datetime.datetime.now().strftime("%Y_%m_%d")
    result_dir = os.path.join("../results_{}".format(result_tag), time)
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)
    for root, folders, files in os.walk(dir_name):
        for f in files:
            file = os.path.join(root, f)
            print(file)
            file = file.replace(" ", "\ ")
            try:
                cmd += " " + file
                launch(cmd, result_dir)
            except Exception as exc:
                print("some exception")
                print(exc)


if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2], sys.argv[3])
