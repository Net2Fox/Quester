package ru.net2fox.quester.ui.tasks.task

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Task
import ru.net2fox.quester.databinding.FragmentTaskBinding
import ru.net2fox.quester.ui.list.ListFragmentDirections

private const val KEY_LIST_ID = "ru.net2fox.quester.ui.task.LIST_ID"

class TaskFragment : Fragment() {

    private lateinit var listId: String
    private lateinit var tasks: List<Task>
    private lateinit var taskViewModel: TaskViewModel
    private var _binding: FragmentTaskBinding? = null
    private lateinit var adapter: TaskRecyclerViewAdapter

    // Это свойство действует только между onCreateView и
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        taskViewModel = ViewModelProvider(this, TaskViewModelFactory())[TaskViewModel::class.java]

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(context)
        adapter = TaskRecyclerViewAdapter(taskViewModel)
        binding.recyclerViewTasks.adapter = adapter
        taskViewModel.taskResult.observe(
            viewLifecycleOwner,
            Observer { tasks ->
                tasks?.let {taskResult ->
                    taskResult.error?.let {
                        showToastFail(it)
                    }
                    taskResult.success?.let {
                        this.tasks = it
                        updateUI()
                    }
                }
            }
        )
        listId = arguments?.getString(KEY_LIST_ID) ?: throw IllegalStateException()
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                taskViewModel.getTasks(listId)
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            taskViewModel.getTasks(listId)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewTasks.adapter!!.notifyDataSetChanged()
        if (binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = false
        }

        if (tasks.isEmpty()) {
            binding.noTaskLayout.root.visibility = View.VISIBLE
        } else {
            binding.noTaskLayout.root.visibility = View.GONE
        }

    }

    fun updateData() {
        lifecycleScope.launch(Dispatchers.IO) {
            taskViewModel.getTasks(listId)
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

    private inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private lateinit var task: Task

        private val checkBoxView: CheckBox = itemView.findViewById(R.id.check_box)
        private val textView: TextView = itemView.findViewById(R.id.text_view)

        init {
            checkBoxView.setOnClickListener(this)
            textView.setOnClickListener(this)
        }

        fun bind(task: Task) {
            this.task = task
            textView.text = task.name
            checkBoxView.isChecked = task.isExecuted
            if (checkBoxView.isChecked) {
                checkBoxView.isEnabled = false
            }
        }

        override fun onClick(v: View) {
            if (v is CheckBox) {
                if (checkBoxView.isChecked && !task.isExecuted) {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setTitle(R.string.mark_task_complete_dialog_title)
                    builder.setMessage(R.string.mark_task_complete_dialog_message)
                    builder.setPositiveButton(R.string.yes_dialog_button, null)
                    builder.setNegativeButton(R.string.no_dialog_button, null)
                    builder.setOnCancelListener {
                        checkBoxView.isChecked = false
                    }
                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        alertDialog.dismiss()
                        task.isExecuted = true
                        checkBoxView.isEnabled = false
                        lifecycleScope.launch(Dispatchers.IO) {
                            taskViewModel.saveTask(task, task.isExecuted)
                        }
                    }
                    alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
                        alertDialog.dismiss()
                        checkBoxView.isChecked = false
                    }
                }
            }
            else if (v is TextView) {
                val action = ListFragmentDirections.actionListFragmentToTaskDetailedFragment(task.strId!!, arguments?.getString(
                    KEY_LIST_ID
                )!!)
                findNavController().navigate(action)
            }
        }
    }

    private inner class TaskRecyclerViewAdapter(private val taskDetailViewModel: TaskViewModel) : RecyclerView.Adapter<TaskViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
            return TaskViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
            val task = taskDetailViewModel.taskResult.value?.success?.get(position)
            if (task != null) {
                holder.bind(task)
            }
        }

        override fun getItemCount(): Int {
            return taskDetailViewModel.taskResult.value?.success?.size ?: 0
        }
    }

    companion object {
        fun newInstance(param1: String) =
            TaskFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_LIST_ID, param1)
                }
            }
    }
}