package com.example.myapplication.storyCreator.DTOs

import com.example.myapplication.storyCreator.model.ArtDimension
import com.example.myapplication.storyCreator.model.ArtStyle
import com.example.myapplication.storyCreator.model.ProjectArtStyle
import com.example.myapplication.storyCreator.model.Reference
import com.example.myapplication.storyCreator.model.ReferenceType
import kotlinx.serialization.Serializable

@Serializable
data class ReferenceDto(
    val id: String,
    val type: String, // "CHARACTER" or "ENVIRONMENT"
    val name: String,
    val lore: String,
    val design: String,
    val imageData: String? = null,
    val modelData: String? = null
)

@Serializable
data class ProjectReferencesDto(
    val projectId: String,
    val artDimension: String?, // "TWO_D" or "THREE_D"
    val artStyle: String?, // "PIXEL_ART", "STANDARD_2D", etc.
    val references: List<ReferenceDto>,
    val updatedAt: Long
)

@Serializable
data class UpdateArtStyleDto(
    val artDimension: String, // "TWO_D" or "THREE_D"
    val artStyle: String // "PIXEL_ART", "STANDARD_2D", etc.
)

@Serializable
data class AddReferenceDto(
    val type: String, // "CHARACTER" or "ENVIRONMENT"
    val name: String,
    val lore: String,
    val design: String,
    val imageData: String? = null,
    val modelData: String? = null
)

// Extension function to convert Reference to AddReferenceDto
fun Reference.toAddDto(): AddReferenceDto {
    return AddReferenceDto(
        type = type.name,
        name = name,
        lore = lore,
        design = design,
        imageData = imageData,
        modelData = modelData
    )
}

// Extension functions for conversion
fun Reference.toDto(): ReferenceDto {
    return ReferenceDto(
        id = id,
        type = type.name,
        name = name,
        lore = lore,
        design = design,
        imageData = imageData,
        modelData = modelData
    )
}

fun ReferenceDto.toReference(): Reference {
    return Reference(
        id = id,
        type = ReferenceType.valueOf(type),
        name = name,
        lore = lore,
        design = design,
        imageData = imageData,
        modelData = modelData
    )
}




fun ProjectArtStyle.toUpdateDto(): UpdateArtStyleDto {
    return UpdateArtStyleDto(
        artDimension = dimension.name,
        artStyle = style.name
    )
}

fun ProjectReferencesDto.toProjectArtStyle(): ProjectArtStyle? {
    if (artDimension == null || artStyle == null) return null
    return ProjectArtStyle(
        dimension = ArtDimension.valueOf(artDimension),
        style = ArtStyle.valueOf(artStyle)
    )
}