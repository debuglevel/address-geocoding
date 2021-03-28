package de.debuglevel.addressgeocoding.geocoding.photon

import io.micronaut.http.client.HttpClient
import io.micronaut.http.uri.UriBuilder
import java.net.URL

class PhotonClientImpl(url: String) : PhotonClient {
    val httpClient: HttpClient = HttpClient.create(URL(url))

    override fun geocode(address: String): ResultSet {
        val uri = UriBuilder.of("/api/")
            .queryParam("q", address)
            .toString()
        val resultSet = httpClient.toBlocking().retrieve(uri, ResultSet::class.java)
        return resultSet
    }
}