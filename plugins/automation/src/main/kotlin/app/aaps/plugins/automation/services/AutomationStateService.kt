package app.aaps.plugins.automation.services

import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.sharedPreferences.SP
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AutomationStateService @Inject constructor(
    private val aapsLogger: AAPSLogger,  private val sp: SP
) {

    // @Inject lateinit var aapsLogger: AAPSLogger
    // @Inject lateinit var sp: SP
    private var automationStates: HashMap<String,String> =HashMap()

    init {
        val string=sp.getString("automation_state_service", "{}")

        automationStates=Json.decodeFromString(string)
    }
     fun inState(stateName:String,state:String):Boolean{
        return automationStates.getOrDefault(stateName.trim(),"_______")==state.trim()
     }
     fun setState(stateName:String,state:String){
        automationStates[stateName.trim()] = state.trim()
         sp.putString("automation_state_service",Json.encodeToString(automationStates))
    }
}