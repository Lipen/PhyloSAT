PY = python
SCRIPT = run.py
DIR_GRASS2 = data/tests/small/Grass2
DIR_GRASS3 = data/tests/small/Grass3

default:

all: tests Grass2 Grass3

# test_medium test_hard
tests: test_easiest test_easy test_folded qwerty4 qwerty5 test_simple test_intermediate test_folded_large
test_easiest:
	${PY} ${SCRIPT} test_easiest.trees network_test_easiest ${EXTRA}
test_easy:
	${PY} ${SCRIPT} test_easy.trees network_test_easy ${EXTRA}
test_folded:
	${PY} ${SCRIPT} test_folded.trees network_test_folded ${EXTRA}
qwerty4:
	${PY} ${SCRIPT} qwerty4.trees network_qwerty4 ${EXTRA}
qwerty5:
	${PY} ${SCRIPT} qwerty5.trees network_qwerty5 ${EXTRA}
test_simple:
	${PY} ${SCRIPT} test_simple.trees network_test_simple ${EXTRA}
test_intermediate:
	${PY} ${SCRIPT} test_intermediate.trees network_test_intermediate ${EXTRA}
test_medium:
	${PY} ${SCRIPT} test_medium.trees network_test_medium ${EXTRA}
test_hard:
	${PY} ${SCRIPT} test_hard.trees network_test_hard ${EXTRA}
test_folded_large:
	${PY} ${SCRIPT} test_folded_large.trees network_test_folded_large ${EXTRA}

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts
Grass2_: Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy
Grass2NdhfWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy ${EXTRA}
Grass2PhytRbcl:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl ${EXTRA}
Grass2PhytRpoc:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc ${EXTRA}
Grass2PhytWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy ${EXTRA}
Grass2RbclWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy ${EXTRA}
Grass2RpocWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy ${EXTRA}
Grass2WaxyIts:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts ${EXTRA}

Grass3:Grass3NdhfPhytRbcl Grass3NdhfPhytRpoc Grass3NdhfPhytWaxy Grass3NdhfRbclWaxy Grass3NdhfRpocWaxy Grass3NdhfWaxyIts Grass3PhytRbclIts Grass3PhytRbclRpoc Grass3PhytRbclWaxy Grass3PhytRpocIts Grass3PhytRpocWaxy Grass3PhytWaxyIts Grass3RbclRpocIts Grass3RbclRpocWaxy Grass3RbclWaxyIts Grass3RpocWaxyIts
Grass3NdhfPhytRbcl:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfPhytRbcl.tree.restrict.num network_Grass3NdhfPhytRbcl ${EXTRA}
Grass3NdhfPhytRpoc:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfPhytRpoc.tree.restrict.num network_Grass3NdhfPhytRpoc ${EXTRA}
Grass3NdhfPhytWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfPhytWaxy.tree.restrict.num network_Grass3NdhfPhytWaxy ${EXTRA}
Grass3NdhfRbclWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfRbclWaxy.tree.restrict.num network_Grass3NdhfRbclWaxy ${EXTRA}
Grass3NdhfRpocWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy ${EXTRA}
Grass3NdhfWaxyIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfWaxyIts.tree.restrict.num network_Grass3NdhfWaxyIts ${EXTRA}
Grass3PhytRbclIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytRbclIts.tree.restrict.num network_Grass3PhytRbclIts ${EXTRA}
Grass3PhytRbclRpoc:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytRbclRpoc.tree.restrict.num network_Grass3PhytRbclRpoc ${EXTRA}
Grass3PhytRbclWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytRbclWaxy.tree.restrict.num network_Grass3PhytRbclWaxy ${EXTRA}
Grass3PhytRpocIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytRpocIts.tree.restrict.num network_Grass3PhytRpocIts ${EXTRA}
Grass3PhytRpocWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytRpocWaxy.tree.restrict.num network_Grass3PhytRpocWaxy ${EXTRA}
Grass3PhytWaxyIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3PhytWaxyIts.tree.restrict.num network_Grass3PhytWaxyIts ${EXTRA}
Grass3RbclRpocIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3RbclRpocIts.tree.restrict.num network_Grass3RbclRpocIts ${EXTRA}
Grass3RbclRpocWaxy:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3RbclRpocWaxy.tree.restrict.num network_Grass3RbclRpocWaxy ${EXTRA}
Grass3RbclWaxyIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3RbclWaxyIts.tree.restrict.num network_Grass3RbclWaxyIts ${EXTRA}
Grass3RpocWaxyIts:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3RpocWaxyIts.tree.restrict.num network_Grass3RpocWaxyIts ${EXTRA}


tests_np: test_easiest_np qwerty4_np qwerty5_np test_simple_np test_medium_np test_hard_np
test_easiest_np:
	${PY} ${SCRIPT} test_easiest.trees network_test_easiest -np -h 1 ${EXTRA}
qwerty4_np:
	${PY} ${SCRIPT} qwerty4.trees network_qwerty4 -np -h 1 ${EXTRA}
qwerty5_np:
	${PY} ${SCRIPT} qwerty5.trees network_qwerty5 -np -h 2 ${EXTRA}
test_simple_np:
	${PY} ${SCRIPT} test_simple.trees network_test_simple -np -h 3 ${EXTRA}
test_medium_np:
	${PY} ${SCRIPT} test_medium.trees network_test_medium -np -h 5 ${EXTRA}
test_hard_np:
	${PY} ${SCRIPT} test_hard.trees network_test_hard -np -h 7 ${EXTRA}

Grass3NdhfRpocWaxy_np:
	${PY} ${SCRIPT} ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy -np -h 3 ${EXTRA}

Grass2_np: Grass2NdhfWaxy_np Grass2PhytRbcl_np Grass2PhytRpoc_np Grass2PhytWaxy_np Grass2RbclWaxy_np Grass2RpocWaxy_np Grass2WaxyIts_np
Grass2NdhfWaxy_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy -np -h 6 ${EXTRA}
Grass2PhytRbcl_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl -np -h 4 ${EXTRA}
Grass2PhytRpoc_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc -np -h 4 ${EXTRA}
Grass2PhytWaxy_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy -np -h 3 ${EXTRA}
Grass2RbclWaxy_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy -np -h 4 ${EXTRA}
Grass2RpocWaxy_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy -np -h 2 ${EXTRA}
Grass2WaxyIts_np:
	${PY} ${SCRIPT} ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts -np -h 5 ${EXTRA}

clean:
	rm -f *.bee *.beepp *.dimacs *.map

reset_logs:
	echo f,n,k,t,solver > everything.log
	echo f,n,k,t,sat,solver > subtasks.log

meh: meh_thing meh_sub
meh_thing:
	${PY} meh_thing.py everything.log > timing_thing_new.txt
	${PY} meh_thing.py oldthing.log > timing_thing_old.txt
meh_sub:
	${PY} meh_sub.py subtasks.log > timing_sub_new.txt
	${PY} meh_sub.py oldtasks.log > timing_sub_old.txt
