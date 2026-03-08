package app.aaps.plugins.automationstate.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuCompat
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.aaps.core.interfaces.logging.AAPSLogger
import app.aaps.core.interfaces.resources.ResourceHelper
import app.aaps.core.interfaces.rx.AapsSchedulers
import app.aaps.core.interfaces.rx.bus.RxBus
import app.aaps.core.interfaces.rx.events.EventPreferenceChange
import app.aaps.core.ui.toast.ToastUtils
import app.aaps.plugins.automationstate.R
import app.aaps.plugins.automationstate.databinding.AutomationStateFragmentBinding
import app.aaps.plugins.automationstate.databinding.AutomationStateItemBinding
import app.aaps.plugins.automationstate.dialogs.AutomationAddStateDialog
import app.aaps.plugins.automationstate.dialogs.AutomationStateValuesDialog
import app.aaps.plugins.automationstate.services.AutomationStateService
import dagger.android.support.DaggerFragment
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AutomationStateFragment : DaggerFragment(), MenuProvider {

    private data class StateUiModel(
        val stateName: String,
        val currentState: String,
        val values: List<String>
    )

    @Inject lateinit var aapsLogger: AAPSLogger
    @Inject lateinit var rh: ResourceHelper
    @Inject lateinit var automationStateService: AutomationStateService
    @Inject lateinit var rxBus: RxBus
    @Inject lateinit var aapsSchedulers: AapsSchedulers

    companion object {
        const val ID_MENU_ADD_STATE = 601
        private const val VALUES_PER_ROW = 3
    }

    private val disposable = CompositeDisposable()
    private var _binding: AutomationStateFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        AutomationStateFragmentBinding.inflate(inflater, container, false).also { _binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerview.layoutManager = LinearLayoutManager(context)
        binding.recyclerview.adapter = StateAdapter()

        disposable += rxBus
            .toObservable(EventPreferenceChange::class.java)
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(aapsSchedulers.main)
            .subscribe { updateUI() }

        updateUI()
        
        // Add menu provider
        requireActivity().addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        disposable.clear()
        _binding = null
    }

    private fun updateUI() {
        val states = automationStateService.getDefinedStates()
        (binding.recyclerview.adapter as? StateAdapter)?.updateStates()
        binding.noStateText.visibility = if (states.isEmpty()) View.VISIBLE else View.GONE
    }
    
    override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
        menu.add(Menu.FIRST, ID_MENU_ADD_STATE, 0, rh.gs(R.string.add_automation_state))
            .setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER)
        MenuCompat.setGroupDividerEnabled(menu, true)
    }

    override fun onMenuItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            ID_MENU_ADD_STATE -> {
                addState()
                true
            }
            else -> false
        }
        
    private fun addState() {
        AutomationAddStateDialog.newInstance().show(childFragmentManager, "AutomationAddStateDialog")
    }

    inner class StateAdapter : RecyclerView.Adapter<StateAdapter.StateViewHolder>() {

        private val states = mutableListOf<StateUiModel>()

        init {
            updateStates()
        }

        fun updateStates() {
            val newStates = automationStateService.getDefinedStates().map { stateName ->
                StateUiModel(
                    stateName = stateName,
                    currentState = automationStateService.getStateOrNull(stateName).orEmpty(),
                    values = automationStateService.getStateValues(stateName)
                )
            }

            val oldStates = states.toList()
            val diff = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                override fun getOldListSize(): Int = oldStates.size

                override fun getNewListSize(): Int = newStates.size

                override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    oldStates[oldItemPosition].stateName == newStates[newItemPosition].stateName

                override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                    oldStates[oldItemPosition] == newStates[newItemPosition]
            })

            states.clear()
            states.addAll(newStates)
            diff.dispatchUpdatesTo(this)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StateViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.automation_state_item, parent, false)
            return StateViewHolder(view)
        }

        override fun onBindViewHolder(holder: StateViewHolder, position: Int) {
            val item = states[position]
            val stateName = item.stateName
            val currentState = item.currentState
            holder.binding.stateName.text = stateName
            
            // Clear previous state views
            val statesContainer = holder.binding.statesContainer as LinearLayout
            statesContainer.removeAllViews()
            statesContainer.orientation = LinearLayout.VERTICAL
            
            item.values.chunked(VALUES_PER_ROW).forEachIndexed { rowIndex, rowValues ->
                val rowLayout = LinearLayout(holder.itemView.context).apply {
                    orientation = LinearLayout.HORIZONTAL
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply {
                        if (rowIndex > 0) topMargin = 8
                    }
                }

                rowValues.forEach { stateValue ->
                    val stateView = TextView(holder.itemView.context).apply {
                        text = stateValue
                        setPadding(24, 16, 24, 16)
                        setTextAppearance(android.R.style.TextAppearance_Material_Body1)
                        background = ContextCompat.getDrawable(
                            holder.itemView.context,
                            if (stateValue == currentState) {
                                R.drawable.automation_state_active_background
                            } else {
                                R.drawable.automation_state_background
                            }
                        )
                        setOnClickListener {
                            try {
                                automationStateService.setState(stateName, stateValue)
                                updateStates()
                                rxBus.send(EventPreferenceChange(rh.gs(R.string.automation_state_values)))
                            } catch (e: RuntimeException) {
                                aapsLogger.error("Failed to set automation state from list", e)
                                ToastUtils.showToastInUiThread(context, e.message ?: rh.gs(app.aaps.core.ui.R.string.error))
                            }
                        }
                    }

                    val params = LinearLayout.LayoutParams(
                        0,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        1f
                    ).apply {
                        setMargins(0, 0, 8, 0)
                    }
                    rowLayout.addView(stateView, params)
                }

                repeat(VALUES_PER_ROW - rowValues.size) {
                    rowLayout.addView(
                        View(holder.itemView.context),
                        LinearLayout.LayoutParams(0, 0, 1f)
                    )
                }
                statesContainer.addView(rowLayout)
            }

            // Set click listener to open the state values dialog
            holder.itemView.setOnClickListener {
                val dialog = AutomationStateValuesDialog.newInstance(stateName)
                dialog.show(childFragmentManager, "AutomationStateValuesDialog")
            }
        }

        override fun getItemCount(): Int = states.size

        inner class StateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val binding = AutomationStateItemBinding.bind(itemView)
        }
    }
} 
