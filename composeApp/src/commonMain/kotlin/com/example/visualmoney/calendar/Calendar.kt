package com.example.visualmoney.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.EuroSymbol
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.SearchBar
import com.example.visualmoney.SearchResultRow
import com.example.visualmoney.SearchResultRowUi
import com.example.visualmoney.assetDetails.AssetLogoContainer
import com.example.visualmoney.core.DateInputTextField
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.logoUrl
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.IconWithContainerSmall
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.format
import com.example.visualmoney.home.primaryGradient
import com.example.visualmoney.home.theme
import com.example.visualmoney.newAsset.event.AssetInputEvent
import com.example.visualmoney.newAsset.event.ManualAssetInputEvent
import com.example.visualmoney.newAsset.state.AssetInputState
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import visualmoney.composeapp.generated.resources.Res
import visualmoney.composeapp.generated.resources.arrow_back
import visualmoney.composeapp.generated.resources.calendar
import visualmoney.composeapp.generated.resources.chevron_left
import visualmoney.composeapp.generated.resources.chevron_right
import visualmoney.composeapp.generated.resources.close
import visualmoney.composeapp.generated.resources.edit_variant
import visualmoney.composeapp.generated.resources.plus
import visualmoney.composeapp.generated.resources.search
import kotlin.math.min
import kotlin.time.Clock

private val theme @Composable get() = LocalAppTheme.current

// ---------- Models ----------
data class ReminderUi(
    val id: String,
    val title: String,
    val subtitle: String,
    val date: LocalDate,
    val timeLabel: String, // e.g. "09:30"
    val amountLabel: String? = null, // e.g. "$120.00"
    val status: ReminderStatus = ReminderStatus.UPCOMING
)

enum class ReminderStatus { UPCOMING, PAID, MISSED }

fun LocalDate.Companion.now(): LocalDate =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

fun YearMonth.Companion.now(): YearMonth = YearMonth(LocalDate.now().year, LocalDate.now().month)

/**
 * Returns a new [YearMonth] with the given number of months added or subtracted.
 * It has to be adjusted, to account for month 1 and 12
 */
fun YearMonth.minusMonths(months: Int): YearMonth = this.minus(months, DateTimeUnit.MONTH)
fun YearMonth.plusMonths(months: Int): YearMonth = this.plus(months, DateTimeUnit.MONTH)
fun YearMonth.atDay(day: Int): LocalDate = LocalDate(
    month = month.number,
    day = day,
    year = year
)

fun YearMonth.lengthOfMonth(): Int = this.numberOfDays
fun LocalDate.plusDays(days: Int): LocalDate = this.plus(days, DateTimeUnit.DAY)
fun LocalDate.minusDays(days: Int): LocalDate = this.minus(days, DateTimeUnit.DAY)

// ---------- Screen ----------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    viewModel: CalendarScreenViewModel,
    initialMonth: YearMonth = YearMonth.now(),
    reminders: List<ReminderUi> = sampleReminders(),
    onBack: () -> Unit = {},
    onOpenReminder: (ReminderUi) -> Unit = {},
) {
    var month by remember { mutableStateOf(initialMonth) }
    val today = remember { LocalDate.now() }
    var selectedDate by remember { mutableStateOf(today) }

    val monthDays = remember(month) { buildMonthGrid(month, startOfWeek = DayOfWeek.MONDAY) }
    val remindersForSelected = remember(reminders, selectedDate) {
        reminders.filter { it.date == selectedDate }.sortedBy { it.timeLabel }
    }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showNewReminderSheet by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing)
        ) {
            TopNavigationBar(
                title = "Calendar",
                subtitle = "Your reminders & events",
                onBack = onBack
            )

            if (showNewReminderSheet) {
                NewReminderSheet(sheetState, state = viewModel)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
                contentPadding = PaddingValues(
                    top = theme.dimension.pagePadding,
                    bottom = theme.dimension.pagePadding
                )
            ) {

                item {
                    CalendarCard(
                        month = month,
                        monthDays = monthDays,
                        selectedDate = selectedDate,
                        today = today,
                        reminders = reminders,
                        onPrevMonth = { month = month.minusMonths(1) },
                        onNextMonth = { month = month.plusMonths(1) },
                        onSelectDate = { selectedDate = it }
                    )
                }

//            item {
//                AddReminderCta(
//                    title = "Add reminder",
//                    subtitle = "Bills, dividends, earnings, alerts…",
//                    onClick = { onAddReminder(selectedDate) }
//                )
//            }

                item {
                    SelectedDateHeader(
                        selectedDate = selectedDate,
                        count = remindersForSelected.size
                    )
                }

                if (remindersForSelected.isEmpty()) {
                    item { EmptyStateCard(selectedDate = selectedDate) }
                } else {
                    items(remindersForSelected, key = { it.id }) { r ->
                        ReminderRow(
                            item = r,
                            onClick = { onOpenReminder(r) }
                        )
                    }
                }
            }
        }
    }
}

