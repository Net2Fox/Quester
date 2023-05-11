package ru.net2fox.quester.ui.list

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.databinding.FragmentListBinding

private const val KEY_SELECTED_TAB_INDEX = "ru.net2fox.quester.ui.list.SELECTED_TAB_INDEX"

class ListFragment : Fragment() {

    private lateinit var listViewModel: ListViewModel
    private var _binding: FragmentListBinding? = null
    private lateinit var adapter: ListViewPagerAdapter
    private lateinit var tabSelectedIndicator: Drawable
    private var tabTextColors: ColorStateList? = null
    private val typedValueTextColor: TypedValue = TypedValue()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentListBinding.inflate(inflater, container, false)
        binding.fab.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setTitle(R.string.create_task_dialog_title)
            val dialogView: View = inflater.inflate(R.layout.create_alertdialog, null, false)
            dialogView.findViewById<TextInputLayout>(R.id.textInputLayout).hint = getString(R.string.create_task_hint)
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
                    val listId = listViewModel.getListId(binding.tabs.selectedTabPosition)
                    lifecycleScope.launch(Dispatchers.IO) {
                        listViewModel.createTask(listId!!, taskNameEditText?.text.toString())
                    }
                }
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabSelectedIndicator = binding.tabs.tabSelectedIndicator
        tabTextColors = binding.tabs.tabTextColors

        listViewModel = ViewModelProvider(  this, ListViewModelFactory()).get(ListViewModel::class.java)

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_list, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                //return true
                return when (menuItem.itemId) {
                    R.id.action_edit_list -> {
                        changeListNameMaterialAlertDialog()
                        true
                    }
                    R.id.action_delete_list -> {
                        deleteListMaterialAlertDialog()
                        true
                    }
                    else -> false
                }
            }

            override fun onPrepareMenu(menu: Menu) {
                super.onPrepareMenu(menu)
                if (listViewModel.listSize == 0) {
                    menu.findItem(R.id.action_edit_list).isVisible = false
                    menu.findItem(R.id.action_delete_list).isVisible = false
                }

            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        adapter = ListViewPagerAdapter(childFragmentManager, lifecycle, listViewModel)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(binding.tabs, binding.viewPager) { tab, position ->
            tab.text = listViewModel.getListById(position)?.name
        }.attach()
        binding.tabs.selectTab(binding.tabs.getTabAt(savedInstanceState?.getInt(KEY_SELECTED_TAB_INDEX) ?: 0))
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.create_list_tab))

        listViewModel.listResult.observe(viewLifecycleOwner,
        Observer { listResult ->
            listResult.error?.let {
                showToastFail(it)
            }
            listResult.success?.let {
                updateUI()
            }
        })

        listViewModel.listActionResult.observe(viewLifecycleOwner,
        Observer { action ->
            action.error?.let {
                showToastFail(it)
            }
            action.success?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    listViewModel.getListsOfTasks()
                }
            }
        })

        listViewModel.taskActionResult.observe(viewLifecycleOwner,
            Observer { action ->
                action.error?.let {
                    showToastFail(it)
                }
                action.success?.let {
                    updateUI()
                }
            })

        lifecycleScope.launch(Dispatchers.IO) {
            listViewModel.getListsOfTasks()
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.viewPager.adapter!!.notifyDataSetChanged()
        requireActivity().invalidateOptionsMenu()
        binding.tabs.addTab(binding.tabs.newTab().setText(R.string.create_list_tab))
        setTouchListenerToTab()
        if(listViewModel.listSize == 0) {
            binding.fab.visibility = View.INVISIBLE
            binding.tabs.setSelectedTabIndicator(null)
            context?.theme?.resolveAttribute(com.google.android.material.R.attr.colorOnSurfaceVariant, typedValueTextColor, true)
            binding.tabs.setTabTextColors(typedValueTextColor.data, typedValueTextColor.data)
        }
        else if(listViewModel.listSize != 0) {
            binding.fab.visibility = View.VISIBLE
            binding.tabs.setSelectedTabIndicator(tabSelectedIndicator)
            binding.tabs.tabTextColors = tabTextColors
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setTouchListenerToTab() {
        val tabStrip = binding.tabs.getChildAt(0)
        if (tabStrip is ViewGroup) {
            val childCount = tabStrip.childCount
            for (i in 0 until childCount) {
                val tabView = tabStrip.getChildAt(i)
                tabView.setOnTouchListener { _, event ->
                    if (event.action == MotionEvent.ACTION_UP){
                        if(i == binding.tabs.tabCount - 1) {
                            val builder = MaterialAlertDialogBuilder(requireContext())
                            builder.setTitle(R.string.create_list_dialog_title)
                            val dialogView: View = layoutInflater.inflate(R.layout.create_alertdialog, null, false)
                            dialogView.findViewById<TextInputLayout>(R.id.textInputLayout).hint = getString(R.string.create_list_hint)
                            builder.setView(dialogView)
                            builder.setPositiveButton(R.string.ok_dialog_button, null)
                            builder.setNegativeButton(R.string.cancel_dialog_button, null)
                            val alertDialog: AlertDialog = builder.create()
                            alertDialog.show()
                            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                                val listNameEditText: TextInputEditText? = dialogView.findViewById(R.id.editText)
                                val wantToCloseDialog: Boolean = listNameEditText?.text.toString().trim().isEmpty()
                                // Если EditText пуст, отключите закрытие при нажатии на позитивную кнопку
                                if (!wantToCloseDialog) {
                                    alertDialog.dismiss()
                                    lifecycleScope.launch(Dispatchers.IO) {
                                        listViewModel.createListOfTasks(listNameEditText?.text.toString())
                                    }
                                }
                            }
                            return@setOnTouchListener true
                        }
                    }
                    return@setOnTouchListener false
                }
            }
        }
    }

    private fun showToastFail(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun changeListNameMaterialAlertDialog() {
        val listId = listViewModel.getListId(binding.tabs.selectedTabPosition)
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.edit_list_dialog_title)
        val dialogView: View = layoutInflater.inflate(R.layout.create_alertdialog, null, false)
        dialogView.findViewById<TextInputLayout>(R.id.textInputLayout).hint = getString(R.string.create_list_hint)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.ok_dialog_button, null)
        builder.setNegativeButton(R.string.cancel_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        val listNameEditText: TextInputEditText? = dialogView.findViewById(R.id.editText)
        listNameEditText!!.setText(listViewModel.getListById(listId!!)?.name!!)
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            //val listNameEditText: TextInputEditText? = dialogView.findViewById(R.id.editText)
            val wantToCloseDialog: Boolean = listNameEditText?.text.toString().trim().isEmpty()
            // Если EditText пуст, отключите закрытие при нажатии на позитивную кнопку
            if (!wantToCloseDialog) {
                alertDialog.dismiss()
                lifecycleScope.launch(Dispatchers.IO) {
                    listViewModel.editListOfTasks(listId!!, listNameEditText?.text.toString())
                }
            }
        }
    }

    private fun deleteListMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.delete_list_dialog_title)
        builder.setPositiveButton(R.string.yes_dialog_button, null)
        builder.setNegativeButton(R.string.no_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            alertDialog.dismiss()
            val listId = listViewModel.getListId(binding.tabs.selectedTabPosition)
            lifecycleScope.launch(Dispatchers.IO) {
                listViewModel.deleteListOfTasks(listId!!)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_SELECTED_TAB_INDEX, binding.tabs.selectedTabPosition)
    }
}