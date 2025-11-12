package com.takanakonbu.academiamagica

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.takanakonbu.academiamagica.ui.common.NameInputDialog
import com.takanakonbu.academiamagica.ui.navigation.navigationItems
import com.takanakonbu.academiamagica.ui.screen.DepartmentScreen
import com.takanakonbu.academiamagica.ui.screen.FacilityScreen
import com.takanakonbu.academiamagica.ui.screen.PrestigeScreen
import com.takanakonbu.academiamagica.ui.screen.RankingScreen
import com.takanakonbu.academiamagica.ui.screen.SchoolScreen
import com.takanakonbu.academiamagica.ui.theme.AcademiaMagicaTheme
import com.takanakonbu.academiamagica.ui.viewmodel.GameViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val gameViewModel: GameViewModel by viewModels()
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaPlayer = MediaPlayer.create(this, R.raw.bgm)
        mediaPlayer?.isLooping = true

        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        setContent {
            AcademiaMagicaTheme {
                val gameState by gameViewModel.gameState.collectAsState()
                val isLoading by gameViewModel.isLoading.collectAsState()

                if (!isLoading && gameState.schoolName.isBlank()) {
                    NameInputDialog(onNameSet = {
                        gameViewModel.setSchoolName(it)
                    })
                } else if (!isLoading) {
                    val pagerState = rememberPagerState(pageCount = { navigationItems.size })
                    val coroutineScope = rememberCoroutineScope()

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar {
                                navigationItems.forEachIndexed { index, screen ->
                                    NavigationBarItem(
                                        icon = { Icon(screen.icon, contentDescription = null) },
                                        label = { Text(screen.title) },
                                        selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    ) { innerPadding ->
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            when (it) {
                                0 -> SchoolScreen(gameViewModel = gameViewModel, paddingValues = innerPadding)
                                1 -> FacilityScreen(gameViewModel = gameViewModel, paddingValues = innerPadding)
                                2 -> DepartmentScreen(gameViewModel = gameViewModel, paddingValues = innerPadding)
                                3 -> PrestigeScreen(gameViewModel = gameViewModel, paddingValues = innerPadding)
                                4 -> RankingScreen(gameViewModel = gameViewModel, paddingValues = innerPadding)
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        mediaPlayer?.start()
    }

    override fun onStop() {
        super.onStop()
        mediaPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
