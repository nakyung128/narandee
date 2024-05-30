package com.gotcha.narandee.src.login

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.gotcha.narandee.R

class ViewPagerAdapter(private var contentList: ArrayList<Content>) :
    RecyclerView.Adapter<ViewPagerAdapter.PagerViewHolder>() {

    inner class PagerViewHolder(private val view: View) :
        RecyclerView.ViewHolder(view) {
            fun bind(content: Content) {
                view.findViewById<TextView>(R.id.title_tv).text = content.title
                view.findViewById<TextView>(R.id.script_tv).text = content.script
                view.findViewById<ImageView>(R.id.pager_img).setImageResource(content.img)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PagerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pager_item_list, parent, false)
        return PagerViewHolder(view)
    }

    override fun getItemCount(): Int {
        return contentList.size
    }

    override fun onBindViewHolder(holder: PagerViewHolder, position: Int) {
        holder.apply {
            bind(contentList[position])
        }
    }
}