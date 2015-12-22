# Script to generate trees on random data
# Author: Vladimir Ulyantsev

import random

TESTS_CNT = 10
TREES_CNT = 4
SUBTREE_SIZE = 4


def gen_random_brackets(arr):
    ans = list(arr)
    for step in xrange(len(arr) - 1):
        pos = random.randint(0, len(ans) - 2)
        ans = ans[0:pos] + ['(%s,%s)' % (ans[pos], ans[pos + 1])] + ans[pos + 2:]
    return ans[0]


def replace_tax(tree):
    for i in xrange(SUBTREE_SIZE):
        tree = tree.replace(str(i), str(i + SUBTREE_SIZE))
    return tree


def change_taxs(tree, left_pos, right_pos):
    pieces = tree.split(",")
    left, right = pieces[left_pos], pieces[right_pos]
    left_tax, right_tax = ''.join(c for c in left if c.isdigit()), ''.join(c for c in right if c.isdigit())
    left, right = left.replace(left_tax, right_tax), right.replace(right_tax, left_tax)
    pieces[left_pos], pieces[right_pos] = left, right
    tree = ",".join(pieces)
    return tree


def gen_special_test():
    ans = []
    left = [str(x) for x in xrange(SUBTREE_SIZE)]
    lefts = []
    for t in xrange(TREES_CNT):
        lefts += [gen_random_brackets(left)]
        random.shuffle(left)
    for t in xrange(TREES_CNT):
        ans += ['(%s,%s);' % (lefts[t], replace_tax(lefts[TREES_CNT - (t + 1)]))]
    left_rand_pos, right_rand_pos = random.randint(0, SUBTREE_SIZE - 1), random.randint(0, SUBTREE_SIZE - 1) + SUBTREE_SIZE
    tree = ans[-1]
    ans = ans[:-1]
    tree = change_taxs(tree, left_rand_pos, right_rand_pos)
    ans += [tree]
    return "\n".join(ans)


def main():
    for test_no in xrange(TESTS_CNT):
        fname = 'randtrees/%s.txt' % test_no
        print >> open(fname, 'w'), gen_special_test()
        # phylosat_cmd = 'java -jar -Xmx8G phylosat_origin.jar ' + fname
        # os.system(phylosat_cmd + ' --log logs/%s' % test_no)


if __name__ == "__main__":
    main()
