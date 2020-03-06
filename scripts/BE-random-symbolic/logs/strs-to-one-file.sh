#!/bin/bash

case=$1
minsc=$(($2))
maxsc=$(($3))




for (( scope=$minsc;scope<=$maxsc;scope++ )); do

    header=$case-header
    pos=$case-positives$scope.txt
    symb=$case-symbolic$scope.txt
    neg=$case-negatives$scope.txt
    negsymb=$case-negative-symbolic$scope.txt
    res=$case-scope$scope-BE4-STR.txt
#    res=$case-scope$scope-FE.txt

    head -n 1 $pos > $header
    sed -e 's/$/,class/' $header > $header.tmp

    tail -n +2 $pos > $pos.tmp
    tail -n +2 $symb > $symb.tmp
    cp $neg $neg.tmp
    cp $negsymb $negsymb.tmp

    sed -e 's/$/,1/' -i.tmp $pos.tmp
    sed -e 's/$/,1/' -i.tmp $symb.tmp
    sed -e 's/$/,0/' -i.tmp $neg.tmp
    sed -e 's/$/,0/' -i.tmp $negsymb.tmp

    cat $header.tmp $pos.tmp $symb.tmp $neg.tmp $negsymb.tmp > $res

done

