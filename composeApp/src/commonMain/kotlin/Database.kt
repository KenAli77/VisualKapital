import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.SQLiteDriver
import com.example.visualmoney.data.local.CachedQuoteDao
import com.example.visualmoney.data.local.CachedQuoteEntity
import com.example.visualmoney.data.local.InvestmentReminderDao
import com.example.visualmoney.data.local.InvestmentReminderEntity
import com.example.visualmoney.data.local.ManualInvestmentDao
import com.example.visualmoney.data.local.ManualInvestmentEntity
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.PortfolioAssetDAO
import com.example.visualmoney.data.local.PortfolioBuyDao
import com.example.visualmoney.data.local.PortfolioBuyEntity
import com.example.visualmoney.data.local.RoomConverters
import com.example.visualmoney.data.local.TrackedAssetDao
import com.example.visualmoney.data.local.TrackedAssetEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO


@Database(
    entities = [
        TrackedAssetEntity::class,
        PortfolioBuyEntity::class,
        ManualInvestmentEntity::class,
        InvestmentReminderEntity::class,
        CachedQuoteEntity::class,
        PortfolioAsset::class
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(RoomConverters::class)
@ConstructedBy(AppDatabaseConstructor::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackedAssetDao(): TrackedAssetDao
    abstract fun portfolioBuyDao(): PortfolioBuyDao
    abstract fun portfolioAssetDao(): PortfolioAssetDAO
    abstract fun manualInvestmentDao(): ManualInvestmentDao
    abstract fun investmentReminderDao(): InvestmentReminderDao
    abstract fun cachedQuoteDao(): CachedQuoteDao
}

@Suppress("KotlinNoActualForExpect","NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}

expect fun getDatabaseBuilder(): RoomDatabase.Builder<AppDatabase>
expect fun getSQLDriver():SQLiteDriver
