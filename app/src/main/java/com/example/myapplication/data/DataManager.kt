package com.example.myapplication.data

import com.example.myapplication.model.ExperienceItem
import kotlin.random.Random

object DataManager {

    // 模拟网络图片资源
    private val imageUrls = listOf(
        "https://picsum.photos/400/600",
        "https://picsum.photos/400/700",
        "https://picsum.photos/400/500",
        "https://picsum.photos/400/650",
        "https://picsum.photos/400/550",
        "https://picsum.photos/400/750"
    )

    private val avatars = listOf(
        "https://picsum.photos/100/100",
        "https://picsum.photos/100/100?1",
        "https://picsum.photos/100/100?2",
        "https://picsum.photos/100/100?3"
    )

    private val titles = listOf(
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

    private val usernames = listOf(
        "旅行达人小明", "美食侦探小红", "摄影爱好者小李",
        "读书人小王", "健身教练小张", "生活家小赵",
        "探险家小刘", "设计师小陈", "程序员小杨", "教师小周"
    )

    fun generateMockData(count: Int = 20): List<ExperienceItem> {
        val mockItems = mutableListOf<ExperienceItem>()

        for (i in 1..count) {
            val item = ExperienceItem(
                id = i.toString(),
                title = titles.random() + " #$i",
                imageUrl = imageUrls.random() + "?random=$i",
                userAvatar = avatars.random(),
                userName = usernames.random(),
                likeCount = Random.nextInt(100, 5000),
                isLiked = Random.nextBoolean(),
                imageWidth = 400,
                imageHeight = 500 + Random.nextInt(-50, 150) // 随机高度，创造瀑布流效果
            )
            mockItems.add(item)
        }
        return mockItems
    }

    fun generateNewData(count: Int = 10): List<ExperienceItem> {
        // 生成新的数据，用于刷新
        return generateMockData(count)
    }
}