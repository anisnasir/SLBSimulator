#!/bin/bash
JAR="StreamLoadBalancing-0.0.5-SNAPSHOT-jar-with-dependencies.jar"
indir="input"
NumberOfServers="5 10 50 100"
NumberOfReplicas="5 10 100 1000 100000"
#data="twitter"
#data="wiki"
loadBalancer="0" #loadBalancer == 0: consistent hashing
#loadBalancer="5" #loadBalancer == m > 0: power of two choices with stats accumulated over m minutes
if [[ $loadBalancer -eq 0 ]] ; then
  lbname="ch"
else
  lbname="potc$loadBalancer"
fi

maxprocs="10"
command="java -jar ${JAR}"
datasets="twitter wiki"
initialTimestamp["twitter"]=1341791969
initialTimestamp["wiki"]=1199195421

for data in $datasets; do
  input="${indir}/${data}"
  output="output_${data}"
  for ns in $NumberOfServers ; do   
    for nr in $NumberOfReplicas ; do
      echo "$command ${input} ${output}_${ns}_${nr}_${lbname} $ns $nr ${initialTimestamp[$data]} $loadBalancer" >> ${output}_${ns}_${nr}_${lbname}.log
      cmdlines="$cmdlines $command ${input} ${output}_${ns}_${nr}_${lbname} $ns $nr ${initialTimestamp[$data]} $loadBalancer >> ${output}_${ns}_${nr}_${lbname}.log;"
    done
  done
done
echo -e $cmdlines | parallel --max-procs $maxprocs
echo "Done"
