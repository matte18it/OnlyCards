package unical.enterpriceapplication.onlycards.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import unical.enterpriceapplication.onlycards.model.dao.ProductDao
import unical.enterpriceapplication.onlycards.model.dao.ProductTypeDao
import unical.enterpriceapplication.onlycards.model.dao.SearchHistoryDao
import unical.enterpriceapplication.onlycards.model.dao.UserDao
import unical.enterpriceapplication.onlycards.model.dao.UserWishlistDao
import unical.enterpriceapplication.onlycards.model.dao.WishlistDao
import unical.enterpriceapplication.onlycards.model.dao.WishlistProductDao
import unical.enterpriceapplication.onlycards.model.entity.AuthUser
import unical.enterpriceapplication.onlycards.model.entity.Product
import unical.enterpriceapplication.onlycards.model.entity.ProductType
import unical.enterpriceapplication.onlycards.model.entity.SearchHistory
import unical.enterpriceapplication.onlycards.model.entity.UserWishlist
import unical.enterpriceapplication.onlycards.model.entity.Wishlist
import unical.enterpriceapplication.onlycards.model.entity.WishlistProduct
import unical.enterpriceapplication.onlycards.model.entity.WishlistProductCrossRef

@Database(entities = [SearchHistory::class, AuthUser::class, ProductType::class, Product::class, Wishlist::class, UserWishlist::class, WishlistProduct::class, WishlistProductCrossRef::class], version = 4, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun authUserDao(): UserDao
    abstract fun productTypeDao(): ProductTypeDao
    abstract fun productDao(): ProductDao
    abstract fun wishlistDao(): WishlistDao
    abstract fun userWishlistDao(): UserWishlistDao
    abstract fun wishlistProductDao(): WishlistProductDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var localInstance = instance
                if (localInstance == null) {
                    localInstance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "onlyCards-database"
                    ).fallbackToDestructiveMigration().build()
                    instance = localInstance
                }
                return localInstance
            }
        }
    }
}