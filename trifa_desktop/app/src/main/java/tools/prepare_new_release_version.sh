#! /bin/bash

_HOME2_=$(dirname $0)
export _HOME2_
_HOME_=$(cd $_HOME2_;pwd)
export _HOME_

basedir="$_HOME_""/../"

f1="com/zoffcc/applications/trifa/MainActivity.java"

cd "$basedir"

if [[ $(git status --porcelain --untracked-files=no) ]]; then
	echo "ERROR: git repo has changes."
	echo "please commit or cleanup the git repo."
	if [ "$1""x" == "-fx" ]; then
		echo "** force mode **"
	else
		exit 1
	fi
else
	echo "git repo clean."
fi

cur_m_version=$(cat "$f1" | grep 'static final String Version = "' | head -1 | \
	sed -e 's#^.*static final String Version = "##' | \
	sed -e 's#".*$##')

# thanks to: https://stackoverflow.com/a/8653732
next_m_version=$(echo "$cur_m_version"|awk -F. -v OFS=. 'NF==1{print ++$NF}; NF>1{if(length($NF+1)>length($NF))$(NF-1)++; $NF=sprintf("%0*d", length($NF), ($NF+1)%(10^length($NF))); print}')

echo $cur_m_version
echo $next_m_version

sed -i -e 's#static final String Version = ".*#static final String Version = "'"$next_m_version"'";#' "$f1"

commit_message="new version ""$next_m_version"
tag_name="$next_m_version"

cd "$basedir"
./do_compile.sh || exit 1
./do_compile.sh || exit 1

./__make_release.sh

mv -v ./releases/trifa_desktop_1.0.xx.zip ./releases/trifa_desktop_"$next_m_version".zip

git commit -m "$commit_message" "$f1"
git tag -a "$next_m_version" -m "$next_m_version"
