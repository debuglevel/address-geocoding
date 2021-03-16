package de.debuglevel.addressgeocoding.person

data class AddPersonRequest(
    val name: String,
) {
    constructor(person: Person) : this(
        name = person.name
    )

    fun toPerson(): Person {
        return Person(
            id = null,
            name = this.name,
        )
    }
}