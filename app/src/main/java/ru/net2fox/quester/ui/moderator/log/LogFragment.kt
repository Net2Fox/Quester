package ru.net2fox.quester.ui.moderator.log

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.UserLog
import ru.net2fox.quester.databinding.FragmentLogBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

class LogFragment : Fragment() {

    private var _binding: FragmentLogBinding? = null
    private lateinit var logViewModel: LogViewModel
    private lateinit var adapter: LogRecyclerViewAdapter

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        logViewModel = ViewModelProvider(this)[LogViewModel::class.java]
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerViewLogs.layoutManager = layoutManager
        binding.recyclerViewLogs.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        adapter = LogRecyclerViewAdapter(logViewModel)
        binding.recyclerViewLogs.adapter = adapter
        logViewModel.logResult.observe(
            viewLifecycleOwner,
            Observer { logs ->
                logs?.let { log ->
                    log.error?.let {
                        showToastFail(it)
                    }
                    log.success?.let {
                        updateUI()
                    }
                }
            }
        )
        //val standardBottomSheetBehavior = BottomSheetBehavior.from(binding.standardBottomSheet)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_log, menu)
                val searchItem = menu.findItem(R.id.action_search)
                val searchView = searchItem?.actionView as SearchView
                searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(query: String): Boolean {
                        if (query.isEmpty()) {
                            logViewModel.filterLogsByUser(null)
                        } else {
                            logViewModel.filterLogsByUser(query)
                        }
                        return true
                    }

                    override fun onQueryTextChange(newText: String): Boolean {

                        return false
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_date_filter -> {
                        val datePicker =
                            MaterialDatePicker.Builder.datePicker()
                                .setTitleText(R.string.datepicker_select_date)
                                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                                .build()
                        datePicker.addOnPositiveButtonClickListener {
                            updateData(Timestamp(Date(it)))
                        }
                        datePicker.show(parentFragmentManager, "tag")
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                logViewModel.getLogs()
            }
        }

        updateData()
    }

    private fun updateData(timestamp: Timestamp? = null) {
        binding.loading.animate()
            .alpha(1f)
            .setDuration(0.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.loading.visibility = View.VISIBLE
                }
            })
        lifecycleScope.launch(Dispatchers.IO) {
            logViewModel.getLogs(timestamp)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewLogs.adapter!!.notifyDataSetChanged()
        binding.loading.animate()
            .alpha(0f)
            .setDuration(500.toLong())
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    binding.loading.visibility = View.GONE
                }
            })
        binding.swipeRefresh.isRefreshing = false
    }

    private fun showToastFail(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class LogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        private lateinit var log: UserLog
        private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")
        private val textView: TextView = itemView.findViewById(R.id.log_text_view)

        init {
            textView.setOnClickListener(this)
        }

        fun bind(log: UserLog) {
            this.log = log
            val date = log.datetime?.toDate()
            textView.text = getString(R.string.log_string,
                log.userName,
                log.action.toString(),
                log.objectType.toString(),
                log.objectName,
                dateFormat.format(date),
                timeFormat.format(date)
            )
            textView.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.recyclerview_item_anim))
        }

        override fun onClick(v: View) {

        }
    }

    private inner class LogRecyclerViewAdapter(private val logViewModel: LogViewModel) : RecyclerView.Adapter<LogViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
            return LogViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
            val log = logViewModel.logResult.value?.success?.get(position)
            if (log != null) {
                holder.bind(log)
            }
        }

        override fun getItemCount(): Int {
            return logViewModel.logResult.value?.success?.size ?: 0
        }
    }
}