// ---------- Top header ----------
@Composable
private fun CalendarTopHeader(
    title: String,
    onBack: () -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            IconWithContainer(
                onClick = onBack,
                icon = painterResource(Res.drawable.arrow_back),
                containerColor = theme.colors.container
            )
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)) {
                Text(
                    text = title,
                    style = theme.typography.bodyMediumMedium,
                    color = theme.colors.onSurface
                )
                Text(
                    text = "Your reminders & events",
                    style = theme.typography.bodySmall,
                    color = theme.colors.greyTextColor
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)) {
            IconWithContainer(
                onClick = onAdd,
                icon = painterResource(Res.drawable.plus),
            )
        }
    }
}

fun LocalDate.format(): String {
    val format = LocalDate.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        dayOfMonth()
        char(' ')
    }

    return format.format(this)
}

// ---------- Calendar card ----------
@Composable
private fun CalendarCard(
    month: YearMonth,
    monthDays: List<LocalDate?>,
    selectedDate: LocalDate,
    today: LocalDate,
    reminders: List<ReminderUi>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val monthLabel = remember(month) {
        month.atDay(1).format()
    }

    CardContainer(
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconWithContainerSmall(
                    icon = painterResource(Res.drawable.calendar),
                    contentDescription = "Calendar"
                )
                Text(
                    text = monthLabel,
                    style = theme.typography.titleSmallMedium,
                    color = theme.colors.onSurface,
                    modifier = Modifier.padding(start = theme.dimension.mediumSpacing)
                )
                Spacer(Modifier.weight(1f))
                IconWithContainer(
                    onClick = onPrevMonth,
                    icon = painterResource(Res.drawable.chevron_left)
                )
                Spacer(Modifier.size(theme.dimension.closeSpacing))
                IconWithContainer(
                    onClick = onPrevMonth,
                    icon = painterResource(Res.drawable.chevron_right)
                )
            }

            WeekdayRow(startOfWeek = DayOfWeek.MONDAY)

            // 7 columns grid (6 rows max = 42 cells)
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)) {
                monthDays.chunked(7).forEach { week ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                    ) {
                        week.forEach { date ->
                            val hasReminder = remember(reminders, date) {
                                date != null && reminders.any { it.date == date }
                            }
                            DayCell(
                                date = date,
                                isSelected = date == selectedDate,
                                isToday = date == today,
                                hasReminder = hasReminder,
                                inCurrentMonth = date?.month == month.month,
                                onClick = { if (date != null) onSelectDate(date) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun WeekdayRow(
    startOfWeek: DayOfWeek,
    modifier: Modifier = Modifier
) {
    val days = remember(startOfWeek) {
        val ordered = DayOfWeek.entries.toMutableList()
        while (ordered.first() != startOfWeek) ordered.add(ordered.removeAt(0))
        ordered
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {
        days.forEach { dow ->
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = dow.getDisplayName(),
                    style = theme.typography.bodySmallMedium,
                    color = theme.colors.greyScale.c60
                )
            }
        }
    }
}

fun DayOfWeek.getDisplayName(): String {
    return when (this) {
        DayOfWeek.MONDAY -> "Mon"
        DayOfWeek.TUESDAY -> "Tue"
        DayOfWeek.WEDNESDAY -> "Wed"
        DayOfWeek.THURSDAY -> "Thu"
        DayOfWeek.FRIDAY -> "Fri"
        DayOfWeek.SATURDAY -> "Sat"
        DayOfWeek.SUNDAY -> "Sun"
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    isSelected: Boolean,
    isToday: Boolean,
    hasReminder: Boolean,
    inCurrentMonth: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val baseTextColor = when {
        date == null -> Color.Transparent
        !inCurrentMonth -> theme.colors.greyScale.c50
        else -> theme.colors.onSurface
    }

    val container by animateColorAsState(
        targetValue = when {
            date == null -> Color.Transparent
            isSelected -> theme.colors.primary.c50
            isToday -> theme.colors.surface
            else -> theme.colors.surface
        },
        label = "dayContainer"
    )

    val borderColor = when {
        date == null -> Color.Transparent
        isSelected -> Color.Transparent
        isToday -> theme.colors.primary.c50
        else -> theme.colors.border
    }

    val textColor = when {
        date == null -> Color.Transparent
        isSelected -> theme.colors.onSurface
        else -> baseTextColor
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(theme.dimension.smallRadius))
            .background(container)
            .border(1.dp, borderColor, RoundedCornerShape(theme.dimension.smallRadius))
            .clickable(enabled = date != null) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = theme.typography.bodySmallMedium,
                    color = textColor
                )
                Spacer(Modifier.height(4.dp))
                // Tiny indicator dot for reminders
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                !hasReminder -> Color.Transparent
                                isSelected -> theme.colors.onSurface
                                else -> theme.colors.primary.c50
                            }
                        )
                )
            }
        }
    }
}

