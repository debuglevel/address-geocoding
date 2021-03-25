package de.debuglevel.addressgeocoding.geocode

enum class Status {
    Pending,
    AddressNotFound,
    Succeeded,
    FailedDueToQuotaExceeded,
    FailedDueToUnreachableService,
    FailedDueToUnexpectedError,
}
