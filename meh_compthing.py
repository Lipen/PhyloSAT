import pandas
import numpy as np

filepath_new = 'everything.log'
filepath_old = 'oldthing.log'


def get_data(filepath):
    df = pandas.read_csv(filepath, dtype={'n': np.int32, 'k': np.int32, 't': np.float64})
    data = {}
    dataStd = {}
    for f in df.f.unique():
        xx = df[df.f == f]
        assert all(xx.n.iloc[i] == xx.n.iloc[0] for i in range(len(xx.n))), "unequality over ns"
        assert all(xx.k.iloc[i] == xx.k.iloc[0] for i in range(len(xx.k))), "unequality over ks"
        data[xx.f.iloc[0]] = (xx.n.iloc[0], xx.k.iloc[0], xx.t.mean())
        dataStd[xx.f.iloc[0]] = xx.t.std()
    return data, dataStd


def main():
    data_new, dataStd_new = get_data(filepath_new)
    data_old, dataStd_old = get_data(filepath_old)

    names = sorted(set.union(set(data_new), set(data_old)))
    # ll = max(len('name'), max(len(f) for f in data_new), max(len(f) for f in data_old))
    # print('[*] {1: ^{0}} | n_old | n_new | k_old | k_new | time_old | time_new'.format(ll, 'name'))
    for name in names:
        if name in data_new:
            n_new, k_new, t_new = data_new[name]
            # n_new = '{: ^5}'.format(n_new)
            # k_new = '{: ^5}'.format(k_new)
            # t_new = '{:.3f}s'.format(t_new)
        else:
            # n_new = ' --- '
            # k_new = ' --- '
            # t_new = ' -------'
            n_new = k_new = t_new = '--'

        if name in data_old:
            n_old, k_old, t_old = data_old[name]
            # n_old = '{: ^5}'.format(n_old)
            # k_old = '{: ^5}'.format(k_old)
            # t_old = '{: >7.3f}s'.format(t_old)
        else:
            # n_old = ' --- '
            # k_old = ' --- '
            # t_old = '--------'
            n_old = k_old = t_old = '--'

        # f = '{1: ^{0}}'.format(ll, name)
        # print(' >  ' + ' | '.join([f, n_old, n_new, k_old, k_new, t_old, t_new]))
        if n_new != n_old or k_new != k_old:
            # print('[!] Skipping {}'.format(name))
            continue
        print('\t\\texttt{{{}}} & {} & {} & {:.3f} \\pm {:.2f} & {:.3f} \\pm {:.2f} \\\\'.format(name.replace('_', '\\_'), n_new, k_new, t_old, dataStd_old[name], t_new, dataStd_new[name]))


if __name__ == '__main__':
    main()
