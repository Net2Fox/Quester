package ru.net2fox.quester.ui.userprofile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.MainActivity
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Achievement
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserSkill
import ru.net2fox.quester.databinding.FragmentUserProfileBinding
import java.util.Locale


class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private lateinit var achievementAdapter: AchievementRecyclerViewAdapter
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var currentUser: User
    private var userSkills: List<UserSkill> = listOf()
    private var achievements: List<Achievement> = listOf()
    private lateinit var skills: List<Skill>
    private lateinit var skillAdapter: SkillRecyclerViewAdapter
    private val args: UserProfileFragmentArgs by navArgs()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userProfileViewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
        val layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerViewAchievements.layoutManager = layoutManager
        achievementAdapter = AchievementRecyclerViewAdapter(achievements)
        binding.recyclerViewAchievements.adapter = achievementAdapter

        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(context)
        skillAdapter = SkillRecyclerViewAdapter(userSkills)
        binding.recyclerViewSkills.adapter = skillAdapter

        userProfileViewModel.userProfileResult.observe(
            viewLifecycleOwner,
            Observer { user ->
                user?.let { userResult ->
                    userResult.error?.let {
                        showToast(it)
                    }
                    userResult.success?.let {
                        this.currentUser = it
                        if (it.achievements != null) {
                            this.achievements = it.achievements!!
                            achievementAdapter.updateAchievements(achievements)
                        }
                        if (it.userSkills != null) {
                            this.userSkills = it.userSkills!!
                            skillAdapter.updateSkills(userSkills)
                        }
                        updateUI()
                    }
                }
            }
        )

        userProfileViewModel.userSkillResult.observe(
            viewLifecycleOwner,
            Observer { userSkills ->
                userSkills?.let { skillResult ->
                    skillResult.error?.let {
                        showToast(it)
                    }
                    skillResult.success?.let {
                        this.userSkills = it
                        skillAdapter.updateSkills(it)
                        updateUI()
                    }
                }
            }
        )

        userProfileViewModel.skillResult.observe(
            viewLifecycleOwner,
            Observer { skills ->
                skills?.let { skillResult ->
                    skillResult.error?.let {
                        showToast(it)
                    }
                    skillResult.success?.let {
                        this.skills = it
                    }
                }
            }
        )

        userProfileViewModel.addSkillResult.observe(
        viewLifecycleOwner,
        Observer { result ->
            result?.error?.let {
                showToast(it)
            }
            result?.success?.let {
                lifecycleScope.launch(Dispatchers.IO) {
                    userProfileViewModel.getUserSkills()
                }
            }
        })

        userProfileViewModel.blockUserResult.observe(
            viewLifecycleOwner,
            Observer { result ->
                if (result) {
                    showToast(R.string.text_user_blocked)
                    findNavController().popBackStack()
                } else {
                    showToast(R.string.get_data_error)
                }
            }
        )

        binding.allAchievementsButton.setOnClickListener {
            val action = UserProfileFragmentDirections.actionUserProfileFragmentToAchievementsFragment()
            findNavController().navigate(action)
        }

        binding.swipeRefresh.setOnRefreshListener {
            updateData()
        }

        updateData()

        if (args.isModerator) {
            updateModeratorUI()
        }
    }

    private fun updateData() {
        if(args.userId == null) {
            binding.addSkill.setOnClickListener {
                addSkillMaterialAlertDialog()
            }

            lifecycleScope.launch(Dispatchers.IO) {
                binding.swipeRefresh.isRefreshing = true
                userProfileViewModel.getUser()
                userProfileViewModel.getSkills()
            }
        } else {
            binding.addSkill.visibility = View.GONE
            binding.noAchievementsLayout.noAchievementsText.visibility = View.GONE
            binding.noSkillsLayout.noSkillsText.visibility = View.GONE
            requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation).visibility = View.GONE
            (requireActivity() as MainActivity).setDefaultNavBarColor()
            lifecycleScope.launch(Dispatchers.IO) {
                binding.swipeRefresh.isRefreshing = true
                userProfileViewModel.getUser(args.userId)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewAchievements.adapter!!.notifyDataSetChanged()
        binding.recyclerViewSkills.adapter!!.notifyDataSetChanged()
        binding.recyclerViewAchievements.adapter!!.notifyDataSetChanged()

        if (::currentUser.isInitialized) {
            binding.textViewUserName.text = currentUser.name
            val per: Double = ((currentUser.experience.toDouble() / 1000) * 100)
            binding.progressIndicator.progress = per.toInt()
            binding.textViewUserLevel.text = getString(R.string.level_string, currentUser.level)
            binding.textViewUserPercent.text = getString(R.string.percent_string, per.toInt())
        }

        if (achievements.isEmpty()) {
            //binding.noAchievementsGroup.visibility = View.VISIBLE
            binding.noAchievementsLayout.root.visibility = View.VISIBLE
        } else {
            //binding.noAchievementsGroup.visibility = View.GONE
            binding.noAchievementsLayout.root.visibility = View.GONE
        }

        if (userSkills.isEmpty()) {
            //binding.noSkillsGroup.visibility = View.VISIBLE
            binding.noSkillsLayout.root.visibility = View.VISIBLE
        } else {
            //binding.noSkillsGroup.visibility = View.GONE
            binding.noSkillsLayout.root.visibility = View.GONE
        }

        binding.swipeRefresh.isRefreshing = false
    }

    private fun blockConfirmMaterialAlertDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.confirm_title)
            .setMessage(R.string.block_user_dialog_text)
            .setNegativeButton(R.string.cancel_dialog_button, null)
            .setPositiveButton(resources.getString(R.string.ok_dialog_button)) { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch(Dispatchers.IO) {
                    userProfileViewModel.blockUser(args.userId!!)
                }
            }
            .show()
    }

    private fun updateModeratorUI() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_profile_moderator, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_block_profile -> {
                        blockConfirmMaterialAlertDialog()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun showToast(@StringRes string: Int) {
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, string, Toast.LENGTH_LONG).show()
    }

    private fun addSkillMaterialAlertDialog() {
        val builder = MaterialAlertDialogBuilder(requireContext())
        builder.setTitle(R.string.add_skill_dialog_title)
        val dialogView: View = LayoutInflater.from(this.context).inflate(R.layout.skill_alertdialog, null, false)
        val skillsAdapter = ArrayAdapter(requireContext(), R.layout.item_difficulty, skills)
        val autoComplete = dialogView.findViewById<AutoCompleteTextView>(R.id.auto_complete)
        var selectedSkill: Skill? = null
        autoComplete.setAdapter(skillsAdapter)
        autoComplete.setOnItemClickListener { parent, _, position, _ ->
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
            if (selectedSkill != null) {
                var userAlreadyHaveSkill = false
                for (userSkill in userSkills) {
                    if (userSkill.nameEN == selectedSkill!!.nameEN) {
                        showToast(R.string.error_add_skill)
                        userAlreadyHaveSkill = true
                        break
                    }
                }
                if (!userAlreadyHaveSkill) {
                    alertDialog.dismiss()
                    lifecycleScope.launch(Dispatchers.IO) {
                        userProfileViewModel.addSkill(selectedSkill!!)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var achievement: Achievement
        private val titleText: TextView = itemView.findViewById(R.id.title)
        private val descriptionText: TextView = itemView.findViewById(R.id.description)


        fun bind(achievement: Achievement) {
            this.achievement = achievement
            titleText.text = achievement.toString()
            descriptionText.text = getString(R.string.achievement_description_string, achievement.skill.toString(), achievement.skillLevel)
        }
    }

    private inner class AchievementRecyclerViewAdapter(private var achievements: List<Achievement>) : RecyclerView.Adapter<AchievementViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
            return AchievementViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
            val achievement = achievements[position]
            holder.bind(achievement)
        }

        override fun getItemCount(): Int {
            return achievements.size
        }

        fun updateAchievements(achievements: List<Achievement>) {
            this.achievements = achievements
        }
    }

    private inner class SkillViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var userSkill: UserSkill

        private val skillNameTextView: TextView = itemView.findViewById(R.id.text_view_skill_name)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progress_indicator)
        private val skillLevelTextView: TextView = itemView.findViewById(R.id.text_view_skill_level)
        private val skillPercentTextView: TextView = itemView.findViewById(R.id.text_view_skill_percent)


        fun bind(userSkill: UserSkill) {
            this.userSkill = userSkill
            skillNameTextView.text = if (Locale.getDefault().language.equals(Locale("ru").language)) {
                userSkill.nameRU
            } else {
                userSkill.nameEN
            }
            val per: Double = ((userSkill.experience.toDouble() / userSkill.needExperience.toDouble()) * 100)
            progressBar.progress = per.toInt()
            skillLevelTextView.text = getString(R.string.level_string, userSkill.level)
            skillPercentTextView.text = getString(R.string.percent_string, per.toInt())
        }
    }

    private inner class SkillRecyclerViewAdapter(private var skills: List<UserSkill>) : RecyclerView.Adapter<SkillViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
            return SkillViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
            val skill = skills[position]
            holder.bind(skill)
        }

        override fun getItemCount(): Int {
            return skills.size
        }

        fun updateSkills(skills: List<UserSkill>) {
            this.skills = skills
        }
    }
}