package de.debuglevel.addressgeocoding.geocoding.photon.client

import io.micronaut.http.annotation.Get
import io.micronaut.http.client.annotation.Client
import javax.validation.constraints.NotBlank

@Client("photon", path = "/api/")
interface PhotonClient {
    @Get("/?q={address}")
    fun geocode(@NotBlank address: String): ResultSet
}