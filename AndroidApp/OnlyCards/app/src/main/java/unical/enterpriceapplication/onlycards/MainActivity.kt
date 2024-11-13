package unical.enterpriceapplication.onlycards

import EditOrderScreen
import ProductEditScreen
import ProductTypeEditScreen
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import unical.enterpriceapplication.onlycards.ui.*
import unical.enterpriceapplication.onlycards.ui.theme.standardTheme.OnlyCardsTheme
import unical.enterpriceapplication.onlycards.viewmodels.*
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import net.openid.appauth.AuthorizationService
import unical.enterpriceapplication.onlycards.model.AppDatabase
import unical.enterpriceapplication.onlycards.ui.privateProfile.Addresses
import unical.enterpriceapplication.onlycards.ui.privateProfile.Orders
import unical.enterpriceapplication.onlycards.ui.privateProfile.PrivateProfile
import unical.enterpriceapplication.onlycards.ui.privateProfile.Support
import unical.enterpriceapplication.onlycards.ui.privateProfile.Transactions
import unical.enterpriceapplication.onlycards.ui.privateProfile.Wallet
import unical.enterpriceapplication.onlycards.ui.privateProfile.seller.ProductRequest
import unical.enterpriceapplication.onlycards.ui.privateProfile.seller.SellProduct
import unical.enterpriceapplication.onlycards.ui.privateProfile.seller.UploadedProducts
import unical.enterpriceapplication.onlycards.ui.privateProfile.seller.UploadedProductsModify
import unical.enterpriceapplication.onlycards.ui.theme.christmasTheme.ChristmasTheme
import unical.enterpriceapplication.onlycards.ui.theme.halloweenTheme.HalloweenTheme
import unical.enterpriceapplication.onlycards.viewmodels.filter.FilterViewModel
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private lateinit var service: AuthorizationService
    private lateinit var authViewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        service = AuthorizationService(this)
        authViewModel = AuthViewModel(application, launcher)

        setContent {
            // Creazione delle istanze dei DAO
            val productDao = AppDatabase.getInstance(application).productDao()
            val productTypeDao = AppDatabase.getInstance(application).productTypeDao()

            // Creazione delle istanze dei ViewModel
            val userViewModel = UserViewModel(application, authViewModel)
            val gameViewModel = GameViewModel(application, productTypeDao)
            val orderViewModel = OrderViewModel(application, authViewModel)
            val singleWishlistViewModel = SingleWishlistViewModel(application, authViewModel)
            val wishlistsViewModel = WishlistsViewModel(application, authViewModel)
            val productTypeViewModel = ProductTypeViewModel(application, authViewModel, productTypeDao)
            val addressesViewModel = AddressesViewModel(application, authViewModel)
            val profileViewModel = ProfileViewModel(application, authViewModel)
            val homeProductTypeViewModel = HomeProductTypeViewModel(application, productTypeDao)
            val homeProductViewModel = HomeProductViewModel(application, productDao)
            val productTypeDetailsViewModel = ProductTypeDetailsViewModel(application)
            val setViewModel = SetViewModel(application)
            val saleProductViewModel = ProductViewModel(application, authViewModel)
            val supportViewModel = SupportViewModel(application, authViewModel)
            val ordersViewModel = ProfileOrdersViewModel(application, authViewModel)
            val filterViewModel = FilterViewModel(application)
            val productRequestViewModel = ProductRequestViewModel(application, authViewModel)
            val uploadedProductsViewModel = UploadedProductsViewModel(application, authViewModel, productDao)
            val productTypeEditViewModel = ProductTypeEditViewModel(application, authViewModel)
            val productEditViewModel = ProductEditViewModel(application, authViewModel)
            val sellProductsViewModel = SellProductsViewModel(application, authViewModel)
            val walletViewModel = WalletViewModel(application, authViewModel)
            val transactionsViewModel = TransactionsViewModel(application, authViewModel)
            val cartViewModel = CartViewModel(application, authViewModel)

            // Valore per settare il tema
            val month = LocalDate.now().monthValue

            if(month == 10) {
                HalloweenTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HomePage(
                            searchHistoryViewModel = SearchHistoryViewModel(this.application),
                            authViewModel = authViewModel,
                            userViewModel = userViewModel,
                            orderViewModel = orderViewModel,
                            singleWishlistViewModel = singleWishlistViewModel,
                            wishlistsViewModel = wishlistsViewModel,
                            application = application,
                            productTypeViewModel = productTypeViewModel,
                            addressesViewModel = addressesViewModel,
                            profileViewModel = profileViewModel,
                            homeProductViewModel = homeProductViewModel,
                            homeProductTypeViewModel = homeProductTypeViewModel,
                            productTypeDetailsViewModel = productTypeDetailsViewModel,
                            setViewModel = setViewModel,
                            saleProductViewModel = saleProductViewModel,
                            gameViewModel = gameViewModel,
                            supportViewModel = supportViewModel,
                            ordersViewModel = ordersViewModel,
                            filterViewModel = filterViewModel,
                            productRequestViewModel = productRequestViewModel,
                            uploadedProductsViewModel = uploadedProductsViewModel,
                            productTypeEditViewModel = productTypeEditViewModel,
                            productEditViewModel = productEditViewModel,
                            sellProductsViewModel = sellProductsViewModel,
                            walletViewModel = walletViewModel,
                            transactionsViewModel = transactionsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }
                }
            }
            else if(month == 12 || month == 1) {
                ChristmasTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HomePage(
                            searchHistoryViewModel = SearchHistoryViewModel(this.application),
                            authViewModel = authViewModel,
                            userViewModel = userViewModel,
                            orderViewModel = orderViewModel,
                            singleWishlistViewModel = singleWishlistViewModel,
                            wishlistsViewModel = wishlistsViewModel,
                            application = application,
                            productTypeViewModel = productTypeViewModel,
                            addressesViewModel = addressesViewModel,
                            profileViewModel = profileViewModel,
                            homeProductViewModel = homeProductViewModel,
                            homeProductTypeViewModel = homeProductTypeViewModel,
                            productTypeDetailsViewModel = productTypeDetailsViewModel,
                            setViewModel = setViewModel,
                            saleProductViewModel = saleProductViewModel,
                            gameViewModel = gameViewModel,
                            supportViewModel = supportViewModel,
                            ordersViewModel = ordersViewModel,
                            filterViewModel = filterViewModel,
                            productRequestViewModel = productRequestViewModel,
                            uploadedProductsViewModel = uploadedProductsViewModel,
                            productTypeEditViewModel = productTypeEditViewModel,
                            productEditViewModel = productEditViewModel,
                            sellProductsViewModel = sellProductsViewModel,
                            walletViewModel = walletViewModel,
                            transactionsViewModel = transactionsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }
                }
            }
            else {
                OnlyCardsTheme {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        HomePage(
                            searchHistoryViewModel = SearchHistoryViewModel(this.application),
                            authViewModel = authViewModel,
                            userViewModel = userViewModel,
                            orderViewModel = orderViewModel,
                            singleWishlistViewModel = singleWishlistViewModel,
                            wishlistsViewModel = wishlistsViewModel,
                            application = application,
                            productTypeViewModel = productTypeViewModel,
                            addressesViewModel = addressesViewModel,
                            profileViewModel = profileViewModel,
                            homeProductViewModel = homeProductViewModel,
                            homeProductTypeViewModel = homeProductTypeViewModel,
                            productTypeDetailsViewModel = productTypeDetailsViewModel,
                            setViewModel = setViewModel,
                            saleProductViewModel = saleProductViewModel,
                            gameViewModel = gameViewModel,
                            supportViewModel = supportViewModel,
                            ordersViewModel = ordersViewModel,
                            filterViewModel = filterViewModel,
                            productRequestViewModel = productRequestViewModel,
                            uploadedProductsViewModel = uploadedProductsViewModel,
                            productTypeEditViewModel = productTypeEditViewModel,
                            productEditViewModel = productEditViewModel,
                            sellProductsViewModel = sellProductsViewModel,
                            walletViewModel = walletViewModel,
                            transactionsViewModel = transactionsViewModel,
                            cartViewModel = cartViewModel
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        service.dispose()
    }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        authViewModel.handleAuthorizationResponse(result)
    }
}

