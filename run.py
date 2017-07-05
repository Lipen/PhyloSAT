import os
import sys

argc = len(sys.argv)

if argc > 1:
    filename_input = sys.argv[1]
else:
    filename_input = 'test.tree'
if argc > 2:
    filename_output = sys.argv[2]
else:
    filename_output = 'network'
if argc > 3:
    extra_args = '-ds -h {}'.format(sys.argv[3])
else:
    extra_args = ''

with open(filename_input) as f:
    trees_amount = 0
    for line in f:
        if line:
            trees_amount += 1

engine = 'dot'
format_ = 'png'


def run(cmd):
    print('[.] Running "{}"...'.format(cmd))
    os.system(cmd)

run('java -jar out/artifacts/PhyloSAT_jar/PhyloSAT.jar {} {} -r {}'.format(filename_input, extra_args, filename_output))

print('[*] Rendering...')
run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_output))
for i in range(trees_amount):
    filename_tree = filename_output + '.tree{}'.format(i)
    run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_tree))
