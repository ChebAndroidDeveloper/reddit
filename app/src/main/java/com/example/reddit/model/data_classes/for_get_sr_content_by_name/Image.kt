package com.example.reddit.model.data_classes.for_get_sr_content_by_name

data class Image(
    val id: String,
    val resolutions: List<Resolution>,
    val source: Source,
    val variants: Variants
)