@Composable
fun NavigationViews(
    navHostController: NavHostController,
    application: Application,
    onToggleSearchBar: (Boolean) -> Unit,
    onGameSelected: (String) -> Unit,
    selectedGame: String,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    singleWishlistViewModel: SingleWishlistViewModel,
    wishlistsViewModel: WishlistsViewModel,
    productTypeViewModel: ProductTypeViewModel,
    addressesViewModel: AddressesViewModel,
    profileViewModel: ProfileViewModel,
    homeProductViewModel: HomeProductViewModel,
    homeProductTypeViewModel: HomeProductTypeViewModel,
    productTypeDetailsViewModel: ProductTypeDetailsViewModel,
    setViewModel: SetViewModel,
    saleProductViewModel: ProductViewModel,
    gameViewModel: GameViewModel,
    supportViewModel: SupportViewModel,
    ordersViewModel: ProfileOrdersViewModel,
    filterViewModel: FilterViewModel,
    productRequestViewModel: ProductRequestViewModel,
    uploadedProductsViewModel: UploadedProductsViewModel,
    productTypeEditViewModel: ProductTypeEditViewModel,
    productEditViewModel: ProductEditViewModel,
    sellProductsViewModel: SellProductsViewModel,
    walletViewModel: WalletViewModel,
    transactionsViewModel: TransactionsViewModel,
    cartViewModel: CartViewModel
    ) {
    val currentUser = authViewModel.currentUser.collectAsState(initial = null)

    NavHost(navController = navHostController, startDestination = "home") {
        composable("home") {
            HomeScreen(selectedGame, navHostController, homeProductViewModel, homeProductTypeViewModel, gameViewModel)
        }
        composable("search/{game}?query={query}") { backStackEntry ->
            val game = backStackEntry.arguments?.getString("game") ?: "pokemon"
            val query = backStackEntry.arguments?.getString("query")
            val isAdmin = currentUser.value?.roles?.contains("ROLE_ADMIN") ?: false
            SearchScreen(query, game, onGameSelected, navHostController, productTypeViewModel, isAdmin)
        }

        composable("editProduct/{productTypeId}") { backStackEntry ->
            val productTypeId = backStackEntry.arguments?.getString("productTypeId")
            if (productTypeId != null) {
                ProductTypeEditScreen(
                    productTypeId = productTypeId,
                    productTypeEditViewModel
                )
            }
        }


        composable("login") {
            LoginScreen(navHostController, authViewModel)
        }
        composable("account") {
            val user = currentUser.value
            if (user != null) {
                val isAdmin = user.roles.contains("ROLE_ADMIN")
                if (isAdmin) {
                    AdminAccountScreen(navHostController, authViewModel)
                } else {
                    UserAccountScreen(navHostController, authViewModel)
                }
            } else {
                LoginScreen(navHostController, authViewModel)
            }
        }
        composable("register") {
            RegisterScreen(navHostController, userViewModel)
        }
        composable("wishlist") {
            WishList(navHostController, currentUser, wishlistsViewModel)
        }
        composable("wishlist-sharing-options?isPublicOrToken={isPublicOrToken}") { backStackEntry ->
            val isPublicOrToken = backStackEntry.arguments?.getString("isPublicOrToken")?.toBoolean() ?: false
            WishlistSharingOptionScreen(singleWishlistViewModel, navHostController, currentUser, isPublicOrToken)
        }

        composable(
            "users/{username}/wishlists"
        ) {
            val username = navHostController.currentBackStackEntry?.arguments?.getString("username")
            if (username != null) {
                WishList(navHostController, currentUser, wishlistsViewModel, username)
            }

        }
        composable(
            "users/{username}/wishlists/{wishlistName}"
        ) {
            val username = navHostController.currentBackStackEntry?.arguments?.getString("username")
            val wishlistName = navHostController.currentBackStackEntry?.arguments?.getString("wishlistName")
            if (username != null && wishlistName != null) {
                WishlistDetail(application = application, username = username, wishlsitName = wishlistName, navHostController = navHostController, currentUser = currentUser, wishlistsViewModel = singleWishlistViewModel, productViewModel = saleProductViewModel)

            }

        }
        composable(
            "wishlist/{id}",
            deepLinks = listOf(navDeepLink {
                uriPattern = "${application.getString(R.string.app_url)}/wishlist/{token}"
                action = Intent.ACTION_VIEW
            }),
            arguments = listOf(navArgument("token") { type = NavType.StringType; defaultValue = "" })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")
            val token = backStackEntry.arguments?.getString("token")
            if (id != null) {
                WishlistDetail(application = application, id = id, navHostController = navHostController, currentUser = currentUser, wishlistsViewModel = singleWishlistViewModel,  productViewModel = saleProductViewModel)
            } else {
                WishlistDetail(application = application, token = token, navHostController = navHostController, currentUser = currentUser, wishlistsViewModel = singleWishlistViewModel,  productViewModel = saleProductViewModel)
            }
        }
        composable("product/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            if (productId != null) SaleProduct(navHostController, productId, authViewModel, saleProductViewModel)
        }
        composable("productTypeDetails/{productTypeId}") { backStackEntry ->
            val productTypeId = backStackEntry.arguments?.getString("productTypeId")
            if (productTypeId != null) ProductTypeDetails(productTypeId, navHostController, productTypeDetailsViewModel)
        }
        composable("cart") {
            Cart(cartViewModel, navHostController)
        }
        composable("users/{username}",
            deepLinks = listOf(navDeepLink {
                uriPattern = "${application.getString(R.string.app_url)}/users/{username}"
                action = Intent.ACTION_VIEW
            }),
            arguments = listOf(navArgument("username") { type = NavType.StringType; defaultValue = "" })) {
            val username = navHostController.currentBackStackEntry?.arguments?.getString("username")
            if (username != null) {
                UserProfileScreen(navHostController, userViewModel, username, saleProductViewModel)
            }
        }
        composable("adminUsers") {
            AdminUsersScreen(navHostController, userViewModel)
        }
        composable("editUser/{userId}") { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            if (userId != null) {
                EditUserScreen(userViewModel, navHostController, userId)
            }
        }
        composable("adminOrders") {
            AdminOrdersScreen(navHostController, orderViewModel)
        }
        composable("editOrder/{orderId}") { backStackEntry ->
            val orderId = backStackEntry.arguments?.getString("orderId") ?: ""
            EditOrderScreen(orderViewModel, navHostController, orderId)
        }
        composable("set/{setName}/{game}") { backStackEntry ->
            val setName = backStackEntry.arguments?.getString("setName") ?: ""
            val activeGame = backStackEntry.arguments?.getString("game") ?: selectedGame
            SetActivity(setName, navHostController, activeGame, setViewModel)
        }
        composable("profile") {
            PrivateProfile(profileViewModel, navHostController)
        }
        composable("orders") {
            Orders(navHostController, ordersViewModel, filterViewModel)
        }
        composable("wallet") {
            Wallet(walletViewModel, navHostController)
        }
        composable("addresses") {
            Addresses(navHostController, addressesViewModel)
        }
        composable("transactions") {
            Transactions(transactionsViewModel)
        }
        composable("support") {
            Support(supportViewModel)
        }
        composable("adminAddProductType") {
            AdminAddProductTypeScreen(navHostController, productTypeEditViewModel)
        }
        composable("sellProduct") {
            SellProduct(navHostController, sellProductsViewModel, gameViewModel)
        }
        composable("uploadedProducts") {
            UploadedProducts(navHostController, uploadedProductsViewModel)
        }
        composable("productRequest") {
            ProductRequest(gameViewModel, productRequestViewModel)
        }
        composable("uploadedProductEdit/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            UploadedProductsModify(productId, navHostController, uploadedProductsViewModel)
        }
        composable(
            "editSingleProduct/{productId}",
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductEditScreen(
                productId = productId,
                navHostController,
                productEditViewModel
            )
        }
    }
}

