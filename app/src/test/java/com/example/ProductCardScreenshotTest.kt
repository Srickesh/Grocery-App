package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.model.Product
import com.example.ui.components.ProductCard
import com.example.ui.theme.PratyushStoreTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class ProductCardScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    @Test
    fun product_card_screenshot() {
        val sampleProduct = Product(
            id = "prod_organic_apples",
            name = "Fresh Organic Apples",
            brand = "Mustang Farms",
            categoryId = "fruits_vegetables",
            priceNrs = 200,
            originalPriceNrs = 250,
            unit = "1 kg",
            isExpress = true,
            isOrganic = true,
            isLocal = true,
            inStock = true,
            description = "Crisp and juicy apples from Mustang"
        )

        composeTestRule.setContent {
            PratyushStoreTheme {
                Box(modifier = Modifier.padding(16.dp).width(200.dp)) {
                    ProductCard(
                        product = sampleProduct,
                        onProductClick = {},
                        onAddToCart = {},
                        isWishlisted = false,
                        onToggleWishlist = {}
                    )
                }
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/product_card.png")
    }
}
