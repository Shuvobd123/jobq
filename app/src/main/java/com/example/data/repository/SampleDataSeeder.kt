package com.example.data.repository

object SampleDataSeeder {

    data class SeedQuestion(
        val subject: String,
        val topic: String,
        val difficulty: String,
        val questionText: String,
        val optionA: String,
        val optionB: String,
        val optionC: String,
        val optionD: String,
        val correctOptionIndex: Int,
        val explanation: String,
        val marks: Int = 1
    )

    val starterQuestions = listOf(
        SeedQuestion(
            subject = "সাধারণ জ্ঞান",
            topic = "বাংলাদেশ বিষয়াবলি",
            difficulty = "easy",
            questionText = "বাংলাদেশের সংবিধান কবে থেকে কার্যকর হয়?",
            optionA = "১৬ ডিসেম্বর ১৯৭২",
            optionB = "২৬ মার্চ ১৯৭২",
            optionC = "৪ নভেম্বর ১৯৭২",
            optionD = "১০ জানুয়ারি ১৯৭২",
            correctOptionIndex = 0,
            explanation = "১৯৭২ সালের ৪ নভেম্বর গণপরিষদে সংবিধান গৃহীত হয় এবং ১৬ ডিসেম্বর ১৯৭২ থেকে কার্যকর হয়।"
        ),
        SeedQuestion(
            subject = "সাধারণ জ্ঞান",
            topic = "মুক্তিযুদ্ধ ও ইতিহাস",
            difficulty = "medium",
            questionText = "১৯৭১ সালের মহান মুক্তিযুদ্ধে বাংলাদেশকে কয়টি সামরিক সেক্টরে ভাগ করা হয়েছিল?",
            optionA = "৯টি সেক্টরে",
            optionB = "১০টি সেক্টরে",
            optionC = "১১টি সেক্টরে",
            optionD = "১২টি সেক্টরে",
            correctOptionIndex = 2,
            explanation = "মুক্তিযুদ্ধ পরিচালনার সুবিধার্থে সমগ্র বাংলাদেশকে ১১টি সেক্টরে ভাগ করা হয়েছিল।"
        ),
        SeedQuestion(
            subject = "বাংলা সাহিত্য",
            topic = "আধুনিক যুগ",
            difficulty = "easy",
            questionText = "‘গীতাঞ্জলি’ কাব্যগ্রন্থের রচয়িতা কে?",
            optionA = "কাজী নজরুল ইসলাম",
            optionB = "রবীন্দ্রনাথ ঠাকুর",
            optionC = "মাইকেল মধুসূদন দত্ত",
            optionD = "জীবনানন্দ দাশ",
            correctOptionIndex = 1,
            explanation = "রবীন্দ্রনাথ ঠাকুর ‘গীতাঞ্জলি’ কাব্যের জন্য ১৯১৩ সালে সাহিত্যে নোবেল পুরস্কার লাভ করেন।"
        ),
        SeedQuestion(
            subject = "বাংলা সাহিত্য",
            topic = "নজরুল সাহিত্য",
            difficulty = "medium",
            questionText = "কাজী নজরুল ইসলামের ‘বিদ্রোহী’ কবিতাটি কোন কাব্যগ্রন্থের অন্তর্গত?",
            optionA = "অগ্নিবীণা",
            optionB = "বিষের বাঁশি",
            optionC = "সর্বহারা",
            optionD = "দোলনচাঁপা",
            correctOptionIndex = 0,
            explanation = "‘বিদ্রোহী’ কবিতাটি ১৯২২ সালে প্রকাশিত কাজী নজরুল ইসলামের প্রথম কাব্যগ্রন্থ ‘অগ্নিবীণা’র অন্তর্গত।"
        ),
        SeedQuestion(
            subject = "গণিত",
            topic = "বীজগণিত ও শতকরা",
            difficulty = "medium",
            questionText = "যদি x + y = 12 এবং x - y = 4 হয়, তবে xy এর মান কত?",
            optionA = "24",
            optionB = "32",
            optionC = "36",
            optionD = "48",
            correctOptionIndex = 1,
            explanation = "এখানে 2x = 16 বা x = 8 এবং y = 4। সুতরাং xy = 8 × 4 = 32।"
        ),
        SeedQuestion(
            subject = "গণিত",
            topic = "জ্যামিতি",
            difficulty = "easy",
            questionText = "একটি সমকোণী ত্রিভুজের সমকোণ ব্যতীত অপর দুটি কোণের সমষ্টি কত ডিগ্রি?",
            optionA = "৪৫ ডিগ্রি",
            optionB = "৬০ ডিগ্রি",
            optionC = "৯০ ডিগ্রি",
            optionD = "১৮০ ডিগ্রি",
            correctOptionIndex = 2,
            explanation = "ত্রিভুজের তিন কোণের সমষ্টি ১৮০°। সমকোণটি ৯০° হওয়ায় অপর দুই কোণের সমষ্টি ৯০°।"
        ),
        SeedQuestion(
            subject = "ইংরেজি",
            topic = "Grammar & Preposition",
            difficulty = "medium",
            questionText = "Choose the correct preposition: 'He died ___ overwork.'",
            optionA = "of",
            optionB = "by",
            optionC = "from",
            optionD = "for",
            correctOptionIndex = 2,
            explanation = "রোগে মারা গেলে 'die of' বসে, কিন্তু অতিরিক্ত পরিশ্রম বা অন্য কোনো কারণে মারা গেলে 'die from' বসে।"
        ),
        SeedQuestion(
            subject = "বিজ্ঞান ও প্রযুক্তি",
            topic = "কম্পিউটার ও তথ্যপ্রযুক্তি",
            difficulty = "easy",
            questionText = "কোনটি কম্পিউটারের স্থায়ী স্মৃতিশক্তি (Non-volatile Memory)?",
            optionA = "RAM",
            optionB = "ROM",
            optionC = "Cache Memory",
            optionD = "Register",
            correctOptionIndex = 1,
            explanation = "ROM (Read Only Memory) হলো স্থায়ী বা Non-volatile মেমোরি, বিদ্যুৎ চলে গেলেও এর তথ্য মুছে যায় না।"
        )
    )

