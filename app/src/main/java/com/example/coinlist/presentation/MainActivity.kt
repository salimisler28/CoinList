package com.example.coinlist.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.coinlist.data.db.AppDatabase
import com.example.coinlist.data.network.CoinApi
import com.example.coinlist.domain.models.CoinModel
import com.example.coinlist.domain.usecases.InitUseCase
import com.example.coinlist.domain.usecases.RefreshUseCase
import com.example.coinlist.presentation.ui.theme.CoinListTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoinListTheme {
                val mainViewModel: MainViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            val db = AppDatabase.create(this@MainActivity)
                            val dao = db.getCoinDao()
                            val coinApi = CoinApi()
                            val initUseCase = InitUseCase(coinApi, dao)
                            val refreshUseCase = RefreshUseCase(coinApi)
                            return MainViewModel(refreshUseCase, initUseCase) as T
                        }
                    }
                )
                val screenState = mainViewModel.screenState.collectAsStateWithLifecycle().value
                App(screenState)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(screenState: MainScreenState) {
    val lazyListState = rememberLazyListState()
    // I didn't know what you mean by "simple manual navigation" that's why i did this part like this
    if (screenState.detail != null) {
        DetailScreen(screenState)
    } else {
        ListScreen(screenState, lazyListState)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(screenState: MainScreenState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenState.detail!!.name
                    )
                },
                navigationIcon = {
                    IconButton(onClick = screenState.onDetailBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "${screenState.detail!!.symbol} - ${screenState.detail.price} USD")
            AsyncImage(
                model = screenState.detail.imageUrl,
                contentDescription = screenState.detail.symbol,
                modifier = Modifier.size(65.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListScreen(screenState: MainScreenState, lazyListState: LazyListState) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "List Screen"
                    )
                }
            )
        }
    ) { innerPadding ->
        if (screenState.isFullScreenLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            PullToRefreshBox(
                isRefreshing = screenState.isRefreshing,
                onRefresh = screenState.onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    state = lazyListState
                ) {
                    screenState.coinList?.forEachIndexed { index, item ->
                        item(key = item.id) {
                            Column {
                                CoinItem(
                                    modifier = Modifier.fillMaxWidth(),
                                    coinModel = item,
                                    onClick = {
                                        screenState.onItemClick(item)
                                    }
                                )
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CoinItem(
    modifier: Modifier = Modifier,
    coinModel: CoinModel,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Text(text = "${coinModel.name} - ${coinModel.symbol}", modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight,
                contentDescription = null
            )
        }
    }
}