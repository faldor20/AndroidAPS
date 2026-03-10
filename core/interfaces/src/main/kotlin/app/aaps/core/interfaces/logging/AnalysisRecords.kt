package app.aaps.core.interfaces.logging

import app.aaps.core.data.model.BS
import app.aaps.core.interfaces.aps.APSResult
import app.aaps.core.interfaces.aps.AutosensResult
import app.aaps.core.interfaces.aps.CurrentTemp
import app.aaps.core.interfaces.aps.GlucoseStatusAutoIsf
import app.aaps.core.interfaces.aps.GlucoseStatusSMB
import app.aaps.core.interfaces.aps.IobTotal
import app.aaps.core.interfaces.aps.MealData
import app.aaps.core.interfaces.aps.OapsProfile
import app.aaps.core.interfaces.aps.OapsProfileAutoIsf
import app.aaps.core.interfaces.aps.RT
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

val analysisJson: Json = Json {
    encodeDefaults = true
    explicitNulls = false
}

@Serializable
data class AnalysisDetermineBasalRecord(
    val schemaVersion: Int = 1,
    val event: String = "aps.determine_basal",
    val algorithm: String,
    val timestamp: Long,
    val inputs: AnalysisDetermineBasalData,
    val result: JsonObject
)

@Serializable
data class AnalysisBolusRecord(
    val schemaVersion: Int = 1,
    val event: String = "treatment.bolus",
    val timestamp: Long,
    val bolus: AnalysisBolusData
)

@Serializable
data class AnalysisDetermineBasalData(
    val glucoseStatus: AnalysisGlucoseStatus,
    val iobData: List<AnalysisIobDataValue>,
    val currentTemp: AnalysisCurrentTemp,
    val profile: AnalysisProfile,
    val mealData: AnalysisMealData,
    val autosensData: AnalysisAutosensData,
    val reservoirData: Double? = null,
    val microBolusAllowed: Boolean,
    val smbAlwaysAllowed: Boolean = false,
    val currentTime: Long,
    val flatBGsDetected: Boolean
)

@Serializable
data class AnalysisGlucoseStatus(
    val glucose: Double,
    val noise: Double,
    val delta: Double,
    val short_avgdelta: Double,
    val long_avgdelta: Double,
    val date: Long,
    val dura_ISF_minutes: Double = 0.0,
    val dura_ISF_average: Double = 0.0,
    val useFSL1minuteRaw: Boolean = false,
    val parabola_fit_correlation: Double = 0.0,
    val parabola_fit_minutes: Double = 0.0,
    val parabola_fit_last_delta: Double = 0.0,
    val parabola_fit_next_delta: Double = 0.0,
    val parabola_fit_a0: Double = 0.0,
    val parabola_fit_a1: Double = 0.0,
    val parabola_fit_a2: Double = 0.0,
    val bg_acceleration: Double = 0.0
)

@Serializable
data class AnalysisCurrentTemp(
    val temp: String = "absolute",
    val duration: Int,
    val rate: Double,
    val minutesrunning: Int = 0
)

@Serializable
data class AnalysisMealData(
    val carbs: Double,
    val mealCOB: Double,
    val slopeFromMaxDeviation: Double,
    val slopeFromMinDeviation: Double,
    val lastBolusTime: Long,
    val lastCarbTime: Long,
    val bwFound: Boolean = false,
    val bwCarbs: Boolean = false
)

@Serializable
data class AnalysisAutosensData(
    val ratio: Double
)

@Serializable
data class AnalysisIobDataValue(
    val iob: Double,
    val basaliob: Double,
    val bolussnooze: Double,
    val activity: Double,
    val lastBolusTime: Long,
    val time: Long,
    val iobWithZeroTemp: AnalysisIobDataValue? = null
)

