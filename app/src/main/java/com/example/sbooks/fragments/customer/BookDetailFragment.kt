package com.example.sbooks.fragments.customer

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.sbooks.R
import com.example.sbooks.adapters.ReviewAdapter
import com.example.sbooks.database.DatabaseHelper
import com.example.sbooks.database.dao.BookDao
import com.example.sbooks.database.dao.ReviewDao
import com.example.sbooks.database.dao.UserDao
import com.example.sbooks.models.BookModel
import com.example.sbooks.models.ReviewModel
import com.example.sbooks.utils.CartManager
import com.example.sbooks.utils.ImageUtils
import com.example.sbooks.utils.SharedPrefsHelper

class BookDetailFragment : Fragment() {
    private lateinit var bookDao: BookDao
    private lateinit var reviewDao: ReviewDao
    private lateinit var userDao: UserDao
    private lateinit var sharedPrefsHelper: SharedPrefsHelper
    private var bookId: Int = -1
    private var currentBook: BookModel? = null

    // Review data
    private lateinit var reviewAdapter: ReviewAdapter
    private var allReviews: List<ReviewModel> = emptyList()
    private var filteredReviews: List<ReviewModel> = emptyList()

    // Views - Book Info
    private lateinit var btnBack: ImageButton
    private lateinit var bookImage: ImageView
    private lateinit var tvCategory: TextView
    private lateinit var tvBookTitle: TextView
    private lateinit var tvBookAuthor: TextView
    private lateinit var tvRating: TextView
    private lateinit var tvReviewCount: TextView
    private lateinit var tvOriginalPrice: TextView
    private lateinit var tvBookPrice: TextView
    private lateinit var tvDiscount: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnAddToCart: Button

    // Views - Reviews
    private lateinit var btnWriteReview: Button
    private lateinit var tvAverageRating: TextView
    private lateinit var ratingBarAverage: RatingBar
    private lateinit var tvTotalReviews: TextView

    // Rating breakdown
    private lateinit var progressBar5Star: ProgressBar
    private lateinit var progressBar4Star: ProgressBar
    private lateinit var progressBar3Star: ProgressBar
    private lateinit var progressBar2Star: ProgressBar
    private lateinit var progressBar1Star: ProgressBar
    private lateinit var tvCount5Star: TextView
    private lateinit var tvCount4Star: TextView
    private lateinit var tvCount3Star: TextView
    private lateinit var tvCount2Star: TextView
    private lateinit var tvCount1Star: TextView

    // Filter chips
    private lateinit var chipAll: TextView
    private lateinit var chip5Star: TextView
    private lateinit var chip4Star: TextView
    private lateinit var chip3Star: TextView
    private lateinit var chip2Star: TextView
    private lateinit var chip1Star: TextView
    private lateinit var chipWithComment: TextView

