package ru.net2fox.quester.ui.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import ru.net2fox.quester.ui.tasks.task.TaskFragment


private const val KEY_LIST_ID = "ru.net2fox.quester.ui.task.LIST_ID"

class ListViewPagerAdapter(private val fm: FragmentManager, lifecycle: Lifecycle, private val listViewModel: ListViewModel) : FragmentStateAdapter(fm, lifecycle) {

    private val fragmentList = mutableMapOf<String, TaskFragment>()

    override fun createFragment(position: Int): TaskFragment {
        val listId = listViewModel.getListById(position)?.id!!
        val fragment = TaskFragment.newInstance(listId)
        fragmentList.put(listId, fragment)
        return fragment
    }

    override fun getItemCount(): Int = listViewModel.listSize ?: 0

    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        //super.onBindViewHolder(holder, position, payloads)
        val tag = "f" + holder.itemId
        val fragment: Fragment? = fm.findFragmentByTag(tag)
        if (fragment != null) {
            (fragment as TaskFragment).updateData()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}