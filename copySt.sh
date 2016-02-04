#! /bin/sh
dir1=s13t264_0$1
dir2=s13t264_0$2
nicebattle=${dir2}/nicebattle

echo ${dir1}
echo ${dir2}
mkdir ${dir2}
mkdir ${nicebattle}
cp ${dir1}/*.java ${dir1}/*.md ${dir2}
