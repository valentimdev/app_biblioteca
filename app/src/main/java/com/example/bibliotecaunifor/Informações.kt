package com.example.libraryapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Book(
    val title: String,
    val author: String,
    val synopsis: String,
    val genre: String,
    val year: Int,
    val availableQuantity: Int
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailsScreen(
    book: Book,
    onBack: () -> Unit,
    onRent: () -> Unit,
    onAddToWishlist: () -> Unit
) {
    var isFavorite by remember { mutableStateOf(false) }

    val gradientBackground = Brush.verticalGradient(
        listOf(Color(0xFFf5f5f7), Color(0xFFe8eaf6))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhes do Livro", color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.Black)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        isFavorite = !isFavorite
                        onAddToWishlist()
                    }) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (isFavorite) Color(0xFFFF1744) else Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradientBackground)
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(book.title, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("por ${book.author}", color = Color(0xFF5C6BC0), fontSize = 16.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Gênero: ${book.genre} • Ano: ${book.year}", color = Color.Gray)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text("Sinopse", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF303F9F))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(book.synopsis, fontSize = 15.sp, color = Color(0xFF333333), lineHeight = 22.sp)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    modifier = Modifier.shadow(4.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val availableColor = if (book.availableQuantity > 0) Color(0xFF43A047) else Color(0xFFD32F2F)
                        Text("Exemplares disponíveis", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "${book.availableQuantity}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 40.sp,
                            color = availableColor
                        )
                    }
                }

                if (book.availableQuantity > 0) {
                    Button(
                        onClick = onRent,
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF5C6BC0)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("📚 Alugar Livro", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(55.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Indisponível para empréstimo", color = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}
