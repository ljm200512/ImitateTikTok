package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.myapplication.adapter.ExperienceAdapter
import com.example.myapplication.cache.ImageCacheManager
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.model.ExperienceItem
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: ExperienceAdapter
    private var isSingleColumnMode = false // 默认双列
    private var isLoading = false // 是否正在加载更多
    private var currentPage = 1 // 当前页码

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 初始化图片缓存管理器
        ImageCacheManager.initialize(this)

        setupRecyclerView()
        setupSwipeRefresh()
        setupLoadMoreListener()
        loadData()

        setContentView(binding.root)

        // 设置Toolbar
        //setSupportActionBar(binding.toolbar)

        // 初始化图片缓存管理器
        ImageCacheManager.initialize(this)

        setupRecyclerView()
        setupSwipeRefresh()
        setupLoadMoreListener()
        loadData()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_switch_layout -> {
                switchLayoutMode()
                true
            }
            R.id.menu_clear_cache -> {
                clearImageCache()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        // 设置瀑布流布局管理器
        val layoutManager = createLayoutManager()
        binding.recyclerView.layoutManager = layoutManager

        adapter = ExperienceAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.itemAnimator = DefaultItemAnimator()

        // 设置监听器
        adapter.onLikeClickListener = { item, position ->
            handleLikeClick(item, position)
        }

        adapter.onItemClickListener = { item, position ->
            Toast.makeText(this, "点击了: ${item.title}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createLayoutManager(): StaggeredGridLayoutManager {
        return StaggeredGridLayoutManager(
            if (isSingleColumnMode) 1 else 2,
            StaggeredGridLayoutManager.VERTICAL
        ).apply {
            gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
        }
    }

    /**
     * 切换布局模式（单列/双列）
     */
    private fun switchLayoutMode() {
        isSingleColumnMode = !isSingleColumnMode

        // 创建新的布局管理器
        val newLayoutManager = createLayoutManager()

        // 保存当前滚动位置
        val firstVisiblePositions = IntArray(2)
        (binding.recyclerView.layoutManager as? StaggeredGridLayoutManager)?.let { oldManager ->
            oldManager.findFirstVisibleItemPositions(firstVisiblePositions)
        }

        // 应用新布局
        binding.recyclerView.layoutManager = newLayoutManager

        // 恢复滚动位置
        val scrollToPosition = firstVisiblePositions.minOrNull() ?: 0
        binding.recyclerView.scrollToPosition(scrollToPosition.coerceAtLeast(0))

        // 通知适配器刷新（如果需要重新计算item高度）
        adapter.notifyDataSetChanged()

        val modeName = if (isSingleColumnMode) "单列" else "双列"
        Toast.makeText(this, "已切换到${modeName}模式", Toast.LENGTH_SHORT).show()
    }

    /**
     * 清理图片缓存
     */
    private fun clearImageCache() {
        ImageCacheManager.clearCache()
        Toast.makeText(this, "图片缓存已清理", Toast.LENGTH_SHORT).show()
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshData()
                binding.swipeRefreshLayout.isRefreshing = false
            }, 1500)
        }

        binding.swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light,
            android.R.color.holo_red_light
        )
    }

    private fun setupLoadMoreListener() {
        binding.recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                val layoutManager = recyclerView.layoutManager as? StaggeredGridLayoutManager ?: return
                val visibleItemCount = layoutManager.childCount
                val totalItemCount = layoutManager.itemCount
                val firstVisibleItems = IntArray(2)
                layoutManager.findFirstVisibleItemPositions(firstVisibleItems)

                // 判断是否滚动到底部
                if (!isLoading &&
                    visibleItemCount + firstVisibleItems[0] >= totalItemCount &&
                    firstVisibleItems[0] >= 0 &&
                    totalItemCount >= 10) { // 至少有10个item才触发加载更多
                    loadMoreData()
                }
            }
        })
    }

    private fun refreshData() {
        currentPage = 1
        val newData = generateMockData(20)
        adapter.updateData(newData)
        Toast.makeText(this, "刷新成功，更新了 ${newData.size} 条内容", Toast.LENGTH_SHORT).show()
    }

    private fun loadData() {
        currentPage = 1
        val mockData = generateMockData(20)
        adapter.updateData(mockData)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun loadMoreData() {
        if (isLoading) return

        isLoading = true
        currentPage++

        Toast.makeText(this, "正在加载更多内容...", Toast.LENGTH_SHORT).show()

        Handler(Looper.getMainLooper()).postDelayed({
            val moreData = generateMockData(10)
            adapter.addData(moreData)
            isLoading = false

            Toast.makeText(this, "加载了 ${moreData.size} 条新内容", Toast.LENGTH_SHORT).show()
        }, 1000)
    }

    private fun handleLikeClick(item: ExperienceItem, position: Int) {
        val newLikedState = !item.isLiked
        val newLikeCount = if (newLikedState) item.likeCount + 1 else item.likeCount - 1

        val updatedItem = item.copy(
            isLiked = newLikedState,
            likeCount = newLikeCount
        )

        adapter.updateItem(position, updatedItem)

        val message = if (newLikedState) "已点赞" else "取消点赞"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun generateMockData(count: Int = 10): List<ExperienceItem> {
        val mockItems = mutableListOf<ExperienceItem>()

        // 图片资源
        val imageUrls = arrayOf(
            "https://picsum.photos/400/600",
            "https://picsum.photos/400/700",
            "https://picsum.photos/400/500",
            "https://picsum.photos/400/650",
            "https://picsum.photos/400/550",
            "https://picsum.photos/400/750",
            "https://picsum.photos/400/800",
            "https://picsum.photos/400/450"
        )

        // 用户头像资源（使用不同的头像）
        val avatars = arrayOf(
            "https://randomuser.me/api/portraits/men/1.jpg",
            "https://randomuser.me/api/portraits/women/1.jpg",
            "https://randomuser.me/api/portraits/men/2.jpg",
            "https://randomuser.me/api/portraits/women/2.jpg",
            "https://randomuser.me/api/portraits/men/3.jpg",
            "https://randomuser.me/api/portraits/women/3.jpg",
            "https://randomuser.me/api/portraits/men/4.jpg",
            "https://randomuser.me/api/portraits/women/4.jpg"
        )

        val titles = arrayOf(
            "这是我最近发现的一个超棒的地方！风景真的太美了",
            "分享一个生活小技巧，让你每天都能充满能量",
            "旅行日记：探索未知的美丽角落",
            "美食探店：这家餐厅的菜品让我流连忘返",
            "健身心得：坚持锻炼带来的变化",
            "读书分享：最近读的一本好书推荐",
            "摄影技巧：如何拍出令人惊艳的照片",
            "职场经验：提升工作效率的小方法",
            "亲子时光：和孩子一起度过的美好周末",
            "DIY手工：自己动手制作的家居装饰"
        )

        val usernames = arrayOf(
            "旅行达人小明", "美食侦探小红", "摄影爱好者小李",
            "读书人小王", "健身教练小张", "生活家小赵",
            "探险家小刘", "设计师小陈", "程序员小杨", "教师小周"
        )

        val startId = (currentPage - 1) * count + 1

        for (i in startId until startId + count) {
            val item = ExperienceItem(
                id = i.toString(),
                title = titles[i % titles.size] + " #$i",
                imageUrl = imageUrls[i % imageUrls.size] + "?random=$i&page=$currentPage",
                userAvatar = avatars[i % avatars.size],
                userName = usernames[i % usernames.size],
                likeCount = Random.nextInt(100, 5000),
                isLiked = Random.nextBoolean(),
                imageWidth = 400,
                imageHeight = 500 + Random.nextInt(-50, 150) // 随机高度创造瀑布流
            )
            mockItems.add(item)
        }
        return mockItems
    }
}