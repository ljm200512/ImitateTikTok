package com.example.myapplication.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.myapplication.cache.ImageCacheManager
import com.example.myapplication.databinding.ItemExperienceBinding
import com.example.myapplication.model.ExperienceItem

class ExperienceAdapter : RecyclerView.Adapter<ExperienceAdapter.ViewHolder>() {

    private val items = mutableListOf<ExperienceItem>()

    // 定义点赞点击监听器接口
    var onLikeClickListener: ((ExperienceItem, Int) -> Unit)? = null
    var onItemClickListener: ((ExperienceItem, Int) -> Unit)? = null

    // 图片加载配置
    private val glideOptions = RequestOptions()
        .diskCacheStrategy(DiskCacheStrategy.ALL)
        .centerCrop()
        .dontAnimate()

    /**
     * 更新数据（用于刷新）
     */
    fun updateData(newItems: List<ExperienceItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()

        // 预加载后续图片
        preloadNextImages()
    }

    /**
     * 添加更多数据（用于上拉加载更多）
     */
    fun addData(newItems: List<ExperienceItem>) {
        val startPosition = items.size
        items.addAll(newItems)
        notifyItemRangeInserted(startPosition, newItems.size)

        // 预加载后续图片
        preloadNextImages()
    }

    /**
     * 更新单个item（用于点赞状态更新）
     */
    fun updateItem(position: Int, updatedItem: ExperienceItem) {
        if (position in 0 until items.size) {
            items[position] = updatedItem
            notifyItemChanged(position)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExperienceBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item, position)

        // 预加载后续3项的图片
        if (position + 3 < items.size) {
            preloadSpecificImages(position + 1, 3)
        }
    }

    override fun getItemCount(): Int = items.size

    /**
     * 预加载后续图片
     */
    private fun preloadNextImages() {
        if (items.size > 5) {
            val urlsToPreload = items.takeLast(5).flatMap {
                listOf(it.imageUrl, it.userAvatar)
            }
            ImageCacheManager.preloadImages(urlsToPreload)
        }
    }

    /**
     * 预加载指定位置的图片
     */
    private fun preloadSpecificImages(startPosition: Int, count: Int) {
        val endPosition = (startPosition + count).coerceAtMost(items.size)
        val urlsToPreload = items.subList(startPosition, endPosition).flatMap {
            listOf(it.imageUrl, it.userAvatar)
        }
        ImageCacheManager.preloadImages(urlsToPreload)
    }

    inner class ViewHolder(private val binding: ItemExperienceBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ExperienceItem, position: Int) {
            with(binding) {
                // 设置标题和用户名
                tvTitle.text = item.title
                tvUserName.text = item.userName
                tvLikeCount.text = formatLikeCount(item.likeCount)

                // 设置点赞图标
                val likeIconRes = if (item.isLiked) {
                    com.example.myapplication.R.drawable.ic_like_liked
                } else {
                    com.example.myapplication.R.drawable.ic_like_unliked
                }
                ivLike.setImageResource(likeIconRes)

                // 加载经验图片（使用缓存）
                loadImageWithCache(item.imageUrl, ivExperienceImage)

                // 加载用户头像（使用缓存，圆形裁剪）
                loadAvatarWithCache(item.userAvatar, ivUserAvatar)

                // 设置点赞点击事件
                layoutLike.setOnClickListener {
                    onLikeClickListener?.invoke(item, absoluteAdapterPosition)
                }

                // 设置整个item的点击事件
                root.setOnClickListener {
                    onItemClickListener?.invoke(item, absoluteAdapterPosition)
                }
            }
        }

        /**
         * 加载图片（带缓存）
         */
        private fun loadImageWithCache(url: String, imageView: android.widget.ImageView) {
            // 先尝试从内存缓存获取
            val cachedBitmap = ImageCacheManager.getFromMemoryCache(url)
            if (cachedBitmap != null) {
                imageView.setImageBitmap(cachedBitmap)
            } else {
                // 使用Glide加载并缓存
                Glide.with(imageView.context)
                    .load(url)
                    .apply(glideOptions)
                    .into(imageView)
            }
        }

        /**
         * 加载头像（带缓存，圆形裁剪）
         */
        private fun loadAvatarWithCache(url: String, imageView: android.widget.ImageView) {
            Glide.with(imageView.context)
                .load(url)
                .apply(glideOptions)
                .circleCrop() // 圆形裁剪
                .into(imageView)
        }

        private fun formatLikeCount(count: Int): String {
            return when {
                count >= 10000 -> "${count / 10000}w"
                count >= 1000 -> "${count / 1000}k"
                else -> count.toString()
            }
        }
    }
}