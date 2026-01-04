package com.example.mediplus.uii

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mediplus.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class HealthyReaderActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HealthyReaderScreen(onFinish = {
                finish()
            })
        }
    }
}

data class ArticleData(
    val title: String,
    val content: String
)

@Composable
fun HealthyReaderScreen(onFinish: () -> Unit) {
    val context = LocalContext.current

    val primaryColor = colorResource(id = R.color.medi_purple_primary)
    val bgColor = Color(0xFFE5E5E5)
    val articles = listOf(
        ArticleData(
            title = "The Importance of Health Awareness",
            content = "Health is one of the most valuable aspects of human life. It allows individuals to carry out daily activities, pursue goals, and maintain overall well-being. In modern society, where schedules are often busy and responsibilities continue to increase, many people tend to overlook the basic habits that support a healthy lifestyle. This lack of attention can lead to health problems that may affect physical, mental, and emotional conditions.\n\nMaintaining good health requires consistent effort. A balanced diet is one of the most important components. Consuming nutritious foods such as vegetables, fruits, whole grains, and lean proteins can help the body function properly. Adequate water intake is also necessary to ensure that the body stays hydrated throughout the day. In addition to proper nutrition, regular physical activity plays a significant role. Exercise helps strengthen muscles, improve heart function, and maintain a healthy body weight.\n\nAnother essential aspect of health is getting enough rest. Many individuals experience stress and fatigue due to long working hours or academic demands. Sleep allows the body to recover and restore energy. Without sufficient rest, the risk of illness increases, and cognitive performance may decrease. Maintaining a regular sleep schedule supports both physical health and mental clarity.\n\nMental health is equally important in ensuring a balanced life. Stress management, emotional stability, and social connections contribute to mental well-being. Taking time to relax, communicate with supportive people, or engage in enjoyable activities can reduce stress and improve mood. Regular health check-ups are also necessary, as they help detect potential health issues at an early stage. Early detection increases the chances of effective treatment and prevents complications.\n\nOverall, being aware of personal health is a responsibility that every individual should prioritize. Small, consistent actions—such as eating nutritious meals, exercising, resting properly, and managing stress—can create significant benefits for long-term well-being. By adopting these habits, individuals can maintain a healthier lifestyle and enhance their overall quality of life."
        ),
        ArticleData(
            title = "The Importance of Daily Water Intake",
            content = "Water is an essential element for the human body and plays a vital role in almost every bodily function. About 60% of the human body is made up of water, which means maintaining proper hydration is crucial for overall health and daily performance. Drinking enough water helps the body function efficiently and supports both physical and mental well-being.\n\nOne of the main benefits of adequate water intake is regulating body temperature. Through sweating and normal bodily processes, the body loses fluids every day. Replacing these fluids by drinking water helps prevent dehydration, especially during physical activity, hot weather, or illness. Dehydration can cause symptoms such as dizziness, dry mouth, fatigue, and reduced concentration.\n\nWater also supports the digestive system. It helps break down food, absorb nutrients, and prevent constipation. Proper hydration allows the kidneys to filter waste and toxins more effectively, reducing the risk of kidney stones and urinary tract problems. In addition, drinking enough water supports blood circulation, allowing oxygen and nutrients to reach body tissues more efficiently.\n\nMaintaining good hydration can also help control appetite and body weight. Drinking water before or between meals may reduce overeating and help replace high-sugar beverages that contribute to weight gain. Water is a calorie-free choice that supports a healthier lifestyle and lowers the risk of chronic diseases such as obesity and type 2 diabetes.\n\nFor most adults, it is recommended to drink 6–8 glasses of water per day. However, individual needs may vary depending on age, activity level, climate, and health conditions. Listening to your body and drinking water regularly throughout the day is the best approach.\n\nMake hydration part of your daily routine. Start your day with a glass of water, carry a reusable water bottle, and use this mobile health app to set reminders and track your daily intake. Consistent hydration is a simple habit that can significantly improve your long-term health and quality of life."
        ),
        ArticleData(
            title = "The Importance of Regular Physical Activity",
            content = "Regular physical activity is a key component of a healthy lifestyle. Staying active helps the body remain strong, improves endurance, and supports mental well-being. You do not need intense workouts to gain health benefits—consistent, moderate activity can have a powerful impact on your overall health.\n\nPhysical activity strengthens the heart and lungs by improving blood circulation and oxygen flow throughout the body. Regular movement helps control blood pressure, reduce bad cholesterol levels, and maintain a healthy body weight. It also lowers the risk of chronic diseases such as heart disease, stroke, and type 2 diabetes.\n\nExercise plays an important role in maintaining muscle strength, joint flexibility, and bone health. Activities like walking, stretching, or light strength training help prevent muscle loss and reduce the risk of falls, especially as we age. Staying active also improves posture and balance, making daily activities easier and safer.\n\nBeyond physical benefits, regular physical activity has a positive effect on mental health. Exercise helps reduce stress, anxiety, and symptoms of depression by releasing endorphins, the body’s natural “feel-good” hormones. It can also improve sleep quality, boost energy levels, and enhance focus throughout the day.\n\nHealth experts recommend at least 20–30 minutes of moderate physical activity each day. This can be divided into short sessions that fit your schedule. Simple activities such as walking, cycling, household chores, or gentle stretching are effective ways to stay active.\n\nMake movement a part of your daily routine. Use this mobile health app to track your activity, set daily goals, and monitor your progress. Small, consistent steps toward physical activity can lead to long-term health benefits and a more active, fulfilling life."
        ),
        ArticleData(
            title = "Understanding the Importance of Quality Sleep",
            content = "Quality sleep is a fundamental pillar of good health, equal in importance to proper nutrition and regular physical activity. Sleep is not only a time for rest, but also a critical period when the body repairs tissues, balances hormones, and strengthens the immune system. Without adequate and quality sleep, both physical and mental performance can decline significantly.\n\nDuring sleep, the brain processes information, consolidates memories, and supports learning and emotional regulation. Poor sleep quality can result in difficulty concentrating, reduced productivity, mood changes, and increased stress levels. Over time, chronic sleep deprivation may contribute to anxiety, depression, and other mental health challenges.\n\nFrom a physical perspective, sleep plays an essential role in regulating metabolism and maintaining heart health. Inadequate sleep is associated with an increased risk of high blood pressure, heart disease, obesity, and type 2 diabetes. Hormones that control hunger and fullness can become imbalanced, leading to overeating and unhealthy food choices.\n\nMost adults require 7–9 hours of sleep each night, although individual needs may vary. Consistency is just as important as duration. Going to bed and waking up at the same time every day helps regulate the body’s internal clock, making it easier to fall asleep and wake up feeling refreshed.\n\nPracticing good sleep hygiene can greatly improve sleep quality. Creating a calm and comfortable sleeping environment, reducing noise and light, and maintaining a cool room temperature can help the body relax. Limiting screen time before bedtime is also important, as blue light from electronic devices can interfere with the body’s natural sleep cycle.\n\nLifestyle choices also affect sleep quality. Avoiding caffeine, nicotine, and heavy meals in the evening can help prevent sleep disturbances. Engaging in relaxing activities such as reading, deep breathing, or gentle stretching before bed can signal the body that it is time to rest."
        ),
        ArticleData(
            title = "Building a Balanced and Nutritious Diet",
            content = "A balanced and nutritious diet is essential for maintaining good health and supporting daily activities. The food you consume provides energy, supports body functions, and helps prevent various health problems. Understanding how to choose the right foods can improve overall well-being and long-term health.\n\nA healthy diet consists of a balance of carbohydrates, proteins, healthy fats, vitamins, and minerals. Carbohydrates provide the main source of energy, while proteins help build and repair body tissues. Healthy fats support brain function and help absorb essential vitamins. Including a variety of fruits and vegetables ensures the body receives important nutrients and antioxidants.\n\nEating regular meals helps maintain stable energy levels and supports metabolism. Skipping meals may lead to overeating later in the day and unhealthy food choices. Portion control is also important—eating in moderation helps maintain a healthy weight and reduces the risk of obesity and related diseases.\n\nLimiting sugar, salt, and saturated fat intake can significantly improve health outcomes. Excessive sugar consumption increases the risk of diabetes, while high salt intake can contribute to high blood pressure. Choosing fresh, minimally processed foods instead of packaged or fast foods is a healthier option.\n\nHydration is an important part of a balanced diet. Drinking enough water helps digestion and supports nutrient absorption. Combining healthy eating habits with adequate water intake creates a strong foundation for a healthy lifestyle.\n\nUse this mobile health app to plan meals, track daily nutrition, and receive practical dietary tips. Making small, consistent improvements in your eating habits can lead to better health, higher energy levels, and improved quality of life."
        )
    )

    var currentIndex by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val db = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
            val questRef = db.getReference("users").child(user.uid).child("quests").child("reading")

            questRef.get().addOnSuccessListener { snapshot ->
                val points = snapshot.getValue(Int::class.java) ?: 0
                currentIndex = if (points >= 5) 4 else points
                isLoading = false
            }.addOnFailureListener {
                isLoading = false
            }
        }
    }

    val currentArticle = articles[currentIndex]

    val scrollState = rememberScrollState()

    val isScrolledToBottom by remember {
        derivedStateOf {
            val maxScroll = scrollState.maxValue
            val currentScroll = scrollState.value
            maxScroll == 0 || currentScroll >= (maxScroll - 10)
        }
    }

    LaunchedEffect(currentIndex) {
        scrollState.scrollTo(0)
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize().background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
        return
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(primaryColor)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "Reading",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
            )

            LinearProgressIndicator(
                progress = { (currentIndex) / 5f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = Color(0xFFFFA726),
                trackColor = Color.White.copy(alpha = 0.5f)
            )
            Text(
                text = "${currentIndex}/5",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(80.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.starr),
                        contentDescription = "Star Icon",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Healthy Reader",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Text(
                            text = "Read 5 articles",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                        shape = RoundedCornerShape(50),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Text("Star")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                    ) {
                        Text(
                            text = currentArticle.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = currentArticle.content,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = Color.DarkGray,
                            textAlign = TextAlign.Justify
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val isAllFinished = currentIndex >= 4 && !isScrolledToBottom

                    Button(
                        onClick = {
                            addReadingPoints(context)

                            if (currentIndex < 4) {
                                currentIndex++
                                Toast.makeText(context, "+1 Point! Next Article...", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "All Done! You are amazing!", Toast.LENGTH_SHORT).show()
                                onFinish()
                            }
                        },
                        enabled = isScrolledToBottom,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            disabledContainerColor = Color.Gray
                        ),
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(120.dp)
                    ) {
                        Text(if (currentIndex < 4) "Done" else "Finish")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = primaryColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.starr),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Rewards",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(
                                    text = " Your point: $currentIndex ",
                                    fontSize = 12.sp,
                                    color = primaryColor,
                                    modifier = Modifier.padding(4.dp)
                                )
                            }

                            Surface(
                                color = Color(0xFF4A0072),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("★ 1", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun addReadingPoints(context: android.content.Context) {
    val user = FirebaseAuth.getInstance().currentUser ?: return
    val db = FirebaseDatabase.getInstance("https://mediplusapp-e6128-default-rtdb.firebaseio.com")
    val questRef = db.getReference("users").child(user.uid).child("quests").child("reading")

    questRef.runTransaction(object : com.google.firebase.database.Transaction.Handler {
        override fun doTransaction(currentData: com.google.firebase.database.MutableData): com.google.firebase.database.Transaction.Result {
            var currentVal = currentData.getValue(Int::class.java) ?: 0
            if (currentVal < 5) {
                currentVal++
            }
            currentData.value = currentVal
            return com.google.firebase.database.Transaction.success(currentData)
        }

        override fun onComplete(error: com.google.firebase.database.DatabaseError?, committed: Boolean, snapshot: com.google.firebase.database.DataSnapshot?) {
        }
    })
}