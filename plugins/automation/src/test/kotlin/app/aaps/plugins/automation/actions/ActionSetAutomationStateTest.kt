package app.aaps.plugins.automation.actions

import app.aaps.core.interfaces.queue.Callback
import app.aaps.plugins.automation.R
import com.google.common.truth.Truth.assertThat
import org.json.JSONObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert

class ActionSetAutomationStateTest : ActionsTestBase() {

    private lateinit var sut: ActionSetAutomationState

    @BeforeEach
    fun setUp() {
        whenever(rh.gs(R.string.set_state)).thenReturn("Set state")
        whenever(rh.gs(R.string.set_state_description, "Night", "On")).thenReturn("Set state Night=On")
        whenever(rh.gs(R.string.automation_state_not_defined, "Night")).thenReturn("Automation state \"Night\" is not defined")
        whenever(rh.gs(R.string.automation_state_value_not_allowed, "Night", "On")).thenReturn("Automation state \"Night\" does not allow value \"On\"")
        whenever(rh.gs(R.string.automation_state_set_failed)).thenReturn("Failed to set automation state")
        whenever(automationState.getDefinedStates()).thenReturn(listOf("Night"))
        whenever(automationState.getStateValues("Night")).thenReturn(listOf("On", "Off"))
        whenever(automationState.hasStateValues("Night")).thenReturn(true)

        sut = ActionSetAutomationState(injector)
        sut.fromJSON("""{"inputStateName":"Night","inputState":"On"}""")
    }

    @Test
    fun isValidForAllowedStateValue() {
        assertThat(sut.isValid()).isTrue()
    }

    @Test
    fun isInvalidForUnsupportedValue() {
        sut.fromJSON("""{"inputStateName":"Night","inputState":"Unknown"}""")
        assertThat(sut.isValid()).isFalse()
    }

    @Test
    fun doActionReturnsFailureWhenStateMissing() {
        whenever(automationState.hasStateValues("Night")).thenReturn(false)

        sut.doAction(object : Callback() {
            override fun run() {
                assertThat(result.success).isFalse()
                assertThat(result.comment).isEqualTo("Automation state \"Night\" is not defined")
            }
        })
    }

    @Test
    fun doActionReturnsFailureWhenSetStateThrows() {
        whenever(automationState.setState(any(), any())).thenThrow(IllegalStateException("boom"))

        sut.doAction(object : Callback() {
            override fun run() {
                assertThat(result.success).isFalse()
                assertThat(result.comment).isEqualTo("boom")
            }
        })
    }

    @Test
    fun toJsonFromJsonRoundTrip() {
        val json = sut.toJSON()
        val parsed = JSONObject(json)
        assertThat(parsed.getString("type")).isEqualTo("ActionSetAutomationState")
        JSONAssert.assertEquals(
            """{"inputStateName":"Night","inputState":"On"}""",
            parsed.getJSONObject("data").toString(),
            true
        )
    }
}
