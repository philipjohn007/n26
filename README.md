# n26

RESTful API for statistics

### APIs

> POST /api/transactions

    {
        "amount": 30.0,
        "timestamp": 1526485619863
    }

> GET /api/statistics

    {
        "sum": 81.0,
        "avg": 27.0,
        "max": 30.0,
        "min": 24.0,
        "count": 3
    }

### Setting up

The project is Spring Boot Maven. Please use the following commands to setup the application.

* Build
```
mvn clean package
```
* Run
```
java -jar target/transaction-0.0.1-SNAPSHOT.jar
```
* Test
```
mvn test
```

### Tests added

* **testTransactionCreateThrowBadRequest** - to verify if an exception is thrown if timestamp field is not provided
* **testTransactionCreateThrowExpired** - to verify if an exception is thrown if timestamp field is expired
* **testTransactionCreateSuccessful** - to verify if transaction creation is successful
* **testEmptyStatistics** - to verify the statistics result if there are no transactions
* **testStatisticsDefault** - to verify the statistics when transactions are present
* **testTransactionStatisticsUpdateAfterExpiredTransaction** - to verify if the statistics get updated after the transaction gets expired
