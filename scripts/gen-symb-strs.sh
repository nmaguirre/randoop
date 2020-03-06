#!/bin/bash


CP=../build/libs/randoop-all-3.0.6.jar

case=$1
minsc=$(($2))
maxsc=$(($3))

if [[ $case == avl ]]; then
    testclass=symbolicheap.bounded.AvlTree
elif [[ $case == treeset ]]; then
    testclass=symbolicheap.bounded.TreeSet
elif [[ $case == binheap ]]; then
    testclass=symbolicheap.bounded.BinomialHeap
else
    echo "Wring case study"
    exit 1
fi

filter=BE
posreps=100
negreps=100
#posreps=600
#negreps=1
#negreps=5000
#posreps=600
#negreps=1000
#filter=FE
#posreps=600
#negreps=3000
#symbmut=500


for (( scope=$minsc;scope<=$maxsc;scope++ ))
do

doublescope=$(($scope*2))

logs=../logs
literals=../literals/literals$scope.txt

cmd="java -ea -cp $CP randoop.main.Main gentests --testclass=$testclass --outputlimit=10000000 --inputlimit=10000000 --timelimit=3600 --junit-output-dir=src/test/java --junit-package-name=symbolicheap.bounded --regression-test-basename=RandoopTest --vectorization-max-objects=$scope --only-test-public-members=true --omitmethods="subList" --vectors-file=$logs/$case-positives$scope.txt --negative-vectors-file=$logs/$case-negatives$scope.txt --no-default-primitives=true --vectorization-remove-unused=true --vectorization-max-array-objects=$scope --vectorization-mutate-all-objects=true --vectorization-mutate-within-extensions=false --vectorization-ignore-static=true --randomseed=0 --literals-level=ALL --literals-file=$literals --filtering=$filter --symbolic-vectors-file=$logs/$case-symbolic$scope.txt --negative-symbolic-vectors-file=$logs/$case-negative-symbolic$scope.txt --max-sequence-length=50 --pos-strs-rep=$posreps --neg-strs-rep=$negreps --pseudoexhaustive-mutations=false --symbolic-mutations-by-structure=$doublescope"
#--omitfields=element|key"

#--pos-strs-rep=$posreps --symbolic-mutations-by-structure=$doublescope --iterations-to-omitfields=5 --omitfields-after-iterations=element|key"
#--omitfields=element|key --symbolic-mutations-by-structure=$symbmut"

echo $cmd
$cmd


done
