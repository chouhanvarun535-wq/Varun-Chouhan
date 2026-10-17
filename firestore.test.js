const { test, before, after, beforeEach } = require('node:test');
const assert = require('node:assert');
const { initializeTestEnvironment, assertFails, assertSucceeds } = require('@firebase/rules-unit-testing');
const fs = require('fs');

const PROJECT_ID = 'demo-no-project';
let testEnv;

before(async () => {
  const rules = fs.readFileSync('firestore.rules', 'utf8');
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules: rules,
      host: '127.0.0.1',
      port: 8085
    }
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

test('Unauthenticated user cannot read user profile', async () => {
  const unauthedDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthedDb.collection('users').doc('user123').get());
});

test('Authenticated user can create their own profile', async () => {
  const authedDb = testEnv.authenticatedContext('user123').firestore();
  await assertSucceeds(
    authedDb.collection('users').doc('user123').set({
      userId: 'user123',
      userName: 'Test User',
      coins: 10,
      totalXp: 50,
      createdAt: new Date()
    })
  );
});

test('Authenticated user cannot write to another user profile', async () => {
  const authedDb = testEnv.authenticatedContext('user_attacker').firestore();
  await assertFails(
    authedDb.collection('users').doc('user_victim').set({
      userId: 'user_victim',
      userName: 'Hacked',
      coins: 999,
      totalXp: 1000,
      createdAt: new Date()
    })
  );
});
