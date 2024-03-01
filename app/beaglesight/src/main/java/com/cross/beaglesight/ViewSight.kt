package com.cross.beaglesight

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import com.cross.beaglesight.composables.SightGraphComposable
import com.cross.beaglesight.ui.theme.BeagleSightTheme
import com.cross.beaglesight.ui.theme.Typography
import com.cross.beaglesightlibs.bowconfigs.BowConfig
import com.cross.beaglesightlibs.bowconfigs.BowManager
import com.cross.beaglesightlibs.bowconfigs.PositionPair

class ViewSight : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bowManager = BowManager.getInstance(applicationContext)!!
        val id = intent.extras?.getString("ID")!!
        val config = bowManager.getBowConfig(id)!!
        setContent {
            BeagleSightTheme {
                // A surface container using the 'background' color from the theme
                ViewSightContent(
                    exitFn = { this.finish() },
                    config
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewSightContent(
    exitFn: () -> Unit, config: BowConfig
) {
    var dist by remember { mutableStateOf(0.0f) }
    var pos by remember { mutableStateOf(config.calcPosition(dist)) }
    val context = LocalContext.current

    var selectedPair: PositionPair? = null
    for (pair in config.positionArray) {
        if (kotlin.math.abs(pair.distance - dist) <= 0.3) {
            selectedPair = pair
            break
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Column {
                    Text(text = config.name, style = Typography.labelLarge)
                    Text(text = config.description, style = Typography.labelSmall)
                }
            },
            navigationIcon = {
                IconButton(onClick = exitFn) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }
            },
            actions = {
                if (selectedPair != null) {
                    IconButton(onClick = {
                        config.removePos(selectedPair!!)
                        selectedPair = null
                        dist += 0.001F  // Forces redraw, hack.
                    }
                    ) {
                        Icon(Icons.Filled.Delete, "Delete")
                    }
                }
                IconButton(onClick = {
                    val bowsIntent = Intent(context, AddPosition::class.java)
                    bowsIntent.putExtra("ID", config.id)
                    ContextCompat.startActivity(context, bowsIntent, null)
                }
                ) {
                    Icon(Icons.Filled.Add, "Add")
                }
            })
    }, content = { padding ->
        Column(modifier = Modifier.padding(padding)) {
            Row(Modifier.zIndex(1.0f)) {
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    value = "Dist: %.1f".format(dist),
                    onValueChange = { value ->
                        try {
                            dist = value.toFloat()
                            pos = config.calcPosition(dist)
                        } catch (_: NumberFormatException) {
                        }
                    }
                )
                TextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    value = "Position: %.1f".format(pos),
                    enabled = true,
                    onValueChange = {}
                )
            }
            Row(Modifier.zIndex(0.0f)) {
                SightGraphComposable(modifier = Modifier.testTag("sightgraph"),
                    bowConfig = config,
                    selectedDistanceCallback = { d, p ->
                        dist = d
                        pos = p
                    })
            }
        }
    })
}

@Preview(showBackground = true)
@Composable
fun ViewSightContentPreview() {
    val config = BowConfig()
    config.name = "Test Bow"
    config.description = "This is a sample bow"
    config.addPos(PositionPair(10.0f, 10.0f))
    config.addPos(PositionPair(20.0f, 20.0f))
    config.addPos(PositionPair(25.0f, 30.0f))
    config.addPos(PositionPair(30.0f, 40.0f))

    BeagleSightTheme {
        ViewSightContent(exitFn = {}, config = config)
    }
}