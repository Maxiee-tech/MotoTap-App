package ke.co.mototap.features.driver

import androidx.annotation.DrawableRes
import ke.co.mototap.R

@DrawableRes
fun catalogServicePhotoRes(categoryId: String): Int = when (categoryId) {
    "road-assistance" -> R.drawable.service_road_assistance
    "towing-services" -> R.drawable.service_towing_services
    "mobile-mechanic" -> R.drawable.service_mobile_mechanic
    "emergency-tire-services" -> R.drawable.service_emergency_tire_services
    "emergency-battery-services" -> R.drawable.service_emergency_battery_services
    "garage-services" -> R.drawable.service_garage_services
    "preventive-routine-maintenance" -> R.drawable.service_preventive_routine_maintenance
    "vehicle-diagnostics" -> R.drawable.service_vehicle_diagnostics
    "auto-electrical-services" -> R.drawable.service_auto_electrical_services
    "ac-services" -> R.drawable.service_ac_services
    "tire-wheel-services" -> R.drawable.service_tire_wheel_services
    "car-wash-services" -> R.drawable.service_car_wash_services
    "car-body-cosmetic-services" -> R.drawable.service_car_body_cosmetic_services
    "car-customization-upgrades" -> R.drawable.service_car_customization_upgrades
    "vehicle-electronics-security" -> R.drawable.service_auto_electrical_services
    else -> 0
}

@DrawableRes
fun catalogPartsPhotoRes(categoryId: String): Int = when (categoryId) {
    "engine-drivetrain" -> R.drawable.parts_engine_drivetrain
    "brakes" -> R.drawable.parts_brakes_suspension
    "electrical" -> R.drawable.parts_electrical_electronics
    "filters-fluids" -> R.drawable.parts_filters_fluids
    "body-exterior" -> R.drawable.parts_body_exterior
    "tyres-wheels" -> R.drawable.parts_tyres_wheels
    "vehicle-electronics-security" -> R.drawable.parts_electrical_electronics
    else -> 0
}
