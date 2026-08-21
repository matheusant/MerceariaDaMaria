package com.heracles.troco.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.heracles.troco.R
import com.heracles.troco.domain.model.Transaction
import com.heracles.troco.domain.model.TransactionItem
import com.heracles.troco.domain.model.TransactionType
import com.heracles.troco.ui.screen.CatalogScreen
import com.heracles.troco.ui.screen.LoginScreen
import com.heracles.troco.ui.screen.MeuFiadoScreen
import com.heracles.troco.ui.screen.MeuFiadoUiState
import com.heracles.troco.ui.screen.PrivacyPoliticsScreen
import com.heracles.troco.ui.screen.SignUpScreen
import com.heracles.troco.ui.screen.TermsAndConditionsScreen
import com.heracles.troco.ui.theme.RubikFontFamily
import com.heracles.troco.ui.viewmodel.SignupViewModel

object Routes {
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val CATALOG = "catalog"
    const val MEU_FIADO = "meu_fiado"
    const val TERMS_AND_CONDITIONS = "terms_and_conditions"
    const val PRIVACY_POLITICS = "privacy_politics"
}

@Composable
fun MerceariaApp(
    signUpViewModel: SignupViewModel = hiltViewModel()
) {
    val focusManger = LocalFocusManager.current
    val navController = rememberNavController()
    val navBarStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBarStackEntry?.destination?.route

    val startDestination = remember { Routes.SIGN_UP }

    val bottomBarItems = listOf(
        BarItem.Catalog,
        BarItem.MeuFiado,
    )
    val topBarItems = listOf(
        BarItem.Catalog,
        BarItem.MeuFiado,
    )

    Scaffold(
        topBar = {
            AppTopBar(
                currentRoute = currentRoute,
                item = topBarItems.firstOrNull { currentRoute == it.route } ?: BarItem.Catalog
            )
        },
        bottomBar = {
            AppBottomBar(
                navController = navController,
                currentRoute = currentRoute,
                items = bottomBarItems
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .displayCutoutPadding()
                .navigationBarsPadding()
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    bottom = innerPadding.calculateBottomPadding()
                )
                .pointerInput(Unit) {
                    detectTapGestures(onTap = { focusManger.clearFocus() })
                }
        ) {
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier
                    .padding(horizontal = 20.dp)
            ) {
                composable(Routes.SIGN_UP) {
                    val signUpState by signUpViewModel.uiState.collectAsState()

                    LaunchedEffect(signUpState.autenticado) {
                        if (signUpState.autenticado) {
                            navController.navigate(Routes.CATALOG) {
                                popUpTo(Routes.SIGN_UP) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                    SignUpScreen(
                        state = signUpState,
                        onPhoneChange = signUpViewModel::onPhoneChange,
                        onPasswordChange = signUpViewModel::onPasswordChange,
                        onNameChange = signUpViewModel::onNameChange,
                        onLastnameChange = signUpViewModel::onLastnameChange,
                        onSignup = signUpViewModel::signUp,
                        onLoginSelected = {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.id) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onSignupSelected = { navController.navigate(Routes.SIGN_UP) },
                        onSignIn = {
                            navController.navigate(Routes.CATALOG) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                                launchSingleTop = true
                            }
                        },
                    )
                }

                composable(Routes.CATALOG) {
                    var searchQuery by rememberSaveable { mutableStateOf("") }

                    CatalogScreen(
                        query = searchQuery,
                        onQueryChange = { newQuery ->
                            searchQuery = newQuery
                        }
                    )
                }

                composable(Routes.MEU_FIADO) {
                    MeuFiadoScreen(
                        state = MeuFiadoUiState(
                            balance = "R$ 42,90",
                            dueDate = "11 de maio de 2026",
                            transactions = listOf(
                                Transaction(
                                    title = "2x Café Torrado 500g, 1x Açucar 500g",
                                    formattedDate = "Hoje às 09:12",
                                    details = listOf(
                                        TransactionItem(
                                            name = "2x Café Torrado 500g",
                                            subtotal = "39,90"
                                        ),
                                        TransactionItem(
                                            name = "1x Açucar 500g",
                                            subtotal = "3,00"
                                        )
                                    ),
                                    amount = "- R$ 42,90",
                                    type = TransactionType.PURCHASE
                                ),
                                Transaction(
                                    title = "Pagamento parcial em dinheiro",
                                    formattedDate = "Hoje às 15:00",
                                    amount = "+ R$ 20,00",
                                    type = TransactionType.PAYMENT
                                ),
                                Transaction(
                                    title = "1x Açucar 500g",
                                    formattedDate = "Hoje às 16:15",
                                    details = listOf(
                                        TransactionItem(
                                            name = "1x Açucar 500g",
                                            subtotal = "6,90"
                                        )
                                    ),
                                    amount = "- R$ 6,90",
                                    type = TransactionType.PURCHASE
                                )
                            )
                        )
                    )
                }

                composable(Routes.TERMS_AND_CONDITIONS) {
                    TermsAndConditionsScreen()
                }

                composable(Routes.PRIVACY_POLITICS) {
                    PrivacyPoliticsScreen()
                }
            }
        }
    }
}

@Composable
fun AppTopBar(
    currentRoute: String?,
    item: BarItem
) {
    val showTopBar = currentRoute == item.route
    if (showTopBar) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = item.title,
                style = TextStyle(
                    fontFamily = RubikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    }
}

@Composable
fun AppBottomBar(
    navController: NavHostController,
    currentRoute: String?,
    items: List<BarItem>
) {
    val showBottomBar = currentRoute in items.map { it.route }

    if (showBottomBar) {
        val outlineColor = MaterialTheme.colorScheme.outlineVariant
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            modifier = Modifier
                .fillMaxWidth()
                .drawWithContent {
                    drawContent()
                    drawLine(
                        color = outlineColor,
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
        ) {
            items.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Box(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title
                            )
                        }
                    },
                    label = { Text(item.title) },
                    selected = currentRoute == item.route,
                    onClick = {
                        if (currentRoute != item.route) {
                            navController.navigate(item.route) {
                                popUpTo(Routes.CATALOG) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        // Pílula verde no Dark (GreenContainerDark) e Light (GreenContainer)
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,

                        // Ícone e texto selecionados (OnGreenContainer / OnGreenContainerDark)
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,

                        // Ícone e texto inativos usando variantes de superfície
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}


sealed class BarItem(val route: String, val title: String, @param:DrawableRes val icon: Int) {
    object Catalog : BarItem(Routes.CATALOG, "Catálogo", R.drawable.ic_catalog)
    object MeuFiado : BarItem(Routes.MEU_FIADO, "Meu Fiado", R.drawable.ic_meu_fiado)
}