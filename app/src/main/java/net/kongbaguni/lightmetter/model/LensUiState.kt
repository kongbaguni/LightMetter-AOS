package net.kongbaguni.lightmetter.model

data class LensUiState(
    val lensList: List<LensModel> = emptyList(),
    val selected: LensModel? = null
)