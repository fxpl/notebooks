################################################################################
# Move all files listed in $1 to the corresponding directory under $1
################################################################################

if [ $# -ne 2 ];
then
	echo "Usage: move_specified.sh file_with_paths target_dir"
	exit
fi
paths=$1
targetDir=$2

projectDir="/proj/uppstore2019098"
srcDir="$projectDir/notebooks"
targetDir="$projectDir/$2"

cat $paths | while read path;
do
	notebook=`echo $path | rev | cut -d'/' -f1 | rev`
	subDir=`echo $path | rev | cut -d'/' -f2- | rev`
	mkdir -p $targetDir/$subDir
	mv $srcDir/$subDir/$notebook $targetDir/$subDir
done
