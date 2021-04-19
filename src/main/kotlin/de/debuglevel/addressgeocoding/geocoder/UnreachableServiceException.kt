package de.debuglevel.addressgeocoding.geocoder

class UnreachableServiceException(exception: Exception) :
    Exception("The geocoding service was not reachable.", exception)