// ---------- CTA ----------
@Composable
private fun AddReminderCta(
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(theme.dimension.defaultRadius))
            .background(primaryGradient),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                    .background(theme.colors.container),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Alarm,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(theme.dimension.largeSpacing)
                        .size(theme.dimension.iconSize)
                )
            }

            Spacer(Modifier.size(theme.dimension.mediumSpacing))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = theme.typography.titleSmallMedium)
                Text(subtitle, style = theme.typography.bodySmall, color = theme.colors.onSurface)
            }

            Icon(
                Icons.Rounded.NorthEast,
                contentDescription = "Open",
                modifier = Modifier.size(theme.dimension.iconSize)
            )
        }
    }
}

// ---------- Selected date header ----------
@Composable
private fun SelectedDateHeader(
    selectedDate: LocalDate,
    count: Int,
    modifier: Modifier = Modifier
) {
    val label = remember(selectedDate) {
        selectedDate.format()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = theme.typography.titleSmallMedium,
            color = theme.colors.onSurface
        )
        Spacer(Modifier.weight(1f))
        Surface(
            shape = RoundedCornerShape(theme.dimension.verySmallRadius),
            color = theme.colors.greyScale.c10
        ) {
            Text(
                text = "$count",
                style = theme.typography.bodySmallMedium,
                color = theme.colors.greyScale.c60,
                modifier = Modifier.padding(
                    horizontal = theme.dimension.closeSpacing,
                    vertical = theme.dimension.veryCloseSpacing
                )
            )
        }
    }
}

// ---------- Empty state ----------
@Composable
private fun EmptyStateCard(
    selectedDate: LocalDate,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        border = BorderStroke(1.dp, theme.colors.border),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface)
    ) {
        Column(
            modifier = Modifier.padding(theme.dimension.largeSpacing),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
        ) {
            Text(
                text = "No reminders",
                style = theme.typography.titleSmallMedium
            )
            Text(
                text = "You’re clear for this day. Add a bill or alert to stay on track.",
                style = theme.typography.bodySmall,
                color = theme.colors.greyTextColor
            )
        }
    }
}

// ---------- Reminder row ----------
@Composable
private fun ReminderRow(
    item: ReminderUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(theme.dimension.defaultRadius),
        border = BorderStroke(1.dp, theme.colors.border),
        elevation = CardDefaults.cardElevation(0.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = theme.colors.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(theme.dimension.mediumSpacing),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(theme.colors.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Rounded.Alarm,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(theme.dimension.mediumSpacing)
                        .size(theme.dimension.smallIconSize)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
            ) {
                Text(
                    text = item.title,
                    style = theme.typography.titleSmallMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${item.timeLabel} • ${item.subtitle}",
                    style = theme.typography.bodySmall,
                    color = theme.colors.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                if (item.amountLabel != null) {
                    Text(
                        text = item.amountLabel,
                        style = theme.typography.titleSmallMedium
                    )
                } else {
                    Text(
                        text = " ",
                        style = theme.typography.titleSmallMedium
                    )
                }

                val statusColor = when (item.status) {
                    ReminderStatus.UPCOMING -> theme.colors.greenScale.c50
                    ReminderStatus.PAID -> theme.colors.greyScale.c60
                    ReminderStatus.MISSED -> theme.colors.error
                }
                Text(
                    text = item.status.name.lowercase().replaceFirstChar { it.uppercase() },
                    style = theme.typography.bodySmallMedium,
                    color = statusColor
                )
            }
        }
    }
}

