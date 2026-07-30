package com.linhtetko.j2k_auto_android_sample.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.linhtetko.j2k_auto_android_sample.data.network.NetworkModule
import com.linhtetko.j2k_auto_android_sample.data.repository.CommentRepository
import com.linhtetko.j2k_auto_android_sample.data.repository.PostRepository
import com.linhtetko.j2k_auto_android_sample.data.repository.ProductRepository
import com.linhtetko.j2k_auto_android_sample.data.repository.RecipeRepository
import com.linhtetko.j2k_auto_android_sample.ui.viewmodel.CommentViewModel
import com.linhtetko.j2k_auto_android_sample.ui.viewmodel.PostViewModel
import com.linhtetko.j2k_auto_android_sample.ui.viewmodel.ProductViewModel
import com.linhtetko.j2k_auto_android_sample.ui.viewmodel.RecipeViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Products : Screen("products", "Products", Icons.Default.ShoppingCart)
    object Recipes : Screen("recipes", "Recipes", Icons.Default.Restaurant)
    object Posts : Screen("posts", "Posts", Icons.Default.Feed)
    object Comments : Screen("comments", "Comments", Icons.Default.Comment)
}

val items = listOf(
    Screen.Products,
    Screen.Recipes,
    Screen.Posts,
    Screen.Comments,
)

@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = Screen.Products.route, Modifier.padding(innerPadding)) {
            composable(Screen.Products.route) {
                val viewModel: ProductViewModel = viewModel(factory = createFactory {
                    ProductViewModel(ProductRepository(NetworkModule.apiService))
                })
                ProductListScreen(viewModel)
            }
            composable(Screen.Recipes.route) {
                val viewModel: RecipeViewModel = viewModel(factory = createFactory {
                    RecipeViewModel(RecipeRepository(NetworkModule.apiService))
                })
                RecipeListScreen(viewModel)
            }
            composable(Screen.Posts.route) {
                val viewModel: PostViewModel = viewModel(factory = createFactory {
                    PostViewModel(PostRepository(NetworkModule.apiService))
                })
                PostListScreen(viewModel)
            }
            composable(Screen.Comments.route) {
                val viewModel: CommentViewModel = viewModel(factory = createFactory {
                    CommentViewModel(CommentRepository(NetworkModule.apiService))
                })
                CommentListScreen(viewModel)
            }
        }
    }
}

inline fun <reified T : ViewModel> createFactory(crossinline creator: () -> T): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <U : ViewModel> create(modelClass: Class<U>): U {
            return creator() as U
        }
    }
}
