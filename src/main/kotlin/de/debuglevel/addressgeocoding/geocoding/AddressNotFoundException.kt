package de.debuglevel.addressgeocoding.geocoding

data class AddressNotFoundException(val address: String) :
    Exception("No results found for address '$address'; could not geocode.")
