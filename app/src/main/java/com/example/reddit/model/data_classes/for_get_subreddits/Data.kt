package com.example.reddit.model.data_classes.for_get_subreddits

data class Data(
    val after: String,
    val before: Any,
    val children: List<Children>,
    val dist: Int,
    val geo_filter: String,
    val modhash: Any
)