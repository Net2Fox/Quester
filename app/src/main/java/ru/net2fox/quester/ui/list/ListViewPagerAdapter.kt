package ru.net2fox.quester.ui.list

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.adapter.FragmentViewHolder
import ru.net2fox.quester.data.model.TaskList
import ru.net2fox.quester.ui.tasks.task.TaskFragment

class ListViewPagerAdapter(private val fm: FragmentManager, lifecycle: Lifecycle, private val lists: List<TaskList>) : FragmentStateAdapter(fm, lifecycle) {

    override fun createFragment(position: Int): TaskFragment {
        //val listId = listViewModel.getListById(position)?.strId!!
        return TaskFragment.newInstance(lists[position].strId!!)
    }

    override fun getItemCount(): Int = lists.size


    override fun onBindViewHolder(
        holder: FragmentViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        val tag = "f" + holder.itemId
        val fragment: Fragment? = fm.findFragmentByTag(tag)
        if (fragment != null) {
            (fragment as TaskFragment).updateData()
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }
}