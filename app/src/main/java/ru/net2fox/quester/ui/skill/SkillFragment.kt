package ru.net2fox.quester.ui.skill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.databinding.FragmentSkillBinding

private const val KEY_SKILL_ID = "ru.net2fox.quester.ui.skill.SKILL_ID"

class SkillFragment : Fragment() {

    private lateinit var currnetUserSkill: UserSkill
    private var _binding: FragmentSkillBinding? = null
    private val args: SkillFragmentArgs by navArgs()
    private lateinit var skillViewModel: SkillViewModel

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSkillBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        skillViewModel = ViewModelProvider(this, SkillViewModelFactory())[SkillViewModel::class.java]

        // The usage of an interface lets you inject your own implementation
        val menuHost: MenuHost = requireActivity()

        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.menu_skill, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                //return true
                return when (menuItem.itemId) {
                    R.id.action_delete_skill -> {
                        deleteSkillMaterialAlertDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        skillViewModel.skillResult.observe(
            viewLifecycleOwner,
            Observer { skill ->
                skill.error?.let {
                    showToastFail(it)
                }
                skill.success?.let {
                    currnetUserSkill = it
                    updateUI(currnetUserSkill)
                }
            }
        )

        skillViewModel.skillActionResult.observe(
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

        lifecycleScope.launch(Dispatchers.IO) {
            skillViewModel.getUserSkill(args.skillId)
        }
    }

    private fun updateUI(userSkill: UserSkill) {
        binding.editTextName.setText(userSkill.name)
    }

    private fun showToastFail(@StringRes errorString: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, errorString, Toast.LENGTH_LONG).show()
    }

    private fun deleteSkillMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.delete_skill_dialog_title)
        builder.setPositiveButton(R.string.yes_dialog_button, null)
        builder.setNegativeButton(R.string.no_dialog_button, null)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            alertDialog.dismiss()
            lifecycleScope.launch(Dispatchers.IO) {
                skillViewModel.deleteUserSkill(currnetUserSkill)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @return A new instance of fragment SkillFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String) =
            SkillFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_SKILL_ID, param1)
                }
            }
    }
}