@Serializable
data class AnalysisProfile(
    val max_iob: Double,
    val type: String = "current",
    val max_daily_basal: Double,
    val max_basal: Double,
    val min_bg: Double,
    val max_bg: Double,
    val target_bg: Double,
    val carb_ratio: Double,
    val sens: Double,
    val max_daily_safety_multiplier: Double,
    val current_basal_safety_multiplier: Double,
    val high_temptarget_raises_sensitivity: Boolean,
    val low_temptarget_lowers_sensitivity: Boolean,
    val sensitivity_raises_target: Boolean,
    val resistance_lowers_target: Boolean,
    val adv_target_adjustments: Boolean,
    val exercise_mode: Boolean,
    val half_basal_exercise_target: Double,
    val full_basal_exercise_target: Double? = null,
    val maxCOB: Int,
    val skip_neutral_temps: Boolean,
    val remainingCarbsCap: Int,
    val remainingCarbsFraction: Double = 1.0,
    val enableUAM: Boolean,
    val A52_risk_enable: Boolean,
    val SMBInterval: Int,
    val enableSMB_with_COB: Boolean,
    val enableSMB_with_temptarget: Boolean,
    val allowSMB_with_high_temptarget: Boolean,
    val enableSMB_always: Boolean,
    val enableSMB_after_carbs: Boolean,
    val maxSMBBasalMinutes: Int,
    val maxUAMSMBBasalMinutes: Int,
    val bolus_increment: Double,
    val carbsReqThreshold: Int,
    val current_basal: Double,
    val temptargetSet: Boolean,
    val autosens_max: Double,
    val autoISF_version: String = "",
    val enable_autoISF: Boolean = false,
    val autoISF_max: Double = 1.0,
    val autoISF_min: Double = 1.0,
    val bgAccel_ISF_weight: Double = 0.0,
    val bgBrake_ISF_weight: Double = 0.0,
    val pp_ISF_weight: Double = 0.0,
    val lower_ISFrange_weight: Double = 0.0,
    val higher_ISFrange_weight: Double = 0.0,
    val dura_ISF_weight: Double = 0.0,
    val smb_delivery_ratio: Double = 0.5,
    val smb_delivery_ratio_min: Double = 0.5,
    val smb_delivery_ratio_max: Double = 0.5,
    val smb_delivery_ratio_bg_range: Double = 0.0,
    val smb_max_range_extension: Double = 1.0,
    val enableSMB_EvenOn_OddOff_always: Boolean = false,
    val iob_threshold_percent: Int = 100,
    val profile_percentage: Int = 100,
    val out_units: String,
    val recentSteps5Minutes: Int,
    val recentSteps10Minutes: Int,
    val recentSteps15Minutes: Int,
    val recentSteps30Minutes: Int,
    val recentSteps60Minutes: Int,
    val activity_detection: Boolean,
    val phone_moved: Boolean,
    val activity_scale_factor: Double = 1.0,
    val inactivity_scale_factor: Double = 1.0,
    val ignore_inactivity_overnight: Boolean = true,
    val inactivity_idle_start: Int = 22,
    val inactivity_idle_end: Int = 6,
    val time_since_start: Long,
    val noisyCGMTargetMultiplier: Double = 1.1,
    val maxRaw: Int = 250
)

@Serializable
data class AnalysisBolusData(
    val id: Long,
    val timestamp: Long,
    val amount: Double,
    val type: String,
    val isBasalInsulin: Boolean
)

fun AnalysisDetermineBasalRecord.toJson(): String = analysisJson.encodeToString(this)

fun AnalysisBolusRecord.toJson(): String = analysisJson.encodeToString(this)

