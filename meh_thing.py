import sys
import pandas
import numpy as np

if len(sys.argv) > 1:
    filepath = sys.argv[1]
else:
    # filepath = 'serverthing.log'
    # filepath = 'oldthing.log'
    # filepath = 'everything.log'
    raise ValueError("Please, pass log file as an argument")
df = pandas.read_csv(filepath, dtype={'n': np.int32, 'k': np.int32, 't': np.float64})

data = {}
data_all = {}
ns = sorted(df.n.unique())
for n in ns:
    is_n = df.n == n
    data_all[n] = df[is_n].t.mean()
    ks = sorted(df[is_n].k.unique())
    for k in ks:
        is_k = df.k == k
        data[(n, k)] = df[is_n & is_k].t.mean()

print('[*]  n  k  ::   time')
for (n, k), t in sorted(data.items()):
    print('  > {: >2}  {: <2} :: {: >7.3f}s'.format(n, k, t))

print('[*]  n ::    time')
for n, t in sorted(data_all.items()):
    print('  > {: >2} :: {: >7.3f}s'.format(n, t))
