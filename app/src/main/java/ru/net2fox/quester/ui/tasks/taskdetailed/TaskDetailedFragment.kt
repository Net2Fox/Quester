package ru.net2fox.quester.ui.tasks.taskdetailed

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.databinding.FragmentTaskDetailedBinding
import ru.net2fox.quester.ui.character.CharacterFragment
import ru.net2fox.quester.ui.character.CharacterFragmentDirections
import ru.net2fox.quester.ui.character.CharacterViewModel

private const val KEY_TASK_ID = "ru.net2fox.quester.ui.task.TASK_ID"

class TaskDetailedFragment : Fragment() {

    private lateinit var skills: List<Skill>
    private lateinit var currnetTask: Task
    private var _binding: FragmentTaskDetailedBinding? = null
    private lateinit var difficultyArrayAdapter: ArrayAdapter<Difficulty>
    private val args: TaskDetailedFragmentArgs by navArgs()
    private lateinit var taskDetailedViewModel: TaskDetailedViewModel
    private lateinit var adapterRecyclerView: SkillRecyclerViewAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskDetailedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        taskDetailedViewModel = ViewModelProvider(this, TaskDetailedViewModelFactory()).get(
            TaskDetailedViewModel::class.java)
        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(context)
        adapterRecyclerView = SkillRecyclerViewAdapter(taskDetailedViewModel)
        binding.recyclerViewSkills.adapter = adapterRecyclerView

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_task_detailed, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                //return true
                return when (menuItem.itemId) {
                    R.id.action_delete_task -> {
                        deleteTaskMaterialAlertDialog()
                        true
                    }
                    R.id.action_save_task -> {
                        currnetTask.name = binding.editTextName.text.toString()
                        currnetTask.description = binding.editTextDescription.text.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            taskDetailedViewModel.saveTask(currnetTask)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.addSkillButton.setOnClickListener {
            createSkillMaterialAlertDialog()
        }

        taskDetailedViewModel.taskDetailedResult.observe(
            viewLifecycleOwner,
            Observer { task ->
                task.error?.let {
                    showToastFail(it)
                }
                task.success?.let {
                    currnetTask = it
                    updateUI(currnetTask)
                }
            }
        )

        taskDetailedViewModel.taskActionResult.observe(
            viewLifecycleOwner,
            Observer { action ->
                action.error?.let {
                    showToastFail(it)
                }
                action.success?.let {
                    findNavController().popBackStack()
                }
            }
        )

        taskDetailedViewModel.skillsResult.observe(
            viewLifecycleOwner,
            Observer { skills ->
                skills.success?.let {
                    this.skills = it
                }
            }
        )

        lifecycleScope.launch(Dispatchers.IO) {
            taskDetailedViewModel.getTask(args.listId, args.taskId)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            taskDetailedViewModel.getSkills()
        }

        val difficulties: Array<Difficulty> = arrayOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
        difficultyArrayAdapter = ArrayAdapter(requireContext(), R.layout.item_difficulty, difficulties)
        binding.autoComplete.setAdapter(difficultyArrayAdapter)

        binding.extendedFab.setOnClickListener {
            currnetTask.isExecuted = !currnetTask.isExecuted
            lifecycleScope.launch(Dispatchers.IO) {
                taskDetailedViewModel.taskMarkChange(currnetTask, !currnetTask.isExecuted, true)
            }
        }

        binding.autoComplete.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position)
            if (item is Difficulty) {
                currnetTask.difficulty = item
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI(task: Task) {
        binding.recyclerViewSkills.adapter?.notifyDataSetChanged()
        binding.editTextName.setText(task.name)
        binding.editTextDescription.setText(task.description)
        binding.autoComplete.setText(difficultyArrayAdapter.getItem(difficultyArrayAdapter.getPosition(task.difficulty)).toString(), false)
        if (task.isExecuted) {
            binding.extendedFab.text = "Задача не выполнена"
        } else {
            binding.extendedFab.text = "Задача выполнена"
        }
    }

    private fun addTaskSkill(skill: Skill) {
        if (currnetTask.skills == null) {
            currnetTask.skills = mutableListOf()
        }
        currnetTask.listSkills?.add(skill)
    }

    private fun showToastFail(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun createSkillMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.create_skill_dialog_title)
        val dialogView: View = LayoutInflater.from(this.context).inflate(R.layout.skill_alertdialog, null, false)
        val skillsAdapter = ArrayAdapter(requireContext(), R.layout.item_difficulty, skills)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.auto_complete)
        var selectedSkill: Skill? = null
        autoComplete.setAdapter(skillsAdapter)
        autoComplete.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position)
            if (item is Skill) {
                selectedSkill = item
            }
        }
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_dialog_button, null)
        builder.setNegativeButton(R.string.cancel_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            // Если selectedSkill пуст, отключите закрытие при нажатии на позитивную кнопку
            if (selectedSkill != null) {
                alertDialog.dismiss()
                addTaskSkill(selectedSkill!!)
                updateUI(currnetTask)
            }
        }
    }

    private fun deleteTaskMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.delete_task_dialog_title)
        builder.setPositiveButton(R.string.yes_dialog_button, null)
        builder.setNegativeButton(R.string.no_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            alertDialog.dismiss()
            lifecycleScope.launch(Dispatchers.IO) {
                taskDetailedViewModel.deleteTask(currnetTask)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var skill: Skill

        private val skillNameTextView: TextView = itemView.findViewById(R.id.text_view_skill_name)

        fun bind(skill: Skill) {
            this.skill = skill
            skillNameTextView.text = skill.name
        }
    }

    private inner class SkillRecyclerViewAdapter(private val taskDetailedViewModel: TaskDetailedViewModel) : RecyclerView.Adapter<SkillViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill_specified, parent, false)
            return SkillViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
            val skill = taskDetailedViewModel.taskDetailedResult.value?.success?.listSkills?.get(position)
            if (skill != null) {
                holder.bind(skill)
            }
        }

        override fun getItemCount(): Int {
            return taskDetailedViewModel.taskDetailedResult.value?.success?.listSkills?.size ?: 0
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment TaskDetailedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            TaskDetailedFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TASK_ID, param1)
                }
            }
    }
}