package com.example.coinlist.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coinlist.domain.models.CoinModel
import com.example.coinlist.domain.usecases.InitState
import com.example.coinlist.domain.usecases.InitUseCase
import com.example.coinlist.domain.usecases.RefreshUseCase
import com.example.coinlist.presentation.MainScreenEvent.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainScreenState(
    val events: Flow<MainScreenEvent>,
    val isFullScreenLoading: Boolean = true,
    val coinList: List<CoinModel>? = null,
    val isRefreshing: Boolean = false,
    val onRefresh: () -> Unit,
    val detail: CoinModel? = null,
    val onDetailBackClick: () -> Unit,
    val onItemClick: (CoinModel) -> Unit,
)

sealed class MainScreenEvent {
    data class ShowSnackBar(val message: String) : MainScreenEvent()
}

class MainViewModel constructor(
    private val refreshUseCase: RefreshUseCase,
    private val initUseCase: InitUseCase
) : ViewModel() {
    private val _event = Channel<MainScreenEvent>()

    private val _screenState = MutableStateFlow(
        MainScreenState(
            onRefresh = ::onRefresh,
            onDetailBackClick = ::onDetailBackClick,
            onItemClick = ::onItemClick,
            events = _event.receiveAsFlow()
        )
    )
    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            initUseCase().collectLatest { initState ->
                when (initState) {
                    is InitState.AllDone -> {
                        _screenState.update {
                            it.copy(
                                isFullScreenLoading = false,
                                coinList = initState.data,
                                isRefreshing = false
                            )
                        }
                    }

                    is InitState.DbDoneApiError -> {
                        _screenState.update {
                            it.copy(
                                isFullScreenLoading = false,
                                coinList = initState.data,
                                isRefreshing = false
                            )
                        }

                        _event.send(
                            ShowSnackBar(
                                initState.exception?.message ?: "An Error Occurred"
                            )
                        )
                    }

                    is InitState.DbDoneApiWaiting -> {
                        _screenState.update {
                            it.copy(
                                isFullScreenLoading = false,
                                coinList = initState.data,
                                isRefreshing = true
                            )
                        }
                    }

                    InitState.Loading -> {
                        _screenState.update {
                            it.copy(
                                isFullScreenLoading = true,
                            )
                        }
                    }

                    is InitState.Error -> {
                        _screenState.update {
                            it.copy(
                                isFullScreenLoading = false,
                            )
                        }

                        _event.send(
                            ShowSnackBar(
                                initState.appError.message ?: "An Error Occurred"
                            )
                        )
                    }
                }
            }
        }
    }

    private fun onDetailBackClick() {
        _screenState.update {
            it.copy(detail = null)
        }
    }

    private fun onRefresh() {
        if (_screenState.value.isRefreshing) {
            return
        }

        _screenState.update {
            it.copy(
                isRefreshing = true
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            val refreshRes = refreshUseCase()

            if (refreshRes.isSuccess) {
                _screenState.update {
                    it.copy(
                        coinList = refreshRes.getOrThrow()
                    )
                }
            } else {
                _event.send(
                    MainScreenEvent.ShowSnackBar(
                        refreshRes.exceptionOrNull()?.message ?: "An Error Occurred"
                    )
                )
            }

            _screenState.update {
                it.copy(
                    isRefreshing = false
                )
            }
        }
    }

    private fun onItemClick(coinModel: CoinModel) {
        _screenState.update {
            it.copy(
                detail = coinModel
            )
        }
    }
}