    suspend fun seedServerBankIfNeeded(repository: QuestionBankRepository): Result<Int> = runCatching {
        var count = 0
        for (item in starterQuestions) {
            repository.createQuestion(
                subject = item.subject,
                topic = item.topic,
                difficulty = item.difficulty,
                questionText = item.questionText,
                optionA = item.optionA,
                optionB = item.optionB,
                optionC = item.optionC,
                optionD = item.optionD,
                correctOptionIndex = item.correctOptionIndex,
                explanation = item.explanation,
                marks = item.marks,
                isActive = true
            ).getOrThrow()
            count++
        }

        repository.createExam(
            title = "বিসিএস ও ব্যাংক জব স্পেশাল মডেল টেস্ট - ১",
            subject = "সব বিষয়",
            description = "সাধারণ জ্ঞান, বাংলা, ইংরেজি, গণিত ও বিজ্ঞান বিষয়ের সমন্বিত পূর্ণাঙ্গ এমসিকিউ পরীক্ষা। প্রতিটি ভুল উত্তরের জন্য ০.২৫ নম্বর কাটা যাবে।",
            durationMinutes = 10,
            totalQuestions = 8,
            negativeMarkPerWrong = 25,
            passPercentage = 40,
            isPublished = true,
            allowRetake = true
        ).getOrThrow()

        repository.createExam(
            title = "সাধারণ জ্ঞান ও বাংলা কুইক টেস্ট",
            subject = "সাধারণ জ্ঞান",
            description = "বাংলাদেশ বিষয়াবলি ও মুক্তিযুদ্ধ ভিত্তিক দ্রুত মূল্যায়ন পরীক্ষা।",
            durationMinutes = 5,
            totalQuestions = 5,
            negativeMarkPerWrong = 0,
            passPercentage = 50,
            isPublished = true,
            allowRetake = true
        ).getOrThrow()

        count
    }
}