fun buildAnalysisDetermineBasalRecord(
    algorithm: APSResult.Algorithm,
    timestamp: Long,
    glucoseStatus: app.aaps.core.interfaces.aps.GlucoseStatus,
    currentTemp: CurrentTemp,
    iobData: Array<IobTotal>,
    profile: Any,
    autosensData: AutosensResult,
    mealData: MealData,
    microBolusAllowed: Boolean,
    flatBGsDetected: Boolean,
    result: RT,
    activityScaleFactor: Double = 1.0,
    inactivityScaleFactor: Double = 1.0,
    ignoreInactivityOvernight: Boolean = true,
    inactivityIdleStart: Int = 22,
    inactivityIdleEnd: Int = 6,
    smbAlwaysAllowed: Boolean = false,
    reservoirData: Double? = null
): AnalysisDetermineBasalRecord = AnalysisDetermineBasalRecord(
    algorithm = algorithm.name,
    timestamp = timestamp,
    inputs = AnalysisDetermineBasalData(
        glucoseStatus = glucoseStatus.toAnalysis(),
        iobData = iobData.map { it.toAnalysis() },
        currentTemp = currentTemp.toAnalysis(),
        profile = when (profile) {
            is OapsProfile -> profile.toAnalysis(
                activityScaleFactor = activityScaleFactor,
                inactivityScaleFactor = inactivityScaleFactor,
                ignoreInactivityOvernight = ignoreInactivityOvernight,
                inactivityIdleStart = inactivityIdleStart,
                inactivityIdleEnd = inactivityIdleEnd
            )

            is OapsProfileAutoIsf -> profile.toAnalysis(
                activityScaleFactor = activityScaleFactor,
                inactivityScaleFactor = inactivityScaleFactor,
                ignoreInactivityOvernight = ignoreInactivityOvernight,
                inactivityIdleStart = inactivityIdleStart,
                inactivityIdleEnd = inactivityIdleEnd
            )

            else -> error("Unsupported analysis profile type: ${profile::class.java.name}")
        },
        mealData = mealData.toAnalysis(),
        autosensData = autosensData.toAnalysis(),
        reservoirData = reservoirData,
        microBolusAllowed = microBolusAllowed,
        smbAlwaysAllowed = smbAlwaysAllowed,
        currentTime = timestamp,
        flatBGsDetected = flatBGsDetected
    ),
    result = result.toAnalysisJsonObject()
)

private fun RT.toAnalysisJsonObject(): JsonObject {
    val parsed = analysisJson.parseToJsonElement(serialize())
    require(parsed is JsonObject) { "Expected RT serialization to produce a JSON object" }

    return buildJsonObject {
        parsed.forEach { (key, value) ->
            when (key) {
                "timestamp", "deliverAt" -> put(key, normalizeTimestamp(value))
                else -> put(key, value)
            }
        }
    }
}

private fun normalizeTimestamp(value: JsonElement): JsonElement {
    val primitive = value as? JsonPrimitive ?: return value
    if (!primitive.isString) return value
    return JsonPrimitive(RT.TimestampToIsoSerializer.fromISODateString(primitive.content))
}

fun BS.toAnalysisBolusRecord(): AnalysisBolusRecord = AnalysisBolusRecord(
    timestamp = timestamp,
    bolus = AnalysisBolusData(
        id = id,
        timestamp = timestamp,
        amount = amount,
        type = type.name,
        isBasalInsulin = isBasalInsulin
    )
)

private fun app.aaps.core.interfaces.aps.GlucoseStatus.toAnalysis(): AnalysisGlucoseStatus = when (this) {
    is GlucoseStatusAutoIsf -> AnalysisGlucoseStatus(
        glucose = glucose,
        noise = noise,
        delta = delta,
        short_avgdelta = shortAvgDelta,
        long_avgdelta = longAvgDelta,
        date = date,
        dura_ISF_minutes = duraISFminutes,
        dura_ISF_average = duraISFaverage,
        parabola_fit_correlation = corrSqu,
        parabola_fit_minutes = parabolaMinutes,
        parabola_fit_last_delta = deltaPl,
        parabola_fit_next_delta = deltaPn,
        parabola_fit_a0 = a0,
        parabola_fit_a1 = a1,
        parabola_fit_a2 = a2,
        bg_acceleration = bgAcceleration
    )

    is GlucoseStatusSMB -> AnalysisGlucoseStatus(
        glucose = glucose,
        noise = noise,
        delta = delta,
        short_avgdelta = shortAvgDelta,
        long_avgdelta = longAvgDelta,
        date = date
    )

    else -> AnalysisGlucoseStatus(
        glucose = glucose,
        noise = noise,
        delta = delta,
        short_avgdelta = shortAvgDelta,
        long_avgdelta = longAvgDelta,
        date = date
    )
}

private fun CurrentTemp.toAnalysis(): AnalysisCurrentTemp = AnalysisCurrentTemp(
    duration = duration,
    rate = rate,
    minutesrunning = minutesrunning ?: 0
)

