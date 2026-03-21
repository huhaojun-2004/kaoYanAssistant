package com.example.kaoyanassistant.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import com.example.kaoyanassistant.ui.theme.Cloud
import com.example.kaoyanassistant.ui.theme.Paper
import com.example.kaoyanassistant.ui.theme.Pine
import com.example.kaoyanassistant.ui.theme.Slate
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

data class ChartBarEntry(
    val label: String,
    val value: Float,
    val color: Color,
    val supportingText: String = "",
)

data class TrendEntry(
    val label: String,
    val value: Float,
)

data class PieSliceEntry(
    val subjectId: Long,
    val label: String,
    val value: Float,
    val color: Color,
    val supportingText: String,
    val calloutText: String = "",
)

private data class PieCallout(
    val index: Int,
    val rightSide: Boolean,
    val anchor: Offset,
    val lineEnd: Offset,
    val labelWidth: Float,
    val labelX: Float,
    val labelY: Float,
)

private enum class PieQuadrant {
    TOP_LEFT,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_RIGHT,
}

@Composable
fun HeroCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    accent: Color = Pine,
    compact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(20.dp),
    ) {
        val contentPadding = if (compact) 12.dp else 16.dp
        val spacing = if (compact) 6.dp else 10.dp
        val titleStyle = if (compact) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
        val subtitleStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = accent.copy(alpha = 0.08f))
                .padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(spacing),
            content = {
                Text(
                    text = title,
                    style = titleStyle,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = subtitleStyle,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    )
                }
                content()
            },
        )
    }
}

