package com.example.farmer_application

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.example.farmer_application.ui.theme.FarmerApplicationTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID
import kotlin.text.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FarmerApplicationTheme {
                val navController = rememberNavController()

                NavHost(navController, startDestination = "authScreen") {
                    composable("authScreen") { AuthenticationScreen(navController) }
                    composable("farmerScreen") { FarmerScreen(navController) }
                    composable("consumerScreen") { ConsumerScreen(navController) }
                    composable("browseProductsScreen") { BrowseProductsScreen(navController) }
                    composable("addProductScreen") { AddProductScreen(navController) }
                    composable("productListScreen") { ProductListScreen(navController)}
                    composable("modifyProductScreen/{productId}") { backStackEntry ->
                        val productId = backStackEntry.arguments?.getString("productId") ?: ""
                        ModifyProductScreen(navController, productId)
                    }
                    composable("trackEarningsScreen/{farmerId}") { backStackEntry ->
                        val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
                        TrackEarningsScreen(farmerId)
                    }
                    composable("viewOrdersScreen/{farmerId}") { backStackEntry ->
                        val farmerId = backStackEntry.arguments?.getString("farmerId") ?: ""
                        ViewOrdersScreen(farmerId)
                    }
                    composable("orderHistoryScreen") { OrderHistoryScreen(navController)}
                }
            }
        }
    }
}
data class Product(
    val id: String = "",       // Product ID
    val name: String = "",     // Product name
    val price: String = "",    // Product price (store as String to avoid type mismatch)
    val imageUrl: String = "", // Product image URL
    val farmerId: String = ""  // Farmer's ID
)
data class Order(
    val id: String = "",
    val productId: String = "",
    val consumerId: String = "",
    val farmerId: String = "",
    val status: String = "Pending"
)
@Composable
fun AuthenticationScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf("Farmer") } // Default user type
    var isSignUp by remember { mutableStateOf(true) } // Toggle Sign In/Up
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated Card for title
        Text(
            text = if (isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in),
            style = MaterialTheme.typography.headlineMedium,
            color = Color.Black, // Ensuring black text
            fontWeight = FontWeight.Bold
        )


        Spacer(modifier = Modifier.height(16.dp))

        // Email Input Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(text = stringResource(R.string.email), color = Color.Black) }, // Black placeholder
            textStyle = TextStyle(color = Color.Black), // **Ensures black text while typing**
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Password Input Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(text = stringResource(R.string.password), color = Color.Black) }, // Black placeholder
            textStyle = TextStyle(color = Color.Black), // **Ensures black text while typing**
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        // User Type Dropdown
        DropdownMenuUserType(userType) { userType = it }


        Spacer(modifier = Modifier.height(16.dp))

        // Sign Up / Sign In Button
        Button(
            onClick = {
                if (isSignUp) {
                    signUpUser(auth, db, email, password, userType, context)
                } else {
                    signInUser(auth, db, email, password, context, navController)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00897B)) // Teal color
        ) {
            Text(
                text = if (isSignUp) stringResource(R.string.sign_up) else stringResource(R.string.sign_in),
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Switch between Sign Up & Sign In
        Text(
            text = if (isSignUp) stringResource(R.string.already_have_account) else stringResource(R.string.dont_have_account),
            modifier = Modifier
                .clickable { isSignUp = !isSignUp }
                .padding(8.dp),
            color = Color(0xFF00897B), // Teal color to match button
            fontWeight = FontWeight.Medium
        )
    }
}