private fun MealData.toAnalysis(): AnalysisMealData = AnalysisMealData(
    carbs = carbs,
    mealCOB = mealCOB,
    slopeFromMaxDeviation = slopeFromMaxDeviation,
    slopeFromMinDeviation = slopeFromMinDeviation,
    lastBolusTime = lastBolusTime,
    lastCarbTime = lastCarbTime
)

private fun AutosensResult.toAnalysis(): AnalysisAutosensData = AnalysisAutosensData(ratio = ratio)

private fun IobTotal.toAnalysis(): AnalysisIobDataValue = AnalysisIobDataValue(
    iob = iob,
    basaliob = basaliob,
    bolussnooze = bolussnooze,
    activity = activity,
    lastBolusTime = lastBolusTime,
    time = time,
    iobWithZeroTemp = iobWithZeroTemp?.toAnalysis()
)

private fun OapsProfile.toAnalysis(
    activityScaleFactor: Double,
    inactivityScaleFactor: Double,
    ignoreInactivityOvernight: Boolean,
    inactivityIdleStart: Int,
    inactivityIdleEnd: Int
): AnalysisProfile = AnalysisProfile(
    max_iob = max_iob,
    max_daily_basal = max_daily_basal,
    max_basal = max_basal,
    min_bg = min_bg,
    max_bg = max_bg,
    target_bg = target_bg,
    carb_ratio = carb_ratio,
    sens = sens,
    max_daily_safety_multiplier = max_daily_safety_multiplier,
    current_basal_safety_multiplier = current_basal_safety_multiplier,
    high_temptarget_raises_sensitivity = high_temptarget_raises_sensitivity,
    low_temptarget_lowers_sensitivity = low_temptarget_lowers_sensitivity,
    sensitivity_raises_target = sensitivity_raises_target,
    resistance_lowers_target = resistance_lowers_target,
    adv_target_adjustments = adv_target_adjustments,
    exercise_mode = exercise_mode,
    half_basal_exercise_target = half_basal_exercise_target,
    maxCOB = maxCOB,
    skip_neutral_temps = skip_neutral_temps,
    remainingCarbsCap = remainingCarbsCap,
    enableUAM = enableUAM,
    A52_risk_enable = A52_risk_enable,
    SMBInterval = SMBInterval,
    enableSMB_with_COB = enableSMB_with_COB,
    enableSMB_with_temptarget = enableSMB_with_temptarget,
    allowSMB_with_high_temptarget = allowSMB_with_high_temptarget,
    enableSMB_always = enableSMB_always,
    enableSMB_after_carbs = enableSMB_after_carbs,
    maxSMBBasalMinutes = maxSMBBasalMinutes,
    maxUAMSMBBasalMinutes = maxUAMSMBBasalMinutes,
    bolus_increment = bolus_increment,
    carbsReqThreshold = carbsReqThreshold,
    current_basal = current_basal,
    temptargetSet = temptargetSet,
    autosens_max = autosens_max,
    out_units = out_units,
    recentSteps5Minutes = recent_steps_5_minutes ?: 0,
    recentSteps10Minutes = recent_steps_10_minutes ?: 0,
    recentSteps15Minutes = recent_steps_15_minutes ?: 0,
    recentSteps30Minutes = recent_steps_30_minutes ?: 0,
    recentSteps60Minutes = recent_steps_60_minutes ?: 0,
    activity_detection = activity_detection ?: false,
    phone_moved = phone_moved ?: false,
    activity_scale_factor = activityScaleFactor,
    inactivity_scale_factor = inactivityScaleFactor,
    ignore_inactivity_overnight = ignoreInactivityOvernight,
    inactivity_idle_start = inactivityIdleStart,
    inactivity_idle_end = inactivityIdleEnd,
    time_since_start = time_since_start ?: 0,
    full_basal_exercise_target = null,
    remainingCarbsFraction = 1.0,
    noisyCGMTargetMultiplier = 1.1,
    maxRaw = 250
)

