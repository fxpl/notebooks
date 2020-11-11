count() {
	file=$1
	args=$2

	for arg in ${args[@]}
	do
		numCalls=`grep -E "$arg\s*=" $file | wc -l`
		echo "$numCalls $arg"
	done
}

echo "PLOT:"
file=`./get_last_output.sh "matplotlib.pyplot.plot-calls"`
args=(scalex scaley data agg_filter alpha animated anitaliased clip_box clip_on clip_path color contains dash_capstyle dash_joinstyle dashes drawstyle figure fillstyle gid in_layout label linestyle linewidth marker markeredgecolor markeredgewidth markerfacecolor markerfacecoloralt markersize markevery path_effects picker pickradius rasterized sketch_params snap solid_capstyle solid_joinstyle transform url visible xdata ydata)
count $file $args | sort -rn
echo""

echo "ARRAY:"
file=`./get_last_output.sh "numpy.array-calls"`
args=(dtype copy order subok ndmin)
count $file $args | sort -rn
echo""

echo "READ_CSV:"
file=`./get_last_output.sh "pandas.read_csv-calls"`
args=(sep delimiter header names index_col usecols squeeze prefix mangle_dupe_cols dtype engine converters true_values false_values skipinitialspace skiprows skipfooter nrows na_values keep_default_na na_filter verbose skip_blank_lines parse_dates infer_datetime_format keep_date_col date_parser dayfirst cache_dates iterator chunksize compression thousands decimal lineterminator quotechar quoting doublequote escapechar comment encoding dialect error_bad_lines warn_bad_lines delim_whitespace low_memory memory_map float_precision)
count $file $args | sort -rn
echo""

echo "DATAFRAME:"
file=`./get_last_output.sh "pandas.DataFrame-calls"`
args=(data index columns dtype copy)
count $file $args | sort -rn
echo""

