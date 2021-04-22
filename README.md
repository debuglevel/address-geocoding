<!--- some badges to display on the GitHub page -->

![Travis (.org)](https://img.shields.io/travis/debuglevel/address-geocoding?label=Travis%20build)
![Gitlab pipeline status](https://img.shields.io/gitlab/pipeline/debuglevel/address-geocoding?label=GitLab%20build)
![GitHub release (latest SemVer)](https://img.shields.io/github/v/release/debuglevel/address-geocoding?sort=semver)
![GitHub](https://img.shields.io/github/license/debuglevel/address-geocoding)
[![Gitpod ready-to-code](https://img.shields.io/badge/Gitpod-ready--to--code-blue?logo=gitpod)](https://gitpod.io/#https://github.com/debuglevel/address-geocoding)

# Address geocoding microservice

This microservice is used for geocoding an address (e.g. `96047 Bamberg Markusplatz 3`) to a longitude and latitude
pair (e.g. `latitude: 49.89568, longitude: 10.88380`).

# HTTP API

## OpenAPI / Swagger

There is an OpenAPI (former: Swagger) specification created, which is available
at <http://localhost:8080/swagger/greeter-microservice-0.1.yml>, `build/tmp/kapt3/classes/main/META-INF/swagger/` in the
source directory and `META-INF/swagger/` in the jar file. It can easily be pasted into
the [Swagger Editor](https://editor.swagger.io) which provides a live demo
for [Swagger UI](https://swagger.io/tools/swagger-ui/), but also offers to create client libraries
via [OpenAPI Generator](https://openapi-generator.tech).

## Add geocode

Add a geocode (or rather a request to geocode a valid address):

```bash
$ curl --location --request POST 'http://localhost:8080/geocodes/' \
--header 'Content-Type: application/json' \
--data-raw '{
    "address": "96047 Bamberg, Markusplatz 3"
}'

{
    "id": "44987fb9-d114-4956-99ae-cb83573c3677",
    "address": "96047 Bamberg, Markusplatz 3",
    "status": "Pending",
    "failedAttempts": 0,
    "createdOn": "2021-04-22T23:36:41.745",
    "lastModifiedOn": "2021-04-22T23:36:41.745"
}
```

Of course also an invalid address might be added:

```bash
$ curl --location --request POST 'http://localhost:8080/geocodes/' \
--header 'Content-Type: application/json' \
--data-raw '{
    "address": "96047 Bamberg, Sonstnochwasweg 3243"
}'

{
    "id": "a09220a3-33f1-422f-9609-d5e7a880e703",
    "address": "96047 Bamberg, Sonstnochwasweg 3243",
    "status": "Pending",
    "failedAttempts": 0,
    "createdOn": "2021-04-22T23:38:03.066",
    "lastModifiedOn": "2021-04-22T23:38:03.066"
}
```

## Get all geocodes

All ever submitted geocodes can be retrieved:

```bash
$ curl --location --request GET 'http://localhost:8080/geocodes/'

[
    {
        "id": "44987fb9-d114-4956-99ae-cb83573c3677",
        "address": "96047 Bamberg, Markusplatz 3",
        "status": "Succeeded",
        "longitude": 10.883799413534243,
        "latitude": 49.8956786,
        "lastGeocodingOn": "2021-04-22T23:36:41.787",
        "failedAttempts": 0,
        "createdOn": "2021-04-22T23:36:41.745",
        "lastModifiedOn": "2021-04-22T23:36:44.282"
    },
    {
        "id": "a09220a3-33f1-422f-9609-d5e7a880e703",
        "address": "96047 Bamberg, Sonstnochwasweg 3243",
        "status": "AddressNotFound",
        "lastGeocodingOn": "2021-04-22T23:38:03.076",
        "failedAttempts": 1,
        "createdOn": "2021-04-22T23:38:03.066",
        "lastModifiedOn": "2021-04-22T23:38:03.243"
    }
]
```

## Get geocode

Or an individual geocode can be retrieved:

```bash
$ curl --location --request GET 'http://localhost:8080/geocodes/44987fb9-d114-4956-99ae-cb83573c3677'

{
    "id": "44987fb9-d114-4956-99ae-cb83573c3677",
    "address": "96047 Bamberg, Markusplatz 3",
    "status": "Succeeded",
    "longitude": 10.883799413534243,
    "latitude": 49.8956786,
    "lastGeocodingOn": "2021-04-22T23:36:41.787",
    "failedAttempts": 0,
    "createdOn": "2021-04-22T23:36:41.745",
    "lastModifiedOn": "2021-04-22T23:36:44.282"
}
```

## Get statistics

As this microservice heavily relies on upstream services, statistics about them can be retrieved:

```bash
$ curl --location --request GET 'http://localhost:8080/statistics/'

[
    {
        "name": "NominatimGeocoder",
        "unreachable": 0,
        "unknownAddress": 1,
        "success": 6,
        "averageRequestDuration": 0.20779604285714287,
        "queueSize": 0
    },
    {
        "name": "PhotonGeocoder",
        "unreachable": 0,
        "unknownAddress": 3,
        "success": 7,
        "averageRequestDuration": 0.14565645643435547,
        "queueSize": 0
    }
]
```

# Configuration

There is a `application.yml` included in the jar file. Its content can be modified and saved as a
separate `application.yml` on the level of the jar file. Configuration can also be applied via the other supported ways
of Micronaut (see <https://docs.micronaut.io/latest/guide/index.html#config>). For Docker, the configuration via
environment variables is the most interesting one (see `docker-compose.yml`).

# Development

## Batch POST addresses

Might be useful for testing or for initial population

```bash
cat teststrassen.txt | while read line ; do curl --location --request POST 'http://localhost:8080/geocodes/' --header 'Content-Type: application/json' --data-raw "{ \"address\": \"$line\" }"; done
```

parallel version:

```bash
cat teststrassen.txt | while read line ; do curl --location --request POST 'http://localhost:8080/geocodes/' --header 'Content-Type: application/json' --data-raw "{ \"address\": \"$line\" }" & echo test; done
```