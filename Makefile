PY = python
DIR_GRASS2 = data/tests/small/Grass2
DIR_GRASS3 = data/tests/small/Grass3

default:

all: tests Grass2 Grass3

# test_medium test_hard
tests: test_easiest test_folded qwerty4 qwerty5 test_simple test_easy
test_easiest:
	${PY} run.py test_easiest.trees network_test_easiest ${EXTRA}
test_folded:
	${PY} run.py test_folded.trees network_test_folded ${EXTRA}
qwerty4:
	${PY} run.py qwerty4.trees network_qwerty4 ${EXTRA}
qwerty5:
	${PY} run.py qwerty5.trees network_qwerty5 ${EXTRA}
test_simple:
	${PY} run.py test_simple.trees network_test_simple ${EXTRA}
test_easy:
	${PY} run.py test_easy.trees network_test_easy ${EXTRA}
test_medium:
	${PY} run.py test_medium.trees network_test_medium ${EXTRA}
test_hard:
	${PY} run.py test_hard.trees network_test_hard -tl 300 -ftl 30 ${EXTRA}

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts
Grass2NdhfWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy ${EXTRA}
Grass2PhytRbcl:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl ${EXTRA}
Grass2PhytRpoc:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc ${EXTRA}
Grass2PhytWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy ${EXTRA}
Grass2RbclWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy ${EXTRA}
Grass2RpocWaxy:
	${PY} run.py ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy ${EXTRA}
Grass2WaxyIts:
	${PY} run.py ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts ${EXTRA}

Grass3:Grass3NdhfPhytRbcl Grass3NdhfPhytRpoc Grass3NdhfPhytWaxy Grass3NdhfRbclWaxy Grass3NdhfRpocWaxy Grass3NdhfWaxyIts Grass3PhytRbclIts Grass3PhytRbclRpoc Grass3PhytRbclWaxy Grass3PhytRpocIts Grass3PhytRpocWaxy Grass3PhytWaxyIts Grass3RbclRpocIts Grass3RbclRpocWaxy Grass3RbclWaxyIts Grass3RpocWaxyIts
Grass3NdhfPhytRbcl:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRbcl.tree.restrict.num network_Grass3NdhfPhytRbcl ${EXTRA}
Grass3NdhfPhytRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytRpoc.tree.restrict.num network_Grass3NdhfPhytRpoc ${EXTRA}
Grass3NdhfPhytWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfPhytWaxy.tree.restrict.num network_Grass3NdhfPhytWaxy ${EXTRA}
Grass3NdhfRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRbclWaxy.tree.restrict.num network_Grass3NdhfRbclWaxy ${EXTRA}
Grass3NdhfRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy ${EXTRA}
Grass3NdhfWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3NdhfWaxyIts.tree.restrict.num network_Grass3NdhfWaxyIts ${EXTRA}
Grass3PhytRbclIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclIts.tree.restrict.num network_Grass3PhytRbclIts ${EXTRA}
Grass3PhytRbclRpoc:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclRpoc.tree.restrict.num network_Grass3PhytRbclRpoc ${EXTRA}
Grass3PhytRbclWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRbclWaxy.tree.restrict.num network_Grass3PhytRbclWaxy ${EXTRA}
Grass3PhytRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocIts.tree.restrict.num network_Grass3PhytRpocIts ${EXTRA}
Grass3PhytRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytRpocWaxy.tree.restrict.num network_Grass3PhytRpocWaxy ${EXTRA}
Grass3PhytWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3PhytWaxyIts.tree.restrict.num network_Grass3PhytWaxyIts ${EXTRA}
Grass3RbclRpocIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocIts.tree.restrict.num network_Grass3RbclRpocIts ${EXTRA}
Grass3RbclRpocWaxy:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclRpocWaxy.tree.restrict.num network_Grass3RbclRpocWaxy ${EXTRA}
Grass3RbclWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RbclWaxyIts.tree.restrict.num network_Grass3RbclWaxyIts ${EXTRA}
Grass3RpocWaxyIts:
	${PY} run.py ${DIR_GRASS3}/Grass3RpocWaxyIts.tree.restrict.num network_Grass3RpocWaxyIts ${EXTRA}
