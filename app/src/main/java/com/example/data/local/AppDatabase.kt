package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.models.*
import com.example.utils.PasswordHasher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        User::class,
        Category::class,
        Question::class,
        QuizResult::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun categoryDao(): CategoryDao
    abstract fun questionDao(): QuestionDao
    abstract fun quizResultDao(): QuizResultDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_master_db"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(db: AppDatabase) {
                // Admin User Seed
                val (adminHash, adminSalt) = PasswordHasher.hashPassword("admin123")
                val adminUser = User(
                    username = "admin",
                    email = "admin@quizmaster.com",
                    passwordHash = adminHash,
                    salt = adminSalt,
                    isAdmin = true
                )
                db.userDao().insertUser(adminUser)

                // Demo User Seed
                val (userHash, userSalt) = PasswordHasher.hashPassword("user123")
                val demoUser = User(
                    username = "Alex Quizzer",
                    email = "alex@example.com",
                    passwordHash = userHash,
                    salt = userSalt,
                    isAdmin = false
                )
                db.userDao().insertUser(demoUser)

                // Categories Seed
                val categories = listOf(
                    Category(name = "Python", iconName = "Code", description = "Python language, GIL, OOP, generators"),
                    Category(name = "Programming", iconName = "Terminal", description = "General core programming logic"),
                    Category(name = "Java", iconName = "Coffee", description = "JVM, OOP, Collections, Multithreading"),
                    Category(name = "C++", iconName = "Memory", description = "Pointers, Memory management, Templates"),
                    Category(name = "AI & ML", iconName = "Psychology", description = "Neural networks, Deep learning, NLP"),
                    Category(name = "Data Structures", iconName = "AccountTree", description = "Arrays, Trees, Graphs, HashTables"),
                    Category(name = "Computer Networks", iconName = "Hub", description = "TCP/IP, OSI model, HTTP/HTTPS"),
                    Category(name = "Operating System", iconName = "SettingsSystemDaydream", description = "Processes, Threads, Deadlocks"),
                    Category(name = "DBMS", iconName = "Storage", description = "SQL, ACID properties, Normalization"),
                    Category(name = "General Knowledge", iconName = "Public", description = "World facts and general awareness"),
                    Category(name = "History", iconName = "MenuBook", description = "Historical milestones and world leaders"),
                    Category(name = "Science", iconName = "Science", description = "Physics, Chemistry, Biology concepts"),
                    Category(name = "Sports", iconName = "SportsSoccer", description = "Olympics, Cricket, Football facts"),
                    Category(name = "Current Affairs", iconName = "Newsmode", description = "Global events and updates"),
                    Category(name = "Mathematics", iconName = "Calculate", description = "Algebra, Probability, Geometry")
                )
                db.categoryDao().insertCategories(categories)

                // Initial Question Bank Seed
                val questions = listOf(
                    // Python - Easy
                    Question(
                        categoryName = "Python",
                        difficulty = "Easy",
                        questionText = "Which of the following keywords is used to define a function in Python?",
                        optionA = "func",
                        optionB = "def",
                        optionC = "function",
                        optionD = "define",
                        correctAnswerIndex = 1,
                        explanation = "'def' is the standard Python keyword used to define a function.",
                        hint = "It is 3 letters short for define."
                    ),
                    Question(
                        categoryName = "Python",
                        difficulty = "Easy",
                        questionText = "What is the output of print(2 ** 3) in Python?",
                        optionA = "6",
                        optionB = "8",
                        optionC = "9",
                        optionD = "5",
                        correctAnswerIndex = 1,
                        explanation = "The '**' operator represents exponentiation in Python. 2^3 = 8.",
                        hint = "It multiplies 2 three times: 2 * 2 * 2."
                    ),
                    // Python - Medium
                    Question(
                        categoryName = "Python",
                        difficulty = "Medium",
                        questionText = "What is GIL in Python CPython implementation?",
                        optionA = "Global Interface Logic",
                        optionB = "General Interpreter Layer",
                        optionC = "Global Interpreter Lock",
                        optionD = "Graphical Instance Lock",
                        correctAnswerIndex = 2,
                        explanation = "GIL stands for Global Interpreter Lock, a mutex that protects access to Python objects, preventing multiple threads from executing Python bytecodes at once.",
                        hint = "It locks the interpreter execution."
                    ),
                    Question(
                        categoryName = "Python",
                        difficulty = "Medium",
                        questionText = "Which data type in Python is mutable?",
                        optionA = "Tuple",
                        optionB = "String",
                        optionC = "List",
                        optionD = "FrozenSet",
                        correctAnswerIndex = 2,
                        explanation = "Lists in Python can be modified in-place (mutable), unlike tuples or strings.",
                        hint = "Defined using square brackets []."
                    ),
                    // Python - Hard
                    Question(
                        categoryName = "Python",
                        difficulty = "Hard",
                        questionText = "How do decorator functions in Python pass arguments to the decorated function?",
                        optionA = "Using *args and **kwargs in wrapper",
                        optionB = "Using static params",
                        optionC = "Decorators cannot accept arguments",
                        optionD = "Using lambda expressions only",
                        correctAnswerIndex = 0,
                        explanation = "The inner wrapper function uses *args and **kwargs to forward any positional and keyword arguments to the wrapped target function.",
                        hint = "Think about variable positional and keyword argument unpacking."
                    ),

                    // AI & ML
                    Question(
                        categoryName = "AI & ML",
                        difficulty = "Easy",
                        questionText = "What does AI stand for?",
                        optionA = "Automated Intelligence",
                        optionB = "Artificial Intelligence",
                        optionC = "Algorithmic Integration",
                        optionD = "Applied Informatics",
                        correctAnswerIndex = 1,
                        explanation = "AI stands for Artificial Intelligence.",
                        hint = "Opposite of natural intelligence."
                    ),
                    Question(
                        categoryName = "AI & ML",
                        difficulty = "Medium",
                        questionText = "Which activation function outputs values strictly between 0 and 1?",
                        optionA = "ReLU",
                        optionB = "Tanh",
                        optionC = "Sigmoid",
                        optionD = "LeakyReLU",
                        correctAnswerIndex = 2,
                        explanation = "The Sigmoid function, 1 / (1 + e^-x), maps any real number to a probability value between 0 and 1.",
                        hint = "Looks like an S-shaped curve used in binary classification."
                    ),

                    // Data Structures
                    Question(
                        categoryName = "Data Structures",
                        difficulty = "Easy",
                        questionText = "Which data structure operates on a Last-In, First-Out (LIFO) basis?",
                        optionA = "Queue",
                        optionB = "Stack",
                        optionC = "Array",
                        optionD = "LinkedList",
                        correctAnswerIndex = 1,
                        explanation = "A Stack processes items in LIFO order (Push / Pop).",
                        hint = "Think of a stack of plates."
                    ),
                    Question(
                        categoryName = "Data Structures",
                        difficulty = "Medium",
                        questionText = "What is the worst-case time complexity of Quick Sort?",
                        optionA = "O(n log n)",
                        optionB = "O(n)",
                        optionC = "O(n²)",
                        optionD = "O(1)",
                        correctAnswerIndex = 2,
                        explanation = "When pivot selection is poor (e.g. sorted array with first element as pivot), QuickSort degrades to O(n²).",
                        hint = "Quadratic time complexity."
                    ),

                    // Computer Networks
                    Question(
                        categoryName = "Computer Networks",
                        difficulty = "Easy",
                        questionText = "On which layer of the OSI model does HTTP operate?",
                        optionA = "Transport Layer",
                        optionB = "Network Layer",
                        optionC = "Application Layer",
                        optionD = "Data Link Layer",
                        correctAnswerIndex = 2,
                        explanation = "HTTP is a top-level network protocol operating on Layer 7 (Application Layer).",
                        hint = "Layer 7 of the 7-layer OSI model."
                    ),

                    // DBMS
                    Question(
                        categoryName = "DBMS",
                        difficulty = "Medium",
                        questionText = "What does the 'A' in ACID properties of database transactions stand for?",
                        optionA = "Atomicity",
                        optionB = "Availability",
                        optionC = "Authentication",
                        optionD = "Accuracy",
                        correctAnswerIndex = 0,
                        explanation = "Atomicity ensures that all operations within a transaction complete successfully or none are applied (all-or-nothing).",
                        hint = "All-or-nothing property."
                    ),

                    // General Knowledge & Science
                    Question(
                        categoryName = "General Knowledge",
                        difficulty = "Easy",
                        questionText = "Which planet in our solar system is known as the Red Planet?",
                        optionA = "Venus",
                        optionB = "Jupiter",
                        optionC = "Mars",
                        optionD = "Saturn",
                        correctAnswerIndex = 2,
                        explanation = "Mars is called the Red Planet due to iron oxide (rust) on its surface.",
                        hint = "Named after the Roman god of war."
                    ),
                    Question(
                        categoryName = "Science",
                        difficulty = "Easy",
                        questionText = "What chemical element has the symbol 'O'?",
                        optionA = "Osmium",
                        optionB = "Oxygen",
                        optionC = "Oganesson",
                        optionD = "Ozone",
                        correctAnswerIndex = 1,
                        explanation = "Oxygen has atomic number 8 and chemical symbol O.",
                        hint = "Essential gas for human respiration."
                    ),
                    Question(
                        categoryName = "Mathematics",
                        difficulty = "Medium",
                        questionText = "What is the value of Pi (π) rounded to 4 decimal places?",
                        optionA = "3.1415",
                        optionB = "3.1416",
                        optionC = "3.1420",
                        optionD = "3.1412",
                        correctAnswerIndex = 1,
                        explanation = "3.14159... rounds to 3.1416 at 4 decimal places.",
                        hint = "Next digit after 5 is 9."
                    )
                )
                db.questionDao().insertQuestions(questions)

                // Achievements Seed
                val achievements = listOf(
                    Achievement(
                        id = 1,
                        title = "First Quiz",
                        description = "Complete your very first quiz attempt",
                        iconName = "EmojiEvents"
                    ),
                    Achievement(
                        id = 2,
                        title = "100% Score",
                        description = "Score a perfect 100% on any quiz",
                        iconName = "Stars"
                    ),
                    Achievement(
                        id = 3,
                        title = "10 Quizzes Completed",
                        description = "Play and submit 10 full quiz games",
                        iconName = "MilitaryTech"
                    ),
                    Achievement(
                        id = 4,
                        title = "50 Correct Answers",
                        description = "Answer at least 50 questions correctly across all quizzes",
                        iconName = "CheckCircle"
                    ),
                    Achievement(
                        id = 5,
                        title = "Quiz Champion",
                        description = "Achieve an overall average score above 90%",
                        iconName = "WorkspacePremium"
                    )
                )
                db.achievementDao().insertAchievements(achievements)
            }
        }
    }
}
