# Batch insert

Total records count to insert 300 000.

| reWriteBatchedInserts | shouldReturnGeneratedValues | Throughput           |
|-----------------------|-----------------------------|----------------------|
| false                 | true                        | 49 929               |
| true                  | true                        | 50 731      (+ 1%)   |
| false                 | false                       | 64 425      (+ 27%)  |
| **true**              | **false**                   | **238 133** (+ 270%) |

# Batch update

## 1. Correlation with size of the table (updated rows = 10 000, average insert rps = 85 000)

| total count | separate transaction | one transaction | exposed bulk update | temporal table |
|-------------|----------------------|-----------------|---------------------|----------------|
| 50 000      | 673                  | 2 082           | 28 901              | 53 475         |
| 100 000     | 664                  | 1 891           | 22 573              | 55 555         |
| 200 000     | 620                  | 1 770           | 27 027              | 54 054         |

### Conclusion

Update throughput doesn't depend on size of the table.

## 2. Correlation with size of the updated data (total rows = 200 000, average insert rps = 85 000)

| updated count | separate transaction | one transaction | exposed bulk update | temporal table  |
|---------------|----------------------|-----------------|---------------------|-----------------|
| 5 000         | 611                  | 1 913           | 25 380              | 34 013          |
| 10 000        | 655                  | 1 791           | 25 641              | 48 543          |
| 20 000        | 592                  | 1 714 (+ 189%)  | 27 739 (+ 1 518%)   | 65 359 (+ 135%) |

### Conclusion

Update with temporal table asymptotically tends to the data insertion rate 