private fun OapsProfileAutoIsf.toAnalysis(
    activityScaleFactor: Double,
    inactivityScaleFactor: Double,
    ignoreInactivityOvernight: Boolean,
    inactivityIdleStart: Int,
    inactivityIdleEnd: Int
): AnalysisProfile = AnalysisProfile(
    max_iob = max_iob,
    max_daily_basal = max_daily_basal,
    max_basal = max_basal,
    min_bg = min_bg,
    max_bg = max_bg,
    target_bg = target_bg,
    carb_ratio = carb_ratio,
    sens = sens,
    max_daily_safety_multiplier = max_daily_safety_multiplier,
    current_basal_safety_multiplier = current_basal_safety_multiplier,
    high_temptarget_raises_sensitivity = high_temptarget_raises_sensitivity,
    low_temptarget_lowers_sensitivity = low_temptarget_lowers_sensitivity,
    sensitivity_raises_target = sensitivity_raises_target,
    resistance_lowers_target = resistance_lowers_target,
    adv_target_adjustments = adv_target_adjustments,
    exercise_mode = exercise_mode,
    half_basal_exercise_target = half_basal_exercise_target,
    maxCOB = maxCOB,
    skip_neutral_temps = skip_neutral_temps,
    remainingCarbsCap = remainingCarbsCap,
    enableUAM = enableUAM,
    A52_risk_enable = A52_risk_enable,
    SMBInterval = SMBInterval,
    enableSMB_with_COB = enableSMB_with_COB,
    enableSMB_with_temptarget = enableSMB_with_temptarget,
    allowSMB_with_high_temptarget = allowSMB_with_high_temptarget,
    enableSMB_always = enableSMB_always,
    enableSMB_after_carbs = enableSMB_after_carbs,
    maxSMBBasalMinutes = maxSMBBasalMinutes,
    maxUAMSMBBasalMinutes = maxUAMSMBBasalMinutes,
    bolus_increment = bolus_increment,
    carbsReqThreshold = carbsReqThreshold,
    current_basal = current_basal,
    temptargetSet = temptargetSet,
    autosens_max = autosens_max,
    autoISF_version = autoISF_version,
    enable_autoISF = enable_autoISF,
    autoISF_max = autoISF_max,
    autoISF_min = autoISF_min,
    bgAccel_ISF_weight = bgAccel_ISF_weight,
    bgBrake_ISF_weight = bgBrake_ISF_weight,
    pp_ISF_weight = pp_ISF_weight,
    lower_ISFrange_weight = lower_ISFrange_weight,
    higher_ISFrange_weight = higher_ISFrange_weight,
    dura_ISF_weight = dura_ISF_weight,
    smb_delivery_ratio = smb_delivery_ratio,
    smb_delivery_ratio_min = smb_delivery_ratio_min,
    smb_delivery_ratio_max = smb_delivery_ratio_max,
    smb_delivery_ratio_bg_range = smb_delivery_ratio_bg_range,
    smb_max_range_extension = smb_max_range_extension,
    enableSMB_EvenOn_OddOff_always = enableSMB_EvenOn_OddOff_always,
    iob_threshold_percent = iob_threshold_percent,
    profile_percentage = profile_percentage,
    out_units = out_units,
    recentSteps5Minutes = recent_steps_5_minutes ?: 0,
    recentSteps10Minutes = recent_steps_10_minutes ?: 0,
    recentSteps15Minutes = recent_steps_15_minutes ?: 0,
    recentSteps30Minutes = recent_steps_30_minutes ?: 0,
    recentSteps60Minutes = recent_steps_60_minutes ?: 0,
    activity_detection = activity_detection ?: false,
    phone_moved = phone_moved ?: false,
    activity_scale_factor = activityScaleFactor,
    inactivity_scale_factor = inactivityScaleFactor,
    ignore_inactivity_overnight = ignoreInactivityOvernight,
    inactivity_idle_start = inactivityIdleStart,
    inactivity_idle_end = inactivityIdleEnd,
    time_since_start = time_since_start ?: 0,
    full_basal_exercise_target = null,
    remainingCarbsFraction = 1.0,
    noisyCGMTargetMultiplier = 1.1,
    maxRaw = 250
)
