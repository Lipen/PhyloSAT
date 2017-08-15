import os
import re
import sys
import glob
import subprocess
from collections import defaultdict

argc = len(sys.argv)

if argc > 1:
    filename_input = sys.argv[1]
else:
    raise ValueError('Please, pass input filename')
if argc > 2:
    filename_output = sys.argv[2]
else:
    raise ValueError('Please, pass output filename')
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
dir_submerged = 'submerged'
filename_output_network = os.path.join(dir_network, filename_output).replace('\\','/')

os.makedirs(dir_network, exist_ok=True)
os.makedirs(dir_merged, exist_ok=True)


def run(cmd):
    cmd = cmd.strip()
    print('[.] Running "{}"...'.format(cmd))
    subprocess.run(cmd, shell=True)


run('rm -f {}.sub*'.format(filename_output_network))

run('java -jar out/artifacts/PhyloSAT_jar/PhyloSAT.jar -i {} -r {} {}'.format(filename_input, filename_output_network, extra_args))

print('[*] Rendering main network...')
run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_output_network))

print('[*] Rendering isomorphic networks...')
isomorphic_networks = [f[:-len('.gv')] for f in glob.glob('{}.sub*.iso*.gv'.format(filename_output_network))]
for filename_isonetwork in isomorphic_networks:
    run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_isonetwork))

print('[*] Merging isomorphic networks...')
isodata = defaultdict(list)
for filename_isonetwork in isomorphic_networks:
    m = re.match(r'^.*\.sub(\d+)\.iso(\d+)$', filename_isonetwork)
    sub, iso = int(m.group(1)), int(m.group(2))
    isodata[sub].append(iso)
for sub, isos in isodata.items():
    # HACK START
    # Note: this hack will only work if dir is 1-depth
    os.chdir(dir_network)
    isonames = ' '.join(map(lambda iso: '{}.sub{}.iso{}.png'.format(filename_output, sub, iso), sorted(isos, key=int)))
    filename_isomerged = 'submerged_{}.sub{}.{}'.format(re.sub('^network_', '', filename_output), sub, format_)
    filename_isomerged = '../submerged/' + filename_isomerged
    run('magick montage {} -gravity south -geometry +16+0 {}'.format(isonames, filename_isomerged))
    os.chdir('..')
    # HACK END

    # filename_isomerged = 'submerged_{}.sub{}.{}'.format(re.sub('^network_', '', filename_output), sub, format_)
    # isonames = ' '.join(map(lambda iso: '{}.sub{}.iso{}.png'.format(filename_output_network, sub, iso), isos))
    # filename_isomerged = 'submerged_{}.sub{}.{}'.format(re.sub('^network_', '', filename_output), sub, format_)
    # filename_isomerged = os.path.join(dir_submerged, filename_isomerged)
    # run('magick montage {} -gravity south -geometry +16+0 {}'.format(isonames, filename_isomerged))

print('[*] Rendering trees...')
for i in range(trees_amount):
    filename_tree = '{}.tree{}'.format(filename_output_network, i)
    run('{0} -T{1} {2}.gv -o {2}.{1}'.format(engine, format_, filename_tree))

if format_ == 'png':
    print('[*] Merging main network and tree...')
    filenames = ' '.join('{}.tree{}.{}'.format(filename_output_network, i, format_) for i in range(trees_amount))
    filenames += ' {}.{}'.format(filename_output_network, format_)
    filename_merged = 'merged_{}.{}'.format(re.sub('^network_', '', filename_output), format_)
    filename_merged = os.path.join(dir_merged, filename_merged)
    run('magick convert {} -background white -gravity center -append {}'.format(filenames, filename_merged))
