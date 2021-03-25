package de.debuglevel.addressgeocoding.geocoding

class UnreachableServiceException(exception: Exception) :
    Exception("The geocoding service was not reachable.", exception)
