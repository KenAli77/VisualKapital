package com.example.visualmoney.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.visualmoney.LocalAppTheme
import com.example.visualmoney.SearchBar
import com.example.visualmoney.SearchResultRow
import com.example.visualmoney.assetDetails.AssetLogoContainer
import com.example.visualmoney.core.DateInputTextField
import com.example.visualmoney.core.InputTextField
import com.example.visualmoney.core.LargeButton
import com.example.visualmoney.core.ListDivider
import com.example.visualmoney.core.TopNavigationBar
import com.example.visualmoney.data.local.InvestmentReminderEntity
import com.example.visualmoney.data.local.PortfolioAsset
import com.example.visualmoney.data.local.logoUrl
import com.example.visualmoney.greyTextColor
import com.example.visualmoney.home.CardContainer
import com.example.visualmoney.home.IconWithContainer
import com.example.visualmoney.home.IconWithContainerSmall
import com.example.visualmoney.home.borderStroke
import com.example.visualmoney.home.primaryGradient
import com.example.visualmoney.home.theme
import com.example.visualmoney.toSearchResultRowUi
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.YearMonth
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
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
import visualmoney.composeapp.generated.resources.trash
import kotlin.time.Clock

private val theme @Composable get() = LocalAppTheme.current

// ---------- Models ----------
data class ReminderUi(
    val id: String,
    val description: String,
    val asset: PortfolioAsset,
    val note: String,
    val date: LocalDate,
    val timeLabel: String, // e.g. "09:30"
    val isDone: Boolean
)

