package ru.net2fox.quester.ui.leaderboard

import android.annotation.SuppressLint
import android.os.Bundle
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.net2fox.quester.R
import ru.net2fox.quester.data.model.User
import ru.net2fox.quester.databinding.FragmentLeaderboardBinding

class LeaderboardFragment : Fragment() {

    private lateinit var users: List<User>
    private lateinit var leaderboardViewModel: LeaderboardViewModel
    private var _binding: FragmentLeaderboardBinding? = null
    private lateinit var adapter: UsersRecyclerViewAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLeaderboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        leaderboardViewModel = ViewModelProvider(this, LeaderboardViewModelFactory())[LeaderboardViewModel::class.java]
        binding.recyclerViewUsers.layoutManager = LinearLayoutManager(context)
        adapter = UsersRecyclerViewAdapter(leaderboardViewModel)
        binding.recyclerViewUsers.adapter = adapter
        leaderboardViewModel.usersResult.observe(
            viewLifecycleOwner,
            Observer { users ->
                users?.let { userResult ->
                    userResult.error?.let {
                        showToastFail(it)
                    }
                    userResult.success?.let {
                        this.users = it
                        updateUI()
                    }
                }
            }
        )

        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch(Dispatchers.IO) {
                leaderboardViewModel.getUserLeaderboard()
            }
        }

        lifecycleScope.launch(Dispatchers.IO) {
            leaderboardViewModel.getUserLeaderboard()
        }
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

    private inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private lateinit var user: User

        private val userNameTextView: TextView = itemView.findViewById(R.id.text_view_skill_name)
        private val progressBar: LinearProgressIndicator = itemView.findViewById(R.id.progress_indicator)
        private val userLevelTextView: TextView = itemView.findViewById(R.id.text_view_skill_level)
        private val userPercentTextView: TextView = itemView.findViewById(R.id.text_view_skill_percent)

        fun bind(user: User, position: Int) {
            this.user = user
            userNameTextView.text = getString(R.string.leaderboard_string, position + 1, user.name)
            val per: Double = (user.experience.toDouble() / 1000) * 100
            progressBar.max = 1000
            progressBar.progress = per.toInt()
            userLevelTextView.text = getString(R.string.level_string, user.level)
            userPercentTextView.text = getString(R.string.percent_string, per.toInt())
        }
    }

    private inner class UsersRecyclerViewAdapter(private val leaderboardViewModel: LeaderboardViewModel) : RecyclerView.Adapter<UserViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_skill, parent, false)
            return UserViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
            val user = leaderboardViewModel.usersResult.value?.success?.get(position)
            if (user != null) {
                holder.bind(user, position)
            }
        }

        override fun getItemCount(): Int {
            return leaderboardViewModel.usersResult.value?.success?.size ?: 0
        }
    }
}