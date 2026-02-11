package com.firstapp.dogscanai.OnBoarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class OnboardingAdapter(private val layouts: List<Int>) :
    RecyclerView.Adapter<OnboardingAdapter.ViewHolder>() {

    inner class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(layouts[viewType], parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        // Nothing to bind here, layouts handle themselves
    }

    override fun getItemCount() = layouts.size

    override fun getItemViewType(position: Int) = position

    fun isCameraSlide(position: Int) = position == itemCount - 1
}
