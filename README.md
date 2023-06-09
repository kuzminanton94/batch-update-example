### Changing updateSize

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

### Changing totalSize

##### totalSize=50000, updateSize=10000
1. strategy=SEPARATE_TRANSACTION, insert=2.358904291s, update=15.848161625s
2. strategy=ONE_TRANSACTION, insert=1.707492250s, update=5.537854625s
3. strategy=EXPOSED, insert=1.617163041s, update=493.266166ms
4. strategy=TEMP_TABLE, insert=1.799504292s, update=403.157125ms

##### totalSize=100000, updateSize=10000
1. strategy=SEPARATE_TRANSACTION, insert=4.140753084s, update=17.811299208s
2. strategy=ONE_TRANSACTION, insert=3.502762083s, update=7.319330417s
3. strategy=EXPOSED, insert=3.672182625s, update=506.451083ms
4. strategy=TEMP_TABLE, insert=3.683274708s, update=389.385125ms

##### totalSize=200000, updateSize=10000
1. strategy=SEPARATE_TRANSACTION, insert=7.829496584s, update=14.451975958s
2. strategy=ONE_TRANSACTION, insert=6.848157458s, update=5.401840875s
3. strategy=EXPOSED, insert=6.666469292s, update=557.208375ms
4. strategy=TEMP_TABLE, insert=6.975928750s, update=404.143416ms
