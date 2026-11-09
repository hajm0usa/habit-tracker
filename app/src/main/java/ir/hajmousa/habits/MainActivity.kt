package ir.hajmousa.habits

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import ir.hajmousa.habits.core.date.DayClock
import ir.hajmousa.habits.core.date.JalaliConverter
import ir.hajmousa.habits.core.date.PersianFormat

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val today = PersianFormat.date(JalaliConverter.toJalali(DayClock.system().today()))
        setContent {
            val dark = isSystemInDarkTheme()
            val context = LocalContext.current
            val colors = when {
                android.os.Build.VERSION.SDK_INT >= 31 && dark -> dynamicDarkColorScheme(context)
                android.os.Build.VERSION.SDK_INT >= 31 -> dynamicLightColorScheme(context)
                dark -> darkColorScheme()
                else -> lightColorScheme()
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                val fontFamily = FontFamily(
                    Font(R.font.vazirmatn_regular, FontWeight.Normal),
                    Font(R.font.vazirmatn_bold, FontWeight.Bold)
                )
                MaterialTheme(colorScheme = colors, typography = Typography(defaultFontFamily = fontFamily)) {
                    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = getString(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                            Text(text = today, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
