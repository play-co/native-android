all: setup
	ant -f TeaLeaf/build.xml release

clean:
	ant -f TeaLeaf/build.xml clean

setup:
	android update project -p TeaLeaf --target android-15 --subprojects
