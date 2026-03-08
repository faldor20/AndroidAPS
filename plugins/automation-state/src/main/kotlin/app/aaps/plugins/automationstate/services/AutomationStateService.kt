package app.aaps.plugins.automationstate.services

import app.aaps.core.interfaces.automation.AutomationStateInterface
import app.aaps.core.interfaces.sharedPreferences.SP
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutomationStateService  @Inject constructor(
    private val sp: SP
) : AutomationStateInterface {

    // This service is used from UI and automation execution paths, so map access must be synchronized.
    private val lock = Any()
    private var automationStates: MutableMap<String, String> = HashMap()
    private var stateValues: MutableMap<String, List<String>> = HashMap()
    private val spKey = "automation_state_service"
    private val stateValuesKey = "automation_state_values"

    init {
        // Load persisted current values and normalize aggressively to recover from malformed legacy entries.
        val string = sp.getString(spKey, "{}")
        try {
            val decoded: Map<String, String> = Json.decodeFromString(string)
            automationStates = decoded
                .mapNotNull { (name, value) ->
                    normalizeNameOrNull(name)?.let { normalizedName ->
                        normalizeValueOrNull(value)?.let { normalizedValue ->
                            normalizedName to normalizedValue
                        }
                    }
                }
                .toMap(HashMap())
        } catch (e: Exception) {
            automationStates = HashMap()
        }

        val valuesString = sp.getString(stateValuesKey, "{}")
        try {
            val decoded: Map<String, List<String>> = Json.decodeFromString(valuesString)
            stateValues = decoded
                .mapNotNull { (name, values) ->
                    normalizeNameOrNull(name)?.let { normalizedName ->
                        val normalizedValues = normalizeValues(values)
                        if (normalizedValues.isNotEmpty()) {
                            normalizedName to normalizedValues
                        } else {
                            null
                        }
                    }
                }
                .toMap(HashMap())
        } catch (e: Exception) {
            stateValues = HashMap()
        }

        synchronized(lock) {
            // Keep only active values that are still valid according to the current state definitions.
            automationStates = automationStates
                .filter { (name, value) -> stateValues[name]?.contains(value) == true }
                .toMap(HashMap())
            persistLocked()
        }
    }

    /**
     * Returns true when [stateName] currently points to [state].
     * Invalid/blank input is treated as non-matching instead of throwing.
     */
    override fun inState(stateName: String, state: String): Boolean {
        val trimmedName = normalizeNameOrNull(stateName) ?: return false
        val trimmedState = normalizeValueOrNull(state) ?: return false
        synchronized(lock) {
            return automationStates[trimmedName] == trimmedState
        }
    }

    /**
     * Sets the active value for a defined state.
     * Fails when state/value are blank, state does not exist, or value is not allowed.
     */
    override fun setState(stateName: String, state: String) {
        val trimmedName = validateName(stateName)
        val trimmedState = validateValue(state)

        synchronized(lock) {
            val allowedValues = stateValues[trimmedName] ?: throw IllegalStateException("Invalid state name: $trimmedName")
            require(allowedValues.contains(trimmedState)) { "Invalid state value: $trimmedState" }

            automationStates[trimmedName] = trimmedState
            persistLocked()
        }
    }

    /**
     * Returns the active value for [stateName], or null when unset/unknown.
     */
    override fun getStateOrNull(stateName: String): String? {
        val trimmedName = normalizeNameOrNull(stateName) ?: return null
        synchronized(lock) {
            return automationStates[trimmedName]
        }
    }

    /**
     * Backward-compatible accessor that returns empty string when no active value exists.
     */
    override fun getState(stateName: String): String {
        return getStateOrNull(stateName) ?: ""
    }

    /**
     * Returns all state names that have value definitions, sorted for stable UI ordering.
     */
    override fun getDefinedStates(): List<String> {
        synchronized(lock) {
            return stateValues.keys.sorted()
        }
    }

    /**
     * Returns active state/value pairs only (states without active value are omitted).
     */
    override fun getAllStates(): List<Pair<String, String>> {
        synchronized(lock) {
            return automationStates.toList()
        }
    }

    /**
     * Clears only active values while keeping state definitions.
     * Primarily used by tests and reset-style maintenance flows.
     */
    fun clearStates() {
        synchronized(lock) {
            automationStates.clear()
            persistLocked()
        }
    }

    /**
     * Returns allowed values for [stateName], or empty list when unknown.
     */
    override fun getStateValues(stateName: String): List<String> {
        val trimmedName = normalizeNameOrNull(stateName) ?: return emptyList()
        synchronized(lock) {
            return stateValues[trimmedName].orEmpty()
        }
    }

    /**
     * Replaces allowed values for [stateName] after trimming and de-duplicating input.
     * If the current active value becomes invalid, it is removed.
     */
    override fun setStateValues(stateName: String, values: List<String>) {
        val trimmedName = validateName(stateName)
        val trimmedValues = normalizeValues(values)
        require(trimmedValues.isNotEmpty()) { "State values must contain at least one non-empty value" }

        synchronized(lock) {
            val currentState = automationStates[trimmedName]
            if (currentState != null && !trimmedValues.contains(currentState)) {
                automationStates.remove(trimmedName)
            }

            stateValues[trimmedName] = trimmedValues
            persistLocked()
        }
    }

    /**
     * Returns true when [stateName] has at least one configured allowed value.
     */
    override fun hasStateValues(stateName: String): Boolean {
        val trimmedName = normalizeNameOrNull(stateName) ?: return false
        synchronized(lock) {
            return stateValues.containsKey(trimmedName)
        }
    }

    /**
     * Deletes a state definition and its active value, if present.
     */
    override fun deleteState(stateName: String) {
        val trimmedName = validateName(stateName)
        synchronized(lock) {
            automationStates.remove(trimmedName)
            stateValues.remove(trimmedName)
            persistLocked()
        }
    }

    private fun persistLocked() {
        // Persist both maps in one editor transaction to avoid temporary split-brain state on disk.
        sp.edit {
            putString(spKey, Json.encodeToString(automationStates))
            putString(stateValuesKey, Json.encodeToString(stateValues))
        }
    }

    private fun validateName(stateName: String): String =
        normalizeNameOrNull(stateName) ?: throw IllegalArgumentException("State name must not be blank")

    private fun validateValue(state: String): String =
        normalizeValueOrNull(state) ?: throw IllegalArgumentException("State value must not be blank")

    private fun normalizeNameOrNull(stateName: String): String? {
        val trimmed = stateName.trim()
        return if (trimmed.isEmpty()) null else trimmed
    }

    private fun normalizeValueOrNull(state: String): String? {
        val trimmed = state.trim()
        return if (trimmed.isEmpty()) null else trimmed
    }

    private fun normalizeValues(values: List<String>): List<String> {
        // Preserve insertion order while removing blanks/duplicates.
        val deduped = LinkedHashSet<String>()
        values.forEach { value ->
            normalizeValueOrNull(value)?.let { deduped.add(it) }
        }
        return deduped.toList()
    }
}