fun InvestmentReminderEntity.toReminderUI(asset: PortfolioAsset): ReminderUi {
    return ReminderUi(
        id = id,
        description = description,
        note = note,
        date = dueDate,
        asset = asset,
        timeLabel = dueDate.format(),
        isDone = isDone,
    )
}

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
    onBack: () -> Unit = {},
    onAddReminder:()->Unit = {},
) = with(viewModel) {
    var month by remember { mutableStateOf(initialMonth) }
    val today = remember { LocalDate.now() }

    val monthDays = remember(month) { buildMonthGrid(month, startOfWeek = DayOfWeek.MONDAY) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val focusManager = LocalFocusManager.current
    val localController = LocalSoftwareKeyboardController.current
    var showConfirmDelete by remember { mutableStateOf(false) }
    var reminderToDelete: ReminderUi? by remember { mutableStateOf(null) }

    if (showConfirmDelete) {
        ConfirmReminderDeleteAlert(
            onDismiss = {
                showConfirmDelete = false
                reminderToDelete = null
            },
            onConfirm = {
                showConfirmDelete = false
                reminderToDelete?.let {
                    onEvent(CalendarEvent.OnRemoveReminder(it.id))
                }
            }
        )
    }
    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
        ) {
            TopNavigationBar(
                title = "Calendar",
                subtitle = "Your reminders & events",
                onBack = onBack,
                hasAddAction = true,
                onAdd = {
                    onAddReminder()
                }
            )

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
                        selectedDate = state.selectedDate,
                        today = today,
                        reminders = state.remindersUI,
                        onPrevMonth = { month = month.minusMonths(1) },
                        onNextMonth = { month = month.plusMonths(1) },
                        onSelectDate = { onEvent(CalendarEvent.DateSelected(it)) }
                    )
                }

                if (state.remindersForSelected.isEmpty()) {
                    item { EmptyStateCard(title ="No reminders", subtitle = "You’re clear for this day. You have no reminders coming up.") }
                } else {
                    items(state.remindersForSelected, key = { it.id }) { r ->
                        ReminderRow(
                            item = r,
                            onRemove = {
                                reminderToDelete = r
                                showConfirmDelete = true
                            },
                            onDone = {
                                onEvent(CalendarEvent.OnReminderDoneChanged(r.id, it))
                            }
                            //   onClick = { onOpenReminder(r) }
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

val monthNames = listOf(
    "January",
    "February",
    "March",
    "April",
    "May",
    "June",
    "July",
    "August",
    "September",
    "October",
    "November",
    "December"
)

fun LocalDate.formatMonthAndYear(): String {
    val format = LocalDate.Format {
        monthName(MonthNames(monthNames))
        char(' ')
        year()
    }

    return format.format(this)
}

val weekDaysNames =
    listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

fun LocalDate.formatDayOfWeek(): String {
    val format = LocalDate.Format {
        dayOfWeek(DayOfWeekNames(weekDaysNames))
        char(' ')
        dayOfMonth()
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
        month.atDay(1).formatMonthAndYear()
    }

    CardContainer(
        modifier = modifier,
        containerColor = theme.colors.primary.c90
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
        selectedDate.formatDayOfWeek()
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = theme.typography.titleSmallMedium,
            color = theme.colors.onSurface
        )
        Text(
            text = "$count",
            style = theme.typography.bodyMediumStrong,
            color = theme.colors.greyScale.c60,
        )

    }
}

// ---------- Empty state ----------
@Composable
fun EmptyStateCard(
    modifier: Modifier = Modifier,
    title:String = "",
    subtitle:String = ""
) {
    Column(
        modifier = modifier.padding(vertical = theme.dimension.largeSpacing),
        verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
    ) {
        Text(
            text = title,
            style = theme.typography.titleSmallMedium,
            color = theme.colors.onSurface
        )
        Text(
            text = subtitle,
            style = theme.typography.bodySmall,
            color = theme.colors.greyTextColor
        )
    }

}

// ---------- Reminder row ----------
@Composable
private fun ReminderRow(
    item: ReminderUi,
    onDone: (Boolean) -> Unit = {},
    onRemove: () -> Unit = {},
    modifier: Modifier = Modifier
) {

    val swipeToDismissBoxState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            if (it == SwipeToDismissBoxValue.StartToEnd) onRemove()
            else if (it == SwipeToDismissBoxValue.EndToStart) onRemove()
            // Reset item when toggling done status
            false
        }
    )

    SwipeToDismissBox(state = swipeToDismissBoxState, backgroundContent = {
        when (swipeToDismissBoxState.dismissDirection) {
            SwipeToDismissBoxValue.StartToEnd -> {
                Icon(
                    painter = painterResource(Res.drawable.trash),
                    contentDescription = "Remove item",
                    modifier = Modifier
                        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                        .fillMaxSize()
                        .background(theme.colors.error)
                        .wrapContentSize(Alignment.CenterStart)
                        .padding(12.dp),
                    tint = Color.White
                )
            }

            SwipeToDismissBoxValue.EndToStart -> {
                Icon(
                    painter = painterResource(Res.drawable.trash),
                    contentDescription = "Remove item",
                    modifier = Modifier
                        .clip(RoundedCornerShape(theme.dimension.defaultRadius))
                        .fillMaxSize()
                        .background(theme.colors.error)
                        .wrapContentSize(Alignment.CenterEnd)
                        .padding(12.dp),
                    tint = Color.White
                )
            }

            else -> {}
        }
    }) {
        CardContainer(modifier = modifier, containerColor = theme.colors.surface) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(theme.dimension.largeSpacing),
                horizontalArrangement = Arrangement.spacedBy(theme.dimension.mediumSpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssetLogoContainer(item.asset.logoUrl, symbol = item.asset.symbol)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                ) {
                    Text(
                        text = item.description,
                        style = theme.typography.bodyMedium,
                        maxLines = 2,
                        color = theme.colors.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Text(
                        text = item.asset.name.takeIf { it.isNotEmpty() } ?: item.asset.symbol,
                        style = theme.typography.bodySmallStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = theme.colors.greyTextColor,
                        textDecoration = if (item.isDone) TextDecoration.LineThrough else TextDecoration.None
                    )
                }
                Checkbox(
                    modifier = Modifier.clip(RoundedCornerShape(theme.dimension.defaultRadius)),
                    checked = item.isDone,
                    colors = CheckboxDefaults.colors(
                        checkedColor = theme.colors.primary.c50,
                        uncheckedColor = theme.colors.greyTextColor
                    ),
                    onCheckedChange = {
                        onDone(it)
                    },
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewReminderScreen(
    viewModel: CalendarScreenViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) = with(viewModel) {
    val lazyListState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val localController = LocalSoftwareKeyboardController.current
    val assetSearchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var showAssetSearchSheet by remember { mutableStateOf(false) }

    LaunchedEffect(lazyListState.isScrollInProgress) {
        focusManager.clearFocus()
        localController?.hide()
    }
    if (showAssetSearchSheet) {
        PortfolioAssetSearchSheet(
            sheetState = assetSearchSheetState,
            results = viewModel.newReminderState.searchResults,
            query = newReminderState.query,
            onDismiss = {
                scope.launch {
                    assetSearchSheetState.hide()
                }.invokeOnCompletion {
                    showAssetSearchSheet = false
                }
            },
            onQueryEntered = {
                onEvent(
                    ReminderInputEvent.QueryChanged(it)
                )
            },
            onAssetSelected = {
                onEvent(ReminderInputEvent.SymbolChanged(it))
            }
        )
    }
    Box(
        modifier = modifier.fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                    localController?.hide()
                })
            },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = theme.dimension.mediumSpacing)
                .padding(horizontal = theme.dimension.pagePadding),
            verticalArrangement = Arrangement.spacedBy(theme.dimension.veryLargeSpacing),
        ) {
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
                    icon = painterResource(Res.drawable.close), onClick = onBack
                )
            }
            LazyColumn(
                modifier = Modifier.padding(vertical = theme.dimension.largeSpacing),
                state = lazyListState,
                verticalArrangement = Arrangement.spacedBy(theme.dimension.largeSpacing)
            ) {
                item {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(theme.dimension.veryCloseSpacing)
                    ) {
                        Text(
                            modifier = Modifier.padding(bottom = theme.dimension.veryCloseSpacing),
                            text = "Asset *",
                            style = theme.typography.bodySmallStrong,
                            color = theme.colors.onSurface
                        )
                        Row(modifier = Modifier.clickable {
                            showAssetSearchSheet = true
                        }) {
                            newReminderState.selectedAsset?.let {
                                SelectedAssetReminderField(asset = it)
                            } ?: InputTextField(
                                readOnly = true,
                                borderAlwaysVisible = true,
                                placeholder = "Pick an asset from your portfolio",
                                leadingIcon = {
                                    Icon(
                                        painterResource(Res.drawable.search),
                                        null,
                                        modifier = Modifier.size(theme.dimension.smallIconSize),
                                        tint = theme.colors.greyTextColor
                                    )
                                })
                        }
                    }
                }
                item {
                    DateInputTextField(
                        label = "Date",
                        value = newReminderState.date,
                        onValueChange = { onEvent(ReminderInputEvent.DateChanged(it)) })
                }
                item {
                    InputTextField(
                        label = "Description",
                        placeholder = "Eg. Rent payment",
                        required = true,
                        value = newReminderState.description,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        onValueChange = {
                            onEvent(ReminderInputEvent.DescriptionChanged(it))
                        })
                }
                item {
                    InputTextField(
                        label = "Note",
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Default),
                        value = newReminderState.notes,
                        onValueChange = {
                            onEvent(ReminderInputEvent.NoteChanged(it))
                        })
                }
                item {
                    Spacer(modifier = Modifier.height(theme.dimension.bottomBarHeight * 3))
                }
            }
        }
        LargeButton(
            modifier = Modifier.padding(horizontal = theme.dimension.pagePadding),
            text = "Save reminder",
            enabled = newReminderState.isValidForSubmit,
            onClick = {
                onEvent(ReminderInputEvent.Save)
                onBack()
            }
        )

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
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(theme.dimension.closeSpacing)
                    ) {
                        Text(
                            asset.symbol,
                            style = theme.typography.bodyMediumStrong,
                            color = theme.colors.onSurface,
                        )
                        Text(
                            asset.name,
                            Modifier.weight(1f),
                            style = theme.typography.bodyMedium,
                            color = theme.colors.greyTextColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioAssetSearchSheet(
    sheetState: SheetState,
    query: String = "",
    results: List<PortfolioAsset> = emptyList(),
    onQueryEntered: (String) -> Unit = {},
    onAssetSelected: (String) -> Unit = {},
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
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
                        "Pick an asset",
                        style = theme.typography.titleSmall,
                        color = theme.colors.onSurface
                    )
                    IconWithContainer(
                        icon = painterResource(Res.drawable.close), onClick = onDismiss
                    )
                }
                SearchBar(
                    query = query,
                    placeholder = "Search asset",
                    onQueryChange = { onQueryEntered(it) },
                )
                Text(
                    text = "Showing ${results.size} results",
                    style = theme.typography.bodySmall,
                    color = theme.colors.onSurface
                )
                CardContainer {
                    LazyColumn(
                        state = lazyListState,
                        modifier = Modifier,

                        ) {
                        itemsIndexed(results.map { it.toSearchResultRowUi }) { idx, row ->
                            SearchResultRow(
                                item = row, onClick = {
                                    onAssetSelected(row.symbol)
                                    onDismiss()
                                })
                            if (idx != results.lastIndex) {
                                ListDivider(modifier = Modifier.padding(horizontal = theme.dimension.pagePadding))
                            }
                        }
                    }
                }
            }
        }
    }
}