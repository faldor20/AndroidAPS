package app.aaps.plugins.automation.triggers

import app.aaps.plugins.automation.R
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.skyscreamer.jsonassert.JSONAssert

class TriggerAutomationStateTest : TriggerTestBase() {

    private lateinit var sut: TriggerAutomationState

    @BeforeEach
    fun setUp() {
        whenever(rh.gs(R.string.check_state_name)).thenReturn("Check state")
        whenever(rh.gs(R.string.check_state_description, "Sleep", "On")).thenReturn("Check Sleep=On")
        whenever(automationStateService.getDefinedStates()).thenReturn(listOf("Sleep"))
        whenever(automationStateService.getStateValues("Sleep")).thenReturn(listOf("On", "Off"))
        whenever(automationStateService.hasStateValues("Sleep")).thenReturn(true)
        whenever(automationStateService.inState("Sleep", "On")).thenReturn(true)

        sut = TriggerAutomationState(injector)
        sut.fromJSON("""{"stateName":"Sleep","stateValue":"On"}""")
    }

    @Test
    fun shouldRunDelegatesToAutomationStateService() {
        assertThat(sut.shouldRun()).isTrue()
    }

    @Test
    fun toJsonIncludesStateNameAndValue() {
        JSONAssert.assertEquals(
            """{"data":{"stateName":"Sleep","stateValue":"On"},"type":"TriggerAutomationState"}""",
            sut.toJSON(),
            true
        )
    }

    @Test
    fun duplicateKeepsSelection() {
        val duplicate = sut.duplicate() as TriggerAutomationState
        assertThat(duplicate.toJSON()).contains("\"stateName\":\"Sleep\"")
        assertThat(duplicate.toJSON()).contains("\"stateValue\":\"On\"")
    }
}