    // RecyclerView
    private lateinit var recyclerViewReviews: RecyclerView
    private lateinit var btnViewAllReviews: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookId = arguments?.getInt("book_id", -1) ?: -1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_book_detail, container, false)

        // Initialize SharedPrefsHelper
        sharedPrefsHelper = SharedPrefsHelper(requireContext())

        // Initialize database
        val dbHelper = DatabaseHelper(requireContext())
        val db = dbHelper.writableDatabase
        bookDao = BookDao(db)
        reviewDao = ReviewDao(db)
        userDao = UserDao(db)

        // Initialize views
        initViews(root)

        // Setup RecyclerView
        setupRecyclerView()

        // Setup listeners
        setupListeners()

        // Load book data
        loadBookDetail()

        // Load reviews
        loadReviews()

        return root
    }

    private fun initViews(root: View) {
        // Book info views
        btnBack = root.findViewById(R.id.btnBack)
        bookImage = root.findViewById(R.id.bookImage)
        tvCategory = root.findViewById(R.id.tvCategory)
        tvBookTitle = root.findViewById(R.id.tvBookTitle)
        tvBookAuthor = root.findViewById(R.id.tvBookAuthor)
        tvRating = root.findViewById(R.id.tvRating)
        tvReviewCount = root.findViewById(R.id.tvReviewCount)
        tvOriginalPrice = root.findViewById(R.id.tvOriginalPrice)
        tvBookPrice = root.findViewById(R.id.tvBookPrice)
        tvDiscount = root.findViewById(R.id.tvDiscount)
        tvDescription = root.findViewById(R.id.tvDescription)
        btnAddToCart = root.findViewById(R.id.btnAddToCart)

        // Review views
        btnWriteReview = root.findViewById(R.id.btnWriteReview)
        tvAverageRating = root.findViewById(R.id.tvAverageRating)
        ratingBarAverage = root.findViewById(R.id.ratingBarAverage)
        tvTotalReviews = root.findViewById(R.id.tvTotalReviews)

        // Rating breakdown
        progressBar5Star = root.findViewById(R.id.progressBar5Star)
        progressBar4Star = root.findViewById(R.id.progressBar4Star)
        progressBar3Star = root.findViewById(R.id.progressBar3Star)
        progressBar2Star = root.findViewById(R.id.progressBar2Star)
        progressBar1Star = root.findViewById(R.id.progressBar1Star)
        tvCount5Star = root.findViewById(R.id.tvCount5Star)
        tvCount4Star = root.findViewById(R.id.tvCount4Star)
        tvCount3Star = root.findViewById(R.id.tvCount3Star)
        tvCount2Star = root.findViewById(R.id.tvCount2Star)
        tvCount1Star = root.findViewById(R.id.tvCount1Star)

        // Filter chips
        chipAll = root.findViewById(R.id.chipAll)
        chip5Star = root.findViewById(R.id.chip5Star)
        chip4Star = root.findViewById(R.id.chip4Star)
        chip3Star = root.findViewById(R.id.chip3Star)
        chip2Star = root.findViewById(R.id.chip2Star)
        chip1Star = root.findViewById(R.id.chip1Star)
        chipWithComment = root.findViewById(R.id.chipWithComment)

        // RecyclerView
        recyclerViewReviews = root.findViewById(R.id.recyclerViewReviews)
        btnViewAllReviews = root.findViewById(R.id.btnViewAllReviews)
    }

    private fun setupRecyclerView() {
        reviewAdapter = ReviewAdapter(emptyList())
        recyclerViewReviews.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
            isNestedScrollingEnabled = false
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnAddToCart.setOnClickListener {
            currentBook?.let { book ->
                if (book.isOutOfStock()) {
                    Toast.makeText(requireContext(), "Sản phẩm đã hết hàng", Toast.LENGTH_SHORT)
                        .show()
                } else {
                    addToCart(book)
                }
            }
        }

        // Review listeners
        btnWriteReview.setOnClickListener {
            openWriteReviewDialog()
        }

        btnViewAllReviews.setOnClickListener {
            Toast.makeText(requireContext(), "Xem tất cả đánh giá", Toast.LENGTH_SHORT).show()
        }

        // Filter chips
        chipAll.setOnClickListener {
            filterReviews(FilterType.ALL)
        }

        chip5Star.setOnClickListener {
            filterReviews(FilterType.FIVE_STAR)
        }

        chip4Star.setOnClickListener {
            filterReviews(FilterType.FOUR_STAR)
        }

        chip3Star.setOnClickListener {
            filterReviews(FilterType.THREE_STAR)
        }

        chip2Star.setOnClickListener {
            filterReviews(FilterType.TWO_STAR)
        }

        chip1Star.setOnClickListener {
            filterReviews(FilterType.ONE_STAR)
        }

        chipWithComment.setOnClickListener {
            filterReviews(FilterType.WITH_COMMENT)
        }
    }

    private fun loadBookDetail() {
        if (bookId == -1) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin sách", Toast.LENGTH_SHORT)
                .show()
            parentFragmentManager.popBackStack()
            return
        }

        Thread {
            try {
                val book = bookDao.getBookById(bookId)

                activity?.runOnUiThread {
                    if (book != null) {
                        currentBook = book
                        displayBookDetail(book)
                    } else {
                        Toast.makeText(requireContext(), "Không tìm thấy sách", Toast.LENGTH_SHORT)
                            .show()
                        parentFragmentManager.popBackStack()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                }
            }
        }.start()
    }

    private fun displayBookDetail(book: BookModel) {
        // Category
        tvCategory.text = book.categoryName

        // Title and Author
        tvBookTitle.text = book.title
        tvBookAuthor.text = "Tác giả: ${book.author}"

        // Rating
        tvRating.text = String.format("%.1f", book.rating)
        tvReviewCount.text = "(${book.reviewCount} đánh giá)"

        // Price and discount
        val originalPrice = book.price * 1.25
        val discount = ((originalPrice - book.price) / originalPrice * 100).toInt()

        if (discount > 0) {
            tvOriginalPrice.visibility = View.VISIBLE
            tvDiscount.visibility = View.VISIBLE

            tvOriginalPrice.text = String.format("%,.0fđ", originalPrice)
            tvOriginalPrice.paintFlags = tvOriginalPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG

            tvDiscount.text = "-${discount}%"
        } else {
            tvOriginalPrice.visibility = View.GONE
            tvDiscount.visibility = View.GONE
        }

        tvBookPrice.text = book.getFormattedPrice()

        // Description
        if (book.description.isNotEmpty()) {
            tvDescription.text = book.description
        } else {
            tvDescription.text = "Chưa có mô tả cho sản phẩm này."
        }

        // Book image
        loadBookImage(book)

        // Button state
        updateAddToCartButton(book)
    }

    private fun loadBookImage(book: BookModel) {
        if (book.image.isNotEmpty()) {
            val bitmap = ImageUtils.loadImageFromInternalStorage(book.image)
            if (bitmap != null) {
                bookImage.setImageBitmap(bitmap)
            } else {
                bookImage.setImageResource(R.drawable.ic_book_24)
            }
        } else {
            bookImage.setImageResource(R.drawable.ic_book_24)
        }
    }

    private fun updateAddToCartButton(book: BookModel) {
        if (book.isOutOfStock()) {
            btnAddToCart.text = "Hết hàng"
            btnAddToCart.isEnabled = false
            btnAddToCart.alpha = 0.5f
        } else if (book.isLowStock()) {
            btnAddToCart.text = "Thêm vào giỏ hàng (Còn ${book.stock} cuốn)"
            btnAddToCart.isEnabled = true
            btnAddToCart.alpha = 1.0f
        } else {
            btnAddToCart.text = "Thêm vào giỏ hàng"
            btnAddToCart.isEnabled = true
            btnAddToCart.alpha = 1.0f
        }
    }

    private fun addToCart(book: BookModel) {
        CartManager.addToCart(book)
        Toast.makeText(
            requireContext(),
            "Đã thêm '${book.title}' vào giỏ hàng",
            Toast.LENGTH_SHORT
        ).show()
    }

    // ============ REVIEW FUNCTIONS ============

    private fun openWriteReviewDialog() {
        // Check if user is logged in
        val userId = sharedPrefsHelper.getUserId()
        if (userId == -1) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập để đánh giá", Toast.LENGTH_SHORT).show()
            return
        }

        // Get user info in background thread
        Thread {
            try {
                val currentUser = userDao.getUserById(userId)

                activity?.runOnUiThread {
                    if (currentUser == null) {
                        Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    // Check if user already reviewed this book
                    Thread {
                        val hasReviewed = reviewDao.hasUserReviewedBook(currentUser.id, bookId)

                        activity?.runOnUiThread {
                            if (hasReviewed) {
                                Toast.makeText(
                                    requireContext(),
                                    "Bạn đã đánh giá sản phẩm này rồi",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                showWriteReviewDialog(currentUser.id, currentUser.username)
                            }
                        }
                    }.start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(requireContext(), "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showWriteReviewDialog(userId: Int, userName: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_write_review)
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        // Initialize dialog views
        val ratingBarInput = dialog.findViewById<RatingBar>(R.id.ratingBarInput)
        val etReviewComment = dialog.findViewById<EditText>(R.id.etReviewComment)
        val btnCancelReview = dialog.findViewById<Button>(R.id.btnCancelReview)
        val btnSubmitReview = dialog.findViewById<Button>(R.id.btnSubmitReview)

        // Set default rating
        ratingBarInput.rating = 5f

        // Cancel button
        btnCancelReview.setOnClickListener {
            dialog.dismiss()
        }

        // Submit button
        btnSubmitReview.setOnClickListener {
            val rating = ratingBarInput.rating
            val comment = etReviewComment.text.toString().trim()

            if (rating == 0f) {
                Toast.makeText(
                    requireContext(),
                    "Vui lòng chọn số sao đánh giá",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Create review model
            val review = ReviewModel(
                id = 0,
                bookId = bookId,
                userId = userId,
                userName = userName,
                userAvatar = "",
                rating = rating,
                comment = comment,
                isVerifiedPurchase = false,
                createdAt = "",
                updatedAt = ""
            )

            // Submit review
            submitReview(review, dialog)
        }

        dialog.show()
    }

    private fun submitReview(review: ReviewModel, dialog: Dialog) {
        Thread {
            try {
                val result = reviewDao.insertReview(review)

                activity?.runOnUiThread {
                    if (result > 0) {
                        Toast.makeText(
                            requireContext(),
                            "Đánh giá của bạn đã được gửi thành công",
                            Toast.LENGTH_SHORT
                        ).show()

                        dialog.dismiss()

                        // Reload reviews
                        loadReviews()

                        // Update book rating
                        updateBookRating()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Có lỗi xảy ra, vui lòng thử lại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                activity?.runOnUiThread {
                    Toast.makeText(
                        requireContext(),
                        "Lỗi: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }.start()
    }

    private fun updateBookRating() {
        Thread {
            try {
                val averageRating = reviewDao.getAverageRating(bookId)
                val reviewCount = reviewDao.getReviewCount(bookId)

                // Update book in database
                currentBook?.let { book ->
                    book.rating = averageRating
                    book.reviewCount = reviewCount
                    bookDao.updateBook(book)

                    activity?.runOnUiThread {
                        displayBookDetail(book)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun displayReviewSummary() {
        val totalReviews = allReviews.size
        val averageRating = if (totalReviews > 0) {
            allReviews.map { it.rating }.average().toFloat()
        } else {
            0f
        }

        // Average rating
        tvAverageRating.text = String.format("%.1f", averageRating)
        ratingBarAverage.rating = Math.floor(averageRating.toDouble()).toFloat()
        tvTotalReviews.text = "$totalReviews đánh giá"

        // Calculate rating breakdown
        val ratingCounts = IntArray(5)
        allReviews.forEach { review ->
            val index = review.rating.toInt() - 1
            if (index in 0..4) {
                ratingCounts[index]++
            }
        }

        // Update progress bars and counts
        updateRatingBreakdown(5, ratingCounts[4], totalReviews, progressBar5Star, tvCount5Star)
        updateRatingBreakdown(4, ratingCounts[3], totalReviews, progressBar4Star, tvCount4Star)
        updateRatingBreakdown(3, ratingCounts[2], totalReviews, progressBar3Star, tvCount3Star)
        updateRatingBreakdown(2, ratingCounts[1], totalReviews, progressBar2Star, tvCount2Star)
        updateRatingBreakdown(1, ratingCounts[0], totalReviews, progressBar1Star, tvCount1Star)

        // Update filter chips
        val withCommentCount = allReviews.count { it.hasComment() }
        chipAll.text = "Tất cả ($totalReviews)"
        chip5Star.text = "5 ⭐ (${ratingCounts[4]})"
        chip4Star.text = "4 ⭐ (${ratingCounts[3]})"
        chip3Star.text = "3 ⭐ (${ratingCounts[2]})"
        chip2Star.text = "2 ⭐ (${ratingCounts[1]})"
        chip1Star.text = "1 ⭐ (${ratingCounts[0]})"
        chipWithComment.text = "Có bình luận ($withCommentCount)"
    }

    private fun updateRatingBreakdown(
        starLevel: Int,
        count: Int,
        total: Int,
        progressBar: ProgressBar,
        countTextView: TextView
    ) {
        val percentage = if (total > 0) (count * 100) / total else 0
        progressBar.progress = percentage
        countTextView.text = count.toString()
    }

    private fun displayReviews(reviews: List<ReviewModel>) {
        reviewAdapter.updateReviews(reviews)
    }

    private enum class FilterType {
        ALL, FIVE_STAR, FOUR_STAR, THREE_STAR, TWO_STAR, ONE_STAR, WITH_COMMENT
    }

    private fun filterReviews(filterType: FilterType) {
        // Reset all chips
        resetFilterChips()

        // Apply filter
        filteredReviews = when (filterType) {
            FilterType.ALL -> {
                chipAll.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chipAll.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews
            }

            FilterType.FIVE_STAR -> {
                chip5Star.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chip5Star.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.rating.toInt() == 5 }
            }

            FilterType.FOUR_STAR -> {
                chip4Star.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chip4Star.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.rating.toInt() == 4 }
            }

            FilterType.THREE_STAR -> {
                chip3Star.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chip3Star.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.rating.toInt() == 3 }
            }

            FilterType.TWO_STAR -> {
                chip2Star.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chip2Star.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.rating.toInt() == 2 }
            }

            FilterType.ONE_STAR -> {
                chip1Star.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chip1Star.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.rating.toInt() == 1 }
            }

            FilterType.WITH_COMMENT -> {
                chipWithComment.setBackgroundColor(
                    resources.getColor(android.R.color.holo_blue_light, null)
                )
                chipWithComment.setTextColor(resources.getColor(android.R.color.white, null))
                allReviews.filter { it.hasComment() }
            }
        }

        displayReviews(filteredReviews.take(3))
    }

    private fun resetFilterChips() {
        val chips = listOf(chipAll, chip5Star, chip4Star, chip3Star, chip2Star, chip1Star, chipWithComment)
        chips.forEach { chip ->
            chip.setBackgroundColor(resources.getColor(android.R.color.darker_gray, null))
            chip.setTextColor(resources.getColor(android.R.color.black, null))
        }
    }

    private fun loadReviews() {
        Thread {
            val reviews = reviewDao.getReviewsByBookId(bookId)
            activity?.runOnUiThread {
                allReviews = reviews
                filteredReviews = allReviews
                displayReviewSummary()
                displayReviews(filteredReviews.take(3))
            }
        }.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        currentBook = null
    }
}