fun signUpUser(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    email: String,
    password: String,
    userType: String,
    context: android.content.Context
) {
    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                val user = hashMapOf(
                    "email" to email,
                    "userType" to userType
                )
                userId?.let {
                    db.collection("users").document(it).set(user)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Sign-Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}

fun signInUser(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    email: String,
    password: String,
    context: android.content.Context,
    navController: NavController
) {
    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId = auth.currentUser?.uid
                if (userId != null) {
                    db.collection("users").document(userId).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val userType = document.getString("userType")
                                Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                                when (userType) {
                                    "Farmer" -> navController.navigate("farmerScreen")
                                    "Consumer" -> navController.navigate("consumerScreen")
                                    else -> Toast.makeText(context, "Unknown User Type", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                Toast.makeText(context, "User data not found", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Toast.makeText(context, "Firestore Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(context, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
}
@Composable
fun DropdownMenuUserType(selectedType: String, onUserTypeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("Farmer", "Consumer")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true } // Whole box is clickable
            .border(1.dp, Color.Gray, shape = RoundedCornerShape(8.dp))
            .background(Color.White, shape = RoundedCornerShape(8.dp))
            .padding(7.dp, 10.dp, 7.dp, 7.dp)
    ) {
        OutlinedTextField(
            value = selectedType,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("User Type", color = Color.Black, fontSize = 15.sp) },
            textStyle = TextStyle(color = Color.Black), // Ensures the selected text is black
            trailingIcon = {
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { expanded = true } // Icon is now clickable!
                )
            }
        )
    }

    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
        options.forEach { option ->
            DropdownMenuItem(
                text = { Text(option, color = Color.White) },
                onClick = {
                    onUserTypeSelected(option)
                    expanded = false
                }
            )
        }
    }
}

@Composable
fun FarmerScreen(navController: NavController) {
    val context = LocalContext.current
    val animatedVisibility = remember { Animatable(0f) }

    // Animate the entire screen's fade-in
    LaunchedEffect(Unit) {
        animatedVisibility.animateTo(1f, animationSpec = tween(durationMillis = 500))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .graphicsLayer(alpha = animatedVisibility.value),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.farmer_dashboard),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Dashboard options with animations
        AddProductBox(navController)
        DeleteOrModifyProductBox(navController)
        ViewOrdersBox(navController, context)
        TrackEarningsBox(navController)
    }
}
@Composable
fun AddProductBox(navController: NavController) {
    BoxItem(title = "Add Product") {
        navController.navigate("addProductScreen") // Navigates to Add Product Screen
    }
}

