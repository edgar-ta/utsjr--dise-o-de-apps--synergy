package com.example.synergy.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.LazyHorizontalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.synergy.R
import com.example.synergy.domain.model.Subject
import com.example.synergy.domain.model.Task
import com.example.synergy.presentation.components.AddSubjectDialog
import com.example.synergy.presentation.components.SubjectCard
import com.example.synergy.presentation.components.TasksList
import com.example.synergy.presentation.destinations.SubjectScreenRouteDestination
import com.example.synergy.presentation.destinations.TaskScreenRouteDestination
import com.example.synergy.presentation.subject.SubjectScreenNavArgs
import com.example.synergy.presentation.task.TaskScreenNavArgs
import com.example.synergy.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RootNavGraph(start = true)
@Destination
@Composable
fun DashboardScreenRoute(
    navigator: DestinationsNavigator
) {

    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        tasks = tasks,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onSubjectCardClick = { subjectId ->
            subjectId?.let {
                val navArg = SubjectScreenNavArgs(subjectId = subjectId)
                navigator.navigate(SubjectScreenRouteDestination(navArgs = navArg))
            }
        },
        onTaskCardClick = { taskId ->
            val navArg = TaskScreenNavArgs(taskId = taskId, subjectId = null)
            navigator.navigate(TaskScreenRouteDestination(navArgs = navArg))
        },
    )
}

@Preview
@Composable
private fun PreviewDashboardScreen() {
    DashboardScreen(
        state = DashboardState(
            totalSubjectCount = 0,
            totalStudiedHours = 5F,
            totalGoalStudyHours = 10F,
            subjects = emptyList(),
            subjectName = "Algo xd",
            goalStudyHours = "50.0",
            subjectCardColors = Subject.subjectCardColors[0],
            session = null
        ),
        tasks = emptyList(),
        onEvent = {},
        snackbarEvent = null,
        onSubjectCardClick = {},
        onTaskCardClick = {},
    )
}

@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    onEvent: (DashboardEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>?,
    onSubjectCardClick: (Int?) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
) {

    var isAddSubjectDialogOpen by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    if (snackbarEvent != null) {
        LaunchedEffect(key1 = true) {
            snackbarEvent.collectLatest { event ->
                when(event) {
                    is SnackbarEvent.ShowSnackbar -> {
                        snackbarHostState.showSnackbar(
                            message = event.message,
                            duration = event.duration
                        )
                    }

                    SnackbarEvent.NavigateUp -> {}
                }
            }
        }
    }

    AddSubjectDialog(
        isOpen = isAddSubjectDialogOpen,
        title = "Añadir materia",
        subjectName = state.subjectName,
        goalHours = state.goalStudyHours,
        selectedColors = state.subjectCardColors,
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHoursChange = { onEvent(DashboardEvent.OnGoalStudyHoursChange(it)) },
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
        onDismissRequest = { isAddSubjectDialogOpen = false },
        onConfirmButtonClick = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen = false
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { DashboardScreenTopBar() }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SubjectCardsSection(
                    modifier = Modifier.fillMaxWidth(),
                    subjectList = state.subjects,
                    onAddIconClicked = { isAddSubjectDialogOpen = true },
                    onSubjectCardClick = onSubjectCardClick
                )
            }
            item {
                TasksList(
                    sectionTitle = stringResource(R.string.pending_tasks_title),
                    emptyListText = "No tienes tareas pendientes.\n " +
                            "Da clic en + para añadir una nueva.",
                    tasks = tasks,
                    onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
                    onTaskCardClick = onTaskCardClick
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardScreenTopBar() {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "Synergy",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    )
}

@Preview
@Composable
private fun PreviewSubjectCardsSection() {
    SubjectCardsSection(
        subjectList = listOf(
            Subject(
                name = "Diseño de Apps",
                goalHours = 1.5F,
                colors = Subject.subjectCardColors[0].map { color -> color.toArgb() },
                subjectId = null
            ),
            Subject(
                name = "Aplicaciones Web Orientadas a Servicios",
                goalHours = 1.5F,
                colors = Subject.subjectCardColors[1].map { color -> color.toArgb() },
                subjectId = null
            ),
            Subject(
                name = "Estructuras de Datos Aplicadas",
                goalHours = 1.5F,
                colors = Subject.subjectCardColors[2].map { color -> color.toArgb() },
                subjectId = null
            ),
        ),
        onAddIconClicked = {},
        onSubjectCardClick = {}
    )
}

@Composable
private fun SubjectCardsSection(
    modifier: Modifier = Modifier,
    subjectList: List<Subject>,
    emptyListText: String = "No tienes ninguna materia.\n Da clic en + para añadir una nueva.",
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "MATERIAS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = onAddIconClicked) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Subject"
                )
            }
        }
        if (subjectList.isEmpty()) {
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(R.drawable.img_books),
                contentDescription = emptyListText
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = emptyListText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        LazyHorizontalStaggeredGrid(
            rows = StaggeredGridCells.Fixed(2),
            state = rememberLazyStaggeredGridState(),
            modifier = Modifier.height(320.dp),
            horizontalItemSpacing = 6.dp,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            contentPadding = PaddingValues(horizontal = 6.dp)
        ) {
            items(subjectList.size) { index ->
                val subject = subjectList[index]
                SubjectCard(
                    subjectName = subject.name,
                    gradientColors = subject.colors.map { Color(it) },
                    onClick = { onSubjectCardClick(subject.subjectId) }
                )
            }
        }
    }
}