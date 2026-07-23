package com.example.mototap.core.data

/**
 * Moto Tap service catalog — must match web `js/serviceCatalogData.js`
 * (SERVICE_CATALOG_VERSION 3) exactly for mechanic skills filtering and
 * jobs.issueType / jobs.serviceName across the shared Firebase backend.
 */

const val SERVICE_CATALOG_VERSION = 3

const val TOWING_CATEGORY_ID = "towing-services"

val SERVICE_DISPLAY_GROUP_ORDER = listOf(
    "Emergency Services",
    "Maintenance Services",
    "Upgrades & Value-Added",
)

data class ServiceCatalogGroup(
    val title: String,
    val items: List<String>,
)

data class ServiceCatalogCategory(
    val id: String,
    val name: String,
    val displayGroup: String,
    val groups: List<ServiceCatalogGroup>,
) {
    val allItems: List<String>
        get() = groups.flatMap { it.items }
}

val SERVICE_CATEGORIES: List<ServiceCatalogCategory> = listOf(
    // —— Category 1: Emergency Services ——
    ServiceCatalogCategory(
        id = "road-assistance",
        name = "Road Assistance",
        displayGroup = "Emergency Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Road Assistance",
                items = listOf("Jumpstart", "Fuel delivery", "Lockout assistance"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "towing-services",
        name = "Towing Services",
        displayGroup = "Emergency Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Core Types",
                items = listOf("Flatbed towing", "Wheel-lift towing", "Dolly towing"),
            ),
            ServiceCatalogGroup(
                title = "Situation-Based",
                items = listOf(
                    "Accident towing",
                    "Breakdown towing",
                    "Long-distance towing",
                    "Off-road recovery",
                ),
            ),
            ServiceCatalogGroup(
                title = "Specialized",
                items = listOf(
                    "Motorcycle towing",
                    "Heavy vehicle towing",
                    "Low-clearance vehicle towing",
                ),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "mobile-mechanic",
        name = "Mobile Mechanic",
        displayGroup = "Emergency Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Diagnostics",
                items = listOf(
                    "On-site vehicle diagnostics",
                    "Battery & electrical check",
                    "Engine fault identification",
                ),
            ),
            ServiceCatalogGroup(
                title = "Quick Repairs",
                items = listOf(
                    "Battery replacement",
                    "Spark plug replacement",
                    "Belt replacement",
                    "Hose leak fixes",
                ),
            ),
            ServiceCatalogGroup(
                title = "Emergency Fixes",
                items = listOf(
                    "Overheating assistance",
                    "Brake issue temporary fix",
                    "Engine won't start troubleshooting",
                ),
            ),
            ServiceCatalogGroup(
                title = "Mobile Fluids",
                items = listOf(
                    "Engine oil top-up/change",
                    "Coolant refill",
                    "Brake fluid top-up",
                ),
            ),
            ServiceCatalogGroup(
                title = "Mobile Tires",
                items = listOf("Puncture repair", "Tire change"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "emergency-tire-services",
        name = "Emergency Tire Services",
        displayGroup = "Emergency Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Emergency Tire Services",
                items = listOf("General Request", "Puncture Repair"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "emergency-battery-services",
        name = "Emergency Battery Services",
        displayGroup = "Emergency Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Emergency Battery Services",
                items = listOf("General Request", "Battery Jumpstart"),
            ),
        ),
    ),

    // —— Category 2: Maintenance Services ——
    ServiceCatalogCategory(
        id = "garage-services",
        name = "Garage Services",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Engine & Mechanical",
                items = listOf(
                    "Engine overhaul",
                    "Timing belt replacement",
                    "Fuel system repair",
                    "Exhaust system repair",
                ),
            ),
            ServiceCatalogGroup(
                title = "Transmission",
                items = listOf(
                    "Gearbox repair",
                    "Clutch replacement",
                    "Transmission fluid service",
                ),
            ),
            ServiceCatalogGroup(
                title = "Brake System",
                items = listOf(
                    "Brake pad replacement",
                    "Disc skimming",
                    "Full brake system repair",
                ),
            ),
            ServiceCatalogGroup(
                title = "Suspension & Steering",
                items = listOf(
                    "Shock absorber replacement",
                    "Steering rack repair",
                    "Wheel alignment",
                ),
            ),
            ServiceCatalogGroup(
                title = "Auto Electrical",
                items = listOf(
                    "Wiring overhaul",
                    "ECU repair",
                    "Alternator & starter repair",
                ),
            ),
            ServiceCatalogGroup(
                title = "AC & Cooling",
                items = listOf(
                    "AC repair & servicing",
                    "Radiator repair",
                    "Cooling system flush",
                ),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "preventive-routine-maintenance",
        name = "Preventive & Routine Maintenance",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Preventive & Routine Maintenance",
                items = listOf(
                    "Engine oil top-up/change",
                    "General Request",
                    "Coolant refill",
                    "Brake fluid top-up",
                ),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "vehicle-diagnostics",
        name = "Vehicle Diagnostics",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Vehicle Diagnostics",
                items = listOf(
                    "On-site vehicle diagnostics",
                    "General Request",
                    "Battery & electrical check",
                    "Engine fault identification",
                ),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "auto-electrical-services",
        name = "Auto Electrical Services",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Auto Electrical Services",
                items = listOf("Wiring repair", "ECU scanning", "Battery health check"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "ac-services",
        name = "AC Services",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "AC Services",
                items = listOf("Refrigerant refill", "Leak detection", "Compressor repair"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "tire-wheel-services",
        name = "Tire & Wheel Services",
        displayGroup = "Maintenance Services",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Tire & Wheel Services",
                items = listOf("Alignment", "Balancing", "New tire fitting"),
            ),
        ),
    ),

    // —— Category 3: Upgrades & Value-Added ——
    ServiceCatalogCategory(
        id = "car-wash-services",
        name = "Car Wash Services",
        displayGroup = "Upgrades & Value-Added",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Basic",
                items = listOf("Exterior wash", "Interior vacuum", "Tire cleaning"),
            ),
            ServiceCatalogGroup(
                title = "Standard",
                items = listOf(
                    "Exterior + interior cleaning",
                    "Dashboard polish",
                    "Window cleaning",
                ),
            ),
            ServiceCatalogGroup(
                title = "Premium",
                items = listOf(
                    "Full car detailing",
                    "Engine cleaning",
                    "Underbody wash",
                ),
            ),
            ServiceCatalogGroup(
                title = "Specialized",
                items = listOf(
                    "Seat shampoo (fabric)",
                    "Leather conditioning",
                    "Odor removal",
                ),
            ),
            ServiceCatalogGroup(
                title = "Add-ons",
                items = listOf(
                    "Waxing & polishing",
                    "Ceramic coating",
                    "Headlight restoration",
                ),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "car-body-cosmetic-services",
        name = "Car Body & Cosmetic Services",
        displayGroup = "Upgrades & Value-Added",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Car Body & Cosmetic Services",
                items = listOf("Dent removal", "Respraying", "Buffing"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "car-customization-upgrades",
        name = "Car Customization & Upgrades",
        displayGroup = "Upgrades & Value-Added",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Car Customization & Upgrades",
                items = listOf("Audio systems", "Tinting", "Performance tuning"),
            ),
        ),
    ),
    ServiceCatalogCategory(
        id = "vehicle-electronics-security",
        name = "Vehicle Electronics & Security",
        displayGroup = "Upgrades & Value-Added",
        groups = listOf(
            ServiceCatalogGroup(
                title = "Key Programming",
                items = listOf(
                    "Car key programming",
                    "Transponder key coding",
                    "Smart key / key fob programming",
                    "Spare key duplication",
                    "Remote key repair",
                ),
            ),
            ServiceCatalogGroup(
                title = "Dashboard & Display",
                items = listOf(
                    "Instrument cluster repair",
                    "Dashboard display repair",
                    "Digital dash programming",
                    "Touchscreen / infotainment display repair",
                    "EV display diagnostics",
                ),
            ),
            ServiceCatalogGroup(
                title = "Trackers & Security",
                items = listOf(
                    "GPS tracker fitting",
                    "Fleet tracker installation",
                    "Vehicle immobilizer installation",
                    "Car alarm fitting",
                    "Anti-theft system setup",
                ),
            ),
        ),
    ),
)

fun serviceCategoryByName(name: String): ServiceCatalogCategory? {
    val key = name.trim()
    if (key.isEmpty()) return null
    return SERVICE_CATEGORIES.firstOrNull { it.name.equals(key, ignoreCase = true) }
}

fun serviceCategoriesForDisplayGroup(displayGroup: String): List<ServiceCatalogCategory> =
    SERVICE_CATEGORIES.filter { it.displayGroup == displayGroup }

fun getTowingServiceNames(): List<String> =
    SERVICE_CATEGORIES
        .firstOrNull { it.id == TOWING_CATEGORY_ID }
        ?.allItems
        .orEmpty()

private val towingNameLookup: Set<String> =
    getTowingServiceNames().map { it.trim().lowercase() }.toSet()

/** True when a service is billed per kilometre (towing). */
fun isCatalogTowingService(serviceName: String?): Boolean {
    val key = serviceName?.trim()?.lowercase().orEmpty()
    if (key.isEmpty()) return false
    if (key in towingNameLookup) return true
    return key.contains("towing") || key == "off-road recovery"
}
