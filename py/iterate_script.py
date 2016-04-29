import sys
from pycryptosat import Solver


def iterate_solve(cnf_file):
    with open(cnf_file, "r") as cnf:
        s = Solver()
        line = cnf.readline()
        while line:
            line = cnf.readline()
            if "solve point for iterate solver" in line:
                sat, solution = s.solve()
                if not sat:
                    return None
            if len(line) == 0 or line[0] == "c":
                continue
            clause = line.split(" ")[:-1]
            clause = [int(var) for var in clause]
            s.add_clause(clause)
    sat, solution = s.solve()
    solution = change_format(solution)
    print(solution)


def change_format(solution):
    if solution == None:
        return "s UNSATISFIABLE"
    sol = "v"
    for i, var in enumerate(solution):
        if i == 0:
            continue
        v = " " + str(i) if var else " -" + str(i)
        sol += v
    return sol + " 0"


iterate_solve("tmp.cnf")

