package com.dev.lbsuhack.feature.tournaments.ui.board

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.dev.lbsuhack.core.designsystem.theme.HackMonoTextStyle
import com.dev.lbsuhack.core.designsystem.theme.hack
import com.dev.lbsuhack.feature.tournaments.domain.TournamentBracket
import com.dev.lbsuhack.feature.tournaments.domain.TournamentMatch
import kotlin.math.roundToInt

private data class BoardMatchNode(
    val match: TournamentMatch,
    val boardX: Float,
    val boardY: Float,
)

private data class BoardRoundHeader(
    val title: String,
    val boardX: Float,
    val boardY: Float,
)

@Composable
internal fun TournamentBracketBoard(
    bracket: TournamentBracket,
    selectedMatchId: String?,
    onSelectMatch: (String) -> Unit,
    resetTrigger: Long,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current.density
    val cardWidthPx = 240f * density
    val cardHeightPx = 110f * density
    val colSpacingPx = 70f * density
    val rowSpacingPx = 20f * density
    val topPaddingPx = 54f * density

    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset(24f * density, 24f * density)) }

    LaunchedEffect(bracket.stage, resetTrigger) {
        scale = 1f
        panOffset = Offset(24f * density, 24f * density)
    }

    val (nodesByRound, roundHeaders) = remember(bracket, density) {
        val resultNodes = mutableListOf<List<BoardMatchNode>>()
        val resultHeaders = mutableListOf<BoardRoundHeader>()

        if (bracket.rounds.isEmpty()) {
            return@remember Pair(emptyList<List<BoardMatchNode>>(), emptyList<BoardRoundHeader>())
        }

        val startX = 20f * density
        val round0 = bracket.rounds.first()
        val round0Nodes = mutableListOf<BoardMatchNode>()

        resultHeaders.add(
            BoardRoundHeader(
                title = round0.title,
                boardX = startX,
                boardY = 16f * density,
            ),
        )

        for (m in round0.matches.indices) {
            val y = topPaddingPx + m * (cardHeightPx + rowSpacingPx)
            round0Nodes.add(
                BoardMatchNode(
                    match = round0.matches[m],
                    boardX = startX,
                    boardY = y,
                ),
            )
        }
        resultNodes.add(round0Nodes)

        var prevNodes = round0Nodes
        for (r in 1 until bracket.rounds.size) {
            val round = bracket.rounds[r]
            val roundX = startX + r * (cardWidthPx + colSpacingPx)
            val currentNodes = mutableListOf<BoardMatchNode>()

            resultHeaders.add(
                BoardRoundHeader(
                    title = round.title,
                    boardX = roundX,
                    boardY = 16f * density,
                ),
            )

            for (m in round.matches.indices) {
                val parent1Y = prevNodes.getOrNull(2 * m)?.boardY ?: 0f
                val parent2Y = prevNodes.getOrNull(2 * m + 1)?.boardY ?: parent1Y
                val y = (parent1Y + parent2Y) / 2f

                currentNodes.add(
                    BoardMatchNode(
                        match = round.matches[m],
                        boardX = roundX,
                        boardY = y,
                    ),
                )
            }
            resultNodes.add(currentNodes)
            prevNodes = currentNodes
        }

        Pair(resultNodes, resultHeaders)
    }

    val totalRounds = bracket.rounds.size
    val maxRoundMatches = bracket.rounds.maxOfOrNull { it.matches.size } ?: 0
    val boardContentWidthPx = remember(totalRounds, density) {
        if (totalRounds <= 0) 0f
        else 20f * density + totalRounds * (cardWidthPx + colSpacingPx)
    }
    val boardContentHeightPx = remember(maxRoundMatches, density) {
        if (maxRoundMatches <= 0) 0f
        else topPaddingPx + maxRoundMatches * (cardHeightPx + rowSpacingPx) + 80f * density
    }

    var viewportSize by remember { mutableStateOf(IntSize.Zero) }

    fun clampPan(offset: Offset, currentScale: Float): Offset {
        val vw = viewportSize.width.toFloat()
        val vh = viewportSize.height.toFloat()
        if (vw <= 0f || vh <= 0f || boardContentWidthPx <= 0f || boardContentHeightPx <= 0f) return offset

        val marginX = 48f * density
        val marginY = 48f * density
        val scaledW = boardContentWidthPx * currentScale
        val scaledH = boardContentHeightPx * currentScale

        val minX = if (scaledW + 2 * marginX > vw) vw - scaledW - marginX else marginX
        val maxX = if (scaledW + 2 * marginX > vw) marginX else vw - scaledW - marginX

        val minY = if (scaledH + 2 * marginY > vh) vh - scaledH - marginY else marginY
        val maxY = if (scaledH + 2 * marginY > vh) marginY else vh - scaledH - marginY

        val clampedX = offset.x.coerceIn(minOf(minX, maxX), maxOf(minX, maxX))
        val clampedY = offset.y.coerceIn(minOf(minY, maxY), maxOf(minY, maxY))

        return Offset(clampedX, clampedY)
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val glowColor = MaterialTheme.hack.glow

    val infiniteTransition = rememberInfiniteTransition(label = "connectorPulse")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glowPulse",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
            .onSizeChanged { size ->
                viewportSize = size
                if (size.width > 0 && size.height > 0) {
                    panOffset = clampPan(panOffset, scale)
                }
            }
            .pointerInput(bracket.stage) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val oldScale = scale
                    val newScale = (scale * zoom).coerceIn(0.35f, 2.2f)
                    scale = newScale
                    val unconstrainedOffset = (panOffset - centroid) * (newScale / oldScale) + centroid + pan
                    panOffset = clampPan(unconstrainedOffset, newScale)
                }
            },
    ) {
        BoardGridBackground(
            scale = scale,
            panOffset = panOffset,
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            for (r in 1 until nodesByRound.size) {
                val currentRoundNodes = nodesByRound[r]
                val previousRoundNodes = nodesByRound[r - 1]

                for (m in currentRoundNodes.indices) {
                    val curr = currentRoundNodes[m]
                    val parent1 = previousRoundNodes.getOrNull(2 * m)
                    val parent2 = previousRoundNodes.getOrNull(2 * m + 1)

                    val currLeftX = curr.boardX * scale + panOffset.x
                    val currSlot1Y = (curr.boardY + cardHeightPx * 0.35f) * scale + panOffset.y
                    val currSlot2Y = (curr.boardY + cardHeightPx * 0.75f) * scale + panOffset.y

                    if (parent1 != null) {
                        val p1RightX = (parent1.boardX + cardWidthPx) * scale + panOffset.x
                        val p1CenterY = (parent1.boardY + cardHeightPx / 2f) * scale + panOffset.y
                        val midX = (p1RightX + currLeftX) / 2f

                        val isP1Winner = parent1.match.winnerId != null &&
                                (parent1.match.winnerId == curr.match.teamA.id || parent1.match.winnerId == curr.match.teamB?.id)

                        val path1 = Path().apply {
                            moveTo(p1RightX, p1CenterY)
                            lineTo(midX, p1CenterY)
                            lineTo(midX, currSlot1Y)
                            lineTo(currLeftX, currSlot1Y)
                        }

                        if (isP1Winner) {
                            drawPath(
                                path = path1,
                                color = glowColor.copy(alpha = glowPulse),
                                style = Stroke(
                                    width = 5.5f * density * scale,
                                    cap = StrokeCap.Round,
                                ),
                            )
                            drawPath(
                                path = path1,
                                color = primaryColor,
                                style = Stroke(
                                    width = 2.2f * density * scale,
                                    cap = StrokeCap.Square,
                                ),
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 3.5f * density * scale,
                                center = Offset(currLeftX, currSlot1Y),
                            )
                        } else {
                            drawPath(
                                path = path1,
                                color = outlineColor.copy(alpha = 0.45f),
                                style = Stroke(
                                    width = 1.2f * density * scale,
                                    cap = StrokeCap.Square,
                                ),
                            )
                            drawCircle(
                                color = outlineColor.copy(alpha = 0.6f),
                                radius = 2.5f * density * scale,
                                center = Offset(currLeftX, currSlot1Y),
                            )
                        }
                    }

                    if (parent2 != null) {
                        val p2RightX = (parent2.boardX + cardWidthPx) * scale + panOffset.x
                        val p2CenterY = (parent2.boardY + cardHeightPx / 2f) * scale + panOffset.y
                        val midX = (p2RightX + currLeftX) / 2f

                        val isP2Winner = parent2.match.winnerId != null &&
                                (parent2.match.winnerId == curr.match.teamA.id || parent2.match.winnerId == curr.match.teamB?.id)

                        val path2 = Path().apply {
                            moveTo(p2RightX, p2CenterY)
                            lineTo(midX, p2CenterY)
                            lineTo(midX, currSlot2Y)
                            lineTo(currLeftX, currSlot2Y)
                        }

                        if (isP2Winner) {
                            drawPath(
                                path = path2,
                                color = glowColor.copy(alpha = glowPulse),
                                style = Stroke(
                                    width = 5.5f * density * scale,
                                    cap = StrokeCap.Round,
                                ),
                            )
                            drawPath(
                                path = path2,
                                color = primaryColor,
                                style = Stroke(
                                    width = 2.2f * density * scale,
                                    cap = StrokeCap.Square,
                                ),
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = 3.5f * density * scale,
                                center = Offset(currLeftX, currSlot2Y),
                            )
                        } else {
                            drawPath(
                                path = path2,
                                color = outlineColor.copy(alpha = 0.45f),
                                style = Stroke(
                                    width = 1.2f * density * scale,
                                    cap = StrokeCap.Square,
                                ),
                            )
                            drawCircle(
                                color = outlineColor.copy(alpha = 0.6f),
                                radius = 2.5f * density * scale,
                                center = Offset(currLeftX, currSlot2Y),
                            )
                        }
                    }
                }
            }
        }

        nodesByRound.forEach { roundNodes ->
            roundNodes.forEach { node ->
                val screenX = (node.boardX * scale + panOffset.x).roundToInt()
                val screenY = (node.boardY * scale + panOffset.y).roundToInt()

                Box(
                    modifier = Modifier
                        .offset { IntOffset(screenX, screenY) }
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            transformOrigin = TransformOrigin(0f, 0f)
                        },
                ) {
                    TournamentMatchCard(
                        match = node.match,
                        isSelected = node.match.id == selectedMatchId,
                        onClick = { onSelectMatch(node.match.id) },
                    )
                }
            }
        }

        val stickyY = (12f * density).roundToInt()

        roundHeaders.forEachIndexed { rIndex, header ->
            val screenX = (header.boardX * scale + panOffset.x).roundToInt()

            Surface(
                modifier = Modifier
                    .offset { IntOffset(screenX, stickyY) }
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                        transformOrigin = TransformOrigin(0f, 0f)
                    }
                    .zIndex(2f),
                shape = RoundedCornerShape(2.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Surface(
                        shape = RoundedCornerShape(1.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(width = 3.dp, height = 10.dp),
                    ) {}
                    Text(
                        text = "[ 0${rIndex + 1} // ${header.title.uppercase()} ]",
                        style = HackMonoTextStyle.copy(
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp,
                        ),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        FloatingBoardControls(
            onZoomIn = {
                val oldScale = scale
                val newScale = (scale * 1.25f).coerceAtMost(2.2f)
                scale = newScale
                panOffset = clampPan(panOffset * (newScale / oldScale), newScale)
            },
            onZoomOut = {
                val oldScale = scale
                val newScale = (scale * 0.8f).coerceAtLeast(0.35f)
                scale = newScale
                panOffset = clampPan(panOffset * (newScale / oldScale), newScale)
            },
            onReset = {
                scale = 1f
                panOffset = clampPan(Offset(24f * density, 24f * density), 1f)
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .zIndex(3f),
        )
    }
}

@Composable
private fun FloatingBoardControls(
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.96f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        shadowElevation = 6.dp,
    ) {
        Column(
            modifier = Modifier.width(38.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            IconButton(
                onClick = onZoomIn,
                modifier = Modifier.size(36.dp),
            ) {
                Text(
                    text = "+",
                    style = HackMonoTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            IconButton(
                onClick = onZoomOut,
                modifier = Modifier.size(36.dp),
            ) {
                Text(
                    text = "–",
                    style = HackMonoTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                thickness = 1.dp,
            )

            IconButton(
                onClick = onReset,
                modifier = Modifier.size(36.dp),
            ) {
                Text(
                    text = "1:1",
                    style = HackMonoTextStyle.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
