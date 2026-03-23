package net.kongbaguni.lightmetter.model

data class BodyUiState(
    val bodies: List<BodyModel> = emptyList(),
    val selected: BodyModel? = null
)