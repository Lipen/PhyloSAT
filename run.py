import os
import re
import sys
import subprocess

argc = len(sys.argv)

if argc > 1:
    filename_input = sys.argv[1]
else:
    filename_input = 'test_simple.trees'
if argc > 2:
    filename_output = sys.argv[2]
else:
    filename_output = 'network'
extra_args = ' '.join(sys.argv[3:])

with open(filename_input) as f:
    trees_amount = 0
    for line in f:
        if line:
            trees_amount += 1

engine = 'dot'
format_ = 'png'
dir_network = 'networks'
dir_merged = 'merged'
filename_output_network = os.path.join(dir_network, filename_output)

os.makedirs(dir_network, exist_ok=True)
os.makedirs(dir_merged, exist_ok=True)


def run(cmd):
    print('[.] Running "{}"...'.format(cmd))
    subprocess.run(cmd, shell=True)


run('java -jar out/artifacts/PhyloSAT_jar/PhyloSAT.jar {} {} -r {}'.format(filename_input, extra_args, filename_output_network))

print('[*] Rendering...')
run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_output_network))
for i in range(trees_amount):
    filename_tree = '{}.tree{}'.format(filename_output_network, i)
    run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_tree))

if format_ == 'png':
    print('[*] Merging...')
    filenames = ' '.join('{}.tree{}.{}'.format(filename_output_network, i, format_) for i in range(trees_amount))
    filenames += ' {}.{}'.format(filename_output_network, format_)
    filename_merged = 'merged_{}.{}'.format(re.sub('^network_', '', filename_output), format_)
    filename_merged = os.path.join(dir_merged, filename_merged)
    run('magick convert {} -background white -gravity center -append {}'.format(filenames, filename_merged))
