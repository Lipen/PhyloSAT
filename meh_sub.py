import sys
import pandas
import numpy as np

if len(sys.argv) > 1:
    filepath = sys.argv[1]
else:
    # filepath = 'servertasks.log'
    # filepath = 'oldtasks.log'
    # filepath = 'subtask.log'
    raise ValueError("Please, pass log file as an argument")
df = pandas.read_csv(filepath, dtype={'n': np.int32, 'k': np.int32, 't': np.float64})

is_sat = df.sat == 'SAT'

data_sat = {}
data_sat_all = {}
ns = sorted(df[is_sat].n.unique())
for n in ns:
    is_n = df.n == n
    data_sat_all[n] = df[is_sat & is_n].t.mean()
    ks = sorted(df[is_sat & is_n].k.unique())
    for k in ks:
        is_k = df.k == k
        data_sat[(n, k)] = df[is_sat & is_n & is_k].t.mean()

data_unsat = {}
data_unsat_all = {}
ns = sorted(df[~is_sat].n.unique())
for n in ns:
    is_n = df.n == n
    data_unsat_all[n] = df[~is_sat & is_n].t.mean()
    ks = sorted(df[~is_sat & is_n].k.unique())
    for k in ks:
        is_k = df.k == k
        data_unsat[(n, k)] = df[~is_sat & is_n & is_k].t.mean()


print('[*]  n  k  ::   sat  \tunsat times')
for n, k in sorted(set.union(set(data_sat), set(data_unsat))):
    if (n, k) in data_sat and (n, k) in data_unsat:
        t1 = data_sat[(n, k)]
        t2 = data_unsat[(n, k)]
        print('  > {: >2}  {: <2} :: {: >7.3f}s\t{:.3f}s'.format(n, k, t1, t2))
    elif (n, k) in data_sat:
        t1 = data_sat[(n, k)]
        print('  > {: >2}  {: <2} :: {: >7.3f}s\t-----'.format(n, k, t1))
    elif (n, k) in data_unsat:
        t2 = data_unsat[(n, k)]
        print('  > {: >2}  {: <2} ::  ----- \t{:.3f}s'.format(n, k, t2))

print('[*]  n ::    sat \tunsat times')
for n in sorted(set.union(set(data_sat_all), set(data_unsat_all))):
    if n in data_sat_all and n in data_unsat_all:
        t1 = data_sat_all[n]
        t2 = data_unsat_all[n]
        print('  > {: >2} :: {: >7.3f}s\t{:.3f}s'.format(n, t1, t2))
    elif n in data_sat_all:
        t1 = data_sat_all[n]
        print('  > {: >2} :: {: >7.3f}s\t-----'.format(n, t1))
    elif n in data_unsat_all:
        t2 = data_unsat_all[n]
        print('  > {: >2} ::  -----  \t{:.3f}s'.format(n, t2))
