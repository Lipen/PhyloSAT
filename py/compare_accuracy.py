# Script to compare accuracy on random data
# Author: Vladimir Ulyantsev

import random
import os

TESTS_CNT = 10
TREES_CNT = 4
LEFT_SIZE, RIGHT_SIZE = 4, 4
fname = 'randtrees.txt'
phylosat_cmd = 'java -jar ../dist/PhyloSAT.jar ' + fname

def gen_random_brackets(arr):
    ans = list(arr)
    for step in xrange(len(arr) - 1):
        pos = random.randint(0, len(ans) - 2)
        ans = ans[0:pos] + ['(%s,%s)' % (ans[pos], ans[pos + 1])] + ans[pos + 2:]
    return ans[0]

def gen_special_test():
    ans = ''
    left, right = [str(x) for x in xrange(LEFT_SIZE)], [str(x + LEFT_SIZE) for x in xrange(RIGHT_SIZE)]
    for t in xrange(TREES_CNT - 1):
        ans += '(%s,%s);\n' % (gen_random_brackets(left), gen_random_brackets(right))
        random.shuffle(left)
        random.shuffle(right)
    
    left_rand_pos, right_rand_pos = random.randint(0, len(left) - 1), random.randint(0, len(right) - 1)
    left[left_rand_pos], right[right_rand_pos] = right[right_rand_pos], left[left_rand_pos]
    ans += '(%s,%s);\n' % (gen_random_brackets(left), gen_random_brackets(right))

    return ans

def main():
    #for i in xrange(30):
    #    print gen_random([str(x) for x in xrange(5)])
    for test_no in xrange(TESTS_CNT):
        print >>open(fname, 'w'), gen_special_test()
        os.system(phylosat_cmd + ' --log logs/%s' % test_no)


if __name__ == "__main__":
    main()