@Composable
fun AddProductScreen(navController: NavController) {
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }  // ✅ New: Loading state
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()

    Spacer(modifier = Modifier.height(20.dp))
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.add_product), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text(stringResource(R.string.product_name), color=Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,  // ✅ User typed text color (when focused)
                unfocusedTextColor = Color.Black, // ✅ User typed text color (when not focused)
                focusedContainerColor = Color.White, // ✅ Background color
                unfocusedContainerColor = Color.White // ✅ Background color when not focused
            )

        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productPrice,
            onValueChange = { productPrice = it },
            label = { Text(text= stringResource(R.string.product_price), color=Color.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,  // ✅ User typed text color (when focused)
                unfocusedTextColor = Color.Black, // ✅ User typed text color (when not focused)
                focusedContainerColor = Color.White, // ✅ Background color
                unfocusedContainerColor = Color.White // ✅ Background color when not focused
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(
                    model = imageUri,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Text(text = stringResource(R.string.tap_to_select_image), color = Color.Black)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ✅ Show loading indicator while uploading
        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {
                if (productName.isNotBlank() && productPrice.isNotBlank() && imageUri != null) {
                    isUploading = true  // ✅ Start loading
                    uploadImageToFirebase(
                        storage, db, auth, imageUri!!, productName, productPrice, context, navController
                    ) { success ->
                        isUploading = false  // ✅ Stop loading
                        if (success) {
                            navController.navigate("farmerScreen")  // ✅ Only go back if successful
                        }
                    }
                } else {
                    Toast.makeText(context, "Fill all fields!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isUploading // ✅ Disable button while uploading
        ) {
            Text(stringResource(R.string.add_product))
        }
    }
}

// Delete or Modify Product Box
@Composable
fun DeleteOrModifyProductBox(navController: NavController) {
    BoxItem(title = "Modify/Delete Product") {
        navController.navigate("productListScreen")
    }
}
@Composable
fun ProductListScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    var products by remember { mutableStateOf<List<DocumentSnapshot>>(emptyList()) }

    LaunchedEffect(currentUser) {
        currentUser?.let { user ->
            db.collection("products")
                .whereEqualTo("farmerId", user.uid) // ✅ Fetch only the current farmer's products
                .get()
                .addOnSuccessListener { result ->
                    products = result.documents
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        Text(text = stringResource(R.string.select_product_modify), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        if (products.isEmpty()) {
            Text("No products found.", style = MaterialTheme.typography.bodyLarge)
        } else {
            LazyColumn {
                items(products) { product ->
                    val productId = product.id
                    val productName = product.getString("name") ?: "Unnamed Product"
                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { navController.navigate("modifyProductScreen/$productId") },
                        shape = RoundedCornerShape(10.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = productName, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyProductScreen(navController: NavController, productId: String) {
    val db = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance()
    var productName by remember { mutableStateOf("") }
    var productPrice by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    LaunchedEffect(Unit) {
        db.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                document?.let {
                    productName = it.getString("name") ?: ""
                    productPrice = it.getString("price") ?: ""
                    imageUrl = it.getString("imageUrl") ?: ""
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.modify_delete_product), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = productName,
            onValueChange = { productName = it },
            label = { Text(stringResource(R.string.product_name), color = Color.Black) },
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,  // ✅ User typed text color (when focused)
                unfocusedTextColor = Color.Black, // ✅ User typed text color (when not focused)
                focusedContainerColor = Color.White, // ✅ Background color
                unfocusedContainerColor = Color.White // ✅ Background color when not focused
            )
        )



        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = productPrice,
            onValueChange = { productPrice = it },
            label = { Text(stringResource(R.string.product_price), color = Color.Black) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,  // ✅ User typed text color (when focused)
                unfocusedTextColor = Color.Black, // ✅ User typed text color (when not focused)
                focusedContainerColor = Color.White, // ✅ Background color
                unfocusedContainerColor = Color.White // ✅ Background color when not focused
            )
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray, shape = RoundedCornerShape(8.dp))
                .clickable {
                    launcher.launch("image/*")
                },
            contentAlignment = Alignment.Center
        ) {
            if (imageUri != null) {
                AsyncImage(model = imageUri, contentDescription = "Product Image", modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            } else {
                AsyncImage(model = imageUrl, contentDescription = "Product Image", modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            updateProduct(db, storage, productId, productName, productPrice, imageUri, imageUrl, context, navController)
        }, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.save_changes))
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            deleteProduct(db, storage, productId, imageUrl, context, navController)
        }, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(Color.Red)) {
            Text(stringResource(R.string.delete_product))
        }
    }
}
fun updateProduct(
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    productId: String,
    name: String,
    price: String,
    newImageUri: Uri?,
    oldImageUrl: String,
    context: Context,
    navController: NavController
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

    if (newImageUri != null) {
        val storageRef = storage.reference.child("products/$userId/${UUID.randomUUID()}.jpg")

        // ✅ Delete old image first
        if (oldImageUrl.isNotEmpty()) {
            val oldImageRef = storage.getReferenceFromUrl(oldImageUrl)
            oldImageRef.delete().addOnFailureListener {
                Log.e("FirebaseStorage", "Failed to delete old image: ${it.message}")
            }
        }

        // ✅ Upload new image
        storageRef.putFile(newImageUri).addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { newImageUrl ->
                db.collection("products").document(productId)
                    .update(
                        mapOf(
                            "name" to name,
                            "price" to price,
                            "imageUrl" to newImageUrl.toString()
                        )
                    )
                    .addOnSuccessListener {
                        Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    } else {
        // ✅ No image change, just update text fields
        db.collection("products").document(productId)
            .update(mapOf("name" to name, "price" to price))
            .addOnSuccessListener {
                Toast.makeText(context, "Product updated successfully!", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Update failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}

fun deleteProduct(
    db: FirebaseFirestore,
    storage: FirebaseStorage,
    productId: String,
    imageUrl: String,
    context: Context,
    navController: NavController
) {
    storage.getReferenceFromUrl(imageUrl).delete()
    db.collection("products").document(productId).delete()
        .addOnSuccessListener {
            Toast.makeText(context, "Product deleted successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
}
// View Orders Box
@Composable
fun ViewOrdersBox(navController: NavController, context: Context) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    val farmerId = currentUser?.uid // Get the logged-in farmer's ID ✅

    BoxItem(title = "View Orders") {
        if (farmerId != null) {
            navController.navigate("viewOrdersScreen/$farmerId") // Navigate with farmerId ✅
        } else {
            Toast.makeText(context, "Error: Farmer ID not found!", Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
fun ViewOrdersScreen(farmerId: String) {
    val db = FirebaseFirestore.getInstance()
    val orders = remember { mutableStateListOf<Order>() }
    val productNames = remember { mutableStateMapOf<String, String>() } // Store Product ID -> Name mapping
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereEqualTo("farmerId", farmerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    orders.clear()
                    for (document in snapshot.documents) {
                        val order = document.toObject(Order::class.java)?.copy(id = document.id)
                        if (order != null) {
                            orders.add(order)

                            // Fetch Product Name
                            val productId = order.productId
                            db.collection("products").document(productId).get()
                                .addOnSuccessListener { productDoc ->
                                    val productName = productDoc.getString("name") ?: "Unknown Product"
                                    productNames[productId] = productName
                                }
                        }
                    }
                }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(text = stringResource(R.string.order), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(orders) { order ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "${stringResource(R.string.product)} ${productNames[order.productId] ?: "Loading..."}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(text = "Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Button(onClick = {
                                db.collection("orders").document(order.id).update("status", "Accepted")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Order Accepted", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to update order", Toast.LENGTH_SHORT).show()
                                    }
                            }) {
                                Text(stringResource(R.string.accept))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(onClick = {
                                db.collection("orders").document(order.id).update("status", "Rejected")
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Order Rejected", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener {
                                        Toast.makeText(context, "Failed to update order", Toast.LENGTH_SHORT).show()
                                    }
                            }) {
                                Text(stringResource(R.string.reject))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Track Earnings Box
@Composable
fun TrackEarningsBox(navController: NavController) {
    val farmerId : String? = FirebaseAuth.getInstance().currentUser?.uid
    BoxItem(title = "Track Earnings") {
        navController.navigate("trackEarningsScreen/$farmerId") // Navigates to Track Earnings Screen
    }
    Log.d("TrackEarningsScreen", "inside box")
}
@Composable
fun TrackEarningsScreen(farmerId: String) {
    val db = FirebaseFirestore.getInstance()
    val earnings = remember { mutableStateOf(0.0) }
    val loading = remember { mutableStateOf(true) }
    val animatedEarnings by animateFloatAsState(targetValue = earnings.value.toFloat(), label = "")

    LaunchedEffect(Unit) {
        val productIds = mutableListOf<String>()

        db.collection("orders")
            .whereEqualTo("farmerId", farmerId)
            .whereEqualTo("status", "Accepted")
            .get()
            .addOnSuccessListener { orderSnapshot ->
                for (document in orderSnapshot.documents) {
                    val productId = document.getString("productId")
                    if (!productId.isNullOrEmpty()) {
                        productIds.add(productId)
                    }
                }

                if (productIds.isEmpty()) {
                    earnings.value = 0.0
                    loading.value = false
                    return@addOnSuccessListener
                }

                db.collection("products")
                    .whereIn(FieldPath.documentId(), productIds)
                    .get()
                    .addOnSuccessListener { productSnapshot ->
                        var totalEarnings = 0.0
                        for (productDoc in productSnapshot.documents) {
                            val priceField = productDoc.get("price")
                            val price = when (priceField) {
                                is Number -> priceField.toDouble()
                                is String -> priceField.toDoubleOrNull() ?: 0.0
                                else -> 0.0
                            }
                            totalEarnings += price
                        }
                        earnings.value = totalEarnings
                        loading.value = false
                    }
                    .addOnFailureListener {
                        loading.value = false
                    }
            }
            .addOnFailureListener {
                loading.value = false
            }
    }

    // 🌟 UI Design
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (loading.value) {
            // 🌟 Placeholder Loading Card
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = stringResource(R.string.calculating_earnings), fontSize = 18.sp, color = Color.Gray)
            }
        } else {
            // 🌟 Earnings Display Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = stringResource(R.string.total_earnings),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "₹${String.format("%.2f", animatedEarnings)}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF4CAF50) // Green color for earnings
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.updated_real_time),
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}


fun uploadImageToFirebase(
    storage: FirebaseStorage,
    db: FirebaseFirestore,
    auth: FirebaseAuth,
    imageUri: Uri,
    productName: String,
    productPrice: String,
    context: Context,
    navController: NavController,
    onComplete: (Boolean) -> Unit // Ensures UI updates properly
) {
    val userId = auth.currentUser?.uid
    if (userId == null) {
        Toast.makeText(context, "User not logged in!", Toast.LENGTH_SHORT).show()
        onComplete(false)
        return
    }

    val productId = UUID.randomUUID().toString()
    val storageRef = storage.reference.child("products/$userId/$productId.jpg")

    storageRef.putFile(imageUri)
        .addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()

                // ✅ Now logging values before storing in Firestore
                Log.d("UploadDebug", "Image uploaded successfully. Image URL: $imageUrl")
                Log.d("UploadDebug", "Saving product details: Name = $productName, Price = $productPrice")

                val product = hashMapOf(
                    "id" to productId,
                    "name" to productName,
                    "price" to productPrice,
                    "imageUrl" to imageUrl,  // ✅ Ensuring Firestore gets the URL
                    "farmerId" to userId
                )

                db.collection("products").document(productId).set(product)
                    .addOnSuccessListener {
                        Log.d("UploadDebug", "Product saved successfully in Firestore.")
                        Toast.makeText(context, "Product added successfully!", Toast.LENGTH_SHORT).show()
                        onComplete(true) // ✅ Stops loading properly
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        Log.e("UploadDebug", "Failed to save product: ${e.message}")
                        Toast.makeText(context, "Failed to save product: ${e.message}", Toast.LENGTH_SHORT).show()
                        onComplete(false) // ✅ Stops loading even if Firestore fails
                    }
            }.addOnFailureListener {
                Log.e("UploadDebug", "Failed to get image URL.")
                Toast.makeText(context, "Failed to get image URL", Toast.LENGTH_SHORT).show()
                onComplete(false)
            }
        }
        .addOnFailureListener { e ->
            Log.e("UploadDebug", "Image upload failed: ${e.message}")
            Toast.makeText(context, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            onComplete(false)
        }
}

@Composable
fun ConsumerScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = stringResource(R.string.consumer_dashboard), style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))

        BrowseProductsBox(navController)
        OrderHistoryBox(navController)
    }
}
@Composable
fun ProductItem(product: Product, consumerId: String?, navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = "Product Image",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = product.name, style = MaterialTheme.typography.bodyLarge)
                    Text(text = "${stringResource(R.string.price)} ${product.price}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val orderData = hashMapOf(
                        "productId" to product.id,
                        "consumerId" to consumerId,
                        "farmerId" to product.farmerId,
                        "status" to "Pending"
                    )
                    db.collection("orders").add(orderData)
                        .addOnSuccessListener {
                            Toast.makeText(
                                context,
                                "Order placed successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("OrderError", "Failed to place order", e)
                            Toast.makeText(
                                context,
                                "Failed to place order: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.order))
            }
        }
    }
}

@Composable
fun BrowseProductsBox(navController: NavController) {
    BoxItem(title = "Browse Products", onClick = { navController.navigate("browseProductsScreen") })
}

@Composable
fun BrowseProductsScreen(navController: NavController) {
    val consumerId = FirebaseAuth.getInstance().currentUser?.uid
    var searchQuery by remember { mutableStateOf("") }
    val db = FirebaseFirestore.getInstance()
    val products = remember { mutableStateListOf<Product>() }

    LaunchedEffect(Unit) {
        db.collection("products").get()
            .addOnSuccessListener { documents ->
                products.clear()
                for (document in documents) {
                    val product = document.toObject(Product::class.java).copy(id = document.id)
                    products.add(product)
                }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            label = { Text(stringResource(R.string.search_products), color = Color.Black) },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(products.filter { it.name.contains(searchQuery, ignoreCase = true) }) { product ->
                ProductItem(product, consumerId = consumerId, navController)
            }
        }
    }
}
@Composable
fun OrderHistoryBox(navController: NavController) {
    BoxItem(title = "Order History", onClick = { navController.navigate("orderHistoryScreen") })
}
@Composable
fun OrderHistoryScreen(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    val orders = remember { mutableStateListOf<Order>() }
    val context = LocalContext.current
    val consumerId = FirebaseAuth.getInstance().currentUser?.uid

    LaunchedEffect(Unit) {
        db.collection("orders")
            .whereEqualTo("consumerId", consumerId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    orders.clear()
                    for (document in snapshot.documents) {
                        val order = document.toObject(Order::class.java)?.copy(id = document.id)
                        if (order != null) {
                            orders.add(order)
                        }
                    }
                }
            }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = stringResource(R.string.order_history), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(orders) { order ->
                var productName by remember { mutableStateOf("Loading...") }

                // Fetch product name using productId
                LaunchedEffect(order.productId) {
                    db.collection("products").document(order.productId).get()
                        .addOnSuccessListener { productDoc ->
                            productName = productDoc.getString("name") ?: "Unknown Product"
                        }
                        .addOnFailureListener {
                            productName = "Unknown Product"
                        }
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = "Product: $productName", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "Status: ${order.status}", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
@Composable
fun BoxItem(title: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .border(2.dp, Color.Gray, RoundedCornerShape(16.dp)) // Adds a boundary
            .clickable { onClick() }, // Keeps it simple
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White) // White background
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthenticationScreenPreview() {
    FarmerApplicationTheme {
        val navController = rememberNavController()
        AuthenticationScreen(navController)
    }
}
