package com.example.mototap.features.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mototap.R
import com.example.mototap.ui.theme.MotoRed

data class ServiceGroup(val header: String, val services: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubServiceSelectionScreen(
    categoryName: String,
    viewModel: DriverHomeViewModel,
    onSubServiceSelected: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val groupedServices = getGroupedServicesForCategory(categoryName)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = categoryName.uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MotoRed
                )
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            groupedServices.forEach { group ->
                if (group.header.isNotEmpty()) {
                    item {
                        Text(
                            text = group.header,
                            color = MotoRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                }
                items(group.services) { serviceName ->
                    SubServiceItem(
                        name = serviceName,
                        onItemClick = { 
                            viewModel.onIssueChanged(serviceName)
                            onSubServiceSelected(serviceName)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SubServiceItem(name: String, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Entire card is now clickable
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = name,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MotoRed
                )
            }
        }
    }
}

@Composable
fun getGroupedServicesForCategory(category: String): List<ServiceGroup> {
    return when (category) {
        stringResource(R.string.road_assistance) -> listOf(
            ServiceGroup("", listOf(
                stringResource(R.string.jumpstart),
                stringResource(R.string.fuel_delivery),
                stringResource(R.string.lockout_assistance)
            ))
        )
        stringResource(R.string.towing_services) -> listOf(
            ServiceGroup("CORE TOWING TYPES", listOf(
                stringResource(R.string.flatbed_towing),
                stringResource(R.string.wheel_lift_towing),
                stringResource(R.string.dolly_towing)
            )),
            ServiceGroup("SITUATION-BASED TOWING", listOf(
                stringResource(R.string.accident_towing),
                stringResource(R.string.breakdown_towing),
                stringResource(R.string.long_distance_towing),
                stringResource(R.string.off_road_recovery)
            )),
            ServiceGroup("SPECIALIZED TOWING", listOf(
                stringResource(R.string.motorcycle_towing),
                stringResource(R.string.heavy_vehicle_towing),
                stringResource(R.string.low_clearance_towing)
            ))
        )
        stringResource(R.string.mobile_mechanic) -> listOf(
            ServiceGroup("DIAGNOSTICS & INSPECTION", listOf(
                stringResource(R.string.onsite_diagnostics),
                stringResource(R.string.battery_electrical_check),
                stringResource(R.string.engine_fault_id)
            )),
            ServiceGroup("QUICK REPAIRS", listOf(
                stringResource(R.string.battery_replacement),
                stringResource(R.string.spark_plug_replacement),
                stringResource(R.string.belt_replacement),
                stringResource(R.string.hose_leak_fixes)
            )),
            ServiceGroup("EMERGENCY FIXES", listOf(
                stringResource(R.string.overheating_assistance),
                stringResource(R.string.brake_fix_temporary),
                stringResource(R.string.engine_wont_start)
            )),
            ServiceGroup("FLUID SERVICES (MOBILE)", listOf(
                stringResource(R.string.oil_topup_change),
                stringResource(R.string.coolant_refill),
                stringResource(R.string.brake_fluid_topup)
            )),
            ServiceGroup("TIRE SERVICES (MOBILE)", listOf(
                stringResource(R.string.puncture_repair),
                stringResource(R.string.tire_change)
            ))
        )
        stringResource(R.string.garage_services) -> listOf(
            ServiceGroup("ENGINE & MECHANICAL REPAIRS", listOf(
                stringResource(R.string.engine_overhaul),
                stringResource(R.string.timing_belt_replacement),
                stringResource(R.string.fuel_system_repair),
                stringResource(R.string.exhaust_system_repair)
            )),
            ServiceGroup("TRANSMISSION SERVICES", listOf(
                stringResource(R.string.gearbox_repair),
                stringResource(R.string.clutch_replacement),
                stringResource(R.string.transmission_fluid_service)
            )),
            ServiceGroup("BRAKE SYSTEM SERVICES", listOf(
                stringResource(R.string.brake_pad_replacement),
                stringResource(R.string.disc_skimming),
                stringResource(R.string.full_brake_system_repair)
            )),
            ServiceGroup("SUSPENSION & STEERING", listOf(
                stringResource(R.string.shock_absorber_replacement),
                stringResource(R.string.steering_rack_repair),
                stringResource(R.string.wheel_alignment)
            )),
            ServiceGroup("AUTO ELECTRICAL (GARAGE-LEVEL)", listOf(
                stringResource(R.string.wiring_overhaul),
                stringResource(R.string.ecu_repair),
                stringResource(R.string.alternator_starter_repair)
            )),
            ServiceGroup("AC & COOLING SYSTEMS", listOf(
                stringResource(R.string.ac_repair_servicing),
                stringResource(R.string.radiator_repair),
                stringResource(R.string.cooling_system_flush)
            ))
        )
        stringResource(R.string.preventive_maintenance) -> listOf(
            ServiceGroup("", listOf(
                stringResource(R.string.oil_topup_change),
                "General Request",
                stringResource(R.string.coolant_refill),
                stringResource(R.string.brake_fluid_topup)
            ))
        )
        stringResource(R.string.vehicle_diagnostics) -> listOf(
            ServiceGroup("", listOf(
                stringResource(R.string.onsite_diagnostics),
                "General Request",
                stringResource(R.string.battery_electrical_check),
                stringResource(R.string.engine_fault_id)
            ))
        )
        stringResource(R.string.car_wash) -> listOf(
            ServiceGroup("BASIC CLEANING", listOf(
                stringResource(R.string.exterior_wash),
                stringResource(R.string.interior_vacuum),
                stringResource(R.string.tire_cleaning)
            )),
            ServiceGroup("STANDARD WASH PACKAGES", listOf(
                stringResource(R.string.exterior_interior_cleaning),
                stringResource(R.string.dashboard_polish),
                stringResource(R.string.window_cleaning)
            )),
            ServiceGroup("PREMIUM DETAILING", listOf(
                stringResource(R.string.full_car_detailing),
                stringResource(R.string.engine_cleaning),
                stringResource(R.string.underbody_wash)
            )),
            ServiceGroup("SPECIALIZED CLEANING", listOf(
                stringResource(R.string.seat_shampoo),
                stringResource(R.string.leather_conditioning),
                stringResource(R.string.odor_removal)
            )),
            ServiceGroup("ADD-ON SERVICES", listOf(
                stringResource(R.string.waxing_polishing),
                stringResource(R.string.ceramic_coating),
                stringResource(R.string.headlight_restoration)
            ))
        )
        else -> listOf(ServiceGroup("", listOf("General Request")))
    }
}
