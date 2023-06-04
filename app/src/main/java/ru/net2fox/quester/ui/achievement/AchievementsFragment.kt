package ru.net2fox.quester.ui.achievement

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.Achievement
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.databinding.FragmentAchievementsBinding
import ru.net2fox.quester.databinding.FragmentLeaderboardBinding
import ru.net2fox.quester.ui.moderator.log.LogFragmentDirections
import ru.net2fox.quester.ui.userprofile.UserProfileViewModel

class AchievementsFragment : Fragment() {

    private lateinit var achievements: List<Achievement>
    private lateinit var achievementsViewModel: AchievementsViewModel
    private var _binding: FragmentAchievementsBinding? = null
    private lateinit var adapter: AchievementRecyclerViewAdapter

    // Это свойство действует только между onCreateView и
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAchievementsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        achievementsViewModel = ViewModelProvider(this)[AchievementsViewModel::class.java]
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(context)
        adapter = AchievementRecyclerViewAdapter(listOf())
        binding.recyclerViewUsers.adapter = adapter

        achievementsViewModel.achievementsResult.observe(
            viewLifecycleOwner,
            Observer { users ->
                users?.let { userResult ->
                    userResult.error?.let {
                        showToastFail(it)
                    }
                    userResult.success?.let {
                        this.achievements = it
                        adapter.updateAchievements(this.achievements)
                        updateUI()
                    }
                }
            }
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                achievementsViewModel.getAchievements()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            achievementsViewModel.getAchievements()
        }
        binding.swipeRefresh.isRefreshing = true
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun updateUI() {
        binding.recyclerViewUsers.adapter!!.notifyDataSetChanged()
        if (binding.swipeRefresh.isRefreshing) {
            binding.swipeRefresh.isRefreshing = false
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
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_achievement_specified, parent, false)
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
}