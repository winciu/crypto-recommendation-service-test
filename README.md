# Crypto Recommendation Service (Test task)

This project is a basic recommendation service based on Spring Boot. Given a set of input data files, it serves a couple of endpoints to check current and past price of cryptocurrencies as well as compare them using a normalized factors. \
Based on the provided data (timestamp and price) the service calculates the following factors: 
* minimal price
* minimal price date
* maximal price
* maximal price date
* oldest price
* oldest price date
* newest price
* newest price date
* daily/weekly/monthly normalized factor

The normalized factors are calculated using the below formula (with respect to the actual date period used):
```math
normalized\_factor = \frac{max\_price - min\_price}{min\_price}
```

# Input data files

In order for this service to work properly, we need to provide a path to a directory with CSV files containing cryptocurrencies' prices.
The service loads input data from those files at startup.\
By default, (during a development), the service is using data files stored in `src/main/resources/static/Prices` folder.
However, if you're testing or running this service from outside the IDE we need to provide a direct path to the folder with data files.
This is done by setting `service.input-data-path` parameter when starting the service, for example:

`--service.input-data-path=file:./src/main/resources/static/Prices`

Each CSV input data file should have the following header denoting data in each column: `timestamp,symbol,price`.\
For instance, below is snippet from a correct input data file.
```
timestamp,symbol,price
1641024000000,ETH,3715.32
1641031200000,ETH,3718.67
1641049200000,ETH,3697.04
1641056400000,ETH,3727.61
1641088800000,ETH,3747
1641121200000,ETH,3743.17
```
The name of the input data file does not matter.

# Scheduling

This service has a scheduler implemented to analyze the input data.
Analysis is done on a daily basis. By default, it is a current day (today).
Due to the nature of already provided input data files (prices are from the past) the scheduler needs to be configured accordingly to load those older data files as well.
This is done by specifying predefined dates for which data should be loaded first. See `service.scheduling.predefined-dates` setting in 
`application.yaml` file for more details. \
By default, scheduler is set to run `every 20 seconds` using a cron expression `*/20 * * * * *`. To run scheduler every two minutes use `--service.scheduling.cron="0 0/2 * * * *"` parameter at service startup time.

# Build

To build the service and run tests, you need to download the source code and save it in the folder of your choice.
Then go to that folder and run the following command to build the jar file.

    mvn clean install

As a prerequisite you need to have `Maven` and `Java 17` already installed.

# Run

Once the service is built, you can run it by executing the command below.

    java -jar target/crypto-recommendation-service-test-0.0.1-SNAPSHOT.jar --service.input-data-path=file:./src/main/resources/static/Prices [OTHER_OPTIONAL_PARAMS]

# Use

This service exposes three endpoints, namely:

    GET /api/cryptos/ranking/{date}/{period}    
    GET /api/cryptos/{symbol}/factors/{date}/{period}
    GET /api/cryptos/best/{date}/{period}

In all endpoints `date` and `period` variables are optional. \
`date` variable should be in `yyyy-MM-dd` format. If `date` is not provided then the current date (today) is used instead by default. \
`period` variable has the following possible values: `DAY, WEEK, MONTH`. This variable can be set if the `date` variable is already set (explicitly).
The default value for this variable is `DAY`. This variable is used to specify the date period in the past. \
For instance, assuming that today is December 31<sup>st</sup> and you would like to get data from last week use the following path: `/2023-12-31/WEEK` \
The same rules for those input variables apply for all endpoints.

#### GET /api/cryptos/ranking
This endpoint returns a descending sorted list of all cryptos using a normalized factor as a comparator. \
Example request:

    curl  "http://localhost:8082/api/cryptos/ranking/2022-01-07/WEEK"

Example response:

```json
[
  {
    "symbol": "XRP"
  },
  {
    "symbol": "DOGE"
  },
  {
    "symbol": "ETH"
  },
  {
    "symbol": "LTC"
  },
  {
    "symbol": "BTC"
  }
]

```

#### GET /api/cryptos/{symbol}
This endpoint returns price factors related to the requested crypto. The `symbol` variable is used to specify the cryptocurrency ticker. It must match the following regular expression: `[A-Z]{2,6}`. \
Example request:

    curl  "http://localhost:8082/api/cryptos/ETH/factors/2022-01-10/WEEK"

Example response:
```json
{
  "symbol": "ETH",
  "referenceDate": "2022-01-10",
  "minPrice": 3238.74000,
  "minPriceDate": "2022-01-07T04:00:00Z",
  "maxPrice": 3821.02000,
  "maxPriceDate": "2022-01-05T15:00:00Z",
  "oldestPrice": 3753.34000,
  "oldestPriceDate": "2022-01-04T07:00:00Z",
  "newestPrice": 3238.74000,
  "newestPriceDate": "2022-01-07T04:00:00Z",
  "period": "WEEK"
}
```

#### GET /api/cryptos/best
This endpoint returns a cryptocurrency with the highest normalized factor for the specified period. \
Example request:

    curl "http://localhost:8082/api/cryptos/best/2022-01-04"

Example response:
```json
{
  "symbol": "ETH"
}
```

# Swagger / Open API

If you would like to check/test this service using its REST API via Swagger, then after running
the service, go to the web browser and type:

    http://localhost:8082/swagger-ui/index.html

# Database
This service is using the H2 relation database and stores the data (fetched exchange rates) in memory.
If you would like to use another DB then please specify all the required connection information in the
`application.yaml` file or by using parameters in the command line during service startup.

# Containerizing

Make sure Docker/Podman is running i.e. `ps aux | grep docker`

0. Go to project root folder
1. Build an image: `docker build --tag=crypto-recommendation-service:latest .`
2. Run the container: `docker run crypto-recommendation-service:latest`

---
Please notice that the default service port is `8082`.
This value is also configurable in the `application.yaml` file or by specifying the corresponding runtime parameter (`--server.port`).