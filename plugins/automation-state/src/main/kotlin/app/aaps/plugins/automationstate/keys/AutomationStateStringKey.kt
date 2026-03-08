package app.aaps.plugins.automationstate.keys

import app.aaps.core.keys.interfaces.StringNonPreferenceKey

enum class AutomationStateStringKey(
    override val key: String,
    override val defaultValue: String,
    override val exportable: Boolean = true
) : StringNonPreferenceKey {

    AutomationStateService("automation_state_service", "{}"),
    AutomationStateValues("automation_state_values", "{}"),
}
