package ru.net2fox.quester.ui.tasks.taskdetailed

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Difficulty
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.databinding.FragmentTaskDetailedBinding
import java.util.Locale

private const val KEY_TASK_ID = "ru.net2fox.quester.ui.task.TASK_ID"

class TaskDetailedFragment : Fragment() {

    private lateinit var userSkills: List<UserSkill>
    private lateinit var currnetTask: Task
    private var _binding: FragmentTaskDetailedBinding? = null
    private lateinit var difficultyArrayAdapter: ArrayAdapter<Difficulty>
    private val args: TaskDetailedFragmentArgs by navArgs()
    private lateinit var taskDetailedViewModel: TaskDetailedViewModel
    private lateinit var adapterRecyclerView: SkillRecyclerViewAdapter
    private var haveChanges = false

    // Это свойство действует только между onCreateView и
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
        taskDetailedViewModel = ViewModelProvider(this, TaskDetailedViewModelFactory())[TaskDetailedViewModel::class.java]
        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(context)
        adapterRecyclerView = SkillRecyclerViewAdapter(taskDetailedViewModel)
        binding.recyclerViewSkills.adapter = adapterRecyclerView

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_task_detailed, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_delete_task -> {
                        deleteTaskMaterialAlertDialog()
                        true
                    }
                    R.id.action_save_task -> {
                        currnetTask.name = binding.editTextName.text.toString()
                        currnetTask.description = binding.editTextDescription.text.toString()
                        lifecycleScope.launch(Dispatchers.IO) {
                            taskDetailedViewModel.saveTask(currnetTask, haveChanges)
                        }
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.addSkillButton.setOnClickListener {
            addSkillMaterialAlertDialog()
        }

        taskDetailedViewModel.taskDetailedResult.observe(
            viewLifecycleOwner,
            Observer { task ->
                task.error?.let {
                    showToast(it)
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
                    showToast(it)
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
                    this.userSkills = it
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
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(R.string.mark_task_complete_dialog_title)
            builder.setMessage(R.string.mark_task_complete_dialog_message)
            builder.setPositiveButton(R.string.yes_dialog_button, null)
            builder.setNegativeButton(R.string.no_dialog_button, null)
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                alertDialog.dismiss()
                currnetTask.isExecuted = true
                lifecycleScope.launch(Dispatchers.IO) {
                    taskDetailedViewModel.taskMarkChange(currnetTask, !currnetTask.isExecuted, true)
                }
            }
        }

        binding.autoComplete.setOnItemClickListener { parent, _, position, _ ->
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
            binding.extendedFab.visibility = View.GONE
        } else {
            binding.extendedFab.visibility = View.VISIBLE
        }
    }

    private fun addTaskSkill(userSkill: UserSkill) {
        if (currnetTask.skills == null) {
            currnetTask.skills = mutableListOf()
        }
        currnetTask.listUserSkills?.add(userSkill)
        haveChanges = true
    }

    private fun showToast(@StringRes string: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, string, Toast.LENGTH_LONG).show()
    }

    private fun addSkillMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.add_skill_dialog_title)
        val dialogView: View = LayoutInflater.from(this.context).inflate(R.layout.skill_alertdialog, null, false)
        val skillsAdapter = ArrayAdapter(requireContext(), R.layout.item_difficulty, userSkills)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.auto_complete)
        var selectedUserSkill: UserSkill? = null
        autoComplete.setAdapter(skillsAdapter)
        autoComplete.setOnItemClickListener { parent, view, position, id ->
            val item = parent.getItemAtPosition(position)
            if (item is UserSkill) {
                selectedUserSkill = item
            }
        }
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_dialog_button, null)
        builder.setNegativeButton(R.string.cancel_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (selectedUserSkill != null) {
                var userAlreadyHaveSkill = false
                for (taskSkill in currnetTask.listUserSkills!!) {
                    if (taskSkill.nameEN == selectedUserSkill!!.nameEN) {
                        showToast(R.string.error_add_skill)
                        userAlreadyHaveSkill = true
                        break
                    }
                }
                if (!userAlreadyHaveSkill) {
                    alertDialog.dismiss()
                    addTaskSkill(selectedUserSkill!!)
                    updateUI(currnetTask)
                }
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

        private lateinit var userSkill: UserSkill

        private val skillNameTextView: TextView = itemView.findViewById(R.id.text_view_skill_name)

        fun bind(userSkill: UserSkill) {
            this.userSkill = userSkill
            skillNameTextView.text = if (Locale.getDefault().language.equals(Locale("ru").language)) {
                userSkill.nameRU
            } else {
                userSkill.nameEN
            }
        }
    }

    private inner class SkillRecyclerViewAdapter(private val taskDetailedViewModel: TaskDetailedViewModel) : RecyclerView.Adapter<SkillViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill_specified, parent, false)
            return SkillViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
            val skill = taskDetailedViewModel.taskDetailedResult.value?.success?.listUserSkills?.get(position)
            if (skill != null) {
                holder.bind(skill)
            }
        }

        override fun getItemCount(): Int {
            return taskDetailedViewModel.taskDetailedResult.value?.success?.listUserSkills?.size ?: 0
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String) =
            TaskDetailedFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_TASK_ID, param1)
                }
            }
    }
}