DIR_GRASS2 = data/tests/small/Grass2

default:

all: all_tests Grass2

Grass2: Grass2NdhfWaxy Grass2PhytRbcl Grass2PhytRpoc Grass2PhytWaxy Grass2RbclWaxy Grass2RpocWaxy Grass2WaxyIts

Grass2NdhfWaxy:
	python run.py $DIR_GRASS2/Grass2NdhfWaxy.tree.restrict.num network_Grass2NdhfWaxy
Grass2PhytRbcl:
	python run.py $DIR_GRASS2/Grass2PhytRbcl.tree.restrict.num network_Grass2PhytRbcl
Grass2PhytRpoc:
	python run.py $DIR_GRASS2/Grass2PhytRpoc.tree.restrict.num network_Grass2PhytRpoc
Grass2PhytWaxy:
	python run.py $DIR_GRASS2/Grass2PhytWaxy.tree.restrict.num network_Grass2PhytWaxy
Grass2RbclWaxy:
	python run.py $DIR_GRASS2/Grass2RbclWaxy.tree.restrict.num network_Grass2RbclWaxy
Grass2RpocWaxy:
	python run.py $DIR_GRASS2/Grass2RpocWaxy.tree.restrict.num network_Grass2RpocWaxy
Grass2WaxyIts:
	python run.py $DIR_GRASS2/Grass2WaxyIts.tree.restrict.num network_Grass2WaxyIts

all_tests: test_easiest test_simple test_medium test_hard

test_easiest:
	python run.py test_easiest.trees network_test_easiest
test_simple:
	python run.py test_simple.trees network_test_simple
test_medium:
	python run.py test_medium.trees network_test_medium
test_hard:
	python run.py test_hard.trees network_test_hard
