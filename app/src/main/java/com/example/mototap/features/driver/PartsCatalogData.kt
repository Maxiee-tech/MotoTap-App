package com.example.mototap.features.driver

/**
 * Spare parts catalog for driver discovery, mirrored from the MotoTap web app
 * (js/partsCatalogData.js, version 2). The part name strings MUST match what
 * parts dealers store in Firestore so client-side filtering works.
 */

data class PartsGroup(val title: String, val items: List<String>)

data class PartsCategory(
    val id: String,
    val name: String,
    val groups: List<PartsGroup>,
)

val PARTS_CATEGORIES: List<PartsCategory> = listOf(
    PartsCategory(
        id = "engine-drivetrain",
        name = "Engine & Drivetrain",
        groups = listOf(
            PartsGroup("Engine", listOf("Spark plugs", "Timing belt", "Oil pump", "Gasket set", "Piston rings")),
            PartsGroup("Drivetrain", listOf("Clutch kit", "Drive shaft", "CV joint", "Gearbox mount")),
        ),
    ),
    PartsCategory(
        id = "brakes",
        name = "Brakes & Suspension",
        groups = listOf(
            PartsGroup("Brakes", listOf("Brake pads", "Brake discs", "Brake fluid", "Brake caliper")),
            PartsGroup("Suspension", listOf("Shock absorber", "Coil spring", "Control arm", "Bushings")),
        ),
    ),
    PartsCategory(
        id = "electrical",
        name = "Electrical & Electronics",
        groups = listOf(
            PartsGroup("Electrical", listOf("Car battery", "Alternator", "Starter motor", "Fuses & relays")),
            PartsGroup("Lighting", listOf("Headlight bulb", "Tail light", "Indicator bulb", "Fog lamp")),
        ),
    ),
    PartsCategory(
        id = "vehicle-electronics-security",
        name = "Vehicle Electronics & Security",
        groups = listOf(
            PartsGroup(
                "Key Programming",
                listOf("Transponder chip", "Key blank", "Key fob shell", "Key fob battery", "Remote key pad"),
            ),
            PartsGroup(
                "Dashboard & Display",
                listOf(
                    "Instrument cluster unit",
                    "Dashboard LCD screen",
                    "Touchscreen module",
                    "Display cable harness",
                    "Cluster bulb",
                ),
            ),
            PartsGroup(
                "Trackers & Security",
                listOf(
                    "GPS tracker unit",
                    "Tracker SIM card",
                    "Immobilizer unit",
                    "Car alarm siren",
                    "Door sensor",
                    "Shock sensor",
                ),
            ),
        ),
    ),
    PartsCategory(
        id = "filters-fluids",
        name = "Filters & Fluids",
        groups = listOf(
            PartsGroup("Filters", listOf("Oil filter", "Air filter", "Fuel filter", "Cabin filter")),
            PartsGroup("Fluids", listOf("Engine oil", "Coolant", "Transmission fluid", "Power steering fluid")),
        ),
    ),
    PartsCategory(
        id = "body-exterior",
        name = "Body & Exterior",
        groups = listOf(
            PartsGroup("Body", listOf("Side mirror", "Bumper", "Door handle", "Windscreen")),
            PartsGroup("Exterior", listOf("Wiper blades", "Number plate holder", "Mud flaps")),
        ),
    ),
    PartsCategory(
        id = "tyres-wheels",
        name = "Tyres & Wheels",
        groups = listOf(
            PartsGroup("Tyres", listOf("Tyre (new)", "Tyre (used)", "Tube", "Valve stem")),
            PartsGroup("Wheels", listOf("Alloy rim", "Steel rim", "Wheel bearing", "Wheel nuts")),
        ),
    ),
)

fun getPartsGroupsForCategory(categoryName: String): List<PartsGroup> {
    return PARTS_CATEGORIES.firstOrNull { it.name == categoryName }?.groups ?: emptyList()
}
