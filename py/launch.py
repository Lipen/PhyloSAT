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
    time = datetime.datetime.now().strftime("%H_%M_%S")
    result_file = os.path.join(result_dir, '{}.txt'.format(time))
    with open(result_file, "a+") as r_f:
       r_f.write("{}\n\n".format(comm[-1]))
#    rewrite(comm[-1])
    comm += ['>>', result_file, '2>&1']
    os.system(" ".join(comm))


def main(tool, dir_name, options=[]):
    time = datetime.datetime.now().strftime("%Y_%m_%d")
    result_dir = os.path.join("../results_{}".format(tool), time)
    if not os.path.exists(result_dir):
        os.makedirs(result_dir)
    for root, folders, files in os.walk(dir_name):
        for f in files:
            file = os.path.join(root, f)
            print(file)
            file = file.replace(" ", "\ ")
            try:
                launch(['java', '-jar', '-Xmx8G', '{}.jar'.format(tool)] + options + [file], result_dir)
            except Exception as exc:
                print("some exception")
                print(exc)
#            exit(0)


if __name__ == "__main__":
    if sys.argv[1] == "hybroscale_1.3":
        main(sys.argv[1], sys.argv[2], ['-mode', 'hNum', '-i'])
    else:
        main(sys.argv[1], sys.argv[2])
