package ru.net2fox.quester.ui.userprofile

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Skill
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.data.model.UserLog
import ru.net2fox.quester.databinding.FragmentUserProfileBinding
import ru.net2fox.quester.ui.character.CharacterFragment
import ru.net2fox.quester.ui.character.CharacterFragmentDirections
import ru.net2fox.quester.ui.character.CharacterViewModel
import ru.net2fox.quester.ui.moderator.log.LogViewModel
import java.text.SimpleDateFormat

class UserProfileFragment : Fragment() {

    private var _binding: FragmentUserProfileBinding? = null
    private lateinit var adapter: AchievementRecyclerViewAdapter
    private lateinit var userProfileViewModel: UserProfileViewModel
    private lateinit var currentUser: User
    private lateinit var skills: List<Skill>
    private lateinit var skillAdapter: SkillRecyclerViewAdapter

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
        adapter = AchievementRecyclerViewAdapter()
        binding.recyclerViewAchievements.adapter = adapter

        binding.recyclerViewSkills.layoutManager = LinearLayoutManager(context)
        skillAdapter = SkillRecyclerViewAdapter(userProfileViewModel)
        binding.recyclerViewSkills.adapter = skillAdapter

        userProfileViewModel.userProfileResult.observe(
            viewLifecycleOwner,
            Observer { user ->
                user?.let { userResult ->
                    userResult.error?.let {
                        showToastFail(it)
                    }
                    userResult.success?.let {
                        this.currentUser = it
                        updateUI()
                        updateCharacterUI()
                    }
                }
            }
        )

        userProfileViewModel.skillResult.observe(
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

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                userProfileViewModel.getSkills()
                userProfileViewModel.getUser()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            binding.swipeRefresh.isRefreshing = true
            userProfileViewModel.getSkills()
            userProfileViewModel.getUser()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewAchievements.adapter!!.notifyDataSetChanged()
        binding.recyclerViewSkills.adapter!!.notifyDataSetChanged()
        binding.swipeRefresh.isRefreshing = false
    }

    private fun updateCharacterUI() {
        if (::currentUser.isInitialized) {
            binding.textViewUserName.text = currentUser.name
            binding.progressIndicator.max = 1000
            val per: Double = ((currentUser.experience.toDouble() / 1000) * 100)
            binding.progressIndicator.progress = per.toInt()
            binding.textViewUserLevel.text = getString(R.string.level_string, currentUser.level)
            binding.textViewUserPercent.text = getString(R.string.percent_string, per.toInt())
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

    private inner class AchievementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        //private lateinit var log: UserLog
        //private val dateFormat: SimpleDateFormat = SimpleDateFormat("dd.MM.yyyy")
        //private val timeFormat: SimpleDateFormat = SimpleDateFormat("HH:mm:ss")
        //private val textView: TextView = itemView.findViewById(R.id.log_text_view)
        private val num: TextView = itemView.findViewById(R.id.title)

        init {
            //textView.setOnClickListener(this)
        }

        fun bind(string: String) {
            //this.log = log
            //val date = log.datetime?.toDate()
            //textView.text = getString(R.string.log_string,
            //    log.userName,
            //    log.action.toString(),
            //    log.objectType.toString(),
            //    log.objectName,
            //    dateFormat.format(date),
            //    timeFormat.format(date)
            //)
            //textView.startAnimation(AnimationUtils.loadAnimation(itemView.context, R.anim.recyclerview_item_anim))
            num.text = string
        }

        override fun onClick(v: View) {

        }
    }

    private inner class AchievementRecyclerViewAdapter() : RecyclerView.Adapter<AchievementViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement, parent, false)
            return AchievementViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
            //val log = logViewModel.logResult.value?.success?.get(position)
            //if (log != null) {
            //    holder.bind(log)
            //}
            holder.bind(position.toString())
        }

        override fun getItemCount(): Int {
            return 10//logViewModel.logResult.value?.success?.size ?: 0
        }
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
            val per: Double = ((skill.experience.toDouble() / skill.needExperience.toDouble()) * 100)
            progressBar.progress = per.toInt()
            skillLevelTextView.text = getString(R.string.level_string, skill.level)
            skillPercentTextView.text = getString(R.string.percent_string, per.toInt())
        }

        override fun onClick(v: View) {
            //TODO Переделать тап по элементам
            if (v is TextView) {
                val action = CharacterFragmentDirections.actionCharacterFragmentToSkillFragment(skill.strId!!)
                findNavController().navigate(action)
            }
        }
    }

    private inner class SkillRecyclerViewAdapter(private val userProfileViewModel: UserProfileViewModel) : RecyclerView.Adapter<SkillViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkillViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
            return SkillViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: SkillViewHolder, position: Int) {
            val skill = userProfileViewModel.skillResult.value?.success?.get(position)
            if (skill != null) {
                holder.bind(skill)
            }
        }

        override fun getItemCount(): Int {
            return userProfileViewModel.skillResult.value?.success?.size ?: 0
        }
    }
}