@Composable
fun MetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    compact: Boolean = false,
) {
    val verticalPadding = if (compact) 6.dp else 8.dp
    val horizontalPadding = if (compact) 8.dp else 10.dp
    val labelStyle = if (compact) MaterialTheme.typography.bodySmall else MaterialTheme.typography.bodyMedium
    val valueStyle = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Cloud),
        shape = RoundedCornerShape(14.dp),
        border = borderColor
            .takeIf { it != Color.Transparent }
            ?.let { BorderStroke(1.dp, it) },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = horizontalPadding, vertical = verticalPadding),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = label,
                style = labelStyle,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f),
            )
            Text(
                text = value,
                style = valueStyle,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun EmptyStateCard(
    text: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Cloud),
        shape = RoundedCornerShape(18.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun StudyPieChart(
    entries: List<PieSliceEntry>,
    modifier: Modifier = Modifier,
    emptyText: String = "暂无统计数据",
    onDeleteEntry: ((PieSliceEntry) -> Unit)? = null,
) {
    if (entries.isEmpty()) {
        EmptyStateCard(text = emptyText, modifier = modifier)
        return
    }

    val total = entries.sumOf { it.value.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            PieChartWithCallouts(entries = entries, total = total)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                entries.sortedByDescending { it.value }.forEach { entry ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .background(entry.color, RoundedCornerShape(999.dp)),
                            )
                            Text(
                                text = entry.label,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = entry.supportingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                            if (onDeleteEntry != null) {
                                IconButton(
                                    onClick = { onDeleteEntry(entry) },
                                    modifier = Modifier.size(28.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = "删除该科目学习时间",
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PieChartWithCallouts(
    entries: List<PieSliceEntry>,
    total: Float,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val labelWidth = 66.dp
    val labelHeight = 20.dp
    val chartHeight = 252.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(chartHeight),
    ) {
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { chartHeight.toPx() }
        val labelWidthPx = with(density) { labelWidth.toPx() }
        val labelHeightPx = with(density) { labelHeight.toPx() }
        val callouts = remember(entries, widthPx, heightPx, total) {
            buildPieCallouts(
                entries = entries,
                total = total,
                widthPx = widthPx,
                heightPx = heightPx,
                labelWidthPx = labelWidthPx,
                labelHeightPx = labelHeightPx,
            )
        }
        val radius = remember(widthPx, heightPx) {
            min(widthPx * 0.2f, heightPx * 0.24f)
        }
        val center = Offset(widthPx / 2f, heightPx / 2f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            var startAngle = -90f
            entries.forEach { entry ->
                val sweep = (entry.value / total) * 360f
                drawArc(
                    color = entry.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = true,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2f, radius * 2f),
                )
                startAngle += sweep
            }

            callouts.forEach { callout ->
                val color = entries[callout.index].color
                drawLine(
                    color = color.copy(alpha = 0.7f),
                    start = callout.anchor,
                    end = callout.lineEnd,
                    strokeWidth = 3f,
                )
                drawCircle(
                    color = color,
                    radius = 5f,
                    center = callout.anchor,
                )
            }
        }

        callouts.forEach { callout ->
            val entry = entries[callout.index]
            Column(
                modifier = Modifier
                    .width(with(density) { callout.labelWidth.toDp() })
                    .absoluteOffset(
                        x = with(density) { callout.labelX.toDp() },
                        y = with(density) { callout.labelY.toDp() },
                    ),
                horizontalAlignment = if (callout.rightSide) Alignment.Start else Alignment.End,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = entry.label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                )
                if (entry.calloutText.isNotBlank()) {
                    Text(
                        text = entry.calloutText,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, lineHeight = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                        maxLines = 1,
                        modifier = Modifier.absoluteOffset(y = (-1).dp),
                    )
                }
            }
        }
    }
}

private fun buildPieCallouts(
    entries: List<PieSliceEntry>,
    total: Float,
    widthPx: Float,
    heightPx: Float,
    labelWidthPx: Float,
    labelHeightPx: Float,
): List<PieCallout> {
    val centerX = widthPx / 2f
    val centerY = heightPx / 2f
    val radius = min(widthPx * 0.2f, heightPx * 0.24f)
    val topBound = 10f
    val bottomBound = heightPx - labelHeightPx - 10f
    val minLabelWidthPx = labelHeightPx * 1.55f
    val minAngleGap = 12f

    data class RawCallout(
        val index: Int,
        val baseAngle: Float,
        val anchor: Offset,
        val labelWidth: Float,
        val quadrant: PieQuadrant,
    )

    val preliminaries = buildList {
        var startAngle = -90f
        entries.forEachIndexed { index, entry ->
            val sweep = (entry.value / total) * 360f
            val midAngle = startAngle + sweep / 2f
            val radians = Math.toRadians(midAngle.toDouble())
            val directionX = cos(radians).toFloat()
            val directionY = sin(radians).toFloat()
            val anchor = Offset(
                x = centerX + directionX * (radius - 2f),
                y = centerY + directionY * (radius - 2f),
            )
            val normalizedAngle = normalizeAngle(midAngle)
            val quadrant = when {
                normalizedAngle >= 270f || normalizedAngle < 90f -> {
                    if (normalizedAngle >= 270f) PieQuadrant.TOP_RIGHT else PieQuadrant.BOTTOM_RIGHT
                }
                normalizedAngle < 180f -> PieQuadrant.BOTTOM_LEFT
                else -> PieQuadrant.TOP_LEFT
            }
            add(
                RawCallout(
                    index = index,
                    baseAngle = normalizedAngle,
                    anchor = anchor,
                    labelWidth = (
                        entry.label.length * labelHeightPx * 0.42f + labelHeightPx * 0.9f
                        ).coerceIn(minLabelWidthPx, labelWidthPx),
                    quadrant = quadrant,
                ),
            )
            startAngle += sweep
        }
    }

    fun angleBounds(quadrant: PieQuadrant): Pair<Float, Float> {
        return when (quadrant) {
            PieQuadrant.TOP_RIGHT -> 274f to 356f
            PieQuadrant.BOTTOM_RIGHT -> 4f to 86f
            PieQuadrant.BOTTOM_LEFT -> 94f to 176f
            PieQuadrant.TOP_LEFT -> 184f to 266f
        }
    }

    fun adjustAngles(quadrant: PieQuadrant): Map<Int, Float> {
        val targets = preliminaries
            .filter { it.quadrant == quadrant }
            .sortedBy { it.baseAngle }
        if (targets.isEmpty()) return emptyMap()

        val (minAngle, maxAngle) = angleBounds(quadrant)
        val adjusted = FloatArray(targets.size)

        targets.forEachIndexed { index, target ->
            val base = target.baseAngle.coerceIn(minAngle, maxAngle)
            adjusted[index] = if (index == 0) {
                base
            } else {
                maxOf(base, adjusted[index - 1] + minAngleGap)
            }
        }

        if (adjusted.last() > maxAngle) {
            val overflow = adjusted.last() - maxAngle
            adjusted.indices.forEach { index ->
                adjusted[index] -= overflow
            }
        }

        if (adjusted.first() < minAngle) {
            val underflow = minAngle - adjusted.first()
            adjusted.indices.forEach { index ->
                adjusted[index] += underflow
            }
        }

        return targets.mapIndexed { index, target ->
            target.index to adjusted[index].coerceIn(minAngle, maxAngle)
        }.toMap()
    }

    val angleMap = buildMap {
        PieQuadrant.entries.forEach { quadrant ->
            putAll(adjustAngles(quadrant))
        }
    }

    return preliminaries.map { raw ->
        val adjustedAngle = angleMap[raw.index] ?: raw.baseAngle
        val radians = Math.toRadians(adjustedAngle.toDouble())
        val directionX = cos(radians).toFloat()
        val directionY = sin(radians).toFloat()
        val rightSide = directionX >= 0f
        val crowding = angleMap.values.count {
            kotlin.math.abs(angleDistance(it, adjustedAngle)) < 16f
        } - 1
        val widthReduction = ((labelWidthPx - raw.labelWidth) / labelWidthPx).coerceIn(0f, 1f) * 10f
        val distance = (when {
            crowding <= 0 -> 64f
            crowding == 1 -> 82f
            else -> 98f + crowding * 12f
        } - widthReduction) * 1.5f
        val lineEnd = Offset(
            x = (centerX + directionX * (radius + distance)).coerceIn(8f, widthPx - 8f),
            y = (centerY + directionY * (radius + distance)).coerceIn(
                topBound + labelHeightPx / 2f,
                bottomBound + labelHeightPx / 2f,
            ),
        )
        val labelX = if (rightSide) {
            (lineEnd.x + 8f).coerceIn(4f, widthPx - raw.labelWidth - 4f)
        } else {
            (lineEnd.x - raw.labelWidth - 8f).coerceIn(4f, widthPx - raw.labelWidth - 4f)
        }
        PieCallout(
            index = raw.index,
            rightSide = rightSide,
            anchor = raw.anchor,
            lineEnd = lineEnd,
            labelWidth = raw.labelWidth,
            labelX = labelX,
            labelY = (lineEnd.y - labelHeightPx / 2f).coerceIn(topBound, bottomBound),
        )
    }
}

private fun normalizeAngle(angle: Float): Float {
    val normalized = angle % 360f
    return if (normalized < 0f) normalized + 360f else normalized
}

private fun angleDistance(a: Float, b: Float): Float {
    val diff = kotlin.math.abs(a - b) % 360f
    return min(diff, 360f - diff)
}

@Composable
fun StudyBarChart(
    entries: List<ChartBarEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        EmptyStateCard(text = "暂无统计数据", modifier = modifier)
        return
    }

    val maxValue = entries.maxOf { it.value }.coerceAtLeast(1f)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp),
            ) {
                val chartHeight = size.height - 24.dp.toPx()
                val chartWidth = size.width
                val gap = 18.dp.toPx()
                val barWidth = (chartWidth - gap * (entries.size + 1)) / entries.size

                repeat(4) { index ->
                    val y = chartHeight / 3 * index
                    drawLine(
                        color = Slate.copy(alpha = 0.12f),
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 2f,
                    )
                }

                entries.forEachIndexed { index, entry ->
                    val left = gap + index * (barWidth + gap)
                    val barHeight = chartHeight * (entry.value / maxValue)
                    drawRoundRect(
                        color = entry.color,
                        topLeft = Offset(left, chartHeight - barHeight),
                        size = androidx.compose.ui.geometry.Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(18f, 18f),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                entries.forEach { entry ->
                    Text(
                        text = entry.label,
                        modifier = Modifier.width(62.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.74f),
                        maxLines = 1,
                    )
                }
            }

            if (entries.any { it.supportingText.isNotBlank() }) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    entries.forEach { entry ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(entry.color, RoundedCornerShape(999.dp)),
                                )
                                Text(
                                    text = entry.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                            Text(
                                text = entry.supportingText,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TrendLineChart(
    entries: List<TrendEntry>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) {
        EmptyStateCard(text = "暂无趋势数据", modifier = modifier)
        return
    }

    val maxValue = entries.maxOf { it.value }.coerceAtLeast(1f)
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Paper),
        shape = RoundedCornerShape(28.dp),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            ) {
                val widthStep = if (entries.size == 1) size.width else size.width / (entries.size - 1)
                val path = Path()

                entries.forEachIndexed { index, entry ->
                    val x = widthStep * index
                    val y = size.height - ((entry.value / maxValue) * (size.height - 20.dp.toPx()))
                    if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    drawCircle(
                        color = Pine,
                        radius = 7.dp.toPx(),
                        center = Offset(x, y),
                    )
                }

                drawPath(
                    path = path,
                    color = Pine,
                    style = Stroke(width = 6f),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                entries.forEach { entry ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = entry.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                        )
                        Text(
                            text = "${(entry.value * 10f).roundToInt() / 10f}h",
                            style = MaterialTheme.typography.labelLarge,
                            color = Pine,
                        )
                    }
                }
            }
        }
    }
}
