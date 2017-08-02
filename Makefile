PY = python
DIR_GRASS2 = data/tests/small/Grass2
DIR_GRASS3 = data/tests/small/Grass3

default:

all: tests Grass2 Grass3

tests: test_easiest qwerty4 qwerty5 test_simple test_medium test_hard
test_easiest:
	${PY} run.py test_easiest.trees network_test_easiest -tl -1
qwerty4:
	${PY} run.py qwerty4.trees network_qwerty4 -tl -1
qwerty5:
	${PY} run.py qwerty5.trees network_qwerty5 -tl -1
test_simple:
	${PY} run.py test_simple.trees network_test_simple -tl -1
test_medium:
	${PY} run.py test_medium.trees network_test_medium -tl -1
test_hard:
	${PY} run.py test_hard.trees network_test_hard -tl -1

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts
Grass2NdhfWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy -ftl -1 -cf 3
Grass2PhytRbcl:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl -ftl -1 -cf 5
Grass2PhytRpoc:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc -ftl -1 -cf 5
Grass2PhytWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy -ftl -1 -cf 5
Grass2RbclWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy -ftl -1 -cf 5
Grass2RpocWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy -ftl -1 -cf 5
Grass2WaxyIts:
	${PY} run.py ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts -ftl -1 -cf 5

Grass3:Grass3NdhfPhytRbcl Grass3NdhfPhytRpoc Grass3NdhfPhytWaxy Grass3NdhfRbclWaxy Grass3NdhfRpocWaxy Grass3NdhfWaxyIts Grass3PhytRbclIts Grass3PhytRbclRpoc Grass3PhytRbclWaxy Grass3PhytRpocIts Grass3PhytRpocWaxy Grass3PhytWaxyIts Grass3RbclRpocIts Grass3RbclRpocWaxy Grass3RbclWaxyIts Grass3RpocWaxyIts
Grass3NdhfPhytRbcl:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRbcl.tree.restrict.num network_Grass3NdhfPhytRbcl -tl -1 -cf 9 -ftl -1
Grass3NdhfPhytRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRpoc.tree.restrict.num network_Grass3NdhfPhytRpoc -tl -1 -cf 9 -ftl -1
Grass3NdhfPhytWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytWaxy.tree.restrict.num network_Grass3NdhfPhytWaxy -tl -1 -cf 9 -ftl -1
Grass3NdhfRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRbclWaxy.tree.restrict.num network_Grass3NdhfRbclWaxy -tl -1 -cf 9 -ftl -1
Grass3NdhfRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy -tl -1 -cf 9 -ftl -1
Grass3NdhfWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfWaxyIts.tree.restrict.num network_Grass3NdhfWaxyIts -tl -1 -cf 9 -ftl -1
Grass3PhytRbclIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclIts.tree.restrict.num network_Grass3PhytRbclIts -tl -1 -cf 9 -ftl -1
Grass3PhytRbclRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclRpoc.tree.restrict.num network_Grass3PhytRbclRpoc -tl -1 -cf 9 -ftl -1
Grass3PhytRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclWaxy.tree.restrict.num network_Grass3PhytRbclWaxy -tl -1 -cf 9 -ftl -1
Grass3PhytRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocIts.tree.restrict.num network_Grass3PhytRpocIts -tl -1 -cf 9 -ftl -1
Grass3PhytRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocWaxy.tree.restrict.num network_Grass3PhytRpocWaxy -tl -1 -cf 9 -ftl -1
Grass3PhytWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytWaxyIts.tree.restrict.num network_Grass3PhytWaxyIts -tl -1 -cf 9 -ftl -1
Grass3RbclRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocIts.tree.restrict.num network_Grass3RbclRpocIts -tl -1 -cf 9 -ftl -1
Grass3RbclRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocWaxy.tree.restrict.num network_Grass3RbclRpocWaxy -tl -1 -cf 9 -ftl -1
Grass3RbclWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclWaxyIts.tree.restrict.num network_Grass3RbclWaxyIts -tl -1 -cf 9 -ftl -1
Grass3RpocWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RpocWaxyIts.tree.restrict.num network_Grass3RpocWaxyIts -tl -1 -cf 9 -ftl -1


tests_ds: test_easiest_ds qwerty4_ds qwerty5_ds test_simple_ds test_medium_ds test_hard_ds
test_easiest_ds:
	${PY} run.py test_easiest.trees network_test_easiest -ds -h 1 -tl -1
qwerty4_ds:
	${PY} run.py qwerty4.trees network_qwerty4 -ds -h 1 -tl -1
qwerty5_ds:
	${PY} run.py qwerty5.trees network_qwerty5 -ds -h 2 -tl -1
test_simple_ds:
	${PY} run.py test_simple.trees network_test_simple -ds -h 3 -tl -1
test_medium_ds:
	${PY} run.py test_medium.trees network_test_medium -ds -h 5 -tl -1
test_hard_ds:
	${PY} run.py test_hard.trees network_test_hard -ds -h 7 -tl -1

Grass3NdhfRpocWaxy_ds:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy -ds -h 3 -tl -1

Grass2_ds: Grass2NdhfWaxy_ds Grass2PhytRbcl_ds Grass2PhytRpoc_ds Grass2PhytWaxy_ds Grass2RbclWaxy_ds Grass2RpocWaxy_ds Grass2WaxyIts_ds
Grass2NdhfWaxy_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy -ds -h 7 -tl -1
Grass2PhytRbcl_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl -ds -h 4 -tl -1
Grass2PhytRpoc_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc -ds -h 4 -tl -1
Grass2PhytWaxy_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy -ds -h 3 -tl -1
Grass2RbclWaxy_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy -ds -h 4 -tl -1
Grass2RpocWaxy_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy -ds -h 2 -tl -1
Grass2WaxyIts_ds:
	${PY} run.py ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts -ds -h 5 -tl -1
