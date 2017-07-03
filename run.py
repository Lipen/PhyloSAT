import os
import sys

argc = len(sys.argv)

if argc > 1:
    filename_input = sys.argv[1]
else:
    filename_input = 'test.tree'
if argc > 2:
    filename_output_base = sys.argv[2]
else:
    filename_output_base = 'network'

with open(filename_input) as f:
    trees_amount = 0
    for line in f:
        if line:
            trees_amount += 1

engine = 'dot'
format_ = 'png'
filename_output = filename_output_base + '.gv'

os.system('java -jar out/artifacts/PhyloSAT_jar/PhyloSAT.jar {} -r {}'.format(filename_input, filename_output))

print('[*] Rendering...')
os.system('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_output_base))
for i in range(trees_amount):
    filename_tree = filename_output_base + '.tree{}'.format(i)
    os.system('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_tree))
