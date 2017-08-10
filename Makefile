PY = python
DIR_GRASS2 = data/tests/small/Grass2
DIR_GRASS3 = data/tests/small/Grass3

default:

all: tests Grass2 Grass3

# test_medium test_hard
tests: test_easiest test_folded qwerty4 qwerty5 test_simple test_easy
test_easiest:
	${PY} run.py test_easiest.trees network_test_easiest
test_folded:
	${PY} run.py test_folded.trees network_test_folded
qwerty4:
	${PY} run.py qwerty4.trees network_qwerty4
qwerty5:
	${PY} run.py qwerty5.trees network_qwerty5
test_simple:
	${PY} run.py test_simple.trees network_test_simple
test_easy:
	${PY} run.py test_easy.trees network_test_easy
test_medium:
	${PY} run.py test_medium.trees network_test_medium
test_hard:
	${PY} run.py test_hard.trees network_test_hard -tl 300 -ftl 30

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts
Grass2NdhfWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy
Grass2PhytRbcl:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl
Grass2PhytRpoc:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc
Grass2PhytWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy
Grass2RbclWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy
Grass2RpocWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy
Grass2WaxyIts:
	${PY} run.py ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts

Grass3:Grass3NdhfPhytRbcl Grass3NdhfPhytRpoc Grass3NdhfPhytWaxy Grass3NdhfRbclWaxy Grass3NdhfRpocWaxy Grass3NdhfWaxyIts Grass3PhytRbclIts Grass3PhytRbclRpoc Grass3PhytRbclWaxy Grass3PhytRpocIts Grass3PhytRpocWaxy Grass3PhytWaxyIts Grass3RbclRpocIts Grass3RbclRpocWaxy Grass3RbclWaxyIts Grass3RpocWaxyIts
Grass3NdhfPhytRbcl:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRbcl.tree.restrict.num network_Grass3NdhfPhytRbcl
Grass3NdhfPhytRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRpoc.tree.restrict.num network_Grass3NdhfPhytRpoc
Grass3NdhfPhytWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytWaxy.tree.restrict.num network_Grass3NdhfPhytWaxy
Grass3NdhfRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRbclWaxy.tree.restrict.num network_Grass3NdhfRbclWaxy
Grass3NdhfRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy
Grass3NdhfWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfWaxyIts.tree.restrict.num network_Grass3NdhfWaxyIts
Grass3PhytRbclIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclIts.tree.restrict.num network_Grass3PhytRbclIts
Grass3PhytRbclRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclRpoc.tree.restrict.num network_Grass3PhytRbclRpoc
Grass3PhytRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclWaxy.tree.restrict.num network_Grass3PhytRbclWaxy
Grass3PhytRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocIts.tree.restrict.num network_Grass3PhytRpocIts
Grass3PhytRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocWaxy.tree.restrict.num network_Grass3PhytRpocWaxy
Grass3PhytWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytWaxyIts.tree.restrict.num network_Grass3PhytWaxyIts
Grass3RbclRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocIts.tree.restrict.num network_Grass3RbclRpocIts
Grass3RbclRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocWaxy.tree.restrict.num network_Grass3RbclRpocWaxy
Grass3RbclWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclWaxyIts.tree.restrict.num network_Grass3RbclWaxyIts
Grass3RpocWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RpocWaxyIts.tree.restrict.num network_Grass3RpocWaxyIts
