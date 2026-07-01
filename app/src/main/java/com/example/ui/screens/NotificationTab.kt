package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NotificationTab() {
    val alerts = listOf(
        AlertItem(
            id = 1,
            title = "নদীবন্দরে ২ নম্বর দূরবর্তী সতর্ক সংকেত",
            description = "কীর্তনখোলা ও মেঘনা নদীতে কালবৈশাখী ঝড়ের আশঙ্কায় দূরবর্তী সংকেত জারি করা হয়েছে। ছোট লঞ্চগুলো সাবধানে চলুন।",
            time = "১০ মিনিট পূর্বে",
            type = "WARNING",
            icon = Icons.Default.Warning,
            color = RedEmergency
        ),
        AlertItem(
            id = 2,
            title = "বিনামূল্যে ডায়াবেটিস ও রক্তদান ক্যাম্পেইন",
            description = "আগামীকাল সকাল ৯:০০ টায় বরিশাল শের-ই-বাংলা মেডিকেল কলেজ (SBMCH) প্রাঙ্গণে বিনামূল্যে স্বাস্থ্য ক্যাম্প অনুষ্ঠিত হবে।",
            time = "২ ঘণ্টা পূর্বে",
            type = "INFO",
            icon = Icons.Default.Info,
            color = NeonTeal
        ),
        AlertItem(
            id = 3,
            title = "কুয়াকাটা সড়কে নতুন লুপ নির্মাণ কাজ সম্পন্ন",
            description = "পায়রা সেতু অতিক্রম করার পর কুয়াকাটা এক্সপ্রেসওয়ের সংযোগ সড়কে কাজ সম্পন্ন হওয়ায় জ্যামমুক্ত যাতায়াত চালু হয়েছে।",
            time = "১ দিন পূর্বে",
            type = "SUCCESS",
            icon = Icons.Default.Campaign,
            color = NeonCyan
        ),
        AlertItem(
            id = 4,
            title = "নতুন নাগরিক অভিযোগ পোর্টাল চালু",
            description = "বরিশালবাসীর সুবিধার্থে অ্যাপে সরাসরি ছবিসহ 'সিটিজেন রিপোর্ট' বা নাগরিক অভিযোগ দায়েরের সুবিধা উন্মুক্ত করা হয়েছে।",
            time = "২ দিন পূর্বে",
            type = "GENERAL",
            icon = Icons.Default.NotificationsActive,
            color = ElectricBlue
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "বিভাগীয় বিজ্ঞপ্তি ও বার্তা",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "বরিশাল বিভাগের গুরুত্বপূর্ণ সকল অফিশিয়াল আপডেট ও নোটিশ",
                color = TextCyan,
                fontSize = 11.sp
            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f).testTag("notifications_list_view"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(alerts) { alert ->
                AlertRow(alert = alert)
            }
        }
    }
}

@Composable
fun AlertRow(alert: AlertItem) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notification_item_${alert.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = DarkNavySurfaceCard),
        border = BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle Icon Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(alert.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = alert.icon,
                    contentDescription = alert.type,
                    tint = alert.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.type.uppercase(),
                        color = alert.color,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = alert.time,
                        color = TextGray,
                        fontSize = 10.sp
                    )
                }

                Text(
                    text = alert.title,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = alert.description,
                    color = TextWhite.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

data class AlertItem(
    val id: Int,
    val title: String,
    val description: String,
    val time: String,
    val type: String,
    val icon: ImageVector,
    val color: Color
)
