DIR_GRASS2 = data/tests/small/Grass2
DIR_GRASS3 = data/tests/small/Grass3

default:

all: tests Grass2 Grass3

tests: test_easiest test_simple test_medium test_hard
test_easiest:
	python run.py test_easiest.trees network_test_easiest
test_simple:
	python run.py test_simple.trees network_test_simple
test_medium:
	python run.py test_medium.trees network_test_medium
test_hard:
	python run.py test_hard.trees network_test_hard -tl 300 -ftl 30

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts
Grass2NdhfWaxy:
	python run.py ${DIR_GRASS2}/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy
Grass2PhytRbcl:
	python run.py ${DIR_GRASS2}/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl
Grass2PhytRpoc:
	python run.py ${DIR_GRASS2}/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc
Grass2PhytWaxy:
	python run.py ${DIR_GRASS2}/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy
Grass2RbclWaxy:
	python run.py ${DIR_GRASS2}/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy
Grass2RpocWaxy:
	python run.py ${DIR_GRASS2}/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy
Grass2WaxyIts:
	python run.py ${DIR_GRASS2}/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts

Grass3:Grass3NdhfPhytRbcl Grass3NdhfPhytRpoc Grass3NdhfPhytWaxy Grass3NdhfRbclWaxy Grass3NdhfRpocWaxy Grass3NdhfWaxyIts Grass3PhytRbclIts Grass3PhytRbclRpoc Grass3PhytRbclWaxy Grass3PhytRpocIts Grass3PhytRpocWaxy Grass3PhytWaxyIts Grass3RbclRpocIts Grass3RbclRpocWaxy Grass3RbclWaxyIts Grass3RpocWaxyIts
Grass3NdhfPhytRbcl:
	python run.py ${DIR_GRASS3}/Grass3NdhfPhytRbcl.tree.restrict.num network_Grass3NdhfPhytRbcl
Grass3NdhfPhytRpoc:
	python run.py ${DIR_GRASS3}/Grass3NdhfPhytRpoc.tree.restrict.num network_Grass3NdhfPhytRpoc
Grass3NdhfPhytWaxy:
	python run.py ${DIR_GRASS3}/Grass3NdhfPhytWaxy.tree.restrict.num network_Grass3NdhfPhytWaxy
Grass3NdhfRbclWaxy:
	python run.py ${DIR_GRASS3}/Grass3NdhfRbclWaxy.tree.restrict.num network_Grass3NdhfRbclWaxy
Grass3NdhfRpocWaxy:
	python run.py ${DIR_GRASS3}/Grass3NdhfRpocWaxy.tree.restrict.num network_Grass3NdhfRpocWaxy
Grass3NdhfWaxyIts:
	python run.py ${DIR_GRASS3}/Grass3NdhfWaxyIts.tree.restrict.num network_Grass3NdhfWaxyIts
Grass3PhytRbclIts:
	python run.py ${DIR_GRASS3}/Grass3PhytRbclIts.tree.restrict.num network_Grass3PhytRbclIts
Grass3PhytRbclRpoc:
	python run.py ${DIR_GRASS3}/Grass3PhytRbclRpoc.tree.restrict.num network_Grass3PhytRbclRpoc
Grass3PhytRbclWaxy:
	python run.py ${DIR_GRASS3}/Grass3PhytRbclWaxy.tree.restrict.num network_Grass3PhytRbclWaxy
Grass3PhytRpocIts:
	python run.py ${DIR_GRASS3}/Grass3PhytRpocIts.tree.restrict.num network_Grass3PhytRpocIts
Grass3PhytRpocWaxy:
	python run.py ${DIR_GRASS3}/Grass3PhytRpocWaxy.tree.restrict.num network_Grass3PhytRpocWaxy
Grass3PhytWaxyIts:
	python run.py ${DIR_GRASS3}/Grass3PhytWaxyIts.tree.restrict.num network_Grass3PhytWaxyIts
Grass3RbclRpocIts:
	python run.py ${DIR_GRASS3}/Grass3RbclRpocIts.tree.restrict.num network_Grass3RbclRpocIts
Grass3RbclRpocWaxy:
	python run.py ${DIR_GRASS3}/Grass3RbclRpocWaxy.tree.restrict.num network_Grass3RbclRpocWaxy
Grass3RbclWaxyIts:
	python run.py ${DIR_GRASS3}/Grass3RbclWaxyIts.tree.restrict.num network_Grass3RbclWaxyIts
Grass3RpocWaxyIts:
	python run.py ${DIR_GRASS3}/Grass3RpocWaxyIts.tree.restrict.num network_Grass3RpocWaxyIts
