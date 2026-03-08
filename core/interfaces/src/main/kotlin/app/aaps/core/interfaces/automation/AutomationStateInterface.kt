package app.aaps.core.interfaces.automation

/**
 * Interface for the AutomationStateService to allow independent compilation
 * of automation and automation-state plugins
 */
interface AutomationStateInterface {
    /**
     * Get the current value of a state, or null if not set.
     * @param stateName The name of the state
     */
    fun getStateOrNull(stateName: String): String?

    /**
     * Get all defined state names, including states without current value.
     */
    fun getDefinedStates(): List<String>

    /**
     * Check if a state has a specific value
     * @param stateName The name of the state to check
     * @param state The value to check for
     * @return true if the state has the specified value, false otherwise
     */
    fun inState(stateName: String, state: String): Boolean

    /**
     * Set a state to a specific value
     * @param stateName The name of the state to set
     * @param state The value to set
     * @throws IllegalArgumentException if state name or value are blank
     * @throws IllegalStateException if the state doesn't exist
     * @throws IllegalStateException if the value is not valid for the state
     */
    fun setState(stateName: String, state: String)

    /**
     * Get current value of a specific state.
     * Returns an empty string when no current value exists.
     * @param stateName The name of the state
     */
    fun getState(stateName: String): String

    /**
     * Get all states and their current values
     * @return List of pairs containing state names and their values
     */
    fun getAllStates(): List<Pair<String, String>>
    
    /**
     * Get the possible values for a state
     * @param stateName The name of the state
     * @return List of possibe values for the state
     */
    fun getStateValues(stateName: String): List<String>
    
    /**
     * Set the possible values for a state
     * @param stateName The name of the state
     * @param values List of possible values for the state
     */
    fun setStateValues(stateName: String, values: List<String>)
    
    /**
     * Check if a state exists
     * @param stateName The name of the state to check
     * @return true if the state exists, false otherwise
     */
    fun hasStateValues(stateName: String): Boolean

    /**
     * Delete a state and its values
     * @param stateName The name of the state to delete
     */
    fun deleteState(stateName: String)
} 
