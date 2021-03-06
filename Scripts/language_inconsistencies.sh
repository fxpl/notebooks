#!/bin/bash -l

################################################################################
# Find notebooks where different languages are specified in metadata.language,
# metadata.language_info.name, metadata.kernelspec.language and/or code
# cells.
################################################################################

langFile=`./get_last_output.sh "all_languages"`
sed -n "2,$ p" $langFile \
	| awk -F ',' \
	'{ if(\
		(" UNKNOWN"!=$2 && " UNKNOWN"!=$3 && $2!=$3) \
		|| (" UNKNOWN"!=$2 && " UNKNOWN"!=$4 && $2!=$4) \
		|| (" UNKNOWN"!=$2 && " UNKNOWN"!=$6 && $2!=$6) \
		|| (" UNKNOWN"!=$3 && " UNKNOWN"!=$4 && $3!=$4) \
		|| (" UNKNOWN"!=$3 && " UNKNOWN"!=$6 && $3!=$6) \
		|| (" UNKNOWN"!=$4 && " UNKNOWN"!=$6 && $4!=$6) \
	) \
	{print $1 " " $2 " " $3 " " $4 " " $6}}'


