package com.zxj.fragment.gridtodetail.adapter

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.zxj.fragment.databinding.ImageCardBinding

class GridAdapter(private val viewHolderListener: ViewHolderListener) :
    RecyclerView.Adapter<ImageViewHolder>() {

    var dataList: List<Int> = mutableListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val itemView = ImageCardBinding.inflate(LayoutInflater.from(parent.context)).root
        return ImageViewHolder(itemView, viewHolderListener)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        holder.setImageResource(dataList[position])
    }

    override fun getItemCount() = dataList.size
}

class ImageViewHolder(
    itemView: View,
    private val viewHolderListener: ViewHolderListener
) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

    val binding = ImageCardBinding.bind(itemView)

    init {
        binding.cardImage.setOnClickListener(this)
    }

    fun setImageResource(resId: Int) {
        binding.cardImage.transitionName = "shared_element_${adapterPosition}"
        Glide
            .with(binding.cardImage)
            .load(resId)
            .addListener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolderListener.onLoadFinish(adapterPosition)
                    return false
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    viewHolderListener.onLoadFinish(adapterPosition)
                    return false
                }
            })
            .into(binding.cardImage)
    }

    override fun onClick(v: View) {
        viewHolderListener.onItemClicked(v, adapterPosition)
    }
}


interface ViewHolderListener {

    fun onItemClicked(view: View, position: Int)

    fun onLoadFinish(position: Int)

}