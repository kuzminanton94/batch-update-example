##### totalSize=200000, updateSize=5000
1. strategy=SEPARATE_TRANSACTION, insert=7.747170084s, update=9.692075166s
2. strategy=ONE_TRANSACTION, insert=7.708564834s, update=3.047392458s
3. strategy=EXPOSED, insert=6.890814541s, update=264.751542ms
4. strategy=TEMP_TABLE, insert=6.842681709s, update=229.193667ms

##### totalSize=200000, updateSize=10000
1. strategy=SEPARATE_TRANSACTION, insert=7.829496584s, update=14.451975958s
2. strategy=ONE_TRANSACTION, insert=6.848157458s, update=5.401840875s
3. strategy=EXPOSED, insert=6.666469292s, update=557.208375ms
4. strategy=TEMP_TABLE, insert=6.975928750s, update=404.143416ms

##### totalSize=200000, updateSize=20000
1. strategy=SEPARATE_TRANSACTION, insert=6.953598584s, update=33.099096s
2. strategy=ONE_TRANSACTION, insert=7.329123250s, update=12.021460209s
3. strategy=EXPOSED, insert=6.901293666s, update=1.020147416s
4. strategy=TEMP_TABLE, insert=6.893988208s, update=832.661750ms
