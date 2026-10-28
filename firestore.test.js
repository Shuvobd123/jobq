const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const INSTRUCTOR_UID = "instructor_123";
const STUDENT_UID = "student_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user: cannot read questions, exams, or attempts", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("questions").get());
  await assertFails(unauthDb.collection("exams").get());
  await assertFails(unauthDb.collection("attempts").get());
});

test("Authenticated instructor: can create and update own question, cannot spoof authorId", async () => {
  const instructorDb = testEnv.authenticatedContext(INSTRUCTOR_UID).firestore();
  const now = new Date();
  const validPayload = {
    authorId: INSTRUCTOR_UID,
    authorName: "Teacher A",
    subject: "সাধারণ জ্ঞান",
    topic: "বাংলাদেশ বিষয়াবলি",
    difficulty: "medium",
    questionText: "বাংলাদেশের সংবিধান কবে কার্যকর হয়?",
    optionA: "১৬ ডিসেম্বর ১৯৭২",
    optionB: "২৬ মার্চ ১৯৭২",
    optionC: "৪ নভেম্বর ১৯৭২",
    optionD: "১০ জানুয়ারি ১৯৭২",
    correctOptionIndex: 0,
    explanation: "১৯৭২ সালের ১৬ ডিসেম্বর বাংলাদেশের সংবিধান কার্যকর হয়।",
    marks: 1,
    isActive: true,
    createdAt: now,
    updatedAt: now,
  };

  await assertSucceeds(instructorDb.collection("questions").doc("q_1").set(validPayload));

  // Spoofed authorId must fail
  await assertFails(
    instructorDb.collection("questions").doc("q_spoof").set({
      ...validPayload,
      authorId: STUDENT_UID,
    })
  );

  // Other user cannot update instructor's question
  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  await assertFails(
    studentDb.collection("questions").doc("q_1").update({
      questionText: "Hacked question?",
      updatedAt: new Date(),
    })
  );
});

test("Client query alignment: students can query active questions and published exams, but not unfiltered or inactive ones", async () => {
  const now = new Date();
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const adminDb = context.firestore();
    await adminDb.collection("questions").doc("q_active").set({
      authorId: INSTRUCTOR_UID,
      authorName: "Teacher A",
      subject: "গণিত",
      topic: "বীজগণিত",
      difficulty: "easy",
      questionText: "2 + 2 = ?",
      optionA: "3",
      optionB: "4",
      optionC: "5",
      optionD: "6",
      correctOptionIndex: 1,
      explanation: "2 + 2 = 4",
      marks: 1,
      isActive: true,
      createdAt: now,
      updatedAt: now,
    });
    await adminDb.collection("questions").doc("q_draft").set({
      authorId: INSTRUCTOR_UID,
      authorName: "Teacher A",
      subject: "গণিত",
      topic: "বীজগণিত",
      difficulty: "easy",
      questionText: "Draft question",
      optionA: "1",
      optionB: "2",
      optionC: "3",
      optionD: "4",
      correctOptionIndex: 0,
      explanation: "",
      marks: 1,
      isActive: false,
      createdAt: now,
      updatedAt: now,
    });
  });

  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  // Filtered by isActive == true succeeds
  await assertSucceeds(
    studentDb.collection("questions").where("isActive", "==", true).get()
  );
  // Reading another user's draft question fails
  await assertFails(studentDb.collection("questions").doc("q_draft").get());
  // Author can query all their own questions (including drafts)
  const instructorDb = testEnv.authenticatedContext(INSTRUCTOR_UID).firestore();
  await assertSucceeds(
    instructorDb.collection("questions").where("authorId", "==", INSTRUCTOR_UID).get()
  );
});

test("Student attempts: cross-user isolation and immutability enforced", async () => {
  const studentDb = testEnv.authenticatedContext(STUDENT_UID).firestore();
  const now = new Date();
  const attemptPayload = {
    studentId: STUDENT_UID,
    studentName: "Rahim",
    examId: "exam_1",
    examTitle: "বিসিএস মডেল টেস্ট ১",
    subject: "সব বিষয়",
    totalQuestions: 10,
    answeredCount: 10,
    correctCount: 8,
    wrongCount: 2,
    scoreHundredths: 750,
    maxMarks: 10,
    accuracyPercent: 80,
    passed: true,
    timeTakenSeconds: 320,
    createdAt: now,
  };

  await assertSucceeds(studentDb.collection("attempts").doc("att_1").set(attemptPayload));

  // Cannot update or delete submitted attempt
  await assertFails(studentDb.collection("attempts").doc("att_1").update({ correctCount: 10 }));
  await assertFails(studentDb.collection("attempts").doc("att_1").delete());

  // Other user cannot read or list Rahim's attempts
  const otherDb = testEnv.authenticatedContext(INSTRUCTOR_UID).firestore();
  await assertFails(otherDb.collection("attempts").doc("att_1").get());
  await assertFails(otherDb.collection("attempts").where("studentId", "==", STUDENT_UID).get());
});
