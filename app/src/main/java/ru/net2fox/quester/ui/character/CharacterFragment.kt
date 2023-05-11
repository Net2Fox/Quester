package ru.net2fox.quester.ui.character

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.databinding.FragmentCharacterBinding
import kotlin.math.sign

class CharacterFragment : Fragment() {

    private lateinit var skills: List<Skill>
    private lateinit var currentUser: User
    private lateinit var characterViewModel: CharacterViewModel
    private var _binding: FragmentCharacterBinding? = null
    private lateinit var adapter: SkillRecyclerViewAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCharacterBinding.inflate(inflater, container, false)
        binding.fab.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(R.string.create_skill_dialog_title)
            val dialogView: View = inflater.inflate(R.layout.create_alertdialog, null, false)
            dialogView.findViewById<TextInputLayout>(R.id.textInputLayout).hint = getString(R.string.create_skill_hint)
            builder.setView(dialogView)
            builder.setPositiveButton(R.string.ok_dialog_button, null)
            builder.setNegativeButton(R.string.cancel_dialog_button, null)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val taskNameEditText: TextInputEditText? = dialogView.findViewById(R.id.editText)
                val wantToCloseDialog: Boolean = taskNameEditText?.text.toString().trim().isEmpty()
                // Если EditText пуст, отключите закрытие при нажатии на позитивную кнопку
                if (!wantToCloseDialog) {
                    alertDialog.dismiss()
                    lifecycleScope.launch(Dispatchers.IO) {
                        characterViewModel.createSkill(taskNameEditText?.text.toString())
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        characterViewModel = ViewModelProvider(this, CharacterViewModelFactory()).get(CharacterViewModel::class.java)
        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(context)
        adapter = SkillRecyclerViewAdapter(characterViewModel)
        binding.recyclerViewSkills.adapter = adapter
        characterViewModel.skillResult.observe(
            viewLifecycleOwner,
            Observer { skills ->
                skills?.let { skillResult ->
                    skillResult.error?.let {
                        showToastFail(it)
                    }
                    skillResult.success?.let {
                        this.skills = it
                        updateUI()
                    }
                }
            }
        )

        characterViewModel.skillActionResult.observe(
            viewLifecycleOwner,
            Observer { action ->
                action.error?.let {
                    showToastFail(it)
                }
                action.success?.let {
                    lifecycleScope.launch(Dispatchers.IO) {
                        characterViewModel.getSkills()
                    }
                }
            }
        )

        characterViewModel.characterResult.observe(
            viewLifecycleOwner,
            Observer { user ->
                user?.let { userResult ->
                    userResult.error?.let {
                        showToastFail(it)
                    }
                    userResult.success?.let {
                        this.currentUser = it
                        updateCharacterUI()
                    }
                }
            }
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                characterViewModel.getSkills()
                characterViewModel.getUser()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            characterViewModel.getSkills()
            characterViewModel.getUser()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewSkills.adapter!!.notifyDataSetChanged()
        if (binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private fun updateCharacterUI() {
        if (::currentUser.isInitialized) {
            binding.characterLayout.textViewUserName.text = currentUser.name
            binding.characterLayout.progressIndicator.max = 1000
            val per: Double = ((currentUser.experience.toDouble() / 1000) * 100)
            binding.characterLayout.progressIndicator.progress = per.toInt()
            binding.characterLayout.textViewUserLevel.text = getString(R.string.level_string, currentUser.level)
            binding.characterLayout.textViewUserPercent.text = getString(R.string.percent_string, per.toInt())
        }
    }

    private fun showToastFail(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private lateinit var skill: Skill

        private val skillNameTextView: TextView = itemView.findViewById(R.id.text_view_skill_name)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progress_indicator)
        private val skillLevelTextView: TextView = itemView.findViewById(R.id.text_view_skill_level)
        private val skillPercentTextView: TextView = itemView.findViewById(R.id.text_view_skill_percent)

        init {
            skillNameTextView.setOnClickListener(this)
        }

        fun bind(skill: Skill) {
            this.skill = skill
            skillNameTextView.text = skill.name
            progressBar.max = skill.needExperience
            val per: Double = ((skill.experience.toDouble() / skill.needExperience.toDouble()) * 100).toDouble()
            progressBar.progress = per.toInt()
            skillLevelTextView.text = getString(R.string.level_string, skill.level)
            skillPercentTextView.text = getString(R.string.percent_string, per.toInt())
        }

        override fun onClick(v: View) {
            //TODO Переделать тап по элементам
            if (v is TextView) {
                val action = CharacterFragmentDirections.actionCharacterFragmentToSkillFragment(skill.id!!)
                findNavController().navigate(action)
            }
        }
    }

    private inner class SkillRecyclerViewAdapter(private val skillViewModel: CharacterViewModel) : RecyclerView.Adapter<SkillViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
            return SkillViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
            val skill = skillViewModel.skillResult.value?.success?.get(position)
            if (skill != null) {
                holder.bind(skill)
            }
        }

        override fun getItemCount(): Int {
            return skillViewModel.skillResult.value?.success?.size ?: 0
        }
    }
}