@Composable
fun HomePage(
    searchHistoryViewModel: SearchHistoryViewModel,
    authViewModel: AuthViewModel,
    userViewModel: UserViewModel,
    orderViewModel: OrderViewModel,
    singleWishlistViewModel: SingleWishlistViewModel,
    wishlistsViewModel: WishlistsViewModel,
    application: Application,
    productTypeViewModel: ProductTypeViewModel,
    addressesViewModel: AddressesViewModel,
    profileViewModel: ProfileViewModel,
    homeProductViewModel: HomeProductViewModel,
    homeProductTypeViewModel: HomeProductTypeViewModel,
    productTypeDetailsViewModel: ProductTypeDetailsViewModel,
    setViewModel: SetViewModel,
    saleProductViewModel: ProductViewModel,
    gameViewModel: GameViewModel,
    supportViewModel: SupportViewModel,
    ordersViewModel: ProfileOrdersViewModel,
    filterViewModel: FilterViewModel,
    productRequestViewModel: ProductRequestViewModel,
    uploadedProductsViewModel: UploadedProductsViewModel,
    productTypeEditViewModel: ProductTypeEditViewModel,
    productEditViewModel: ProductEditViewModel,
    sellProductsViewModel: SellProductsViewModel,
    walletViewModel: WalletViewModel,
    transactionsViewModel: TransactionsViewModel,
    cartViewModel: CartViewModel
    ) {
    val navHostController = rememberNavController()
    authViewModel.navController = navHostController
    val showSearchBar = remember { mutableStateOf(false) }
    val showBottomBar = remember { mutableStateOf(true) }
    val selectedPage = remember { mutableStateOf("home") }
    val selectedGame = remember { mutableStateOf("pokemon") }
    val searchQuery = remember { mutableStateOf("") }

    val currentBackStackEntry = navHostController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry.value) {
        showSearchBar.value = when (currentBackStackEntry.value?.destination?.route) {
            "home", "search/{game}", "search/{game}?query={query}" -> true
            else -> false
        }
        showBottomBar.value = when (currentBackStackEntry.value?.destination?.route) {
            else -> true
        }
    }

    Scaffold(
        topBar = {
            Column {
                Navbar(navHostController, gameViewModel, selectedGame)
                if (showSearchBar.value) {
                    SearchBar(
                        searchHistoryViewModel,
                        navHostController,
                        searchQuery,
                        onSearch = { query ->
                            searchQuery.value = query
                            navHostController.navigate("search/${selectedGame.value}?query=$query")
                        },
                        onValueChange = { query -> searchQuery.value = query }
                    )
                }
            }
        },
        content = {
            Box(modifier = Modifier.padding(it)) {
                NavigationViews(
                    navHostController = navHostController,
                    application = application,
                    selectedGame = selectedGame.value,
                    onToggleSearchBar = { isVisible -> showSearchBar.value = isVisible },
                    onGameSelected = { game -> selectedGame.value = game },
                    authViewModel = authViewModel,
                    userViewModel = userViewModel,
                    orderViewModel = orderViewModel,
                    singleWishlistViewModel = singleWishlistViewModel,
                    wishlistsViewModel = wishlistsViewModel,
                    productTypeViewModel = productTypeViewModel,
                    addressesViewModel = addressesViewModel,
                    profileViewModel = profileViewModel,
                    homeProductViewModel = homeProductViewModel,
                    homeProductTypeViewModel = homeProductTypeViewModel,
                    productTypeDetailsViewModel = productTypeDetailsViewModel,
                    setViewModel = setViewModel,
                    saleProductViewModel = saleProductViewModel,
                    gameViewModel = gameViewModel,
                    supportViewModel = supportViewModel,
                    ordersViewModel = ordersViewModel,
                    filterViewModel = filterViewModel,
                    productRequestViewModel = productRequestViewModel,
                    uploadedProductsViewModel = uploadedProductsViewModel,
                    productTypeEditViewModel = productTypeEditViewModel,
                    productEditViewModel = productEditViewModel,
                    sellProductsViewModel = sellProductsViewModel,
                    walletViewModel = walletViewModel,
                    transactionsViewModel = transactionsViewModel,
                    cartViewModel = cartViewModel
                    )
            }
        },
        bottomBar = {
            if (showBottomBar.value)
                BottomBar(selectedPage, navHostController, onClick = { searchQuery.value = "" }, authViewModel)
        }
    )
}

