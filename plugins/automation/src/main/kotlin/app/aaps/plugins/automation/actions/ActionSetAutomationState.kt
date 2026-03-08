package app.aaps.plugins.automation.actions

import android.widget.LinearLayout
import androidx.annotation.DrawableRes
import app.aaps.core.interfaces.automation.AutomationStateInterface
import app.aaps.core.interfaces.logging.LTag
import app.aaps.core.interfaces.plugin.ActivePlugin
import app.aaps.core.interfaces.queue.Callback
import app.aaps.core.utils.JsonHelper
import app.aaps.plugins.automation.R
import app.aaps.plugins.automation.elements.InputDropdownStateMenu
import app.aaps.plugins.automation.elements.LabelWithElement
import app.aaps.plugins.automation.elements.LayoutBuilder
import dagger.android.HasAndroidInjector
import org.json.JSONObject
import javax.inject.Inject

class ActionSetAutomationState(injector: HasAndroidInjector) : Action(injector) {

    @Inject lateinit var activePlugin: ActivePlugin
    @Inject lateinit var automationState: AutomationStateInterface

    private var stateNameDropdown: InputDropdownStateMenu
    private var stateValueDropdown: InputDropdownStateMenu

    init {
        injector.androidInjector().inject(this)

        stateNameDropdown = InputDropdownStateMenu(rh) { stateName ->
            updateStateValueDropdown(stateName)
        }
        stateValueDropdown = InputDropdownStateMenu(rh)

        // Use defined states (not only currently active ones) so new/inactive states are selectable.
        val stateNames = automationState.getDefinedStates()

        if (stateNames.isNotEmpty()) {
            stateNameDropdown.values = stateNames
            stateNameDropdown.updateAdapter()

            // Initialize state values dropdown if we have states
            updateStateValueDropdown(stateNameDropdown.value)
        }
    }

    private fun updateStateValueDropdown(stateName: String) {
        if (stateName.isNotEmpty()) {
            val stateValues = automationState.getStateValues(stateName)
            stateValueDropdown.values = stateValues
            stateValueDropdown.updateAdapter()
        } else {
            stateValueDropdown.values = emptyList()
            stateValueDropdown.updateAdapter()
        }
    }

    override fun friendlyName(): Int = R.string.set_state

    override fun shortDescription(): String = rh.gs(R.string.set_state_description, stateNameDropdown.value, stateValueDropdown.value)

    @DrawableRes override fun icon(): Int = app.aaps.core.ui.R.drawable.ic_reorder_gray_24dp

    /**
     * Validates that both dropdown selections are populated and still allowed by current definitions.
     */
    override fun isValid(): Boolean {
        return try {
            val stateName = stateNameDropdown.value.trim()
            val stateValue = stateValueDropdown.value.trim()
            stateName.isNotEmpty() &&
                stateValue.isNotEmpty() &&
                automationState.hasStateValues(stateName) &&
                automationState.getStateValues(stateName).contains(stateValue)
        } catch (e: RuntimeException) {
            aapsLogger.error(LTag.AUTOMATION, "Invalid automation state action configuration", e)
            false
        }
    }

    /**
     * Applies the selected automation state and returns success/failure through [callback].
     */
    override fun doAction(callback: Callback) {
        try {
            val stateName = stateNameDropdown.value.trim()
            val stateValue = stateValueDropdown.value.trim()
            // Re-validate at execution time in case definitions changed after the rule was created.
            if (!automationState.hasStateValues(stateName)) {
                callback.result(
                    pumpEnactResultProvider.get()
                        .success(false)
                        .comment(rh.gs(R.string.automation_state_not_defined, stateName))
                ).run()
                return
            }
            if (!automationState.getStateValues(stateName).contains(stateValue)) {
                callback.result(
                    pumpEnactResultProvider.get()
                        .success(false)
                        .comment(rh.gs(R.string.automation_state_value_not_allowed, stateName, stateValue))
                ).run()
                return
            }

            automationState.setState(stateName, stateValue)
            callback.result(pumpEnactResultProvider.get().success(true).comment(app.aaps.core.ui.R.string.ok)).run()
        } catch (e: RuntimeException) {
            aapsLogger.error(LTag.AUTOMATION, "Failed to set automation state", e)
            callback.result(
                pumpEnactResultProvider.get()
                    .success(false)
                    .comment(e.message ?: rh.gs(R.string.automation_state_set_failed))
            ).run()
        }
    }

    override fun toJSON(): String {
        val data = JSONObject()
            .put("inputStateName", stateNameDropdown.value)
            .put("inputState", stateValueDropdown.value)
        return JSONObject()
            .put("type", this.javaClass.simpleName)
            .put("data", data)
            .toString()
    }

    override fun fromJSON(data: String): Action {
        val o = JSONObject(data)
        val stateName = JsonHelper.safeGetString(o, "inputStateName", "")
        val stateValue = JsonHelper.safeGetString(o, "inputState", "")

        stateNameDropdown.value = stateName

        // Always preserve the parsed selection so it is not lost on save
        stateValueDropdown.value = stateValue

        // If definitions exist, populate the dropdown options too
        if (automationState.hasStateValues(stateName)) {
            updateStateValueDropdown(stateName)
            // Ensure the value remains set after adapter update
            stateValueDropdown.value = stateValue
        }

        return this
    }

    override fun hasDialog(): Boolean = true

    override fun generateDialog(root: LinearLayout) {
        LayoutBuilder()
            .add(LabelWithElement(rh, rh.gs(R.string.state_name_label), "", stateNameDropdown))
            .add(LabelWithElement(rh, rh.gs(R.string.state_value_label), "", stateValueDropdown))
            .build(root)
    }
}