// ---------- Helpers ----------
private fun buildMonthGrid(
    month: YearMonth,
    startOfWeek: DayOfWeek = DayOfWeek.MONDAY
): List<LocalDate?> {
    val first = month.atDay(1)
    val daysInMonth = month.lengthOfMonth()

    // 0..6 offset for the first day cell
    val firstDowIndex = ((first.dayOfWeek.isoDayNumber - startOfWeek.isoDayNumber) + 7) % 7
    val totalCells = 42 // 6 rows * 7 columns

    val result = ArrayList<LocalDate?>(totalCells)
    repeat(firstDowIndex) { result.add(null) }

    for (d in 1..daysInMonth) {
        result.add(month.atDay(d))
    }

    while (result.size < totalCells) result.add(null)
    return result
}

private fun sampleReminders(): List<ReminderUi> {
    val today = LocalDate.now()
    return listOf(
        ReminderUi(
            id = "1",
            title = "Rent payment",
            subtitle = "Apartment",
            date = today,
            timeLabel = "09:00",
            amountLabel = "$1,250.00",
            status = ReminderStatus.UPCOMING
        ),
        ReminderUi(
            id = "2",
            title = "AAPL earnings call",
            subtitle = "Watchlist",
            date = today.plusDays(1),
            timeLabel = "16:30",
            amountLabel = null,
            status = ReminderStatus.UPCOMING
        ),
        ReminderUi(
            id = "3",
            title = "Dividend",
            subtitle = "MSFT",
            date = today.plusDays(2),
            timeLabel = "10:00",
            amountLabel = "$42.10",
            status = ReminderStatus.UPCOMING
        ),
        ReminderUi(
            id = "4",
            title = "Credit card",
            subtitle = "Statement due",
            date = today.minusDays(1),
            timeLabel = "12:00",
            amountLabel = "$320.44",
            status = ReminderStatus.MISSED
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReminderSheet(
    sheetState: SheetState,
    state: ReminderInputState,
    onEvent: (ReminderInputEvent) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) = with(state) {
    val lazyListState = rememberLazyListState()

    ModalBottomSheet(
        modifier = modifier,
        sheetState = sheetState,
        onDismissRequest = { onDismiss() },
        dragHandle = {},
        containerColor = theme.colors.container,
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Add a new reminder",
                        style = theme.typography.titleSmall,
                        color = theme.colors.onSurface
                    )
                    IconWithContainer(
                        icon = painterResource(Res.drawable.close), onClick = onDismiss
                    )
                }

                state.selectedAsset?.let {
                    SelectedAssetReminderField(asset = it)
                } ?: InputTextField(
                    readOnly = true,
                    borderAlwaysVisible = true,
                    placeholder = "Select an asset from your portfolio",
                    leadingIcon = {
                        Icon(
                            painterResource(Res.drawable.search),
                            null,
                            modifier = Modifier.size(theme.dimension.smallIconSize),
                            tint = theme.colors.greyTextColor
                        )
                    })

                InputTextField(
                    label = "Note",
                    value = state.notes,
                    onValueChange = {
                        onEvent(ReminderInputEvent.NoteChanged(it))
                    })

                DateInputTextField(
                    label = "Transaction date",
                    value = state.date,
                    onValueChange = { onEvent(ReminderInputEvent.DateChanged(it)) })

            }
        }
    }
}

@Composable
fun SelectedAssetReminderField(modifier: Modifier = Modifier, asset: PortfolioAsset) {
    val boxModifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
        .background(theme.colors.surface)
        .border(
            border = borderStroke,
            shape = RoundedCornerShape(theme.dimension.defaultRadius)
        )
        .padding(horizontal = 12.dp, vertical = 10.dp)

    Column(
        modifier =
            modifier
                .fillMaxWidth().padding(bottom = theme.dimension.veryCloseSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
    ) {
        CardContainer {
            Box(contentAlignment = Alignment.CenterStart, modifier = boxModifier) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing)
                ) {
                    AssetLogoContainer(
                        asset.logoUrl,
                        symbol = asset.symbol,
                        size = theme.dimension.smallIconSize
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                    ) {
                        Text(
                            asset.name,
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface
                        )
                        Text(
                            asset.symbol,
                            style = theme.typography.bodyMedium,
                            color = theme.colors.greyTextColor
                        )
                    }
                    Spacer(Modifier.weight(1f))

                    Icon(
                        painterResource(Res.drawable.edit_variant),
                        null,
                        tint = theme.colors.greyTextColor,
                        modifier = Modifier.size(theme.dimension.smallIconSize),

                        )


                }

            }
        }

    }
}
