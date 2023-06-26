# Batch insert

Total records count to insert 300 000.

| reWriteBatchedInserts | shouldReturnGeneratedValues | Throughput           |
|-----------------------|-----------------------------|----------------------|
| false                 | true                        | 49 929               |
| true                  | true                        | 50 731      (+ 1%)   |
| false                 | false                       | 64 425      (+ 27%)  |
| **true**              | **false**                   | **238 133** (+ 270%) |

# Batch update

## 1. Correlation with size of the table

Updated rows = 10 000
Average insert rps = 85 000

| total count | separate transaction | one transaction | exposed bulk update | temporal table |
|-------------|----------------------|-----------------|---------------------|----------------|
| 50 000      | 673                  | 2 082           | 28 901              | 53 475         |
| 100 000     | 664                  | 1 891           | 22 573              | 55 555         |
| 200 000     | 620                  | 1 770           | 27 027              | 54 054         |

### Conclusion

Update throughput doesn't depend on size of the table.

## 2. Correlation with size of the updated data

Total rows = 200 000
Average insert rps = 85 000

| updated count | separate transaction | one transaction | exposed bulk update | temporal table  |
|---------------|----------------------|-----------------|---------------------|-----------------|
| 5 000         | 611                  | 1 913           | 25 380              | 34 013          |
| 10 000        | 655                  | 1 791           | 25 641              | 48 543          |
| 20 000        | 592                  | 1 714 (+ 189%)  | 27 739 (+ 1 518%)   | 65 359 (+ 135%) |

### Conclusion

Update with temporal table asymptotically tends to the data insertion rate

## 3. Correlation with batch insert flags

Total rows = 200 000
Updated rows = 20 000

| reWriteBatchedInserts | shouldReturnGeneratedValues | insert throughput | exposed bulk update | temporal table      |
|-----------------------|-----------------------------|-------------------|---------------------|---------------------|
| false                 | true                        | 36 330            | 26 109              | 28 901 (+ 10%)      |
| false                 | false                       | 40 700            | 28 050              | 36 630              |
| true                  | true                        | 36 390            | 27 739              | 31 897              |
| true                  | false                       | 91 491            | **28 169**          | **62 695** (+ 122%) |

### Conclusion

If reWriteBatchedInserts is enabled in JDBC driver and shouldReturnGeneratedValues is false,
then bulk update performance with temporal table much better than bulk update with exposed.
Otherwise, they are approximately close.

#### Links

https://jdbc.postgresql.org/documentation/use/

https://github.com/JetBrains/Exposed/wiki/DSL#batch-insert
